package net.rwchess.site.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RWSwissPlayer;
import net.rwchess.site.utils.Mailer;
import net.rwchess.site.utils.UsefulMethods;

public class SwissSignup extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		if (DAO.playsInSwiss(UsefulMethods.getUsername(req.getSession())))
			return;
		
		RWSwissPlayer pl = new RWSwissPlayer();
		String uname = UsefulMethods.getUsername(req.getSession());
		pl.setUsername(uname);
		pl.setFixedRating(Import.getRatingFor(uname));
		DAO.get().getPersistenceManager().makePersistent(pl);
		DAO.flushSwissParticipantsCache();
		
		// mail admins	
		String msgBody = pl.getUsername()
				+ " has registered for upcomming RW Swiss 2010 with fixed rating "
				+ pl.getFixedRating();
		Mailer.emailSignup(msgBody);
		
		res.sendRedirect("/swiss2010");
	}
}
