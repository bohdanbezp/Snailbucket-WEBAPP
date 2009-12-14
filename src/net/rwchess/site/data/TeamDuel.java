package net.rwchess.site.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class TeamDuel implements Serializable {	
	private static final long serialVersionUID = 1147121078067473577L;

	/*
	 * This is an MD5 hash of section + roundNumber variables
	 */
	@PrimaryKey
	@Persistent
	private String key;
	
	@Persistent
	private String rwTeamname;
	
	@Persistent
	private String opponentTeamname;
	
	@Persistent
	private String section;
	
	@Persistent
	private String roundNumber;
	
	@Persistent
	private List<String> rwPlayersList;
	
	@Persistent
	private List<String> opponentPlayersList;
	
	@Persistent
	private List<String> results;
	
	@Persistent
	private boolean isWhiteFirst;
	
	@Persistent
	private List<Boolean> fixated;

	public String getRwTeamname() {
		return rwTeamname;
	}

	public String getOpponentTeamname() {
		return opponentTeamname;
	}

	public List<String> getRwPlayersList() {		
		return rwPlayersList;
	}

	public List<String> getOpponentPlayersList() {
		return opponentPlayersList;
	}

	public void setRwTeamname(String rwTeamname) {
		this.rwTeamname = rwTeamname;
	}

	public void setOpponentTeamname(String opponentTeamname) {
		this.opponentTeamname = opponentTeamname;
	}

	public void addRwPlayer(String player) {
		if (rwPlayersList == null) 
			rwPlayersList = new ArrayList<String>();
		
		rwPlayersList.add(player);
	}

	public void addOpponentPlayer(String opp) {
		if (opponentPlayersList == null) 
			opponentPlayersList = new ArrayList<String>();
		
		opponentPlayersList.add(opp);
	}

	public List<String> getResults() {
		return results;
	}

	public void addResult(String rst) {
		if (results == null) 
			results = new ArrayList<String>();
		
		results.add(rst);
	}

	public String getSection() {
		return section;
	}

	public String getRoundNumber() {
		return roundNumber;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public void setRoundNumber(String roundNumber) {
		this.roundNumber = roundNumber;
	}
	
	public String toString() {
		return "RWPlayers: " + rwPlayersList.toString() + " Opponents: " + opponentPlayersList.toString() + " " + roundNumber;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {		
		this.key = key;
	}
	
	public void setResults(List<String> results) {		
		this.results = results;
	}

	public boolean isWhiteFirst() {
		return isWhiteFirst;
	}

	public void setWhiteFirst(boolean isWhiteFirst) {
		this.isWhiteFirst = isWhiteFirst;
	}

	public List<Boolean> getFixated() {
		return fixated;
	}

	public void setFixated(List<Boolean> fixated) {
		this.fixated = fixated;
	}
}
