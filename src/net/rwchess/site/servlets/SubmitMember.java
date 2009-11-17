package net.rwchess.site.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RWMember;
import net.rwchess.site.utils.UsefulMethods;

public class SubmitMember extends HttpServlet {
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			RWMember m = pm.getObjectById(RWMember.class, req
					.getParameter("username"));
			m.setCountry(req.getParameter("country"));
			m.setGroup(UsefulMethods.wordToGroup(req.getParameter("group")));
		} 
		finally {
			pm.close();
		}
		
		res.sendRedirect("/users/edit");
	}
}
