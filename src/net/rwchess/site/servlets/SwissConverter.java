package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nanoxml.XMLElement;

public class SwissConverter extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		StringBuffer buff = new StringBuffer();
		StringTokenizer st = new StringTokenizer(req.getParameter("contents"),
				"\n");
		buff.append("<table border=\"1\">");
		while (st.hasMoreTokens()) {
			StringBuffer locBuff = new StringBuffer();
			StringTokenizer wordTokenizer = new StringTokenizer(st.nextToken());
			locBuff.append("<tr>");
			while (wordTokenizer.hasMoreTokens()) {
				String token = wordTokenizer.nextToken();
				
				if (token.equals("Result"))
					token = "Date/Result";
				
				if (!token.equals("Feder") && !token.startsWith("("))
					locBuff.append("<td>"+token+"</td>");
			}
			if (locBuff.toString().equals("<tr>"))
				continue;
			
			XMLElement tr = new XMLElement();			
			tr.parseString(locBuff+"</tr>");
			if (((XMLElement) tr.getChildren().get(1)).getContent().equals("Name"))
				locBuff.append("<td>Discussion address</td>");
			else {
				String forumPageName = "Swiss11:R" + "$ROUND" + "_"
						+ ((XMLElement) tr.getChildren().get(1)).getContent()
						+ "-"
						+ ((XMLElement) tr.getChildren().get(3)).getContent();
				locBuff.append("<td><a href=\"/wiki/" + forumPageName
						+ "\">game forum</a></td>");
			}
			locBuff.append("</tr>\n");
			buff.append(locBuff);
		}
		buff.append("</table>");
		res.setContentType("text/plain");
		res.getOutputStream().write(buff.toString().replaceAll(",", "")
				.replaceAll(":", "").replaceAll("Swiss11", "Swiss11:").getBytes());
		res.getOutputStream().flush();
	}
	
	public static String escapeXml(String str) {
		str = str.replaceAll("&", "&amp;");
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(">", "&gt;");
		str = str.replaceAll("\"", "&quot;");
		str = str.replaceAll("'", "&apos;");
		return str;
	}

}
