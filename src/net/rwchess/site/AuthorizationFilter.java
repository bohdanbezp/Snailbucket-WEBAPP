/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.rwchess.site.data.RWMember;
import net.rwchess.site.utils.UsefulMethods;

/**
 * All request go though this class. Practically all security is 
 * implemented via this class.
 */
public class AuthorizationFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = ((HttpServletRequest) request);
		String uri = httpReq.getRequestURI();
		response.setCharacterEncoding("utf-8");
		
		if (uri.equals("/actions/login") || uri.equals("/")
				|| uri.equals("/actions/import") || uri.equals("/index.jsp")) {			
			chain.doFilter(request, response); // go to the requested page			
		}
		else if (uri.startsWith("/actions")) {
			if (fireIfNotRegistered(httpReq, response)) return; 
			String action = uri.substring(9);

			if (action.equals("exit")) {
				httpReq.getSession().removeAttribute("user");
				((HttpServletResponse) response).sendRedirect("/");
				return;
			}
			if (action.startsWith("admin") || action.equals("/actions/userdelete")) {
				if (getUserGroup(httpReq.getSession()) < RWMember.MODERATOR) {
					ErrorsManager.display(ErrorsManager.MODERATOR_ONLY,
							response, request);
					return;
				}
			}
			
			chain.doFilter(request, response); // go to the requested page
		} 
		else if (uri.startsWith("/users")) {
			if (fireIfNotRegistered(httpReq, response)) return;
			String action = uri.substring(7);
			
			if (action.equals("edit")
					&& getUserGroup(httpReq.getSession()) < RWMember.MODERATOR) {
				ErrorsManager.display(ErrorsManager.MODERATOR_ONLY,
						response, request);
			}
			
			chain.doFilter(request, response);
		}
		else if (uri.startsWith("/members")) {
			if (uri.length() <= 8) {
				UsefulMethods.doDesignHeader(request, response);
				request.getRequestDispatcher("/users/members.jsp").include(request,
						response);
				UsefulMethods.doDesignFooter(request, response);
			}
			   
			else {
				String username = uri.substring(9);
				((HttpServletResponse) response).
				sendRedirect("/wiki/User:"+username);
			   }
			return;
		}	
		else if (uri.startsWith("/t41"))
			((HttpServletResponse) response).sendRedirect("/wiki/T41");
		else if (uri.startsWith("/ladder"))
			((HttpServletResponse) response).sendError(404);
		else
			//((HttpServletResponse) response).sendError(404);
			chain.doFilter(request, response);
	}

	private boolean fireIfNotRegistered(HttpServletRequest httpReq,
			ServletResponse response) {
		if (httpReq.getSession().getAttribute("user") == null) {
			try {
				((HttpServletResponse) response).sendError(403);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	/**
	 * A shortcut method that get the info about user rights
	 */
	private int getUserGroup(HttpSession s) {
		RWMember memb = (RWMember) s.getAttribute("user");
		if (memb == null)
			return 0;
		return memb.getGroup();
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}

}
