package net.rwchess.controller;

import net.rwchess.persistent.Member;
import net.rwchess.persistent.dao.MemberDAO;
import net.rwchess.utils.UsefulMethods;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MainController
{

    private MemberDAO memberDAO;

    public MainController(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }

    @RequestMapping("/")
    public String generateList(Model model) {
//        MemberDAO rwMemberDAO = new MemberDAOHib();
//
//        Member rm = new Member();
//        rm.setConfirmed(true);
//        rm.setUsername("b");
//        rm.setPasswordHash(UsefulMethods.getMD5("b"));
//        rm.setCountry("ua");
//        rm.setInsist("no");
//        rm.setPreference("45 45");
//        rm.setEmail("bvk256@gmail.com");
//        rm.setGroup(3);
//        rwMemberDAO.store(rm);
//
//        WikiPageDAO wikiPageDAODAO = new WikiPageDAOHib();
//
//        WikiPage w = new WikiPage("test", "''tst''", null);
//        w.setTdProtected(false);
//        wikiPageDAODAO.store(w);
        return "redirect:/wiki/Main_page";
    }

    @RequestMapping("/members")
    public String generateMembers(Model model) {

        model.addAttribute("membersTable", UsefulMethods.getMembersTableHtml(memberDAO.getAllConfirmedMembers(), null));
        return "members";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profileGet(Model modelMap) {
        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member ourM = memberDAO.getMemberByUsername(user.getUsername());

        List<String> sortables = UsefulMethods.sortToSortables(ourM.getPreference());
        String selectable = UsefulMethods.insistToSelectable(ourM.getInsist());

        modelMap.addAttribute("username", user.getUsername());
        modelMap.addAttribute("sortable1", sortables.get(0));
        modelMap.addAttribute("sortable2", sortables.get(1));
        modelMap.addAttribute("selectable", selectable);
        modelMap.addAttribute("time_order", ourM.getPreference());
        modelMap.addAttribute("bad_times", ourM.getInsist());
        modelMap.addAttribute("key", user.getKey());
        return "profile";
    }

    @RequestMapping(value = "/profile/time", method = RequestMethod.POST)
    public String profileTimePost(@RequestParam(value = "time_order") String time_order,
                                  @RequestParam(value = "bad_times") String badTimes, Model modelMap) {
        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        memberDAO.updateTimeorder(user.getKey(), time_order.replace(",", ", "));
        memberDAO.updateInsist(user.getKey(), badTimes);

        modelMap.addAttribute("title", "");
        modelMap.addAttribute("error", "Your time preferences have been updated.");
        return "error";
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public String profilePost(@RequestParam(value = "password") String password,
                              @RequestParam(value = "newpassword") String passwordRepeat, Model modelMap) {
        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!password.equals(passwordRepeat)) {
            modelMap.addAttribute("title", "");
            modelMap.addAttribute("error", "Passwords don't match.");
            return "error";
        }

        memberDAO.updatePassword(user.getKey(), password);
        modelMap.addAttribute("title", "");
        modelMap.addAttribute("error", "The password has been changed.");
        return "error";
    }

    @RequestMapping("/members/{memName}")
    public String redirectMembers(@PathVariable String memName) {
       return "redirect:/wiki/Special:"+memName;
    }
}