package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.wiki.WikiPage;

public class CreatePairings extends HttpServlet {
	public static String getPairingsFromSource() {
		WikiPage p = DAO.getWikiPage("RW Swiss");
		String source = p.getRawText().getValue();
		boolean isPairings = false;
		StringBuffer result = new StringBuffer();
		StringTokenizer st = new StringTokenizer(source,
		"\n");
		while (st.hasMoreTokens()) {
			if (isPairings)	
				result.append(result);
			
			if (st.nextToken().contains("Pairings"))
				isPairings = true;
		}
		
		return result.toString();
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		if (req.getParameter("button") != null) {			
			parseTable(getPairingsFromSource());
		}
	}

	private void parseTable(String pairingsFromSource) {
		// TODO Auto-generated method stub
		
	}
}
