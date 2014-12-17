package net.rwchess.controller;

import net.rwchess.persistent.DownloadFile;
import net.rwchess.persistent.Member;
import net.rwchess.persistent.WikiPage;
import net.rwchess.persistent.dao.DownloadFileDAO;
import net.rwchess.persistent.dao.MemberDAO;
import net.rwchess.persistent.dao.WikiPageDAO;
import net.rwchess.utils.Mailer;
import net.rwchess.utils.UsefulMethods;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;


/**
 * Created by bodia on 10/12/14.
 */
@Controller
@RequestMapping("/wiki")
public class WikiController {

    private WikiPageDAO wikiDao;
    private DownloadFileDAO downloadFileDAO;
    private MemberDAO memberDAO;
    private Mailer mailer;

    public WikiController(WikiPageDAO wikiDao, DownloadFileDAO downloadFileDAO, MemberDAO memberDAO, Mailer mailer) {
        this.wikiDao = wikiDao;
        this.downloadFileDAO = downloadFileDAO;
        this.memberDAO = memberDAO;
        this.mailer = mailer;
    }

    @RequestMapping(value = "/Special:CreatePage", method = RequestMethod.GET)
    public String wikiCreatePage() {
        return "create-page";
    }

    @RequestMapping(value = "/Special:ImageRegistry", method = RequestMethod.GET)
    public String wikiImageRegistry(ModelMap model) {
        List<DownloadFile> fileList = downloadFileDAO.getAllFiles();

        StringBuilder imgAvailable = new StringBuilder();
        imgAvailable.append("<ul>");
        for (DownloadFile file : fileList) {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".JPG") || file.getName().endsWith(".jpeg")
                    || file.getName().endsWith(".png") || file.getName().endsWith(".PNG")) {
                imgAvailable.append("<li><a href=\"/wikiImg/").append(file.getName()).append("\">File:").append(file.getName()).append("</a> uploaded by <img src=\"/static/images/flags/").append(file.getCreator().getCountry()).append(".png\"/> ").append(file.getCreator().getUsername()).append("</li>");
            }
        }
        imgAvailable.append("</ul>");
        model.addAttribute("title", "Image registry");
        model.addAttribute("error", "<p>Uploaded images list:</p> " + imgAvailable);
        return "error";
    }

    @RequestMapping(value = "/{pageName}", method = RequestMethod.GET)
    public String wikiProcess(@PathVariable String pageName, ModelMap model) {
        pageName = pageName.replace('_', ' ');

        WikiPage page = wikiDao.getWikiPageByName(pageName);

        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (pageName.startsWith("User:")) {
            Member member = memberDAO.getMemberByUsername(pageName.substring(pageName.indexOf(':') + 1));

            if (member == null)
                return pageName;

            StringBuilder toggleUserHtml = new StringBuilder("<div id=\"flip\">Click to show/hide users' time control preferences.</div>\n" +
                    "<div id=\"panel\"><p>I prefer the following time controls, in order:</p><ul id=\"sortable\"> ");
            for (String time : member.getPreference().split(",")) {
                toggleUserHtml.append("<li class=\"ui-state-default\"> ").append(time.replaceAll("_", " ")).append(" <img src=\"/static/images/clock.png\"/></li>");
            }


            toggleUserHtml.append("</ul></div>");


            if (page == null && !(user instanceof String) && pageName.endsWith(((Member) user).getUsername())) {
                model.addAttribute("wikiPageName", pageName);
                model.addAttribute("urlFriendlyName", pageName.replace(' ', '_'));
                return "page-nonexistent";
            }

            if (page == null) {
                page = new WikiPage();
                page.setName(pageName);
                model.addAttribute("wikiPage", page);
                model.addAttribute("tdProtectionText", "");
                model.addAttribute("urlFriendlyName", pageName.replace(' ', '_'));
                model.addAttribute("toggleUser", toggleUserHtml);
                return "view-page";
            } else {
                model.addAttribute("wikiPage", page);
                model.addAttribute("tdProtectionText", (page.isTdProtected() ? "Unset" : "Set") + "TD protection");
                model.addAttribute("urlFriendlyName", page.getName().replace(' ', '_'));
                model.addAttribute("toggleUser", toggleUserHtml);
                return "view-page";
            }
        } else {
            if (page == null) {
                if (pageName.startsWith("Special:"))
                    return pageName;

                model.addAttribute("wikiPageName", pageName);
                model.addAttribute("urlFriendlyName", pageName.replace(' ', '_'));
                return "page-nonexistent";
            } else if (pageName.startsWith("Special:RecentEdits")) {
                StringBuilder recentEdits = new StringBuilder();
                recentEdits.append("<ul>");

                ListIterator<String> iter = page.getHistory().listIterator(page.getHistory().size());

                int i = 30;
                while (iter.hasPrevious()) {
                    recentEdits.append("<li>").append(iter.previous()).append("</li>");
                    if (--i <= 0)
                        break;
                }
                recentEdits.append("</ul>");
                model.addAttribute("title", "Recent Edits");
                model.addAttribute("error", recentEdits);
                return "error";
            }
        }

        model.addAttribute("wikiPage", page);
        model.addAttribute("tdProtectionText", (page.isTdProtected() ? "Unset" : "Set") + "TD protection");
        model.addAttribute("urlFriendlyName", page.getName().replace(' ', '_'));
        model.addAttribute("toggleUser", "");

        return "view-page";
    }

    @RequestMapping(value = "/Special:Edit", method = RequestMethod.GET)
    public String wikiEdit(@RequestParam(value = "page") String pageName, ModelMap model) {
        pageName = pageName.replace('_', ' ');

        WikiPage page = wikiDao.getWikiPageByName(pageName);
        if (page == null)
            return "not-found";

        if (page.isTdProtected()) {
            Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (((Member) user).getGroup() < Member.TD) {
                model.addAttribute("title", "TD protected");
                model.addAttribute("error", "The page is protected and can be edited only by a TD.");
                return "error";
            }
        }

        model.addAttribute("wikiPage", page);
        model.addAttribute("rawText", page.getRawText());
        model.addAttribute("urlFriendlyName", page.getName().replace(' ', '_'));
        return "edit-page";

    }

    @RequestMapping(value = "/Special:Protect", method = RequestMethod.POST)
    public String wikiTdProtect(@RequestParam(value = "pageName") String pageName, HttpServletRequest req) {
        wikiDao.toggleProtectTd(pageName);

        String referer = req.getHeader("Referer");
        return "redirect:" + referer;
    }

    @RequestMapping(value = "/Special:Edit", method = RequestMethod.POST)
    public String wikiEditAct(@RequestParam(value = "pageName") String pageName, @RequestParam(value = "contents") String rawText,
                              @RequestParam(value = "save") String buttVal, ModelMap model) {
        String origPage = pageName;
        pageName = pageName.replace('_', ' ');

        WikiPage page = wikiDao.getWikiPageByName(pageName);
        if (!buttVal.equals("Save")) { // user pressed "preview"
            page.setRawText(rawText);
            model.addAttribute("wikiPage", page);
            model.addAttribute("htmlText", page.getHtmlText());
            model.addAttribute("rawText", page.getRawText());
            model.addAttribute("nextPath", "/wiki/Special:Edit");
            model.addAttribute("urlFriendlyName", page.getName().replace(' ', '_'));
            return "preview-page";
        }

        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!pageName.startsWith("Private:")) {
            WikiPage edits = wikiDao.getWikiPageByName("Special:RecentEdits");

            Stack<String> list = edits.getHistory();
            if (list == null)
                list = new Stack<String>();

            Stack<String> history = new Stack<String>();
            for (String s : list) {
                history.push(s);
            }

            if (history.size() == 30) {
                history.remove(0);
            }
            DateTime zoned = DateTime.now(DateTimeZone.forID("GMT"));
            String date = UsefulMethods.getWikiDateFormatter().print(zoned);
            if (!history.isEmpty() && history.peek().contains(((Member) user).getUsername() + "</a>") &&
                    history.peek().contains(pageName)) {
                history.pop();
            }
            history.push("Page <a href=\"/wiki/" + pageName.replace(' ', '_') + "\">" + pageName + "</a> was edited by <img src=\"/static/images/flags/" + ((Member) user).getCountry() + ".png\"/> <a href=\"/wiki/User:" + ((Member) user).getUsername() + "\">" +
                    ((Member) user).getUsername() + "</a> on " + date + '.');

            wikiDao.updatePageWithText("Special:RecentEdits", "", history);
        }

        wikiDao.updatePageWithText(pageName, rawText, dealWithHistoryStack(page.getHistory(), ((Member) user).getUsername()));

        return "redirect:/wiki/" + origPage;
    }

    @RequestMapping(value = "/Special:Register", method = RequestMethod.GET)
    public String wikiRegisterShow(ModelMap model) {
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof String)
            return "register";
        else {
            model.addAttribute("title", "Multiple registration");
            model.addAttribute("error", "You cannot register twice");
            return "error";
        }
    }

    @RequestMapping(value = "/Special:Confirm", method = RequestMethod.GET)
    public String confirmUser(ModelMap model, @RequestParam(value = "username") String username,
                              @RequestParam(value = "hash") String hash) {
        Member user = memberDAO.getMemberByUsername(username);
        if (user.getPasswordHash().equals(hash)) {
            memberDAO.toggleConfirmed(username);
            model.addAttribute("title", "Confirmed");
            model.addAttribute("error", "The email has been successfully confirmed!");
            return "error";
        } else {
            model.addAttribute("title", "Error");
            model.addAttribute("error", "Error!");
            return "error";
        }
    }

    @RequestMapping(value = "/Special:Register", method = RequestMethod.POST)
    public String wikiRegisterDo(HttpServletRequest request, ModelMap model, @RequestParam(value = "Username") String username,
                                 @RequestParam(value = "Email") String email,
                                 @RequestParam(value = "Password") String password, @RequestParam(value = "Country") String country,
                                 @RequestParam(value = "time_order") String time_order, @RequestParam(value = "bad_times") String bad_times, @RequestParam(value = "hard_times") String hard_times) {
        String timeControlPreferrence = "45 45";
        
        if (request.getParameter("def_time") == null) {
            timeControlPreferrence = time_order.replace(",", ", ");
        }

        Member ourM = memberDAO.getMemberByUsername(username);
        if (ourM == null) {
            model.addAttribute("title", "Error");
            model.addAttribute("error", "Please log in to FICS and issue command 't snailbot join' before filling in this form.");
            return "error";
        }
        if (ourM.isConfirmed()) {
            model.addAttribute("title", "Error");
            model.addAttribute("error", "User with username " + username + " already registered and is confirmed.");
            return "error";
        }
        String passwordHash = UsefulMethods.getMD5(password);

        memberDAO.updateWithData(username, passwordHash, country, bad_times, hard_times, timeControlPreferrence, email);

        mailer.sendEmail("notify@snailbucket.org", "Snailbucket registration", "Please follow this link: http://snailbucket.org/wiki/Special:Confirm?username=" + username
                + "&hash=" + passwordHash, email);

        model.addAttribute("title", "Sucessful registration");
        model.addAttribute("error", "You should confirm your email address by following the link we have sent you. If there are any problems please contact admins or TDs.");
        return "error";
    }

    @RequestMapping(value = "/Special:History", method = RequestMethod.GET)
    public String wikiHistory(@RequestParam(value = "page") String pageName, ModelMap model) {
        pageName = pageName.replace('_', ' ');

        WikiPage page = wikiDao.getWikiPageByName(pageName);

        if (page == null)
            return "not-found";

        StringBuilder historyList = new StringBuilder();
        if (page.getHistory() != null) {
            for (String line : page.getHistory()) {
                historyList.append("<li>").append(line).append("</li>\n");
            }
        }

        model.addAttribute("wikiPage", page);
        model.addAttribute("historyList", historyList);
        model.addAttribute("urlFriendlyName", page.getName().replace(' ', '_'));
        return "history";

    }

    @RequestMapping(value = "/Special:Create", method = RequestMethod.GET)
    public String wikiCreate(@RequestParam(value = "page") String pageName, ModelMap model) {
        pageName = pageName.replace('_', ' ');

        model.addAttribute("wikiPageName", pageName);
        return "page-create";

    }


    @RequestMapping(value = "/File*", method = RequestMethod.GET)
    public String imgGet(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("px-")) {
            return "redirect:/wikiImg/" + uri.substring(uri.indexOf('-') + 1);
        }

        return "redirect:/wikiImg/" + uri.substring(uri.indexOf(':') + 1);
    }
