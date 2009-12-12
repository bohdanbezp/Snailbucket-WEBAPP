package net.rwchess.site.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.utils.TeamDuel;
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
			for (TeamDuel d: tparser.getDuels()) {
				res.getWriter().println(d.toString());
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
