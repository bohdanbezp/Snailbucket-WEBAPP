package net.rwchess.site.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

import nanoxml.XMLElement;
import net.rwchess.site.AuthorizationFilter;
import net.rwchess.site.data.DAO;
import net.rwchess.site.data.File;
import net.rwchess.site.utils.UsefulMethods;
import net.rwchess.wiki.WikiPage;

public class SwissForumService extends HttpServlet {
	
	private SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
			Locale.US);

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		if (AuthorizationFilter.fireIfNotRegistered(req, res)) return; 
		
		String pageName = req.getParameter("pageName");
		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			WikiPage page = (WikiPage) pm.getObjectById(WikiPage.class, pageName);
			String content = req.getParameter("contents");
			
			if (!req.getParameter("month").equals("0")) {
				Calendar cld = Calendar.getInstance();
				cld.set(Calendar.YEAR, 2010);
				cld.set(Calendar.MONTH, Integer.parseInt(req.getParameter("month"))-1);
				cld.set(Calendar.DAY_OF_MONTH, Integer.parseInt(req.getParameter("day")));
				cld.set(Calendar.HOUR_OF_DAY, Integer.parseInt(req.getParameter("hour")));
				cld.set(Calendar.MINUTE, Integer.parseInt(req.getParameter("minute")));
				Date dt = cld.getTime();
				
				SimpleDateFormat forumFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm",
						Locale.US);
				
				String date = forumFormatter.format(dt);
				
				setDate(date, UsefulMethods.getUsername(req.getSession()));
				content = "Has set the scheduled time to " + date;
			}
			else if (content.startsWith("http://www.ficsgames.com") ||
					content.startsWith("http://ficsgames.com")) {				
				URL url = new URL(content);
				String pgn = IOUtils.toString(new InputStreamReader(
						url.openStream()));
				pgn = "\n\n" + pgn;
				try {
					File fl = (File) pm.getObjectById(File.class,
							"swiss2010.pgn");
					fl.setFile(new Blob(UsefulMethods.concat(fl.getFile()
							.getBytes(), pgn.getBytes())));
				} 
				catch (JDOObjectNotFoundException e) {
					Blob blob = new Blob(pgn.getBytes());
					File fl = new File("swiss2010.pgn", blob);
					pm.makePersistent(fl);
				}
				
				System.out.println("retrieveResult(pgn) " + retrieveResult(pgn));
				System.out.println("retrievePlayername(pgn) " + retrievePlayername(pgn));
				setResult(retrieveResult(pgn), retrievePlayername(pgn));
				res.sendRedirect("/wiki/RW_Swiss");
				
				page.setHtmlText(new Text(page.getHtmlText().getValue() + "<p><b>"
						+ UsefulMethods.getUsername(req.getSession()) + "</b> ("
						+ formatter.format(new Date()) + "):<br/>"
						+ req.getParameter("contents").replaceAll("\n", "<br/>")
						+ "</p>\n"));
				
				return;
			}
			
			page.setHtmlText(new Text(page.getHtmlText().getValue() + "<p><b>"
					+ UsefulMethods.getUsername(req.getSession()) + "</b> ("
					+ formatter.format(new Date()) + "):<br/>"
					+ content.replaceAll("\n", "<br/>")
					+ "</p>\n"));
			res.sendRedirect("/wiki/" + page.getName());
		}
		finally {
			pm.close();
		}
	}
	
	private void setDate(String date, String username) {
		String sect = CreatePairings.getPairingsFromSource().replaceAll("\n", "");
		XMLElement table = new XMLElement();
		table.parseString(sect);
	    for (Object ob: table.getChildren()) {    	
	    	XMLElement tr = (XMLElement) ob;
	    	if (tr.toString().contains("BYE")
					|| tr.getChildren().size() < 4
					|| ((XMLElement) tr.getChildren().get(1)).getContent()
							.equals("Name"))
				continue;
	    	
	    	if (tr.toString().contains(username)) {
	    		XMLElement el = ((XMLElement) tr.getChildren().get(2));
	    		String old = tr.toString().replaceAll("HREF", "href");
	    		el.setContent(date);
	    		
	    		PersistenceManager pm = DAO.get().getPersistenceManager();
	    		try {
					WikiPage page = pm
							.getObjectById(WikiPage.class, "RW Swiss");
					page.setRawText(new Text(page.getRawText().getValue()
							.replaceAll(">\n", ">").replaceAll("HREF", "href")
							.replaceAll(old,
									tr.toString().replaceAll("HREF", "href"))));
					page.setHtmlText(UsefulMethods.getHtml(page.getRawText()));
	    		}
	    		finally {
	    			pm.close();
	    		}
	    		return;
	    	}
	    }
	}

	private void setResult(String result, String username) {
		String sect = CreatePairings.getPairingsFromSource().replaceAll("\n", "");
		XMLElement table = new XMLElement();
		table.parseString(sect);
	    for (Object ob: table.getChildren()) {    	
	    	XMLElement tr = (XMLElement) ob;
	    	if (tr.toString().contains("BYE")
					|| tr.getChildren().size() < 4
					|| ((XMLElement) tr.getChildren().get(1)).getContent()
							.equals("Name"))
				continue;
	    	
	    	if (tr.toString().contains(username)) {
	    		XMLElement el = ((XMLElement) tr.getChildren().get(2));
	    		String old = tr.toString().replaceAll("HREF", "href");
	    		el.setContent(result);
	    		
	    		PersistenceManager pm = DAO.get().getPersistenceManager();
	    		try {
					WikiPage page = pm
							.getObjectById(WikiPage.class, "RW Swiss");
					page.setRawText(new Text(page.getRawText().getValue()
							.replaceAll(">\n", ">").replaceAll("HREF", "href")
							.replaceAll(old,
									tr.toString().replaceAll("HREF", "href"))));
					page.setHtmlText(UsefulMethods.getHtml(page.getRawText()));
	    		}
	    		finally {
	    			pm.close();
	    		}
	    		return;
	    	}
	    }
	}

	private String retrieveResult(String pgn) {
		StringTokenizer st = new StringTokenizer(pgn,
		"\n");
		
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			String rst = "";
			boolean in = false;
			if (s.startsWith("[Result ")) {
				for (char c: s.toCharArray()) {
					if (in && c != '\"')
						rst+=c;
					
					if (c == '\"') {						
						if (in)
							return rst;
						
						in = true;
					}
						
				}
			}
		}
		
		return "*";
	}
	
	private String retrievePlayername(String pgn) {
		StringTokenizer st = new StringTokenizer(pgn,
		"\n");
		
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			String rst = "";
			boolean in = false;
			if (s.startsWith("[White ")) {
				for (char c: s.toCharArray()) {
					if (in && c != '\"')
						rst+=c;
					
					if (c == '\"') {						
						if (in)
							return rst;
						
						in = true;
					}
						
				}
			}
		}
		
		return "*";
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String pageName = UsefulMethods.capitalize(req.getRequestURI()
				.substring(6).replaceAll("%20", " "));

		if (pageName.endsWith("/rss.xml")) {
			processRss(req, res, pageName.replaceAll("/rss.xml", ""));
			return;
		}
		else if (pageName.endsWith("/favicon.ico")) {
			res.sendRedirect("/favicon.ico");
			return;
		}

		WikiPage page = DAO.getWikiPage(pageName);
		
		if (page == null) {
			res.sendError(404);
			return;
		}
		
		req.setAttribute("pageRequested", page);
		req.getRequestDispatcher("/WEB-INF/jsp/wiki/swiss/forum.jsp").include(
				req, res);
	}

	private void processRss(HttpServletRequest req, HttpServletResponse res,
			String pageName) throws IOException {
		WikiPage page = DAO.getWikiPage(pageName);
		
		StringBuffer b = new StringBuffer();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<rss version=\"2.0\">\n" + "<channel>\n"
				+ "<title>"+UsefulMethods.convertSwissName(page.getName())+"</title>\n"
				+ "<link>http://rwchess.appspot.com/wiki/"+page.getName()+"</link>\n"
				+ "<description>Game forum RSS feed</description>\n");
		
		StringTokenizer st = new StringTokenizer(page.getHtmlText().getValue(),
		"\n");
		
		while (st.hasMoreTokens()) {
			String purify = st.nextToken().replaceAll("<p>", "").replaceAll(
					"</p>", "").replaceAll("<b>", "").replaceAll(
							"</b>", "").replaceAll(
									"<br/>", "").replaceAll(":", ": ");
			
			XMLElement itemElement = new XMLElement();
			itemElement.setName("item");
			XMLElement link = new XMLElement();
			link.setName("link");
			link.setContent("http://rwchess.appspot.com");
			itemElement.addChild(link);
			XMLElement title = new XMLElement();
			title.setName("title");
			title.setContent(page.getName());
			itemElement.addChild(title);
			XMLElement date = new XMLElement();
			date.setName("pubDate");
			date.setContent(getDate(purify));
			itemElement.addChild(date);
			XMLElement description = new XMLElement();
			description.setName("description");
			description.setContent(purify);
			itemElement.addChild(description);
			XMLElement guid = new XMLElement();
			guid.setName("guid");
			guid.setAttribute("isPermaLink", "false");
			guid.setContent(Long.toString(purify.hashCode()));
			itemElement.addChild(guid);
			b.append(itemElement.toString());
		}
		
		b.append("</channel>\n</rss>");
		res.setContentType("application/xml");
		res.getOutputStream().write(b.toString().replaceAll("ISPERMALINK", "isPermaLink").getBytes());
		res.getOutputStream().flush();
	}

	private String getDate(String purify) {
		boolean in = false;
		StringBuffer b = new StringBuffer();
		for (char c: purify.toCharArray()) {
			if (in && c == ')')
				return b.toString();
				
			if (in)
				b.append(c);
			
			if (c == '(')
				in = true;
		}
		return null;
	}
}
