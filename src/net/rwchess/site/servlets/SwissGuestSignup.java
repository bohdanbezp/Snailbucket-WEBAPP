package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RssItem;
import net.rwchess.site.data.SwissGuest;
import net.rwchess.site.utils.Mailer;
import net.rwchess.site.utils.UsefulMethods;

import com.google.appengine.api.datastore.Text;

public class SwissGuestSignup extends HttpServlet {
	private static final String charset = "0123456789abcdefghijklmnopqrstuvwxyz";
	 
    public static String getRandomString(int length) {
        Random rand = new Random(System.currentTimeMillis());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int pos = rand.nextInt(charset.length());
            sb.append(charset.charAt(pos));
        }
        return sb.toString();
    }
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		if (DAO.playsInSwiss(UsefulMethods.getUsername(req.getSession())))
			return;
		
		// email validation
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
	    Matcher m = p.matcher(req.getParameter("email"));
	    
	    if (!m.matches()) {
	    	res.getOutputStream().println("Please, enter valid email address");
	    	return;
	    }
	    
	    String ip = req.getRemoteAddr();
	    
	    if (DAO.swissGuestWithIpExist(ip)) {
	    	res.getOutputStream().println("There is already exist a guest registered " +
	    			"with IP address " + ip + " in the database. If there is some mistake " +
	    					"please, contact a tourney TD.");
	    	return;
	    }
		
		SwissGuest pl = new SwissGuest();
		String uname = req.getParameter("user");
		pl.setUsername(uname);
		pl.setFixedRating(Import.getRatingFor(uname));
		pl.setConfirmed(false);
		String generatedPlainPassword = getRandomString(10);
		pl.setGeneratedPlainPassword(generatedPlainPassword);
		pl.setPasswordHash(UsefulMethods.getMD5(generatedPlainPassword));
		pl.setCountry(req.getParameter("country"));
		pl.setEmail(req.getParameter("email"));
		pl.setIpAddress(req.getRemoteAddr());
		DAO.get().getPersistenceManager().makePersistent(pl);
		
		// mail admins	
		String msgBody = pl.getUsername()
				+ " has registered for upcomming RW Swiss 2011 with fixed rating "
				+ pl.getFixedRating();
		
		RssItem rss = new RssItem();
		rss.setTitle("RW Swiss registration");
		rss.setContent(new Text(msgBody));
		rss.setDate(new Date());
		DAO.deleteObj("RssFead");
		rss.setType("general");
		DAO.get().getPersistenceManager().makePersistent(rss);
		
		Mailer.swissGuestReg(uname,req.getParameter("email"));
		
		res.getOutputStream().println("Thank you! Our TD has got a notification, you will receive your password via FICS message in the next 2 or 3 days.");
	}
}