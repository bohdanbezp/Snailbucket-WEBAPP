/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site.data;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class ForumMessage  implements Serializable {
	private static final long serialVersionUID = 8211344644578108032L;

	@Persistent
	private String username;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;    
	
	@Persistent
	private String forumName;

	@Persistent
	private String title;
	
	@Persistent
	private Text message;

	@Persistent
	private Date timestamp;

	public ForumMessage(String username, String forumName, Date timestamp,
			String title, Text message) {
		this.username = username;
		this.forumName = forumName;
		this.timestamp = timestamp;
		this.message = message;
		this.title = title;
		key = KeyFactory.createKey(ForumMessage.class.getSimpleName(), 
				timestamp.toString());
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getForumName() {
		return forumName;
	}

	public void setForumName(String forumName) {
		this.forumName = forumName;
	}
	
	public void setKey(Key key) {
        this.key = key;
    }

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Text getMessage() {
		return message;
	}

	public void setMessage(Text message) {
		this.message = message;
	}

}
