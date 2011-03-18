package net.rwchess.site.data;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import net.rwchess.site.utils.UsernameComparable;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class SwissGuest implements Serializable, UsernameComparable {
	private static final long serialVersionUID = -7921613095448187123L;
	
	@PrimaryKey
	@Persistent
	private String username;
	
	@Persistent
	private String passwordHash;
	
	@Persistent
	private int fixedRating;
	
	@Persistent
	private String email;
	
	@Persistent
	private String country;
	
	@Persistent
	private String generatedPlainPassword;
	
	@Persistent
	private boolean confirmed;
	
	@Persistent
	private String ipAddress;
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getGeneratedPlainPassword() {
		return generatedPlainPassword;
	}

	public void setGeneratedPlainPassword(String generatedPlainPassword) {
		this.generatedPlainPassword = generatedPlainPassword;
	}
	
	public int getFixedRating() {
		return fixedRating;
	}

	public void setFixedRating(int fixedRating) {
		this.fixedRating = fixedRating;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
