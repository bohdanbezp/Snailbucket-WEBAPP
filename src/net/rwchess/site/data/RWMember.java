/**
 * No copyright. The code is released into the public domain according to
 * the definition of "public domain" in your country.  
 */

package net.rwchess.site.data;

import java.io.Serializable;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * The class is used to envelope user's persistent data 
 * @author bodia
 * 
 */
@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class RWMember implements Serializable {	
	
	private static final long serialVersionUID = -8157598273836465828L;

	@PrimaryKey
	@Persistent
	private String username;
	
	@Persistent
	private String passwordHash;
	
	@Persistent
	private int group;
	
	@Persistent
	private String country;
	
	public static final int ADMIN = 4;
	public static final int MODERATOR = 3;
	public static final int TD = 2;
	public static final int MEMBER = 1;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public int getGroup() {
		return group;
	}
	
	public void setGroup(int group) {
		this.group = group;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
		
	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}
	
	public RWMember(String username, String passwordHash, int group,
			String country) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.group = group;
		this.country = country;
	}
	
	public String toString() {
		return "Username: " + username + "\n" +
				"PasswordHash: " + passwordHash + "\n" +
						"Group: " + group + "\n" +
								"Country: " + country + "\n";
		
	}
}
