package net.rwchess.site.data;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class RWSwissPlayer implements Serializable  {	
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Persistent
	private String username;
	
	@Persistent
	private int fixedRating;

	public String getUsername() {
		return username;
	}

	public int getFixedRating() {
		return fixedRating;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setFixedRating(int fixedRating) {
		this.fixedRating = fixedRating;
	}
}
