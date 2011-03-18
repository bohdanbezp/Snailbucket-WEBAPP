package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SwissConverter extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		StringBuffer buff = new StringBuffer();
		StringTokenizer st = new StringTokenizer(req.getParameter("contents"),
				"\n");
		buff.append("<table border=\"1\">");
		while (st.hasMoreTokens()) {
			StringTokenizer wordTokenizer = new StringTokenizer(st.nextToken());
			buff.append("<tr>");
			while (wordTokenizer.hasMoreTokens()) {
				String token = wordTokenizer.nextToken();
				
				if (token.equals("Result"))
					token = "Date/Result";
				
				if (!token.equals("Feder") && !token.startsWith("("))
					buff.append("<td>"+token+"</td>");
			}
			buff.append("</tr>\n");
		}
		buff.append("</table>");
		res.setContentType("text/plain");
		res.getOutputStream().write(buff.toString().replaceAll(",", "").getBytes());
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
