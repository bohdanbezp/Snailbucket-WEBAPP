package net.rwchess.services;

import net.rwchess.persistent.Member;
import net.rwchess.persistent.Tournament;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.dao.TourneyDAO;
import net.rwchess.utils.Mailer;
import net.rwchess.utils.UsefulMethods;
import org.apache.log4j.Logger;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RemindersService {
    private TourneyDAO tourneyDAO;
    private GameForumPostService gameForumPostService;
    private Mailer mailer;

    public static Logger log = Logger.getLogger(RemindersService.class.getName());

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public RemindersService(TourneyDAO tourneyDAO, GameForumPostService gameForumPostService, Mailer mailer) {
        this.tourneyDAO = tourneyDAO;
        this.gameForumPostService = gameForumPostService;
        this.mailer = mailer;
    }

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("E, MMM d HH:mm");

    private static String getCorrectDateFormat(DateTime date) {
        return formatter.print(date.withZone(DateTimeZone.forID("America/New_York"))) + " America/New_York";
    }

    private static String getPlayerNotPosted(String playerName, String tdName, String gameForumLink) {
        String contents = "Player " + playerName + ",\n\n" +
                "The first contact deadline in the SB Monthly 2 is the 6th of a month, 2200 FICS server time. Meet this deadline, or you may have to accept one of your opponent's offers. " +
                "Please access your game forum at " + gameForumLink + "\n\n" +
                "Please don't reply to this message. Contact TD " + tdName + " if you have questions.";
        return contents;
    }

    private static String getTwoPlayersNotPosted(String whitePlayerName, String blackPlayerName, String tdName, String gameForumLink) {
        String contents = "Players " + whitePlayerName + " and " + blackPlayerName + ",\n\n" +
                "The first contact deadline in the SB Monthly 2 is the 6th of a month, 2200 FICS server time. Meet this deadline, or you may have to accept one of your opponent's offers. " +
                "Please access your game forum at " + gameForumLink + "\n\n" +
                "Please don't reply to this message. Contact TD " + tdName + " if you have questions.";
        return contents;
    }

    private static String getPlayerNotPostedFinal(String playerName, String tdName, String gameForumLink) {
        String contents = "Player " + playerName + ",\n\n" +
                "The second contact deadline in the SB Monthly 2 is the 8th of a month, 2200 FICS server time. Meet this deadline, or your opponent may claim a forfeit win. " +
                "Please access your game forum at " + gameForumLink + "\n\n" +
                "Please don't reply to this message. Contact TD " + tdName + " if you have questions.";
        return contents;
    }

    public void runService() {
        executor.scheduleWithFixedDelay(() -> {
            System.out.println("Started RemindersService date local: " + new Date());
            List<Tournament> tourneys = tourneyDAO.getAllTourneys();

            DateTime now = DateTime.now(DateTimeZone.forID("America/New_York"));
            for (Tournament tourney : tourneys) {
                DateTime end = new DateTime(tourney.getEndDate(), DateTimeZone.forID("America/New_York"));
                DateTime start = new DateTime(tourney.getStartDate(), DateTimeZone.forID("America/New_York"));

                if (now.isAfter(start)) {
                    List<TournamentGame> gameForums = tourneyDAO.getGamesForTourney(tourney.getShortName());
                    int round = UsefulMethods.getLastRound(gameForums);
                    for (TournamentGame game : gameForums) {
                        if (game.getRound() != round) {
                            continue;
                        }

                        String tdName = "pchesso";
                        if (!game.isFirstReminder()) {

                                    if ((game.getWhitePlayer().getAssocMember().getRr() <= 0 ||
                                            game.getWhitePlayer().getAssocMember().getGroup() < Member.USER) &&
                                            (game.getBlackPlayer().getAssocMember().getRr() <= 0 ||
                                                    game.getBlackPlayer().getAssocMember().getGroup() < Member.USER)) {
                                        String message = "Both players are no longer participating in the tournament. The game is set to -:-.";
                                        gameForumPostService.gameForumPost(game, message, "snailbot(TD)");
                                        tourneyDAO.updateScheduledDate(game, new Date());
                                        tourneyDAO.updateResult(game, "-:-");
                                    }
                                    else if (game.getWhitePlayer().getAssocMember().getRr() <= 0 ||
                                            game.getWhitePlayer().getAssocMember().getGroup() < Member.USER) {
                                        String message = "Player "+game.getWhitePlayer().getAssocMember().getUsername()
                                                +" is no longer participating in the tournament. The game is set to -:+.";

                                        tourneyDAO.updateScheduledDate(game, new Date());
                                        gameForumPostService.gameForumPost(game, message, "snailbot(TD)");
                                        tourneyDAO.updateResult(game, "-:+");

                                    }
                                    else if (game.getBlackPlayer().getAssocMember().getRr() <= 0 ||
                                            game.getBlackPlayer().getAssocMember().getGroup() < Member.USER) {
                                        String message = "Player "+game.getBlackPlayer().getAssocMember().getUsername()
                                                +" is no longer participating in the tournament. The game is set to +:-.";
                                        tourneyDAO.updateScheduledDate(game, new Date());
                                        gameForumPostService.gameForumPost(game, message, "snailbot(TD)");
                                        tourneyDAO.updateResult(game, "+:-");
                                    }


                                    //gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");

                            tourneyDAO.updateFirstReminderSent(game, true);
                            continue;
                        }

                        // Adjusted contact deadlines
                        DateTime initContactDeadline = new DateTime(now.getYear(), now.getMonthOfYear(), 6, 22, 0, DateTimeZone.forID("America/New_York"));
                        DateTime finalContactDeadline = new DateTime(now.getYear(), now.getMonthOfYear(), 8, 22, 0, DateTimeZone.forID("America/New_York"));

                        String gameForumLink = "https://snailbucket.org/tourney/forum/" + game.getGameForumString();

                        // First reminder before 12 hours of initial contact deadline
                        if (now.isBefore(initContactDeadline.minusHours(12)) && !game.isInitContReminderSent()) {
                            if (game.getWhiteLastPost() == null && game.getBlackLastPost() == null) {
                                String cont = getTwoPlayersNotPosted(game.getWhitePlayer().getAssocMember().getUsername(), game.getBlackPlayer().getAssocMember().getUsername(), tdName, gameForumLink);
                                gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                            } else if (game.getWhiteLastPost() == null) {
                                String cont = getPlayerNotPosted(game.getWhitePlayer().getAssocMember().getUsername(), tdName, gameForumLink);
                                gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                            } else if (game.getBlackLastPost() == null) {
                                String cont = getPlayerNotPosted(game.getBlackPlayer().getAssocMember().getUsername(), tdName, gameForumLink);
                                gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                            }
                            tourneyDAO.updateInitContReminderSent(game, true);
                            continue;
                        }

                        // Second reminder before 12 hours of final contact deadline
                        if (now.isBefore(finalContactDeadline.minusHours(12)) && !game.isFirstReminder()) {
                            if (game.getWhiteLastPost() == null) {
                                String cont = getPlayerNotPostedFinal(game.getWhitePlayer().getAssocMember().getUsername(), tdName, gameForumLink);
                                gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                            }
                            if (game.getBlackLastPost() == null) {
                                String cont = getPlayerNotPostedFinal(game.getBlackPlayer().getAssocMember().getUsername(), tdName, gameForumLink);
                                gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                            }
                            tourneyDAO.updateFirstReminderSent(game, true);
                        }

                        // Existing code for pre-game reminders remains unchanged
                        if (game.getSecheduled() != null && !game.isPreGameReminderSent() && game.getResult() == null) {
                            Duration p2 = new Duration(now, new DateTime(game.getSecheduled(), DateTimeZone.forID("America/New_York")));
                            int hoursDifference2 = (int) p2.getStandardHours();
                            if (hoursDifference2 >= 0 && hoursDifference2 <= 1) {
                                sendOneHourReminder(game);
                                tourneyDAO.updatePreGameReminderSent(game, true);
                            }
                        }
                    }
                }
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    private void sendOneHourReminder(TournamentGame game) {
        // Existing code remains unchanged
        String subject = "1-hour reminder: " + game.getTournament().getFullName() + " game";
        DateTime scheduledDateTime = new DateTime(game.getSecheduled(), DateTimeZone.forID("America/New_York"));
        String scheduledTime = getCorrectDateFormat(scheduledDateTime);

        String gameForumLink = "https://snailbucket.org/tourney/forum/" + game.getGameForumString();

        String message = "<html><body>"
                + "<p>Dear Players,</p>"
                + "<p>This is a friendly reminder that you have a game scheduled in the <strong>"
                + game.getTournament().getFullName()
                + "</strong> tournament in 1 hour.</p>"
                + "<p><strong>Scheduled Time:</strong> " + scheduledTime + "</p>"
                + "<p><strong>Game Forum:</strong> <a href=\"" + gameForumLink + "\">" + gameForumLink + "</a></p>"
                + "<p>Please be prepared to participate. Best of luck!</p>"
                + "</body></html>";

        mailer.sendEmail("notify@snailbucket.org", subject, message, game.getWhitePlayer().getAssocMember().getEmail());
        mailer.sendEmail("notify@snailbucket.org", subject, message, game.getBlackPlayer().getAssocMember().getEmail());
    }

}
