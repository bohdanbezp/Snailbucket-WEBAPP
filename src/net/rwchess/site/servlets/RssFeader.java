package net.rwchess.site.servlets;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nanoxml.XMLElement;
import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RssItem;

public class RssFeader extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		res.setContentType("application/xml");
		res.getOutputStream().write(DAO.getRssFead().getBytes());
		res.getOutputStream().flush();
	}

	public static String generateFeed(List<RssItem> items) {
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
				Locale.US);
		StringBuffer b = new StringBuffer();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<rss version=\"2.0\">\n" + "<channel>\n"
				+ "<title>Rainbow Warriors RSS Feed</title>\n"
				+ "<link>http://rwchess.appspot.com</link>\n"
				+ "<description>Rainbow Warriors RSS feed</description>\n");

		for (RssItem item : items) {
			XMLElement itemElement = new XMLElement();
			itemElement.setName("item");
			XMLElement link = new XMLElement();
			link.setName("link");
			link.setContent("http://rwchess.appspot.com");
			itemElement.addChild(link);
			XMLElement title = new XMLElement();
			title.setName("title");
			title.setContent(item.getTitle());
			itemElement.addChild(title);
			XMLElement date = new XMLElement();
			date.setName("pubDate");
			date.setContent(formatter.format(item.getDate()).toString());
			itemElement.addChild(date);
			XMLElement description = new XMLElement();
			description.setName("description");
			description.setContent(item.getContent().getValue());
			itemElement.addChild(description);
			XMLElement guid = new XMLElement();
			guid.setName("guid");
			guid.setAttribute("isPermaLink", "false");
			guid.setContent(Long.toString(item.hashCode()));
			itemElement.addChild(guid);
			b.append(itemElement.toString());
		}
		
    
		b.append("</channel>\n</rss>");
		return b.toString().replaceAll("ISPERMALINK", "isPermaLink");
	}
}
