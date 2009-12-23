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

public class ChangePassword extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			RWMember m = pm.getObjectById(RWMember.class, UsefulMethods.getUsername(
					req.getSession()));
			m.setPasswordHash(UsefulMethods.getMD5(req.getParameter("password")));
		} 
		finally {
			pm.close();
		}
	}
}
