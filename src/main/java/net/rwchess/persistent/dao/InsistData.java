package net.rwchess.persistent.dao;

import java.util.Arrays;
import java.util.List;

/**
 * Created by araszek.
 * Stores information about player's bad and hard times (insist).
 */
public class InsistData {
	private String insistSeparator="#";
	
	private String badTimes;
	private String hardTimes;
	
	/**
	 * Create instance in basis on insist data.
	 */
	public InsistData(String insistData) {
		this.badTimes = "";
		this.hardTimes="";
		
		if(insistData != null) {
			List<String> insis = Arrays.asList(insistData.split("#"));
		
			if(insis.size()>0) {
				this.badTimes=insis.get(0);
			}		
		
			if(insis.size()>1) {
				this.hardTimes=insis.get(1);
			}
		}
	}
	
	/**
	 * Create instance in basis on bad and hard times.
	 */
	public InsistData(String badTimes, String hardTimes) {
		this.badTimes = "";
		this.hardTimes="";
		
		if(badTimes != null) {
			this.badTimes = badTimes;
		}
		
		if(hardTimes != null) {
			this.hardTimes = hardTimes;
		}
    }
	
	/**
	 * Returns string representation of instance (insist).
	 */
	public String toString() {
		return badTimes+insistSeparator+hardTimes;    		
	}
	
	/**
	 * Returns player's bad times.
	 */
	public String getBadTimes() {
		return this.badTimes;
	}
	
	/**
	 * Returns player's hard times.
	 */
	public String getHardTimes() {
		return this.hardTimes;
	}
}