package net.rwchess.site.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RWMember;
import net.rwchess.site.utils.UsefulMethods;

public class UserDelete extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		if (req.getSession().getAttribute("DeleteConfirmation") == null) {
			String uname = req.getParameter("username");		
			if (UsefulMethods.lookLikeXss(uname)) {
				res.getWriter().println("XSS attempt detected");
				return;
			}				
			
			res.getWriter().println("<h3>Please confirm you're really want to delete "+
					req.getParameter("username")+"</h3>");
			res.getWriter().println("<center><a href=\"/actions/userdelete\">Yes</a><br/>" +
					"<a href=\"/users/edit\">No</a></center>");
			req.getSession().setAttribute("DeleteConfirmation", req
					.getParameter("username"));
		}
		else {
			PersistenceManager pm = DAO.get().getPersistenceManager();
			try {
				RWMember m = pm.getObjectById(RWMember.class, req.getSession()
						.getAttribute("DeleteConfirmation"));
				pm.deletePersistent(m);
				req.getSession().removeAttribute("DeleteConfirmation");
				
				res.sendRedirect("/users/edit");
			} 
			finally {
				pm.refreshAll();
				pm.close();
			}
		}
	}

}
