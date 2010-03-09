package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Text;

import nanoxml.XMLElement;
import net.rwchess.site.data.DAO;
import net.rwchess.site.utils.UsefulMethods;
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
			String t = st.nextToken();
			if (isPairings)	
				result.append(t);
			
			if (isPairings && t.startsWith("=="))
				isPairings = false;
			
			if (t.contains("Pairings"))
				isPairings = true;
		}
		
		return result.toString();
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		if (req.getParameter("button") != null) {			
			parseTable(getPairingsFromSource(), req.getParameter("round"));
		}
	}

	private void parseTable(String pairingsFromSource, String round) {
		List<String> toCreate = new ArrayList<String>();
		XMLElement table = new XMLElement();
		table.parseString(pairingsFromSource);
	    for (Object ob: table.getChildren()) {    	
	    	XMLElement tr = (XMLElement) ob;
	    	if (tr.toString().contains("BYE") || tr.getChildren().size() < 4)
	    		continue;
	    	
			toCreate.add("Swiss10:R" + round + "_"
					+ ((XMLElement) tr.getChildren().get(1)).getContent() + "-"
					+ ((XMLElement) tr.getChildren().get(3)).getContent());
		}
	    createPages(toCreate);
	}

	private void createPages(List<String> toCreate) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		for (String name: toCreate) {
			WikiPage page = new WikiPage();
			page.setName(name);
			page.setHtmlText(new Text(""));
			pm.makePersistent(page);
		}
	}
}
