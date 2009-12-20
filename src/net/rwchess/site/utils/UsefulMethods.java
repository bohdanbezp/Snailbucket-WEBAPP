/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site.utils;

import info.bliki.wiki.model.WikiModel;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.datastore.Text;

import net.rwchess.site.data.RWMember;

/**
 * Some useful utility methods.
 * @author bodia
 *
 */
public final class UsefulMethods {
	
	private static final WikiModel wikiModel = new WikiModel(
			"http://rwchess.appspot.com/wiki/${image}",
			"http://rwchess.appspot.com/wiki/${title}");

	private UsefulMethods() {} // provides non-instensability
	
	/**
	 * Gets a plain password string and returns MD5 hash
	 */
	public static String getMD5(String passwd) {
		MessageDigest alg = null;
		try {
			alg = MessageDigest.getInstance("MD5");
			alg.reset(); 		
			alg.update(passwd.getBytes());
		} 
		catch (NoSuchAlgorithmException e) {}			
		
		byte[] digest = alg.digest();
		
		StringBuffer hashedpasswd = new StringBuffer();
		String hx;
		for (int i=0;i<digest.length;i++){
			hx =  Integer.toHexString(0xFF & digest[i]);
			if(hx.length() == 1){hx = "0" + hx;} 
			hashedpasswd.append(hx);
		}

        return hashedpasswd.toString();
	}
	
	/**
	 * Inserts HTML breaklines into the text
	 */
	public static String parseNewsText(String txt) {
		StringBuilder bui = new StringBuilder();
		bui.append("<p>");
		for (char c: txt.toCharArray()) {
			if (c == '\n') 
				bui.append("</p><p>");
			else 
				bui.append(c);
		}
		bui.append("</p>");
		return bui.toString();
	}
	
	/**
	 * Returns current user username using session data
	 */
	public static String getUsername(HttpSession s) {
		if (s.getAttribute("user") != null)
		     return ((RWMember) s.getAttribute("user")).getUsername();
		else throw new RuntimeException("User attribute is null");
	}
	
	/**
	 * Converts the number representation of a group to word
	 */
	public static String groupToWord(int group) {
		switch (group) {
		case RWMember.ADMIN:
			return "admin";
		case RWMember.MODERATOR:
			return "moderator";
		case RWMember.TD:
			return "td";
		default:
			return "member";
		}
	}

	/**
	 * Does the reverse process of groupToWord()
	 */
	public static int wordToGroup(String word) {
		if (word.equalsIgnoreCase("admin")) return RWMember.ADMIN;
		else if (word.equalsIgnoreCase("td")) return RWMember.TD;
		else if (word.equalsIgnoreCase("moderator")) return RWMember.MODERATOR;
		else return RWMember.MEMBER;
	}

	/**
	 * Creates footer to wrap error text
	 */
	public static void doDesignFooter(ServletRequest request,
			ServletResponse response) {
		try {
			// calls bottom jsp
			request.getRequestDispatcher("/blocks/bottom.jsp").include(request,
					response);
		} 
		catch (ServletException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates header to wrap error text
	 */
	public static void doDesignHeader(ServletRequest request,
			ServletResponse response) {
		try {
			request.getRequestDispatcher("/blocks/top.jsp").include(request,
					response);
			request.getRequestDispatcher("/blocks/currevents.jsp").include(
					request, response);
		} 
		catch (ServletException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public static Text getHtml(Text rawText) {
		return new Text(wikiModel.render(rawText.getValue()));
	}

	/**
	 * @param content Result in string representation
	 * @param isRwFirst count points for first or second team
	 * @return number of points what RW members have won
	 */
	public static double parseStringToPoints(String content, boolean isRwFirst) {
		if (content.equals("1/2-1/2"))
			return 0.5;
		else if (((content.equals("1-0") || content.equals("i-o")) && isRwFirst)
				|| ((content.equals("0-1") || content.equals("o-i"))
				&& !isRwFirst))
			return 1;
		return 0;
	}

	public static boolean lookLikeXss(String uname) {
		for (char c: uname.toCharArray()) {
			if (c == '<' || c == '>' || c == ' ' || c == '/')
				return true;
		}
		return false;
	}	
	
	public static String capitalize(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0)
			return str;

		return new StringBuffer(strLen).append(
				Character.toTitleCase(str.charAt(0))).append(str.substring(1))
				.toString();
	}

}
