package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.TlPlayer;
import net.rwchess.site.utils.Mailer;
import net.rwchess.site.utils.UsefulMethods;



public class TlSignup extends HttpServlet {	
	private String gamesFromInt(int i) {
		switch (i) {
		case 0:
			return "1-3 games";
		case 1:
			return "3-5 games";
		case 2:
			return "5-8 games";
		default:
			return "0 games :(";
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		TlPlayer player = new TlPlayer();
		String games = gamesFromInt(Integer.valueOf(req.getParameter("investtime")));
		player.setGames(games);
		player.setCaptain(req.getParameter("capt"));
		player.setComments(req.getParameter("comments"));
		player.setUsername(UsefulMethods.getUsername(req.getSession()));
		player.setFixedRating(Import.getRatingFor(player.getUsername()));
		DAO.get().getPersistenceManager().makePersistent(player);
		
		// mail admins	
		String msgBody = player.getUsername() + " has registered for upcomming " +
				"T46 and marked his availability as \"" + games + "\" with fixed rating of " + player.getFixedRating();
		Mailer.emailSignup(msgBody);		
        
		DAO.flushTlParticipantsCache();
		res.sendRedirect("/t46");
	}
}
