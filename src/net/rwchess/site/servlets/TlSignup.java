package net.rwchess.site.servlets;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.data.DAO;
import net.rwchess.site.data.T41Player;
import net.rwchess.site.utils.UsefulMethods;

public class TlSignup extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		T41Player player = new T41Player();
		player.setAvailability(Byte.valueOf(req.getParameter("investtime")));
		player.setPreferedSection(req.getParameter("section"));
		player.setUsername(UsefulMethods.getUsername(req.getSession()));
		DAO.get().getPersistenceManager().makePersistent(player);
		res.sendRedirect("/");
	}
}
