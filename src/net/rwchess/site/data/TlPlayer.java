package net.rwchess.site.data;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class TlPlayer implements Serializable {
	
	private static final long serialVersionUID = 5810922866005756241L;
	
	@PrimaryKey
	@Persistent
	private String username;	
		
	@Persistent
	private String captain;
	
	@Persistent
	private int fixedRating;	
	
	@Persistent
	private String games;
	
	@Persistent
	private String comments;

	public String getUsername() {
		return username;
	}

	public String getCaptain() {
		return captain;
	}
	
	public String getComments() {
		return comments;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setCaptain(String captain) {
		this.captain = captain;
	}

	public int getFixedRating() {
		return fixedRating;
	}

	public String getGames() {
		return games;
	}

	public void setFixedRating(int fixedRating) {
		this.fixedRating = fixedRating;
	}

	public void setGames(String games) {
		this.games = games;
	}	
}
