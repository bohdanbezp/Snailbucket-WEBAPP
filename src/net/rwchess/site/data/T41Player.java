package net.rwchess.site.data;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class T41Player implements Serializable {
	
	private static final long serialVersionUID = 5810922866005756241L;
	
	@PrimaryKey
	@Persistent
	private String username;
	
	@Persistent
	private byte availability;
		
	@Persistent
	private String preferedSection;
	
	@Persistent
	private int fixedRating;
	
	@Persistent
	private double points;
	
	@Persistent
	private double games;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getUsername() {
		return username;
	}

	public byte getAvailability() {
		return availability;
	}

	public String getPreferedSection() {
		return preferedSection;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setAvailability(byte availability) {
		this.availability = availability;
	}

	public void setPreferedSection(String preferedSection) {
		this.preferedSection = preferedSection;
	}

	public int getFixedRating() {
		return fixedRating;
	}

	public double getPoints() {
		return points;
	}

	public double getGames() {
		return games;
	}

	public void setFixedRating(int fixedRating) {
		this.fixedRating = fixedRating;
	}

	public void setPoints(double points) {
		this.points = points;
	}

	public void setGames(double games) {
		this.games = games;
	}
}
