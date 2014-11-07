package net.rwchess.services;


import net.rwchess.persistent.Tournament;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.dao.TourneyDAO;
import org.apache.log4j.Logger;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemindersService {
    private TourneyDAO tourneyDAO;
    private GameForumPostService gameForumPostService;

    static Logger log = Logger.getLogger(RemindersService.class.getName());

    private ExecutorService executor;

    public RemindersService(TourneyDAO tourneyDAO, GameForumPostService gameForumPostService) {
        this.tourneyDAO = tourneyDAO;
        this.gameForumPostService = gameForumPostService;
    }

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("E, MMM d HH:mm");

    private static String getCorrectDateFormat(DateTime date) {
        return formatter.print(date) + " server time";
    }

    private static final String[] SO = {"Good luck", "Best", "Regards", "Yours Truly", "Thanks", "Thank you",
            "Sincerely", "Kind regards", "Cheers", "Later"};
    private static final int[] SO_W = {40,40,40,40,40,40,40,40,40,40};

    private static String getRandomSignoff() {
        int[] weightSum = new int[SO.length];

        int i, k;
        Random rnd = new Random();

        weightSum[0] = SO_W[0];

        for (i = 1; i < SO.length; i++)
            weightSum[i] = weightSum[i - 1] + SO_W[i];

        k = rnd.nextInt(weightSum[SO.length - 1]);

        for (i = 0; k > weightSum[i]; i++);

        return SO[i];

    }

    private static String getPlayerNotPosted(String abuser, DateTime initContactDeadline) {
        String contents = abuser + ", the initial contact deadline is "+ getCorrectDateFormat(initContactDeadline)+".  If you have not posted by this time, you may be obliged to accept one of your opponent's offers.";
        String signOff = getRandomSignoff();
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getPlayerNotPostedFinal(String abuser, DateTime finalContactDeadline) {
        String contents = abuser + ", the final contact deadline is "+ getCorrectDateFormat(finalContactDeadline)+".  If you have not posted by this time, your opponent may claim the forfeit win.";
        String signOff = getRandomSignoff();
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getBothDeadlineString(String whiteName, String blackName, DateTime initContactDeadline) {
        String signOff = getRandomSignoff();
        String contents = whiteName
                + " and "
                + blackName
                + ",\nthe initial contact deadline ("
                + getCorrectDateFormat(initContactDeadline)
                + ") is "
                + "in 12 hours or less."
                + " Please start negotiating within the next hours, or your game will be forfeited " +
                "unless you and your opponent have explicitly agreed to pause scheduling for a few days.\n\n"
                + "This is an automatic reminder; it may not make sense. Have mercy: I am a robot and don't understand human talk in this Game forum. Please ask the TD if you have any questions.";
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getPreGameReminder(DateTime scheduled, String tourneyName) {
        String contents = "Players,\nkeep in mind: You have a "+tourneyName+" game scheduled at "+getCorrectDateFormat(scheduled)+ '.';
        String signOff = getRandomSignoff();
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getFirstContactString(String whiteName, String blackName,
                                                DateTime contactDeadline) {
        String signOff = getRandomSignoff();
        String contents = whiteName
                + " and "
                + blackName
                + ",\nthe initial contact deadline is "
                + getCorrectDateFormat(contactDeadline)
                + ". Please begin negotiations as soon as possible.\n\n"
                + "This is an automatic reminder. Please ask the TD if you have any questions.";
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    public void runService() {
        executor = Executors.newSingleThreadExecutor();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    log.debug("Started RemindersService date local: " + new Date());
                    List<Tournament> tourneys = tourneyDAO.getAllTourneys();

                    DateTime now = DateTime.now(DateTimeZone.forID("America/Los_Angeles"));
                    for (Tournament tourney: tourneys) {
                        DateTime end = new DateTime(tourney.getEndDate(), DateTimeZone.forID("America/Los_Angeles"));
                        DateTime start = new DateTime(tourney.getStartDate(), DateTimeZone.forID("America/Los_Angeles"));
                        log.debug("Tourney " + tourney.getShortName() + " starts at " + getCorrectDateFormat(start));

                        if (now.isAfter(start)) {
                            LocalDate localDate = now.toLocalDate();
                            log.debug("localDate.getDayOfWeek() " + localDate.getDayOfWeek());
                            if (localDate.getDayOfWeek() > DateTimeConstants.TUESDAY &&
                                    localDate.getDayOfWeek() < DateTimeConstants.SATURDAY) {
                                DateTime roundStart = now.withDayOfWeek(DateTimeConstants.TUESDAY).withHourOfDay(19).withMinuteOfHour(0);
                                DateTime initContactDeadline = roundStart.plusDays(2);
                                DateTime finalContactDeadline = roundStart.plusDays(3);
                                int round = Weeks.weeksBetween(roundStart, now).getWeeks() + 1;

                                log.debug("round " + round);
                                List<TournamentGame> gameForums = tourneyDAO.getGamesForRound(tourney.getShortName(), round);
                                for (TournamentGame game: gameForums) {
//                                     if (!game.isFirstReminder()) {
//                                         String cont = getFirstContactString(game.getWhitePlayer().getAssocMember().getUsername(),
//                                                 game.getBlackPlayer().getAssocMember().getUsername(), initContactDeadline);
//                                         gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
//                                         tourneyDAO.updateFirstReminderSent(game, true);
//                                         continue;
//                                     }

                                    Duration p = new Duration(now, initContactDeadline);
                                    int hoursDifference = (int) p.getStandardHours();
                                    if (hoursDifference <= 12 && !game.isInitContReminderSent()) {
                                        if (game.getSecheduled() != null)
                                            continue;

                                        if ((game.getWhiteLastPost() == null
                                                && game.getBlackLastPost() == null)) {
                                            String cont = getBothDeadlineString(game.getWhitePlayer().getAssocMember().getUsername(),
                                                    game.getBlackPlayer().getAssocMember().getUsername(), initContactDeadline);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateInitContReminderSent(game, true);
                                        }
                                        else if (game.getWhiteLastPost() == null) {
                                            String cont = getPlayerNotPosted(game.getWhitePlayer().getAssocMember().getUsername(), initContactDeadline);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateInitContReminderSent(game, true);
                                        }
                                        else if (game.getBlackLastPost() == null) {
                                            String cont = getPlayerNotPosted(game.getBlackPlayer().getAssocMember().getUsername(), initContactDeadline);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateInitContReminderSent(game, true);
                                        }
                                        continue;
                                    }

                                    p = new Duration(now, finalContactDeadline);
                                    hoursDifference = (int) p.getStandardHours();
                                    if (hoursDifference <= 12 && !game.isFirstReminder()) {
                                        if (game.getWhiteLastPost() == null) {
                                            String cont = getPlayerNotPostedFinal(game.getWhitePlayer().getAssocMember().getUsername(), initContactDeadline);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateFirstReminderSent(game, true);
                                        }
                                        else if (game.getBlackLastPost() == null) {
                                            String cont = getPlayerNotPostedFinal(game.getBlackPlayer().getAssocMember().getUsername(), initContactDeadline);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateFirstReminderSent(game, true);
                                        }
                                    }

                                    else if (game.getSecheduled() != null && !game.isPreGameReminderSent()) {
                                        Duration p2 = new Duration(now, new DateTime(game.getSecheduled(), DateTimeZone.forID("America/Los_Angeles")));
                                        hoursDifference = (int) p2.getStandardHours();
                                        if (hoursDifference <= 6) {
                                            String cont = getPreGameReminder(new DateTime(game.getSecheduled(), DateTimeZone.forID("America/Los_Angeles")), game.getTournament().getFullName());
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updatePreGameReminderSent(game, true);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    try {
                        Thread.sleep(3600000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

            }
        });


    }
}