//    @RequestMapping(value = "/File{filename}.png", method = RequestMethod.GET)
//    public String imgGetPng(@RequestParam(value = "filename") String filename) {
//        return "redirect:/wikiImg/"+filename+".png";
//    }

    @RequestMapping(value = "/Special:Upload", method = RequestMethod.GET)
    public String wikiUploadServe() {
        return "upload-page";
    }

    @RequestMapping(value = "/Special:Upload", method = RequestMethod.POST)
    public String wikiUpload(HttpServletRequest req, ModelMap model) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(2000000);
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(5000000L);
        try {
            String description = null;
            String fileName = null;
            byte[] data = null;

            FileItemIterator iterator = upload.getItemIterator(req);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream in = item.openStream();

                try {
                    if (item.isFormField()
                            && item.getFieldName().equals("description")) {
                        description = IOUtils.toString(in);
                    } else if (!item.isFormField()) {
                        fileName = item.getName().replace(' ', '_');
                        data = IOUtils.toByteArray(in);
                    }
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }

            if (description != null && fileName != null) {
                Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                DownloadFile downloadFile = new DownloadFile();
                downloadFile.setName(fileName);
                downloadFile.setCreator(((Member) user));
                downloadFile.setDescription(description);

                ServletContext context = req.getSession().getServletContext();
                String absPath = context.getRealPath("/") + "/wikiImg/";
                FileOutputStream out = new FileOutputStream(absPath + fileName);
                IOUtils.write(data, out);
                IOUtils.closeQuietly(out);
                downloadFileDAO.store(downloadFile);
                model.addAttribute("title", "Sucessfull upload");
                model.addAttribute("error", "<p>Your image has bee successfully uploaded! Use [[File:" + fileName + "]] syntax to insert" +
                        " it into a wiki page. Use <a href=\"/wiki/Special:ImageRegistry\">Image Registry</a> to see all uploaded images.</p>");
                return "error";
            } else
                throw new FileUploadException("Something went wrong");

        } catch (Exception e) {
            model.addAttribute("title", e.getClass().getName());
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @RequestMapping(value = "/Special:Create", method = RequestMethod.POST)
    public String wikiCreateAct(@RequestParam(value = "pageName") String pageName, @RequestParam(value = "contents") String rawText,
                                @RequestParam(value = "save") String buttVal, ModelMap model) {
        WikiPage page = new WikiPage(pageName, rawText, null);

        if (!buttVal.equals("Save")) { // user pressed "preview"
            page.setRawText(rawText);
            model.addAttribute("wikiPage", page);
            model.addAttribute("htmlText", page.getHtmlText());
            model.addAttribute("rawText", page.getRawText());
            model.addAttribute("nextPath", "/wiki/Special:Create");
            model.addAttribute("urlFriendlyName", page.getName().replace(' ', '_'));
            return "preview-page";
        }

        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!pageName.startsWith("Private:")) {
            WikiPage edits = wikiDao.getWikiPageByName("Special:RecentEdits");

            Stack<String> list = edits.getHistory();
            if (list == null)
                list = new Stack<String>();

            Stack<String> history = new Stack<String>();
            for (String s : list) {
                history.push(s);
            }

            if (history.size() == 50) {
                history.remove(0);
            }
            DateTime zoned = DateTime.now(DateTimeZone.forID("GMT"));
            String date = UsefulMethods.getWikiDateFormatter().print(zoned);
            if (!history.isEmpty() && history.peek().contains(((Member) user).getUsername() + "</a>") &&
                    history.peek().contains(pageName)) {
                history.pop();
            }
            history.push("Page <a href=\"/wiki/" + pageName.replace(' ', '_') + "\">" + pageName + "</a> was created by <img src=\"/static/images/flags/" + ((Member) user).getCountry() + ".png\"/> <a href=\"/wiki/User:" + ((Member) user).getUsername() + "\">" +
                    ((Member) user).getUsername() + "</a> on " + date + '.');

            wikiDao.updatePageWithText("Special:RecentEdits", "", history);
        }

        page.setHistory(dealWithHistoryStack(page.getHistory(), ((Member) user).getUsername()));
        wikiDao.store(page);

        return "redirect:/wiki/" + page.getName().replace(' ', '_');
    }

    @RequestMapping(value = "/files/{fileName:.*}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] wikiImgProcessFile(@PathVariable String fileName, HttpServletRequest request, HttpServletResponse response) {
        ServletContext context = request.getSession().getServletContext();
        String absPath = context.getRealPath("/") + "/wikiImg/";
        String filePath = absPath + fileName;

        try {
            File file = new File(filePath);
            FileInputStream is = new FileInputStream(file);
            byte[] array = IOUtils.toByteArray(is);
            IOUtils.closeQuietly(is);
            return array;
        } catch (Exception e) {
            try {
                response.sendError(404);
            } catch (IOException e1) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private Stack<String> dealWithHistoryStack(Stack<String> list, String username) {
        if (list == null)
            list = new Stack<String>();

        Stack<String> history = new Stack<String>();
        for (String s : list) {
            history.push(s);
        }

        if (history.size() == 30) {
            history.remove(0);
        }

        DateTime zoned = DateTime.now(DateTimeZone.forID("GMT"));
        String date = UsefulMethods.getWikiDateFormatter().print(zoned);
        //history.push(date + " by <a href=\"/members/"+username+"\">"+username+"</a>" );
        history.insertElementAt(date + " by <a href=\"/wiki/User:" + username + "\">" + username + "</a>", 0);
        return history;
    }
}
