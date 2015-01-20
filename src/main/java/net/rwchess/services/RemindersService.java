package net.rwchess.services;


import net.rwchess.persistent.Bucket;
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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemindersService {
    private TourneyDAO tourneyDAO;
    private GameForumPostService gameForumPostService;
    private PythonBucketsGenerationService bucketsGenerationService;
    private Mailer mailer;

    public static Logger log = Logger.getLogger(RemindersService.class.getName());

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public RemindersService(TourneyDAO tourneyDAO, GameForumPostService gameForumPostService,
                            Mailer mailer, PythonBucketsGenerationService bucketsGenerationService) {
        this.tourneyDAO = tourneyDAO;
        this.gameForumPostService = gameForumPostService;
        this.mailer = mailer;
        this.bucketsGenerationService = bucketsGenerationService;
    }

    private static DateTimeFormatter formatter = DateTimeFormat.forPattern("E, MMM d HH:mm");

    private static String getCorrectDateFormat(DateTime date) {
        return formatter.print(date.withZone(DateTimeZone.forID("GMT"))) + " GMT";
    }

    private static final String[] SO = {"Good luck", "Best", "Regards", "Yours Truly", "Thanks", "Thank you",
            "Sincerely", "Kind regards", "Cheers", "Later"};
    private static final int[] SO_W = {40, 40, 40, 40, 40, 40, 40, 40, 40, 40};

    private static String getRandomSignoff() {
        int[] weightSum = new int[SO.length];

        int i, k;
        Random rnd = new Random();

        weightSum[0] = SO_W[0];

        for (i = 1; i < SO.length; i++)
            weightSum[i] = weightSum[i - 1] + SO_W[i];

        k = rnd.nextInt(weightSum[SO.length - 1]);

        for (i = 0; k > weightSum[i]; i++) ;

        return SO[i];

    }

    private static String getPlayerNotPosted(String abuser, DateTime initContactDeadline, String tdName) {
        String contents = abuser + ", the initial contact deadline is " + getCorrectDateFormat(initContactDeadline) + ".  If you have not posted by this time, you may be obliged to accept one of your opponent's offers.\n" +
                "This is an automatic reminder. Please ask TD "+tdName+" if you have any questions.";
        //String signOff = getRandomSignoff();
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getPlayerNotPostedFinal(String abuser, DateTime finalContactDeadline, String tdName) {
        String contents = abuser + ", you have missed the initial contact deadline. The second contact deadline is " + getCorrectDateFormat(finalContactDeadline) + " server time.  If you have not posted " +
                "by this time, your opponent may claim a forfeit win.*\n" +
                "" +
                "This is an automatic reminder. Please ask TD "+tdName+" if you have any questions.";
        //String signOff = getRandomSignoff();
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getBothDeadlineString(String whiteName, String blackName, DateTime initContactDeadline) {
        // String signOff = getRandomSignoff();
        String contents = whiteName
                + " and "
                + blackName
                + ",\nthe initial contact deadline ("
                + getCorrectDateFormat(initContactDeadline)
                + ") is "
                + "in 18 hours or less."
                + " Please start negotiating within the next hours, or your game will be forfeited\n\n"
                + "This is an automatic reminder; it may not make sense. Have mercy: I am a robot and don't understand human talk in this Game forum. Please ask the TD if you have any questions.";
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getPreGameReminder(DateTime scheduled, String tourneyName) {
        String contents = "Players,\nkeep in mind: You have a " + tourneyName + " game scheduled at " + getCorrectDateFormat(scheduled) + '.';
        //String signOff = getRandomSignoff();
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    private static String getFirstContactString(String color, String against, int round, String tourneyName,
                                                String tdName, String gameForumName) {
        //String signOff = getRandomSignoff();
        String contents = "You play "+color+" against "+against+" in round "+round+" of "+tourneyName+". Please access your Game forum at " +
                "http://snailbucket.org/tourney/forum/"+gameForumName+" and begin negotiations soon.\n" +
                '\n' +
                "This is an automatic reminder. Please ask TD "+tdName+" if you have any questions.";
        return contents /*+ "\n\n"+signOff+",\nsnailbot."*/;
    }

    public void runService() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    log.debug("Started RemindersService date local: " + new Date());
                    List<Tournament> tourneys = tourneyDAO.getAllTourneys();

                    DateTime now = DateTime.now(DateTimeZone.forID("GMT"));
                    for (Tournament tourney : tourneys) {
                        DateTime end = new DateTime(tourney.getEndDate(), DateTimeZone.forID("GMT"));
                        DateTime start = new DateTime(tourney.getStartDate(), DateTimeZone.forID("GMT"));
                        log.debug("Tourney " + tourney.getShortName() + " starts at " + getCorrectDateFormat(start));

                        if (now.isAfter(start)) {
                            LocalDate localDate = now.toLocalDate();
                            log.debug("localDate.getDayOfWeek() " + localDate.getDayOfWeek());
                            if (localDate.getDayOfWeek() >= DateTimeConstants.WEDNESDAY &&
                                    localDate.getDayOfWeek() < DateTimeConstants.SATURDAY) {
                                DateTime roundStart = now.withDayOfWeek(DateTimeConstants.WEDNESDAY).withHourOfDay(3).withMinuteOfHour(0);
                                DateTime initContactDeadline = roundStart.plusDays(2);
                                DateTime finalContactDeadline = roundStart.plusDays(3);
                                int round = UsefulMethods.getCurrentRound();

                                log.warn("round " + round);
                                List<Bucket> buckets = bucketsGenerationService.generateBuckets(tourneyDAO.getAllPlayersList(tourney.getShortName()));
                                List<TournamentGame> gameForums = tourneyDAO.getGamesForRound(tourney.getShortName(), round);
                                for (TournamentGame game : gameForums) {
                                    String tdName = UsefulMethods.getBucket(buckets, game).getTd();
                                    log.warn("game " + game.toString() +  "  " + !game.isFirstReminder());
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
                                         else {
                                             String cont = getFirstContactString("white",
                                                     game.getBlackPlayer().getAssocMember().getUsername(), UsefulMethods.getCurrentRound(),
                                                     game.getTournament().getFullName(), tdName, game.toString());

                                             mailer.sendEmail("snailbot", game.getTournament().getFullName() + " game", cont,
                                                     game.getWhitePlayer().getAssocMember().getEmail());

                                             cont = getFirstContactString("black",
                                                     game.getWhitePlayer().getAssocMember().getUsername(), UsefulMethods.getCurrentRound(),
                                                     game.getTournament().getFullName(), tdName, game.toString());

                                             mailer.sendEmail("snailbot", game.getTournament().getFullName() + " game", cont,
                                                     game.getBlackPlayer().getAssocMember().getEmail());
                                         }


                                         //gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");

                                         tourneyDAO.updateFirstReminderSent(game, true);
                                         continue;
                                     }

                                    Duration p = new Duration(now, initContactDeadline);
                                    int hoursDifference = (int) p.getStandardHours();
                                    if (hoursDifference <= 18 && !game.isInitContReminderSent()) {
                                        if (game.getSecheduled() != null)
                                            continue;

                                        if ((game.getWhiteLastPost() == null
                                                && game.getBlackLastPost() == null)) {
                                            String cont = getBothDeadlineString(game.getWhitePlayer().getAssocMember().getUsername(),
                                                    game.getBlackPlayer().getAssocMember().getUsername(), initContactDeadline);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateInitContReminderSent(game, true);
                                        } else if (game.getWhiteLastPost() == null) {
                                            String cont = getPlayerNotPosted(game.getWhitePlayer().getAssocMember().getUsername(), initContactDeadline,
                                                    tdName);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateInitContReminderSent(game, true);
                                        } else if (game.getBlackLastPost() == null) {
                                            String cont = getPlayerNotPosted(game.getBlackPlayer().getAssocMember().getUsername(), initContactDeadline,
                                                    tdName);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateInitContReminderSent(game, true);
                                        }
                                        continue;
                                    }

                                    p = new Duration(now, finalContactDeadline);
                                    hoursDifference = (int) p.getStandardHours();
                                    if (initContactDeadline.isAfter(now) && !game.isFirstReminder()) {
                                        if (game.getWhiteLastPost() == null) {
                                            String cont = getPlayerNotPostedFinal(game.getWhitePlayer().getAssocMember().getUsername(), initContactDeadline,
                                                    tdName);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateFirstReminderSent(game, true);
                                        } else if (game.getBlackLastPost() == null) {
                                            String cont = getPlayerNotPostedFinal(game.getBlackPlayer().getAssocMember().getUsername(), initContactDeadline,
                                                    tdName);
                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
                                            tourneyDAO.updateFirstReminderSent(game, true);
                                        }
                                    } else if (game.getSecheduled() != null && !game.isPreGameReminderSent() && game.getResult() == null) {
//                                        Duration p2 = new Duration(now, new DateTime(game.getSecheduled(), DateTimeZone.forID("America/Los_Angeles")));
//                                        hoursDifference = (int) p2.getStandardHours();
//                                        if (hoursDifference <= 6) {
//                                            String cont = getPreGameReminder(new DateTime(game.getSecheduled(), DateTimeZone.forID("America/Los_Angeles")), game.getTournament().getFullName());
//                                            gameForumPostService.gameForumPost(game, cont, "snailbot(TD)");
//                                            tourneyDAO.updatePreGameReminderSent(game, true);
//                                        }
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
