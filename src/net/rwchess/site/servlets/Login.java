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
			}
			else {
				req.getSession().setAttribute("user", rst);
				res.sendRedirect(req.getParameter("ref"));
				return;
			}
		}
		catch (JDOObjectNotFoundException e) {			
			addLoginError("There is no such user. Please try again.",
					req.getSession());   
		}
		finally {
			pm.close();
		}
		
		res.sendRedirect("/users/login.jsp?ref="+req.getParameter("ref"));
	}

	private void addLoginError(String text, HttpSession session) {
			session.setAttribute("LoginError", text);	
	}
}
