package net.rwchess.site.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.RWMember;
import net.rwchess.site.data.T41Player;
import net.rwchess.site.utils.UsefulMethods;

/**
 * Used to import the preliminary data for once 
 */
public class Import extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		PersistenceManager pm = DAO.get().getPersistenceManager();
		String[] names = { "HerrGott", "Noiro", "piorgovici", "pchesso",
				"Bodia", "Acho", "sachinravi", "jussu", "Natin", "Nitreb",
				"Yaro", "JDFitzpat", "Buriag", "NikolaAntonov", "roberttorma",
				"Byrial", "WilkBardzoZly", "iwulu", "wfletcher", "ivohristov",
				"Maras", "AlesD", "exray", "Gavrilo", "Pallokala", "bodzolca",
				"sangalla" };
		String[] countries = { "ro", "sk", "ro", "de", "ua", "ar", "in", "ee",
				"no", "ca", "us", "ca", "cz", "bg", "hu", "dk", "pl", "ng",
				"za", "bg", "lt", "cz", "ca", "cs", "fi", "si", "id" };
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
				"94d87053603986b693d39b4771bfd6af",
				"bb5790a031b733c9664520b1dc9e23cc",
				"5febe79bf467a4ae709a5cc6ca43cde8",
				"c8b5be92063a1b5f2018c2db605a5685",
				"968b18793e56cbea70692fba31189ae7",
				"d3c9dedc8375d7a68a9f2533e73b1c9c",
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
		}
		
		// test for tl system
		String[] nams = { "HerrGott", "Nitreb", "Bodia", "pchesso", "Maras", 
				"iwulu", "WilkBardzoZly", "SachinRavi" };
		for (int i = 0; i < nams.length; i++) {
			T41Player player = new T41Player();
			player.setAvailability((byte)0);
			player.setPreferedSection("Any section");
			player.setUsername(nams[i]);
			pm.makePersistent(player);
		}
	}
}
