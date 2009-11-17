/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.rwchess.site.utils.UsefulMethods;

/**
 * The class is used for displaying (mostly) access-related errors
 */
public final class ErrorsManager {
	public static final byte ADMIN_ONLY = -0x2;
	public static final byte MODERATOR_ONLY = -0x3;
	public static final byte TD_ONLY = -0x4;
	public static final byte MEMBERS_ONLY = -0x5;
	public static final byte PAGE_NONEXIST = -0x6;

	/**
	 * Displays an error of a specified kind to a user.
	 */
	public static void display(byte errorKind, ServletResponse response,
			ServletRequest request) {
		PrintWriter out = null;
		try {
			UsefulMethods.doDesignHeader(request, response);

			out = response.getWriter();
			
			switch (errorKind) {
			case ADMIN_ONLY:
				out.println("Only admins can see this page");
				break;
			case MODERATOR_ONLY:
				out.println("Only moderators can see this page");
				break;
			case MEMBERS_ONLY:
				out.println("Only members can see this page");
				break;
			case PAGE_NONEXIST:
				out.println("Sorry, this page does not exist yet");
				break;
			}			

			UsefulMethods.doDesignFooter(request, response);
			out.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Displays an error. Used primary for displaying an exception
	 * output.
	 */
	public static void display(String error, ServletResponse response,
			ServletRequest request) {
		PrintWriter out = null;
		try {
			UsefulMethods.doDesignHeader(request, response);

			out = response.getWriter();
			out.println(error);				

			UsefulMethods.doDesignFooter(request, response);
			out.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
