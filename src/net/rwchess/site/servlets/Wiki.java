package net.rwchess.site.servlets;

import java.io.*;
import java.util.Date;
import java.util.Stack;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.datastore.Text;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.LatestEvents;
import net.rwchess.site.utils.UsefulMethods;
import net.rwchess.wiki.WikiPage;
import net.rwchess.wiki.WikiProvider;

/**
 * This servlet manages the entire wiki part, incl. rights management.
 */
public class Wiki extends HttpServlet {	
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		
		if (req.getRequestURI().equals("/wiki") ||
				req.getRequestURI().equals("/wiki/")) {
			res.sendRedirect("/wiki/Main_page");
			return;
		}
		String pageName = UsefulMethods.capitalize(
				req.getRequestURI().substring(6).replace('_', ' '));		
		
		if (pageName.startsWith("Special")) {
			String action = pageName.substring(8);
			/*
			 * We use action.startsWith() here instead of action.equals()
			 * because the URI can contain some parameters which 
			 * cannot be adequately compared 
			 */
			
			if (isAuthorized(req.getSession())) {
				// only members can do following actions
				if (action.startsWith("Edit")) {
					WikiPage pg = DAO.getWikiPage(req.getParameter("page").replace('_', ' '));
					if (pg == null) {
						res.sendError(404);						
					}

					WikiProvider.displayPageEdit(pg, req, res);
					return;
				}
				else if (action.startsWith("CreatePage")) {
					WikiProvider.displayCreatePage(req, res);
					return;
				}
				else if (action.startsWith("Create")) {
					WikiPage pg = new WikiPage();
					pg.setName(req.getParameter("page").replace('_', ' '));
					WikiProvider.displayPageCreate(pg, req, res);
					return;
				}				
			}
			if (action.startsWith("Login")) {
				WikiProvider.displayLoginPage(req, res);
				return;
			} 
			else if (action.startsWith("History")) {
				WikiPage pg = DAO.getWikiPage(req.getParameter("page").replace('_', ' '));
				if (pg == null) {
					res.sendError(404);
					return;
				}				
				
				WikiProvider.displayHistoryPage(pg, req, res);
				return;
			}
			res.sendError(403); // send forbidden
			return;
		}
		
		WikiPage pg = DAO.getWikiPage(pageName);
		if (pg == null) {
			pg = new WikiPage();
			pg.setName(pageName);
			if (pageName.startsWith("User:") && !pageName.substring(5).equals(
							UsefulMethods.getUsername(req.getSession())))
				WikiProvider.displayPageNonExistentUser(pg, req, res);
			else
				WikiProvider.displayPageNonExistent(pg, req, res);
		}
		else
			WikiProvider.displayPage(pg, req, res);
	}
	
	private boolean isAuthorized(HttpSession s) {		
		return s.getAttribute("user") != null;
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		HttpServletRequest httpReq = ((HttpServletRequest) req);		
		
		if (httpReq.getRequestURI().startsWith("/wiki/Special:Edit")) {
			String pageName = req.getParameter("pageName").replace('_', ' ');
			if (req.getParameter("save") != null) { // user pressed "save" button
				PersistenceManager pm = DAO.get().getPersistenceManager();
				try {
					WikiPage page = pm.getObjectById(WikiPage.class, req.getParameter("pageName"));
					page.setName(pageName);
					page.setRawText(new Text(req.getParameter("contents")));
					dealWithHistoryStack(page.getHistory(), UsefulMethods
							.getUsername(req.getSession()));

					res.sendRedirect("/wiki/"+page.getName().replace(' ', '_'));
				}
				finally {
					pm.close();
				}
			}
			else if (req.getParameter("preview") != null) { // user pressed "preview"
				WikiPage page = new WikiPage();
				page.setRawText(new Text(req.getParameter("contents")));
				page.setName(pageName);
				WikiProvider.displayPagePreview(page, false, req, res);				
			}
		}		
		else if (httpReq.getRequestURI().startsWith("/wiki/Special:Create")) {
			String pageName = UsefulMethods.capitalize(
					req.getParameter("pageName").replace('_', ' '));
			
			if (pageName.startsWith("User:") && !pageName.substring(5).
					equals(UsefulMethods.getUsername(req.getSession()))) {
				res.sendError(403);
				return;
			}				
			
			if (req.getParameter("save") != null) {
				PersistenceManager pm = DAO.get().getPersistenceManager();
				try {
					WikiPage page = new WikiPage();
					page.setName(pageName);
					page.setRawText(new Text(req.getParameter("contents")));
					dealWithHistoryStack(page.getHistory(), UsefulMethods
							.getUsername(req.getSession()));
					pm.makePersistent(page);
					LatestEvents.addWikiPageCreation(pageName, UsefulMethods
							.getUsername(req.getSession()));
					res.sendRedirect("/wiki/"+page.getName().replace(' ', '_'));
				}
				finally {
					pm.close();
				}
			}
			else if (req.getParameter("preview") != null) {
				WikiPage page = new WikiPage();
				page.setRawText(new Text(req.getParameter("contents")));
				page.setName(pageName);
				WikiProvider.displayPagePreview(page, true, req, res);				
			}			
		}
		else if (httpReq.getRequestURI().startsWith("/wiki/Special:Login")) {
			new Login().doPost(req, res); // go to the main login procedure
		}		
		else
			res.sendError(404);
	}

	private void dealWithHistoryStack(Stack<String> history, String username) {
		if (history.size() == 30) {
			history.remove(history.size()-1);
		}
		String date = UsefulMethods.getWikiDateFormatter().format(new Date());
		history.push(date + " by <a href=\"/members/"+username+"\">"+username+"</a>" );	
	}
}
