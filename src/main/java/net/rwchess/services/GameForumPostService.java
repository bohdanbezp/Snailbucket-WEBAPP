package net.rwchess.services;

import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.dao.TourneyDAO;
import net.rwchess.utils.Mailer;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by bodia on 11/3/14.
 */
public class GameForumPostService {
    private TourneyDAO tourneyDAO;
    private Mailer mailer;

    static Logger log = Logger.getLogger(GameForumPostService.class.getName());

    public GameForumPostService(TourneyDAO tourneyDAO, Mailer mailer) {
        this.tourneyDAO = tourneyDAO;
        this.mailer = mailer;
    }


    public void gameForumPost(TournamentGame game, String content,
                               String username) {
        log.info("Posted message to " + game.toString());

        DateTime zoned = DateTime.now(DateTimeZone.forID("America/Los_Angeles"));
        String date = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss z").print(zoned);

        String text ="<p><b>" + username
                + "</b> (" + date + "):<br/>"
                + content.replaceAll("\n", "<br/>") + "</p><hr/>\n"
                + game.getGameforumHtml();
        tourneyDAO.updateHtml(game, text);
        game.setGameforumHtml(text);

        if (username.equals(game.getWhitePlayer().getAssocMember().getUsername())) {
            tourneyDAO.updateWhiteLastPost(game, new Date());

            String uname = game.getBlackPlayer().getAssocMember().getUsername();
            if (uname.equals("PankracyRozumek") || uname.equals("pchesso") || uname.equals("Bodia") || uname.equals("BethanyGrace")
                    || uname.equals("RoyRogersC")) {
                mailer.sendEmail(game.getWhitePlayer().getAssocMember().getUsername(), game.getTournament().getFullName() + " game forum message",
                        content, game.getBlackPlayer().getAssocMember().getEmail());
            }
        }
        else if (username.equals(game.getBlackPlayer().getAssocMember().getUsername())) {
            tourneyDAO.updateBlackLastPost(game, new Date());

            String uname = game.getWhitePlayer().getAssocMember().getUsername();
            if (uname.equals("PankracyRozumek") || uname.equals("pchesso") || uname.equals("Bodia") || uname.equals("BethanyGrace")
                    || uname.equals("RoyRogersC")) {
                mailer.sendEmail(game.getBlackPlayer().getAssocMember().getUsername(), game.getTournament().getFullName() + " game forum message",
                        content, game.getWhitePlayer().getAssocMember().getEmail());
            }
        }
    }

    public String dateSetPost(String month, String day, String hour, String minute, TournamentGame game) {
        Calendar cld = Calendar.getInstance();
        cld.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        cld.set(Calendar.YEAR, cld.get(Calendar.YEAR));
        cld.set(Calendar.MONTH, Integer.parseInt(month)-1);
        cld.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        cld.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        cld.set(Calendar.MINUTE, Integer.parseInt(minute));
        Date dt = cld.getTime();

        tourneyDAO.updateScheduledDate(game, dt);
        game.setSecheduled(dt);

        SimpleDateFormat forumFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                Locale.US);
        forumFormatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        String date = forumFormatter.format(dt);

        log.info("Set date of " + game.toString() + " to " + date);
        return "Has set the scheduled time to " + date;
    }

}
