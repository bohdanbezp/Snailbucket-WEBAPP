/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site.data;

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

import net.rwchess.wiki.WikiPage;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

/**
 * Persistence manager class. 
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
        props.put(GCacheFactory.EXPIRATION_DELTA, 3600);

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
		
		Object[] lst;
		if ((lst = (Object[]) cache.get(("NewsList"))) == null) {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			Query query = pm.newQuery(ForumMessage.class);
			query.setFilter("forumName == lastNameParam");
			query.setOrdering("timestamp desc");
			query.declareParameters("String lastNameParam");
			query.setRange(0, amount);
			lst = ((List<ForumMessage>) query.execute("news")).toArray();
			cache.put("NewsList", lst);
			return lst;
		}

		return lst;	
	}
	
	public static Object[] getAllPlayers() {
		
		Object[] lst;
		if ((lst = (Object[]) cache.get(("AllPlayers"))) == null) {
			PersistenceManager pm = pmfInstance.getPersistenceManager();
			Query query = pm.newQuery(RWMember.class);
			query.setOrdering("username asc");			
			lst = ((List<RWMember>) query.execute()).toArray();
			cache.put("AllPlayers", lst);
			return lst;
		}

		return lst;
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
		PersistenceManager pm = pmfInstance.getPersistenceManager();
		Query query = pm.newQuery(File.class);
		query.setFilter("fileName == file");
		query.declareParameters("String file");
		
		return ((List<File>) query.execute(fileName)).get(0).getFile();
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
}
