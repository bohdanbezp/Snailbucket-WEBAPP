package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.Date;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Text;

import net.rwchess.site.data.ForumMessage;
import net.rwchess.site.data.DAO;
import net.rwchess.site.utils.Mailer;
import net.rwchess.site.utils.UsefulMethods;

/**
 * Action is called when a user attempts to post a message in a forum.
 */ 
public class PostMessage extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String username = UsefulMethods.getUsername(req.getSession());
		String title = req.getParameter("title");
		String forum = req.getParameter("forum");
		String message = req.getParameter("message");
		Date timestamp = new Date(System.currentTimeMillis());
		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			if (thereIsSuchForum(pm, forum)) {
				if (forum.equals("news")) {
					String body = username
							+ " has posted the following on the main page: \n\n"
							+ message;
					Mailer.forumPost(body, "Main page post");
				}
				
				pm.makePersistent(new ForumMessage(username, forum, timestamp,
						title, new Text(message)));
				pm.refreshAll();
			}
		} 
		finally {
			pm.close();
			res.sendRedirect("/index.jsp");
		}
	}

	private boolean thereIsSuchForum(PersistenceManager pm, String forum) {
		if (forum.equals("news"))
			return true;

		return false;
	}
}
