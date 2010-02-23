package net.rwchess.site.servlets;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.mail.*;
import javax.mail.internet.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.T41Player;
import net.rwchess.site.data.TeamDuel;
import net.rwchess.site.utils.UsefulMethods;


/**
 * Used to import the preliminary data for once 
 */
public class Import extends HttpServlet {
	
	private static Map m;
	
	public static int getRatingFor(String username) {
		if (username.equalsIgnoreCase("roberttorma")) return 2189;
		if (username.equalsIgnoreCase("PeterSanderson")) return 2206;
		if (username.equalsIgnoreCase("Maras")) return 2162;
		if (username.equalsIgnoreCase("Karima")) return 2175;
		if (username.equalsIgnoreCase("iwulu")) return 2172;
		if (username.equalsIgnoreCase("AIDog")) return 2086;
		if (username.equalsIgnoreCase("zalik")) return 2140;
		if (username.equalsIgnoreCase("Pallokala")) return 2066;
		if (username.equalsIgnoreCase("jussu")) return 1941;
		if (username.equalsIgnoreCase("ivohristov")) return 1961;
		if (username.equalsIgnoreCase("WilkBardzoZly")) return 1734;
		if (username.equalsIgnoreCase("sangalla")) return 1975;
		if (username.equalsIgnoreCase("NatIN")) return 1875;
		if (username.equalsIgnoreCase("AlesD")) return 1879;
		if (username.equalsIgnoreCase("Acho")) return 1819;
		if (username.equalsIgnoreCase("NoiroP")) return 1815;
		if (username.equalsIgnoreCase("bodzolca")) return 1742;
		if (username.equalsIgnoreCase("Bodia")) return 1735;
		if (username.equalsIgnoreCase("HerrGott")) return 1658;
		if (username.equalsIgnoreCase("pchesso")) return 1697;
		if (username.equalsIgnoreCase("SachinRavi")) return 1624;
		if (username.equalsIgnoreCase("Gavrilo")) return 1644;
		if (username.equalsIgnoreCase("piorgovici")) return 1581;
		if (username.equalsIgnoreCase("lutom")) return 1539;
		if (username.equalsIgnoreCase("wfletcher")) return 1533;
		if (username.equalsIgnoreCase("Nitreb")) return 1269;
		else return 0;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {	
		
	/*	String[] names = { "PeterSanderson", "iwulu", "Maras", "Gregorioo",
				"ivohristov", "AlesD", "Acho", "NoiroP", "bodzolca", "Bodia",
				"WilkBardzoZly", "pchesso", "Gavrilo", "sachinravi", "piorgovici",
				"wfletcher", "Nitreb"}; */
		
		PersistenceManager pm = DAO.get().getPersistenceManager();
		try {
			for (TeamDuel d : DAO.getCurrRoundTeamDuels()) {
				System.out.println(d.getResults().size());
					if (d.getResults().size() > 4) {
						d.setResults(null);
						d.setFixated(null);
					}			
			}
		}
		finally {
			pm.close();
		}
		
		
		
		/*String[] names = {"Harmonicus"};
		
		for (String name: names) {
			T41Player pl = new T41Player();
			pl.setUsername(name);
			pl.setAvailability((byte)0);
			pl.setFixedRating(0);
			pl.setPreferedSection("1996");
			pm.makePersistent(pl);
		}*/	
		/**PersistenceManager pm = DAO.get().getPersistenceManager();
		String[] names = { "HerrGott", "Noiro", "piorgovici", "pchesso",
				"Bodia", "Acho", "sachinravi", "jussu", "Natin", "Nitreb",
				"roberttorma", "WilkBardzoZly", "iwulu", "wfletcher", "ivohristov",
				"Maras", "AlesD", "exray", "Gavrilo", "Pallokala", "bodzolca",
				"sangalla" };
		String[] countries = { "ro", "sk", "ro", "de", "ua", "ar", "in", "ee",
				"no", "ca", "hu", "pl", "ng", "za", "bg", "lt", "cz", "" +
						"ca", "cs", "fi", "si", "id" };
		String[] passwords = { "283ffefecd9c77eaac17eb510e0d0fde",
				"c098a4d9bb9516a951b7b510a76418b4",
				"c5258d384b2c9395cc56d0fa9f481306",
				"ce321c24dc777c81666271b4b78bc063",
				"44553e42030473c29b270fe3b1f728be",
				"a28710fcf793cd2374ac0c081e5c3f7d",
				"99693a548357e4b089837816c182a500",
				"d63d20e7ee8cc0dcfd68c038274945a2",
				"098f6bcd4621d373cade4e832627b4f6",
				"801d1b0502f760db02b6e690b0037430",				
				"968b18793e56cbea70692fba31189ae7",
				"3228d24e2ccc9443e82e58d5008c50f3",
				"10ce72c6b816ac8b25b062ebae2108ae",
				"74790f436b9dc6ae4d47bfb6c924d3ad",
				"95cbc4d8d2c2864de256fc08ce23d8c1",
				"7476ed9af142c6fe337846f0c5ac466b",
				"ea2b2676c28c0db26d39331a336c6b92",
				"64719db2fb744db5b11e76a5288323cf",
				"148de99d1e9f33f8ba3f8e0593730413",
				"ca209002fada69add4520c1532bd0ee3",
				"78c6d9c637aeaf5d3fd0be1220ed841e",
				"0aeeeb12859935e447391ce0750788de" };

		for (int i = 0; i < names.length; i++) {
			int rank = 1;
			if (names[i].equals("Bodia")) rank = 3;
			else if (names[i].equals("pchesso")) rank = 2;
			
			pm.makePersistent(new RWMember(names[i], passwords[i], rank,
					countries[i]));
		}
		try {
			res.getOutputStream().println("Done!");
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
