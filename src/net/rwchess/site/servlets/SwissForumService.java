package net.rwchess.site.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Text;

import net.rwchess.site.AuthorizationFilter;
import net.rwchess.site.data.DAO;
import net.rwchess.site.utils.UsefulMethods;
import net.rwchess.wiki.WikiPage;

public class SwissForumService extends HttpServlet  {

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		if (AuthorizationFilter.fireIfNotRegistered(req, res)) return; 
		
		String pageName = req.getParameter("pageName");
		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			WikiPage page = (WikiPage) pm.getObjectById(WikiPage.class, pageName);
			page.setHtmlText(new Text(page.getHtmlText().getValue() + "<p><b>"
					+ UsefulMethods.getUsername(req.getSession()) + ":</b><br/>"
					+ req.getParameter("contents") + "</p>"));
			res.sendRedirect("/wiki/"+page.getName());
		}
		finally {
			pm.close();
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
			String pageName = UsefulMethods.capitalize(
					req.getRequestURI().substring(6).replaceAll("%20", " "));	
			
			WikiPage page = DAO.getWikiPage(pageName);
			req.setAttribute("pageRequested", page);
			req.getRequestDispatcher("/WEB-INF/jsp/wiki/swiss/forum.jsp")
					.include(req, res);
	}
}
