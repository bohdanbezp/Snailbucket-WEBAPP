package net.rwchess.site.data;

import java.util.Stack;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class LatestEvents<E> extends Stack<String> {
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Persistent
	private String key;
	
	private LatestEvents() {
		key = "LatestEvents";
	}
	
	private static String getLinkToUser(String username) {		
		return "<a href=\"//members//"+username+"\">"+username+"</a>";
	}
	
	public static void addWikiPageCreation(String title, String username) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			LatestEvents<String> m = pm.getObjectById(LatestEvents.class, "LatestEvents");
			if (m.size() > 4) {
				m.remove(m.size()-1);
			}
			m.add(getLinkToUser(username)+" created wiki article " +
					"<a href=\"//wiki//"+title+"\">"+title+"</a>");
		} 
		catch (JDOObjectNotFoundException e) {
			LatestEvents<String> m = new LatestEvents<String>();
			DAO.get().getPersistenceManager().makePersistent(m);
		}
		finally {
			pm.close();
		}
	}

	public static void addFileUpload(String filename, String username) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			LatestEvents<String> m = pm.getObjectById(LatestEvents.class, "LatestEvents");
			if (m.size() > 4) {
				m.remove(m.size()-1);
			}
			m.add(getLinkToUser(username)+" has uploaded file " +
					"<a href=\"//files//"+filename+"\">"+filename+"</a>");
		} 
		catch (JDOObjectNotFoundException e) {
			LatestEvents<String> m = new LatestEvents<String>();
			DAO.get().getPersistenceManager().makePersistent(m);
		}
		finally {
			pm.close();
		}
	}
}
