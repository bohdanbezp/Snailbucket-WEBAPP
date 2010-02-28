package net.rwchess.site.data;

import java.io.IOException;
import java.util.Stack;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import net.rwchess.site.utils.Mailer;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class LatestEvents {	
	private static final long serialVersionUID = 17L;
	
	@PrimaryKey
	@Persistent
	private String key;
	
	@Persistent
	private Stack<String> stack;	
	
	private static String getLinkToUser(String username) {		
		return "<a href=\"/members/"+username+"\">"+username+"</a>";
	}
	
	public LatestEvents() {
		key = "LatestEvents";
	}
	
	public static void addWikiPageCreation(String title, String username) {		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		LatestEvents m;
		try {
			m = pm.getObjectById(LatestEvents.class, "LatestEvents");	
			if (m.getStack().size() > 3) {
				m.getStack().remove(0);
			}
			m.getStack().add("<small>"+getLinkToUser(username)+" has created wiki article " +
					"<a href=\"/wiki/"+title+"\">"+title+"</a></small>");
		} 
		catch (JDOObjectNotFoundException e) {
			m = new LatestEvents();
			m.getStack().add("<small>"+getLinkToUser(username)+" has created wiki article " +
					"<a href=\"/wiki/"+title+"\">"+title+"</a></small>");
			DAO.get().getPersistenceManager().makePersistent(m);
		}
		finally {
			pm.close();
		}
		
		try {
			String body = username
					+ " has created wiki page \""
					+ title
					+ "\". You may access it using the following link: http://rwchess.appspot.com/wiki/"
					+ title.replace(' ', '_');
			Mailer.emailWikipageCreation(body);
			DAO.deleteObj("WikiBackupTable");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addFileUpload(String filename, String username) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		LatestEvents m;
		try {
			m = pm.getObjectById(LatestEvents.class, "LatestEvents");
			if (m.getStack().size() > 4) {
				m.getStack().remove(m.getStack().size()-1);
			}
			m.getStack().add("<small>"+getLinkToUser(username)+" has uploaded file " +
					"<a href=\"/files//"+filename+"\">"+filename+"</a></small>");
		} 
		catch (JDOObjectNotFoundException e) {
			m = new LatestEvents();
			m.getStack().add("<small>"+getLinkToUser(username)+" has uploaded file " +
					"<a href=\"/files//"+filename+"\">"+filename+"</a></small>");
			DAO.get().getPersistenceManager().makePersistent(m);			
		}
		finally {
			pm.close();
		}
	}

	public Stack<String> getStack() {
		if (stack == null)
			stack = new Stack<String>();
		
		return stack;
	}

	public void setStack(Stack<String> stack) {
		this.stack = stack;
	}

	public String getKey() {
		if (key == null)
			key = "LatestEvents";
		
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
