package net.rwchess.site.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RWMember;
import net.rwchess.site.data.RWSwissPlayer;
import net.rwchess.site.data.SwissGuest;
import net.rwchess.site.utils.UsefulMethods;

public class ChangePassword extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			if (req.getSession().getAttribute("guest") != null) {
				SwissGuest m = pm.getObjectById(SwissGuest.class, 
						UsefulMethods.getUsername(req.getSession()));
				m.setPasswordHash(UsefulMethods.getMD5(req.getParameter("password")));
				
				if (!m.isConfirmed()) {
					m.setConfirmed(true);
					m.setGeneratedPlainPassword("Approved");
					req.getSession().setAttribute("user", m);
					
					RWSwissPlayer pl = new RWSwissPlayer();
					String uname = m.getUsername();
					pl.setUsername(uname);
					pl.setFixedRating(Import.getRatingFor(uname));
					DAO.get().getPersistenceManager().makePersistent(pl);
					DAO.flushSwissParticipantsCache();	
					DAO.flushSwissGuestCache();
				}
			}
			else {
				RWMember m = pm.getObjectById(RWMember.class, UsefulMethods.getUsername(
						req.getSession()));
				m.setPasswordHash(UsefulMethods.getMD5(req.getParameter("password")));
			}
			res.sendRedirect("/swissreg2011");
		} 
		finally {
			pm.close();
		}
	}
}
