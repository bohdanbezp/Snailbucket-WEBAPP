package net.rwchess.site.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.TeamDuel;
import net.rwchess.site.utils.TlPairingsParser;

public class CheckOutTl extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		try {						
			URL url = new URL("http://teamleague.org/pairings.php");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					url.openStream()));
			
			TlPairingsParser tparser = new TlPairingsParser();
			tparser.parseFromReader(reader);
			
			PersistenceManager pm = DAO.get().getPersistenceManager();
			try {
				for (TeamDuel d : tparser.getDuels()) {
					try {
						TeamDuel stored = (TeamDuel) pm.getObjectById(
								TeamDuel.class, d.getKey());
						stored.setResults(d.getResults());
						stored.setFixated(d.getFixated());
					} 
					catch (JDOObjectNotFoundException e) {
						DAO.get().getPersistenceManager().makePersistent(d);
					}						
				}
			} 
			finally {
				pm.close();
				reader.close();
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
