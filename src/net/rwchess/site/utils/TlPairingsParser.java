package net.rwchess.site.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;
import net.rwchess.site.data.DAO;
import net.rwchess.site.data.TeamDuel;

public class TlPairingsParser {
	
	private String roundNumber;
	
	private String s;
	
	private List<TeamDuel> duels;
	
	/**
	 * Erases all tags, except those starting with 't', also
	 * removes all parameters. You'd love it!
	 */
	public void preParseHTMLMess() {
        StringBuilder buffer = new StringBuilder();

        boolean in = false;
        boolean skip = false;
        char prev = 0;

        for (char c: s.toCharArray()) {
            if (c == '<')
                in = true;
            if (c == '>') {
                skip = false;
                in = false;
                prev = 0;
            }
            if (in && c == ' ')
                skip = true;

            if (prev == '/' && c != 't' && !Character.isDigit(c)) {
            	skip = true;
            }
            else if (prev == '<' && c != 't' && c != '/')
                skip = true;

            if (skip)
                continue;

            buffer.append(c);
            prev = c;
        }
        s = buffer.toString();
        s = s.replaceAll("&nbsp;", ""); // filtering non-breaking space characters
        s = s.replaceAll("<>", ""); // filtering brackets that have been left from removed tags
        s = s.replaceAll("</>", ""); // filtering the residue of removed closing tags         
    }    
	
	public void parseFromReader(BufferedReader reader) throws IOException {
		String line;

		try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("          <div class=\"content-full\">")) {
					s = line;
					duels = new ArrayList<TeamDuel>();
					preParseHTMLMess();
					parseRoundNumber();		
					/* From this moment s apparently can be called a well-formed XML document */
					parseValidXml();
					
					break;
				}
			}
		}
		finally {
			reader.close();
		}
	}

	private void parseRoundNumber() {
		roundNumber = s.substring(27, 35);		
		
		s = s.substring(34); // filtering out non-xml parts 
		if (Character.isDigit(s.charAt(0))) { // damn, round number is two digits long
			s = s.substring(1); // we won't need any kind of that crap in xml
		}
		else
			roundNumber = roundNumber.trim();		
	}

	private void parseValidXml() {
		try {
			XMLElement root = new XMLElement();
			s = "<root>" + s + "</root>"; // frame s with root tags
			
			root.parseString(s); // the culmination moment
			
			String ratingSection = "";	
			
			for (Object e: root.getChildren()) {
				/* non-generics vector obviously isn't my favorite datatype
				 * now we are iterating though about a dozen "table" nodes, 
				 * their children count can easily tell us who's who
				 */
	            XMLElement m = (XMLElement) e; 	                        
	            
	            if (m.countChildren() == 1) { // the declaration of rating section
	            	// we can extract it with 2 simple steps without a real headache
	            	String p = m.toString().substring(15);	            	
	            	ratingSection = p.substring(0, p.length()-18);	            	
	            }
	            else if (m.countChildren() == 5) { // match sections
	            	XMLElement fir = (XMLElement) m.getChildren().get(0); // getting 1st child
	            	boolean isRw = mightyWarriorsPlaying(fir);
	            	
	            	if (!isRw) // why wasting resources for other teams
	            		continue;	 
	            	
	            	XMLElement firstTeam = (XMLElement) fir.getChildren().get(0);
	            	XMLElement secondTeam = (XMLElement) fir.getChildren().get(2);
	            	TeamDuel duel = new TeamDuel();	
	            	duel.setSection(ratingSection);
	            	duel.setRoundNumber(roundNumber);
	            	duel.setRwTeamname(
	            			mightyWarriorsPlaying(firstTeam) ? firstTeam.getContent() :
	            				secondTeam.getContent());
	            	duel.setOpponentTeamname(
	            			!mightyWarriorsPlaying(firstTeam) ? firstTeam.getContent() :
	            				secondTeam.getContent());
	            	if (duel.getFixated() == null) {
	            		List<Boolean> fix = new ArrayList<Boolean>();
	            		for (int j = 0; j < 4; j++) {
	            			fix.add(false);
	            		}
	            		duel.setFixated(fix);
	            	}
	            	List<Boolean> fix = duel.getFixated();
	            	
	            	for (int i = 1; i < 5; i++) {
	            		XMLElement el = (XMLElement) m.getChildren().get(i);
	            		XMLElement firstPl = (XMLElement) el.getChildren().get(1);	            		
	            		XMLElement result = (XMLElement) el.getChildren().get(2);
		            	XMLElement secondPl = (XMLElement) el.getChildren().get(3);
		            	duel.addRwPlayer(mightyWarriorsPlaying(firstTeam) ? 
		            			firstPl.getContent() : secondPl.getContent());
		            	duel.addOpponentPlayer(!mightyWarriorsPlaying(firstTeam) ? 
		            			firstPl.getContent() : secondPl.getContent());
		            	duel.addResult(result.getContent());		
		            	
						if (matchesResultPattern(result.getContent())) {
							if (!fix.get(i - 1)) {
								fix.set(i - 1, true);
								double points = UsefulMethods.parseStringToPoints(
												result.getContent(),
												mightyWarriorsPlaying(firstTeam));
								DAO.fixateResult(mightyWarriorsPlaying(firstTeam) ? 
												firstPl.getContent() :
													secondPl.getContent(), points);								
								duel.setFixated(fix);
							}
		            	}
		            	
	            	}
	            	duel.setWhiteFirst(((XMLElement) ((XMLElement) m.getChildren().get(1))
	            			.getChildren().get(0)).getContent().equals("White"));
	            	duel.setKey(UsefulMethods.getMD5(ratingSection+roundNumber));
	            	duels.add(duel);
	            }
	        }			
		} 
		catch (XMLParseException e) {
			// if we get here, then the input was totally f****d up
			e.printStackTrace(); // I'll see it in logs anyway
		}
	}

	private boolean matchesResultPattern(String content) {
		return content.equals("1/2-1/2") || content.equals("1-0")
				|| content.equals("0-1") || content.equals("i-o")
				|| content.equals("o-i");
	}

	/**
	 * Is this RWarriors match? Works unless any other team
	 * picks the "rainbow" name
	 */
	private boolean mightyWarriorsPlaying(XMLElement e) {	
		return e.toString().toLowerCase().contains("rainbow");
	}

	public List<TeamDuel> getDuels() {
		return duels;
	}
}
