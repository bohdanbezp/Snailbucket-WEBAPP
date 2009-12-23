package net.rwchess.site.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RWMember;

public class AddMember extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		pm.makePersistent(new RWMember(req.getParameter("login"), req
				.getParameter("password"), 1, req.getParameter("country")));		
		res.getWriter().println("User registered! Please inform him ASAP");
	}
}
