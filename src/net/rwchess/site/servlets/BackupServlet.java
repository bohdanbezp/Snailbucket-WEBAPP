package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.File;
import net.rwchess.site.data.RWMember;

public class BackupServlet extends HttpServlet  {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		
		String body = getBody(req.getRequestURI().substring(8));
		if (body == null) {
			res.sendError(404);
			return;
		}
		
		byte[] bytes = body.getBytes();
		res.setContentType("text/plain");
		res.setHeader("Content-Length", Integer.toString(bytes.length));
		res.getOutputStream().write(bytes);
		res.getOutputStream().flush();
	}

	private String getBody(String substring) {
		if (substring.startsWith("members")) {
			return DAO.getMembersBackupTable();
		}
		else if (substring.startsWith("files")) {
			return DAO.getFilesBackupTable();
		}
		else if (substring.startsWith("forum")) {
			return DAO.getForumBackupTable();
		}
		else if (substring.startsWith("wiki")) {
			return DAO.getWikiBackupTable();
		}
		
		return null;
	}

	public static String generateBackupTable(List items) {
		StringBuffer buff = new StringBuffer();
		for (Object o: items) {
			buff.append("{{{\n");
			buff.append(o.toString());
			buff.append("}}}\n");
		}
		return buff.toString();
	}
}
