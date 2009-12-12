package net.rwchess.site.utils;

import java.util.ArrayList;
import java.util.List;

public class TeamDuel {
	private String rwTeamname;
	
	private String opponentTeamname;
	
	private String section;
	
	private String roundNumber;
	
	private List<String> rwPlayersList;
	
	private List<String> opponentPlayersList;
	
	private List<String> results;

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
}
