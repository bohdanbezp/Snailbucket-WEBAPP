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

public class AddMember extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		pm.makePersistent(new RWMember(req.getParameter("login"), UsefulMethods.getMD5(req
				.getParameter("password")), 1, req.getParameter("country")));
		DAO.flushMembersCache();
		res.getWriter().println("User registered! Please inform him ASAP");
	}
}
