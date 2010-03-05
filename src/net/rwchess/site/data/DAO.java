/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import net.rwchess.site.servlets.BackupServlet;
import net.rwchess.site.servlets.RssFeader;
import net.rwchess.site.utils.MembersComparator;
import net.rwchess.site.utils.UsefulMethods;
import net.rwchess.wiki.WikiPage;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

/**
 * Data access object 
 */
public final class DAO {
	private static final PersistenceManagerFactory pmfInstance = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	
	private static final Cache cache = createCache();

	private DAO() {
	}

	public static PersistenceManagerFactory get() {
		return pmfInstance;
	}
	
	private static Cache createCache() {
		Cache cache = null;
		Map props = new HashMap();
        props.put(GCacheFactory.EXPIRATION_DELTA, 1296000);

		try {
		    CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
		    cache = cacheFactory.createCache(props);
		} 
		catch (CacheException e) {
		    // ...
		}
		
		return cache;
	}

	public static Object[] getNewsList(int amount) {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(ForumMessage.class);
		query.setFilter("forumName == lastNameParam");
		query.setOrdering("timestamp desc");
		query.declareParameters("String lastNameParam");
		query.setRange(0, amount);
		Object[] lst = ((List<ForumMessage>) query.execute("news")).toArray();
		return lst;		
	}
	
	public static List<RWMember> getAllPlayers() {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(RWMember.class);
		List<RWMember> members = (List<RWMember>) query.execute();
		Collections.sort(members, MembersComparator.getInstance());
		return members;
	}
	
	public static String getAllMembersTable() {
		String s;
		//if ((s = (String) cache.get("AllMembersTable")) == null) {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			CacheObject co;
			try {
				co = (CacheObject) pm.getObjectById(
						CacheObject.class, "AllMembersTable");
				s = co.getHtml().getValue();
			} 
			catch (JDOObjectNotFoundException e) {
				s = UsefulMethods.getMembersTableHtml(getAllPlayers(), getAliveUsers());
				co = new CacheObject();
				co.setKey("AllMembersTable");
				co.setHtml(new Text(s));
				pm.makePersistent(co);
			}

		//	cache.put("AllMembersTable", s);
		//}
		return s;
	}
	
	public static Object[] getUploadedFiles(String username) {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(UploadedFile.class);
		query.setFilter("uploaderName == uploader");
		query.setOrdering("uploadDate desc");
		query.declareParameters("String uploader");
		Object[] lst = ((List<UploadedFile>) query.execute(username)).toArray();

		return lst;
	}
	
	public static Object[] getLatestUploadedFiles() {		
				
		Object[] lst;
		if ((lst = (Object[]) cache.get(("LatestUploadedFiles"))) == null) {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			Query query = pm.newQuery(UploadedFile.class);
			query.setOrdering("uploadDate desc");
			query.setRange(0, 5);			
			lst = ((List<UploadedFile>) query.execute()).toArray();
			cache.put("LatestUploadedFiles", lst);
			return lst;
		}	
		
		return lst;		
	}

