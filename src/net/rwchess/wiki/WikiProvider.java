package net.rwchess.wiki;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WikiProvider {

	public static void pageNonexistent() {
		// TODO Auto-generated method stub
		
	}

	// TODO: clean it
	
	public static void displayPage(WikiPage page, HttpServletRequest req,
			HttpServletResponse res) {		
		diplay("/WEB-INF/jsp/wiki/view-page.jsp", page, req, res);
	}
	
	public static void displayHistoryPage(WikiPage page, HttpServletRequest req,
			HttpServletResponse res) {
		diplay("/WEB-INF/jsp/wiki/history.jsp", page, req, res);		
	}

	public static void displayPageEdit(WikiPage page, HttpServletRequest req,
			HttpServletResponse res) {		
		diplay("/WEB-INF/jsp/wiki/edit-page.jsp", page, req, res);
	}
	
	public static void displayPagePreview(WikiPage page, boolean previewCreate,
			HttpServletRequest req, HttpServletResponse res) {
		req.setAttribute("previewCreate", new Boolean(previewCreate));
		diplay("/WEB-INF/jsp/wiki/preview-page.jsp", page, req, res);
	}
	
	public static void displayPageNonExistent(WikiPage page,
			HttpServletRequest req, HttpServletResponse res) {
		diplay("/WEB-INF/jsp/wiki/page-nonexist.jsp", page, req, res);		
	}
	
	public static void displayPageCreate(WikiPage page, HttpServletRequest req,
			HttpServletResponse res) {
		diplay("/WEB-INF/jsp/wiki/page-create.jsp", page, req, res);
	}
	
	public static void displayLoginPage(HttpServletRequest req,
			HttpServletResponse res) {
		try {
			req.getRequestDispatcher("/WEB-INF/jsp/wiki/wiki-login.jsp")
					.include(req, res);
		} 
		catch (ServletException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void diplay(String pageUri, WikiPage page, 
			HttpServletRequest req, HttpServletResponse res) {
		try {
			req.setAttribute("pageRequested", page);
			req.getRequestDispatcher(pageUri)
					.include(req, res);
		} 
		catch (ServletException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void displayPageNonExistentUser(WikiPage pg,
			HttpServletRequest req, HttpServletResponse res) {
		diplay("/WEB-INF/jsp/wiki/userpage-nonexist.jsp", pg, req, res);	
	}
}
