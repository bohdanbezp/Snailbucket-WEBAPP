/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site.utils;

import info.bliki.wiki.model.WikiModel;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.datastore.Text;

import net.rwchess.site.data.RWMember;
import net.rwchess.site.data.RWSwissPlayer;
import net.rwchess.site.data.T42Player;

/**
 * Some useful utility methods.
 * @author bodia
 *
 */
public final class UsefulMethods {
	
	private static final WikiModel wikiModel = new WikiModel(
			"/files/${image}",
			"/wiki/${title}");
	
	private static SimpleDateFormat dateFormat;

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
		else return "null";
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

	public static SimpleDateFormat getWikiDateFormatter() {
		if (dateFormat == null)
			dateFormat = new SimpleDateFormat("h:mm, d MMMMM yyyy", Locale.US);
		
		return dateFormat;
	}
	
	public static byte[] concat(byte[] A, byte[] B) {
		   byte[] C= new byte[A.length+B.length];
		   System.arraycopy(A, 0, C, 0, A.length);
		   System.arraycopy(B, 0, C, A.length, B.length);

		   return C;
		}

	
	public static String getMembersTableHtml(List<RWMember> members, List<String> aliveUsers) {
		StringBuffer buff = new StringBuffer();
		//int maxRows = members.size()/4;
		buff.append("<table border=\"0\" align=\"center\">");
		int coloumn = 0;

		for (RWMember m : members) {
			if (coloumn == 0)
				buff.append("<tr>");
			else if (coloumn == 3) {
				buff.append("</tr>");
				coloumn = 0;
			}
			
			buff.append("<td width=\"25%\">");
			buff.append("<img src=\"http://simile.mit.edu/exhibit/examples/flags/images/" +
					""+m.getCountry()+".png\" border=\"0\"/>");
			
			if (containAlive(aliveUsers, m.getUsername())) {
				buff.append("<a href=\"/wiki/User:"
						+ m.getUsername() + "\" style=\"color: #DC143C\">" + m.getUsername()
						+ "</a>");
			} else
				buff.append("<a href=\"/wiki/User:" + m.getUsername() + "\">"
						+ m.getUsername() + "</a>");

			coloumn++;
		}
		buff.append("</table>");
		return buff.toString();
	}
	
	private static boolean containAlive(List<String> aliveUsers, String username) {
		for (String u: aliveUsers) {
			if (u.equals(username))
				return true;
		}
		return false;
	}

	/**
	 * A dirty hack that retrieves the actually requested URI from 
	 * an HTTP request
	 * @param request Text of HTTP request
	 * @return URI
	 */
	public static String getRealQueryURI(String request) {
		boolean start = false;
		boolean sign = false;
		StringBuffer buf = new StringBuffer();
		for (char c: request.toCharArray()) {
			if (c == '?' && sign)
				break;
				
			if (c == '?')
				sign = true;
			
			if (c == ' ' && start) 
				break;
			
			if (c == ' ') {
				start = true;
				continue;
			}
			
			if (c == '\n')
				break;
			
			if (start)
				buf.append(c);
		}
		return buf.toString();
	}
	
	public static String avlbByteToString(byte a) {
		switch(a) {
		case 0:
			return "All time";
		case 1:
			return "Most time";
		case 2:
			return "Unavailable most time";
		case 3:
			return "Reserve player";	
		}
		return "";
	}

	public static String getTlParticipantsHtml(List<T42Player> allPlayers) {
		StringBuffer buff = new StringBuffer();		
		for (T42Player pl : allPlayers) {
			buff.append("<tr>");
			buff.append("<td>" + pl.getUsername() + "</td>");
			buff.append("<td>" + pl.getFixedRating() + "</td>");
			buff.append("<td>" + pl.getPreferedSection() + "</td>");
			buff.append("<td>" + UsefulMethods.avlbByteToString(pl.getAvailability()) + "</td>");
			buff.append("</tr>");
		}
		return buff.toString();
	}
	
	public static String getSwissParticipantsHtml(List<RWSwissPlayer> allPlayers) {
		StringBuffer buff = new StringBuffer();		
		for (RWSwissPlayer pl : allPlayers) {
			buff.append("<tr>");
			buff.append("<td>" + pl.getUsername() + "</td>");
			buff.append("<td>" + pl.getFixedRating() + "</td>");
			buff.append("</tr>");
		}
		return buff.toString();
	}
	
	public static String convertSwissName(String input) {
		if (input == null)
			return "";
		
		return "Swiss Round " + input.charAt(9) + ": "
				+ input.substring(11);
	}

}
