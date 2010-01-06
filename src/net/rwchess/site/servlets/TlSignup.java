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
import net.rwchess.site.data.T41Player;
import net.rwchess.site.utils.UsefulMethods;



public class TlSignup extends HttpServlet {	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		T41Player player = new T41Player();
		player.setAvailability(Byte.valueOf(req.getParameter("investtime")));
		player.setPreferedSection(req.getParameter("section"));
		player.setUsername(UsefulMethods.getUsername(req.getSession()));
		player.setFixedRating(Import.getRatingFor(player.getUsername()));
		DAO.get().getPersistenceManager().makePersistent(player);
		
		// mail admins
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		String msgBody = player.getUsername() + " has registered for upcomming " +
				"T41 and marked his availability as \"" + UsefulMethods.avlbByteToString(
                 player.getAvailability()) + "\" with fixed rating of " + player.getFixedRating();

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("bvk256@gmail.com",
			"RW Notify"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					"psotar@web.de", "pchesso"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					"bvk256@gmail.com", "Bodia"));
			msg.setSubject("T41 registration notification");
			msg.setText(msgBody);
			Transport.send(msg);

		} 
		catch (AddressException e) {
			e.printStackTrace();
		} 
		catch (MessagingException e) {
			e.printStackTrace();
		}
        
		DAO.flushTlParticipantsCache();
		res.sendRedirect("/t41");
	}
}
