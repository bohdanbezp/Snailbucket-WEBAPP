/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site.servlets;

import java.io.IOException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RWMember;
import net.rwchess.site.data.SwissGuest;
import net.rwchess.site.utils.UsefulMethods;

/**
 * Login action describes the login process
 * @author bodia
 *
 */
public class Login extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String username = req.getParameter("login");
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			RWMember rst = pm.getObjectById(RWMember.class, username);
			
			// if there is no such user we shouldn't get here
			String passwordHash = UsefulMethods.getMD5(
					req.getParameter("password"));
			
			if (!rst.getPasswordHash().equals(passwordHash)) {
				addLoginError("Invalid password!", req.getSession());
				res.sendRedirect("/wiki/Special:Login");
				return;
			}
			else {
				req.getSession().setAttribute("user", rst);
				res.sendRedirect(req.getParameter("ref"));
				return;
			}
		}
		catch (JDOObjectNotFoundException e) {
			handleSwissGuest(username,req.getParameter("password"),pm, 
					req.getSession(),res,req);	
		}
		finally {
			pm.close();
		}
		
		if (req.getSession().getAttribute("guest") == null) {			
				res.sendRedirect("/");
		}
	}

	private void handleSwissGuest(String username, String password, 
			PersistenceManager pm, HttpSession session, HttpServletResponse res,
			HttpServletRequest req) throws ServletException, IOException {
		try {	
			SwissGuest rst = pm.getObjectById(SwissGuest.class, username);
			
			if (!rst.isConfirmed()) {
				if (!rst.getGeneratedPlainPassword().equals(password)) {
					addLoginError("Invalid password!", session);
					res.sendRedirect("/wiki/Special:Login");
				}
				else {
					session.setAttribute("guest", "yes");
					session.setAttribute("login", rst.getUsername());
					res.sendRedirect("/swiss2011/guestpasschange");
				}
			}
			else {
				String passwordHash = UsefulMethods.getMD5(password);
				if (!rst.getPasswordHash().equals(passwordHash)) {
					addLoginError("Invalid password!", req.getSession());
					res.sendRedirect("/wiki/Special:Login");
				}
				else {					
					session.setAttribute("user", rst);
					session.setAttribute("guest", "yes");
					if (req.getParameter("ref").endsWith("/wiki/Special:Login"))
						res.sendRedirect("/");
					else
						res.sendRedirect(req.getParameter("ref"));
				}
			}
		}
		catch (JDOObjectNotFoundException e) {
			addLoginError("There is no such user. Please try again.",
					req.getSession());
				res.sendRedirect("/wiki/Special:Login");
		}		
	}

	private void addLoginError(String text, HttpSession session) {
			session.setAttribute("LoginError", text);	
	}
}