	public static Blob getFile(String fileName) {
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			return ((File) pm.getObjectById(File.class, fileName)).getFile();
		} 
		catch (JDOObjectNotFoundException e) {
			return null;
		}
	}

	public static WikiPage getWikiPage(String pageName) {
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			return (WikiPage) pm.getObjectById(WikiPage.class, pageName);
		} 
		catch (JDOObjectNotFoundException e) {
			return null;
		}
	}
	
	public static List<TeamDuel> getCurrRoundTeamDuels() {
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			Query query = pm.newQuery(TeamDuel.class);
			query.setOrdering("roundNumber desc");
			query.setRange(0, 2);
			return (List<TeamDuel>) query.execute();
		} 
		catch (JDOObjectNotFoundException e) {
			return null;
		}
	}

	public static void fixateResult(String player, double winningPoints) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		
		if (player.equals("SachinRavi"))
			player = "sachinravi";
		
		try {
			T41Player mem = (T41Player) pm.getObjectById(
					T41Player.class, player);
			mem.setGames(mem.getGames()+1);
			mem.setPoints(mem.getPoints()+winningPoints);
		}
		catch (JDOObjectNotFoundException e) {			
		}
		finally {
			pm.close();
		}
	}
	
	public static TeamDuel getTlDuel(String key) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			TeamDuel duel = (TeamDuel) pm.getObjectById(
					TeamDuel.class, key);
			return duel;
		}
		catch (JDOObjectNotFoundException e) {		
			return null;
		}
		finally {
			pm.close();
		}
	}
	
	public static void fixateResult(List<Boolean> fix, String key) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			TeamDuel duel = (TeamDuel) pm.getObjectById(
					TeamDuel.class, key);
			duel.setFixated(fix);
		}
		catch (JDOObjectNotFoundException e) {			
		}
		finally {
			pm.close();
		}
	}
	
	public static boolean playsInT41(String name) {
		if (name.equals("null"))
			return false;
		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			T41Player mem = (T41Player) pm.getObjectById(
					T41Player.class, name);
			return true;
		}
		catch (JDOObjectNotFoundException e) {
			return false;
		}
		finally {
			pm.close();
		}
	}
	
	public static boolean playsInSwiss(String name) {
		if (name.equals("null"))
			return false;
		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			RWSwissPlayer mem = (RWSwissPlayer) pm.getObjectById(
					RWSwissPlayer.class, name);
			return true;
		}
		catch (JDOObjectNotFoundException e) {
			return false;
		}
		finally {
			pm.close();
		}
	}
	
	public static List<T41Player> getTlParticipants(boolean sortByPoints) {
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			Query query = pm.newQuery(T41Player.class);
			if (sortByPoints)
				query.setOrdering("points desc");
			else
				query.setOrdering("fixedRating desc");
			return (List<T41Player>) query.execute();
		} 
		catch (JDOObjectNotFoundException e) {
			return null;
		}
	}
	
	public static List<RWSwissPlayer> getSwissParticipants() {
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			Query query = pm.newQuery(RWSwissPlayer.class);
			query.setOrdering("fixedRating desc");
			return (List<RWSwissPlayer>) query.execute();
		} 
		catch (JDOObjectNotFoundException e) {
			return null;
		}
	}
	
	public static String getTlParticipantsTable() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "TlParticipantsTable");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			s = UsefulMethods.getTlParticipantsHtml(getTlParticipants(false));
			co = new CacheObject();
			co.setKey("TlParticipantsTable");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}
	
	public static String getSwissParticipantsTable() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "SwissParticipantsTable");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			s = UsefulMethods.getSwissParticipantsHtml(getSwissParticipants());
			co = new CacheObject();
			co.setKey("SwissParticipantsTable");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}
	
	public static LatestEvents getEvents() {
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();			
			return pm.getObjectById(LatestEvents.class, "LatestEvents");
		} 
		catch (JDOObjectNotFoundException e) {
			return null;
		}
	}
	
	public static List<WikiEditObject> getAllWikiEditObjects() {
		try {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			Query query = pm.newQuery(WikiEditObject.class);
			query.setOrdering("dateStamp desc");
			return (List<WikiEditObject>) query.execute();
		} 
		catch (JDOObjectNotFoundException e) {
			return null;
		}
	}
	
	public static void deleteObj(String name) {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			Object co = pm.getObjectById(CacheObject.class,
					name);
			pm.deletePersistent(co);
		} 
		catch (JDOObjectNotFoundException e) {
		}
	}
	
	public static void deleteWikiObj(String name) {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		try {
			Object co = pm.getObjectById(WikiEditObject.class,
					name);
			pm.deletePersistent(co);
		} 
		catch (JDOObjectNotFoundException e) {
		}
	}
	
	public static void flushMembersCache() {
		deleteObj("AllMembersTable");		
	}
	
	public static void flushTlParticipantsCache() {
		deleteObj("TlParticipantsTable");		
	}
	
	public static void flushSwissParticipantsCache() {
		deleteObj("SwissParticipantsTable");		
	}

	public static String getMembersBackupTable() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "MembersBackupTable");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			s = BackupServlet.generateBackupTable(getAllPlayers());
			co = new CacheObject();
			co.setKey("MembersBackupTable");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}

	public static String getFilesBackupTable() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "FilesBackupTable");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			s = BackupServlet.generateBackupTable(getAllFiles());
			co = new CacheObject();
			co.setKey("FilesBackupTable");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}
	
	public static String getForumBackupTable() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "ForumBackupTable");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			s = BackupServlet.generateBackupTable(getAllForums());
			co = new CacheObject();
			co.setKey("ForumBackupTable");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}
	
	public static List getAllFiles() {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(File.class);
		
		return (List) query.execute();
	}
	
	public static List getAllForums() {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(ForumMessage.class);
		
		return (List) query.execute();
	}

	public static String getWikiBackupTable() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "WikiBackupTable");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			s = BackupServlet.generateBackupTable(getAllWiki());
			co = new CacheObject();
			co.setKey("WikiBackupTable");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}

	private static List getAllWiki() {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(WikiPage.class);
		
		return (List) query.execute();
	}
	
	private static List<RssItem> getRssItems() {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(RssItem.class);
		
		return (List<RssItem>) query.execute();
	}
	
	public static String getAliveUsersTable() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "AliveUsersTable");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			StringBuffer b = new StringBuffer();
			for (String p : getAliveUsers()) {
				b.append("<a href=\"http://rwchess.appspot.com/wiki/User:" + p
						+ "\">" + p + "</a> ");
			}
			s = b.toString();
			co = new CacheObject();
			co.setKey("AliveUsersTable");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}
	
	public static List<String> getAliveUsers() {
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(WikiPage.class);		
		List<WikiPage> pages = (List<WikiPage>) query.execute();
		List<String> result = new ArrayList<String>();
		for (WikiPage page: pages) {
			if (page.getName().startsWith("User:")) {
				result.add(page.getName().substring(5));
			}
		}
		
		return result;
	}

	public static String getRssFead() {
		String s;
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		CacheObject co;
		try {
			co = (CacheObject) pm.getObjectById(
					CacheObject.class, "RssFead");
			s = co.getHtml().getValue();
		} 
		catch (JDOObjectNotFoundException e) {
			s = RssFeader.generateFeed(getRssItems());
			co = new CacheObject();
			co.setKey("RssFead");
			co.setHtml(new Text(s));
			pm.makePersistent(co);
		}
		return s;
	}
}
