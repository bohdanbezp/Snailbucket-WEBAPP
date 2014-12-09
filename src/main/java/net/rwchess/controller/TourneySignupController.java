package net.rwchess.controller;


import chesspresso.game.Game;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import net.rwchess.persistent.*;
import net.rwchess.persistent.dao.MemberDAO;
import net.rwchess.persistent.dao.TourneyDAO;
import net.rwchess.services.*;
import net.rwchess.utils.UsefulMethods;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/tourney")
public class TourneySignupController {

    private TourneyDAO tourneyDAO;
    private MemberDAO memberDAO;
    private CheckRatingsService ratingsService;
    private PythonBucketsGenerationService bucketsGenerationService;
    private PythonPairingsService pairingsService;
    private GameForumPostService gameForumPostService;
    private RemindersService remindersService;
    private PythonStandingsService standingsService;

    private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    public TourneySignupController(TourneyDAO tourneyDAO, MemberDAO memberDAO, CheckRatingsService ratingsService,
                                   PythonBucketsGenerationService bucketsGenerationService,
                                   PythonPairingsService pairingsService, GameForumPostService gameForumPostService,
                                   RemindersService remindersService, PythonStandingsService standingsService) {
        this.tourneyDAO = tourneyDAO;
        this.memberDAO = memberDAO;
        this.ratingsService = ratingsService;
        this.bucketsGenerationService = bucketsGenerationService;
        this.pairingsService = pairingsService;
        this.gameForumPostService = gameForumPostService;
        this.remindersService = remindersService;
        this.standingsService = standingsService;
    }

    void startRemindersService() {
        remindersService.runService();
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String tourneyProcessGet() {
        return "tourney-create";
    }

    @RequestMapping(value = "/manage/{tourneyShortName}", method = RequestMethod.POST)
    public String tourneyManagePost(@PathVariable String tourneyShortName, @RequestParam(value = "submType") String submitVal, ModelMap modelMap) {
       if (submitVal.startsWith("Update ratings")) {
           ratingsService.checkRatings(tourneyShortName);

           modelMap.addAttribute("title", "Notice");
           modelMap.addAttribute("error", "Please wait, the ratings are being updated right now. It takes about 1 sec per player to check ratings. Try refreshing this page after a few minutes");
           return "error";
       }
       else if (submitVal.startsWith("Create pairings")) {
           StringBuilder body = new StringBuilder();
           body.append("<form name=\"input\" action=\"\" method=\"post\"><input name=\"submType\" type=\"submit\" value=\"Save pairings\"></form><br/><br/>");
           body.append("<p>The following games will be created:</p><br/>");

           Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);

           List<Bucket> buckets = bucketsGenerationService.generateBuckets(sorted);

           int lastRound = 0;
           Random random = new Random((long)user.getUsername().hashCode());
           for (Bucket bucket: buckets) {
               body.append("<h2>Bucket ").append(bucket.getName()).append("</h2><br/>");
               List<TournamentGame> games = pairingsService.allRoundsGame(bucket, tourneyDAO.getByShortName(tourneyShortName), random);
               for (TournamentGame game: games) {
                   if (game.getRound() != lastRound) {
                       if (lastRound!=0)
                           body.append("</ul>");

                       body.append("<h3>Round ").append(game.getRound()).append("</h3><br/>");
                       body.append("<ul>");

                       lastRound = game.getRound();
                   }

                   body.append("<li>").append(game.toString()).append("</li>");
               }
               body.append("</ul><br/>");



           }
           modelMap.addAttribute("title", "Notice");
           modelMap.addAttribute("error", body.toString());
           return "error";
       }
       else if (submitVal.startsWith("Save pairings")) {
           if (tourneyDAO.tourneyHasPairings(tourneyShortName)) {
               modelMap.addAttribute("title", "Error");
               modelMap.addAttribute("error", "<p>Somebody has already created pairings for the tourney.</p>");
               return "error";
           }

           Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);

           List<Bucket> buckets = bucketsGenerationService.generateBuckets(sorted);

           Random random = new Random((long)user.getUsername().hashCode());
           for (Bucket bucket: buckets) {

               List<TournamentGame> games = pairingsService.allRoundsGame(bucket, tourneyDAO.getByShortName(tourneyShortName), random);
               for (TournamentGame game: games) {
                   tourneyDAO.storeGame(game);
               }

           }


           modelMap.addAttribute("title", "Notice");
           modelMap.addAttribute("error", "<p>The pairings have been created. Consult TD guide to find links to pairings.</p>");
           return "error";
       }
        return "not-found";
    }

    @RequestMapping(value = "/manage/{tourneyShortName}", method = RequestMethod.GET)
    public String tourneyManageGet(@PathVariable String tourneyShortName, ModelMap modelMap) {
        StringBuilder body = new StringBuilder();

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.getGroup() >= Member.ADMIN) {
            body.append("<form name=\"input\" action=\"\" method=\"post\"><input name=\"submType\" type=\"submit\" value=\"Update ratings\"></form><br/><br/>");
            body.append("<form name=\"input\" action=\"\" method=\"post\"><input name=\"submType\" type=\"submit\" value=\"Create pairings\"></form><br/><br/>");
        }

        List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);

        List<Bucket> buckets = bucketsGenerationService.generateBuckets(sorted);

        int iBucket = 0;
        for (Bucket bucket: buckets) {
            body.append("<h3>Bucket ").append(bucket.getName()).append("</h3><br/>");
            body.append("<ul>");

            for (TournamentPlayer player: bucket.getPlayerList()) {
                body.append("<li><img src=\"/static/images/flags/").append(player.getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(player.getAssocMember().getUsername()).append(' ').append(player.getFixedRating()).append("</li>");

            }

            body.append("</ul>");
            body.append("<br/><br/>");
        }

        modelMap.addAttribute("title", "Manage " + tourneyShortName);
        modelMap.addAttribute("error", body.toString());
        return "error";
    }

    @RequestMapping(value = "/signup/{tourneyShortName}", method = RequestMethod.GET)
    public String tourneySignupGet(@PathVariable String tourneyShortName, ModelMap modelMap) {
        Tournament tournament = tourneyDAO.getByShortName(tourneyShortName);
        if (tournament == null)
            return "not-found";

        String signupMessage;
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean signedUp = false;

        StringBuilder signedList = new StringBuilder();
//        List<TournamentPlayer> playerList = tourneyDAO.getAllPlayersList(tourneyShortName);
//        for (TournamentPlayer player: playerList) {
//            signedList.append("<li><img src=\"/static/images/flags/").append(player.getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(player.getAssocMember().getUsername()).append("</li>");
//            if (!(user instanceof String)) {
//               if (player.getAssocMember().getUsername().equals(((Member) user).getUsername()))
//                   signedUp = true;
//            }
//        }

        List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);

        List<Bucket> buckets = bucketsGenerationService.generateBuckets(sorted);

        int iBucket = 0;
        for (Bucket bucket: buckets) {
            signedList.append("<h2>Bucket ").append(bucket.getName()).append("</h2>" +
                    "<p>TD: "+bucket.getTd()+"</p>");
            signedList.append("<ul>");

            for (TournamentPlayer player: bucket.getPlayerList()) {
                signedList.append("<li><img src=\"/static/images/flags/").append(player.getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(player.getAssocMember().getUsername()).append(' ').append(player.getFixedRating()).append("</li>");
                if (!(user instanceof String)) {
                    if (player.getAssocMember().getUsername().equals(((Member) user).getUsername()))
                        signedUp = true;
                }

            }

            signedList.append("</ul>");
            signedList.append("<br/><br/>");
        }



        DateTime today = DateTime.now(DateTimeZone.forID("America/Los_Angeles"));
        DateTime signupFrom = new DateTime(tournament.getSignupFrom(), DateTimeZone.forID("America/Los_Angeles"));
        DateTime signupTo = new DateTime(tournament.getSignupTo(), DateTimeZone.forID("America/Los_Angeles"));

        if (signupFrom.isAfter(today)) {
            signupMessage = "<p>The registration will start at " + UsefulMethods.getWikiDateFormatter().print(signupFrom) + "</p>";
        }
        else if (signupTo.isBefore(today)) {
            signupMessage = "<p>The registration has been closed at " + UsefulMethods.getWikiDateFormatter().print(signupTo) + "</p>";
        }
        else {
            if (user instanceof String) {
                signupMessage = "<p>Log in in order to sign up.</p>";
            } else if (!signedUp) {
                signupMessage = "<p>Please read the tourney guide before signing up.</p>\n" +
                        "<form id=\"Sign\" action=\"\" method=\"post\">\n" +
                        "               <p>\n" +
                        "            <input id=\"SaveAccount\" type=\"submit\" value=\"Sign up to tourney\" />\n" +
                        "        </p>\n" +
                        "        </form>";
            } else {
                signupMessage = "<p>You are already signed up to the tourney</p>";
            }
        }

         modelMap.addAttribute("tournName", tournament.getFullName());
        modelMap.addAttribute("signupMessage", signupMessage);
        modelMap.addAttribute("signedList", signedList.toString());
        return "signup";
    }

    @RequestMapping(value = "/players", method = RequestMethod.POST)
    public String tourneyPlayersPost(@RequestParam(value = "submit") String submitVal, ModelMap modelMap, HttpServletRequest req) {
        StringBuilder body = new StringBuilder();
         if (submitVal.startsWith("Update")) {
             Long key = Long.parseLong(submitVal.substring(6));
             Member member = memberDAO.getMemberById(key);
             body.append("<p>Set user role for ").append(member.getUsername()).append("</p>");
             body.append("<form name=\"input\" action=\"\" method=\"post\"><select id=\"Role\" name=\"Role\">" + " <option value=\"3\">Admin</option>\n" + "            \t\t\t    <option value=\"2\">TD</option>\n" + "                            <option selected=\"selected\" value=\"1\">User</option>\n" + "                            <option value=\"0\">Banned</option>\n</select>" + "<br/><br/>" + "RR: <input type=\"text\" name=\"rr\" value=\"").append(member.getRr()).append("\"><br/><br/><input name=\"submit\" type=\"submit\" value=\"Submit").append(key).append("\"/></form>");

             body.append("<br/><p>Set new password for ").append(member.getUsername()).append("</p>").append("<form name=\"input\" action=\"\" method=\"post\">Password: <input type=\"text\" name=\"password\"><input type=\"hidden\" name=\"key\" value=\"").append(key).append("\"/><br/><br/><input name=\"submit\" type=\"submit\" value=\"Set new password\"/></form>");
         }
        else if (submitVal.startsWith("Submit")) {
             Long key = Long.parseLong(submitVal.substring(6));
             memberDAO.updateRole(key, Integer.parseInt(req.getParameter("Role")));
             memberDAO.updateRR(key, Integer.parseInt(req.getParameter("rr")));
             return "redirect:/tourney/players";
         }
        else if (submitVal.startsWith("Set new password")) {
             Long key = Long.parseLong(req.getParameter("key"));
             memberDAO.updatePassword(key, req.getParameter("password"));
             body.append("<p>Password has been updated.</p>");
         }

        modelMap.addAttribute("title", "");
        modelMap.addAttribute("error", body.toString());
        return "error";
    }

    @RequestMapping(value = "/players", method = RequestMethod.GET)
    public String tourneyPlayers(ModelMap modelMap) {
        List<Member> members = memberDAO.getAllMembers();

        StringBuilder playersTable = new StringBuilder();
        playersTable.append("<p>Click \"UpdateXX\" button to update user's role or change password. Sort by clicking on column headers.</p><form name=\"input\" action=\"\" method=\"post\"><table class=\"tablesorter\" cellspacing=\"1\"><thead>" +
                "<tr><th>USERNAME</th><th>EMAIL</th><th>CONFIRMED</th><th>SIGNED_UP</th><th>ROLE</th><th>RR</th><th>UPDATE</th></tr></thead><tbody>");
        for (Member member: members) {
            playersTable.append("<tr>");
            if (member.isConfirmed())
                playersTable.append("<td>").append(member.getUsername()).append("</td>");
            else
                playersTable.append("<td><font color=\"red\"><b>").append(member.getUsername()).append("</b></font></td>");


            playersTable.append("<td>").append(member.getEmail()).append("</td>");

            if (member.isConfirmed())
                playersTable.append("<td>").append(member.isConfirmed()).append("</td>");
            else
                playersTable.append("<td><font color=\"red\"><b>").append(member.isConfirmed()).append("</b></font></td>");


            playersTable.append("<td>").append(tourneyDAO.isSignedUp(member)).append("</td>");

            if (member.getGroup() != 0 && member.getRr() > 0)
                playersTable.append("<td>").append(UsefulMethods.groupIdToString(member.getGroup())).append("</td>");
            else if (member.getGroup() == 0)
                playersTable.append("<td><font color=\"red\"><b>banned</b></font></td>");
            else if (member.getRr() <= 0)
                playersTable.append("<td><font color=\"red\"><b>suspended</b></font></td>");

            playersTable.append("<td>").append(member.getRr()).append("</td>");

            playersTable.append("<td><input name=\"submit\" type=\"submit\" value=\"Update").append(member.getKey()).append("\"></td>");
            playersTable.append("</tr>");
        }

        playersTable.append("</tbody></table></form>");
        modelMap.addAttribute("title", "All players list");
        modelMap.addAttribute("error", playersTable.toString());
        return "error";
    }

    @RequestMapping(value = "/forum/{forumString}", method = RequestMethod.POST)
    public String tourneyForumPost(@PathVariable String forumString, ModelMap modelMap, HttpServletRequest req) throws Exception {
        TournamentGame game = tourneyDAO.getGameByForumString(forumString);
        if (game == null)
            return "not-found";


        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (game.getRound() != UsefulMethods.getCurrentRound()) {
            modelMap.addAttribute("title", "Error");
            modelMap.addAttribute("error", "You cannot negotiate about next round yet.");
            return "error";
        }

        String content = req.getParameter("contents");

        if (content.startsWith("http://www.ficsgames.org") ||
                content.startsWith("http://ficsgames.org")) {
           if (game.getResult() != null) {
               modelMap.addAttribute("title", "Error");
               modelMap.addAttribute("error", "The result of the game has already been set.");
               return "error";
           }


            URL url = new URL(content);
            String pgn = IOUtils.toString(new InputStreamReader(
                    url.openStream()));

            boolean in = false;
            char last = ' ';
            StringBuilder buff = new StringBuilder();
            for (char c: pgn.toCharArray()) {
                if (c == ']' && in)
                    in = false;

                if (in)
                    continue;

                if (c == '[' && last == '{')
                    in = true;

                last = c;
                buff.append(c);
            }
            pgn = buff.toString().replace("{[]}", "");

            String result;

            PGNReader red = new PGNReader(IOUtils.toInputStream(pgn), game.getTournament().getShortName()+ ".pgn");
            try {
                Game ga = red.parseGame();
                ga.setTag("Round", Integer.toString(game.getRound()));
                ga.setTag("Event", game.getTournament().getFullName());
                result = ga.getResultStr();

                if (!ga.getWhite().equals(game.getWhitePlayer().getAssocMember().getUsername()) ||
                        !ga.getBlack().equals(game.getBlackPlayer().getAssocMember().getUsername())) {
                    modelMap.addAttribute("title", "Error");
                    modelMap.addAttribute("error", "You posted the wrong game");
                    return "error";
                }
                DateTimeFormatter df = DateTimeFormat.forPattern("yyyy.MM.dd HH:mm:ss");
                DateTime gameDate = df.parseDateTime(ga.getDate() + " " + ga.getTag("Time"));
                DateTime startDate = new DateTime(game.getTournament().getStartDate(), DateTimeZone.forID("America/Los_Angeles"));

                if (gameDate.isBefore(startDate)) {
                    modelMap.addAttribute("title", "Error");
                    modelMap.addAttribute("error", "Game date is before tha date of tournament start.");
                    return "error";
                }

                pgn = UsefulMethods.getPgnRepresentation(ga);
                gameForumPostService.gameForumPost(game, "Game ended as " + result, "snailbot(TD)");
                tourneyDAO.updatePgn(game, pgn);
                tourneyDAO.updateResult(game, result);
                return "redirect:/tourney/forum/"+forumString;
            } catch (PGNSyntaxError e) {
                e.printStackTrace();
            }
        }
        else if (content.startsWith("Game adjudicated as") &&
                !content.endsWith(".") && user.getGroup() >= Member.TD) {
            String result = content.substring("Game adjudicated as ".length()).trim();
            tourneyDAO.updateResult(game, result);
        }


        String tag = "";
        if (user.getGroup() >= Member.TD && !forumString.contains(user.getUsername()))
            tag = "(TD)";

        if (!content.isEmpty()) {
            gameForumPostService.gameForumPost(game, content, user.getUsername() + tag);
        }

        if (!req.getParameter("month").equals("0") && (forumString.contains(user.getUsername()) || user.getGroup() >= Member.TD)) {
            DateTime now = DateTime.now();
            DateTime dateTime = new DateTime(now.getYear(), Integer.parseInt(req.getParameter("month")),
                    Integer.parseInt(req.getParameter("day")), Integer.parseInt(req.getParameter("hour")),
                    Integer.parseInt(req.getParameter("minute")), DateTimeZone.forID("America/Los_Angeles"));
            if (dateTime.isBefore(new DateTime(game.getTournament().getStartDate(), DateTimeZone.forID("America/Los_Angeles")))) {
                modelMap.addAttribute("title", "Error");
                modelMap.addAttribute("error", "The date before tourney start.");
                return "error";
            }

            content = gameForumPostService.dateSetPost(req.getParameter("month"),req.getParameter("day"),
                    req.getParameter("hour"), req.getParameter("minute"),game);
            gameForumPostService.gameForumPost(game, content, user.getUsername() + tag);
        }

        return "redirect:/tourney/forum/"+forumString;
    }

    SimpleDateFormat forumFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm",
            Locale.US);


    @RequestMapping(value = "/standing/{shortName}", method = RequestMethod.GET)
    public String tourneyStandingGet(@PathVariable String shortName, ModelMap modelMap) {
        List<TournamentGame> gameWithResults = tourneyDAO.getGamesByResult(shortName);
        List<TournamentPlayer> players = tourneyDAO.getAllPlayersList(shortName);

        List<Bucket> buckets = bucketsGenerationService.generateBuckets(players);
        StringBuilder wikiTable = new StringBuilder();

        for (Bucket bucket: buckets) {

            List<PythonStandingsService.StandingRecord> records
                    = standingsService.generateStandings(gameWithResults, players, bucket);


            wikiTable.append("<h2>Bucket ").append(bucket.getName()).append("</h2>" +
                    "<p>TD: "+bucket.getTd()+"</p>");
            wikiTable.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
            wikiTable.append("<tr>\n" +
                    "         <th>No</th>\n" +
                    "         <th width=\"150px\">Name</th>\n" +
                    "         <th>Points</th>\n" +
                    "         <th>HTH</th>\n" +
                    "         <th>Wins</th>\n" +
                    "         <th>White</th>\n" +
                    "         <th>Rating</th>\n" +
                    "         <th>Games</th>\n" +
                    "      </tr>");

            int i = 1;
            for (PythonStandingsService.StandingRecord record : records) {
                wikiTable.append("<tr>\n" +
                        "         <td>" + i++ + "</td>\n" +
                        "         <td><img src=\"/static/images/flags/" + record.player.getAssocMember().getCountry() + ".png\"/> " + record.player.getAssocMember().getUsername() + "</td>\n" +
                        "         <td>" + record.points + "</td>\n" +
                        "         <td>" + record.hth + "</td>\n" +
                        "         <td>" + record.won + "</td>\n" +
                        "         <td>" + record.white + "</td>\n" +
                        "         <td>" + record.rating + "</td>\n" +
                        "         <td>" + record.games + "</td>\n" +
                        "      </tr>");
            }
            wikiTable.append("</table><br/>");
        }

        modelMap.addAttribute("title", "Standing for " + players.get(0).getTournament().getFullName());
        modelMap.addAttribute("error", wikiTable);
        return "error";
    }

    @RequestMapping(value = "/pending/{shortName}", method = RequestMethod.GET)
    public String tourneyPendingGet(@PathVariable String shortName, ModelMap modelMap) {
        List<TournamentGame> games = tourneyDAO.getGamesByDate(shortName);
        List<Bucket> buckets = bucketsGenerationService.generateBuckets(tourneyDAO.getAllPlayersList(shortName));

        if (games == null || games.isEmpty()) {
            modelMap.addAttribute("title", "Pending games");
            modelMap.addAttribute("error", "No games yet.");
            return "error";
        }


        StringBuilder wikiTable = new StringBuilder();
        wikiTable.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
        wikiTable.append("<tr>\n" +
                "         <th>No</th>\n" +
                "         <th width=\"150px\">White player</th>\n" +
                "         <th width=\"125px\">Date</th>\n" +
                "         <th width=\"150px\">Black player</th>\n" +
                "         <th>Time control</th>\n" +
                "         <th>Bucket</th>\n" +
                "      </tr>");
        int i = 1;
        for (TournamentGame scheduledGame: games) {
            String bucketName = "";
            for (Bucket bucket: buckets) {
                if (bucket.getPlayerList().contains(scheduledGame.getWhitePlayer()))
                    bucketName = bucket.getName();
            }

            String sched = forumFormatter.format(scheduledGame.getSecheduled());
            DateTime now = DateTime.now(DateTimeZone.forID("America/Los_Angeles"));
            Duration p2 = new Duration(now, new DateTime(scheduledGame.getSecheduled(), DateTimeZone.forID("America/Los_Angeles")));
            if (p2.getStandardHours() <= 4) {
                if (p2.getStandardHours() > 0)
                    sched += " (in " + p2.getStandardHours() + " hours)";
                else if (p2.getStandardHours() == 0) {
                    if (p2.getStandardMinutes() > 0)
                        sched += " (in " + p2.getStandardMinutes() + " minutes)";
                }
            }

            String timeControl = UsefulMethods.recommendTime(scheduledGame.getWhitePlayer().getAssocMember().getPreference(),
                    scheduledGame.getBlackPlayer().getAssocMember().getPreference());

            wikiTable.append("<tr>\n" + "         <td>").append(i++).append("</td>\n")
                    .append("         <td><img src=\"/static/images/flags/").append(scheduledGame.getWhitePlayer().getAssocMember().getCountry())
                    .append(".png\" border=\"0\"> ").append(scheduledGame.getWhitePlayer().getAssocMember().getUsername()).append("</td>\n")
                    .append("         <td><center>").append(sched).append("</center></td>\n")
                    .append("         <td><img src=\"/static/images/flags/")
                    .append(scheduledGame.getBlackPlayer().getAssocMember().getCountry())
                    .append(".png\" border=\"0\"> ").append(scheduledGame.getBlackPlayer().getAssocMember().getUsername())
                    .append("</td>\n").append("<td>").append(timeControl.replaceAll("_"," ")).append("</td>").append("<td>" + bucketName + "</td></tr>");
        }
        wikiTable.append("</table>");

        modelMap.addAttribute("title", "Pending games for " + games.get(0).getTournament().getFullName());
        modelMap.addAttribute("error", wikiTable);
        return "error";
    }

    Pattern gameForumPattern = Pattern.compile("([^:]+):R([0-9]+)");

    @RequestMapping(value = "/pairings/{pairString}", method = RequestMethod.GET)
    public String tourneyPairingsGet(@PathVariable String pairString, ModelMap modelMap) {
        String tourneyShortName = null;
        int round = 0;
        Matcher m = gameForumPattern.matcher(pairString);
        if (m.matches()) {
            tourneyShortName = m.group(1);
            round = Integer.parseInt(m.group(2));


            List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);
            if (sorted == null || sorted.isEmpty())
                return "not-found";

            List<Bucket> buckets = bucketsGenerationService.generateBuckets(sorted);

            StringBuilder wikiTable = new StringBuilder();
            List<TournamentGame> games = tourneyDAO.getGamesForRound(tourneyShortName, round);
            if (games == null || games.isEmpty())
                return "not-found";

            for (Bucket bucket : buckets) {
                wikiTable.append("<h2>Bucket ").append(bucket.getName()).append("</h2>" +
                        "<p>TD: "+bucket.getTd()+"</p>");
                wikiTable.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
                wikiTable.append("<tr>\n" +
                        "         <th>No</th>\n" +
                        "         <th width=\"150px\">White player</th>\n" +
                        "         <th width=\"125px\">Date/Result</th>\n" +
                        "         <th width=\"150px\">Black player</th>\n" +
                        "         <th></th>\n" +
                        "      </tr>");

                int id = 1;
                List<TournamentGame> bucketGames = UsefulMethods.getBucketGames(bucket, games);
                for (TournamentGame game : bucketGames) {
                    String result = "";
                    if (game.getSecheduled() != null)
                        result = forumFormatter.format(game.getSecheduled());
                    if (game.getResult() != null)
                        result = game.getResult();

                    wikiTable.append("<tr>\n" + "         <td>").append(id++).append("</td>\n").append("         <td><img src=\"/static/images/flags/").append(game.getWhitePlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getWhitePlayer().getAssocMember().getUsername()).append("</td>\n").append("         <td><center>").append(result).append("</center></td>\n").append("         <td><img src=\"/static/images/flags/").append(game.getBlackPlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getBlackPlayer().getAssocMember().getUsername()).append("</td>\n").append("         <td><a href=\"/tourney/forum/").append(tourneyShortName).append(":R").append(round).append('_').append(game.getWhitePlayer().getAssocMember().getUsername()).append('-').append(game.getBlackPlayer().getAssocMember().getUsername()).append("\">Game forum</a></td>\n").append("      </tr>");
                }

                wikiTable.append("</table><br/>");
            }

            modelMap.addAttribute("title", "Pairings for " + games.get(0).getTournament().getFullName() + " Round " + round);
            modelMap.addAttribute("error", wikiTable);
            return "error";

        }

        return "not-found";
    }

    @RequestMapping(value = "/pgn/{forumString}", method = RequestMethod.GET, produces = "application/pgn")
    @ResponseBody
    public String tourneyPgnGet(@PathVariable String forumString, HttpServletResponse response) {
        TournamentGame game = tourneyDAO.getGameByForumString(forumString);
        if (game == null)
            return null;

        response.setContentType("application/pgn");
        response.setHeader("Content-Disposition","attachment; filename="+game.getWhitePlayer().getAssocMember().getUsername()
                +"_"+game.getBlackPlayer().getAssocMember().getUsername()+"_"+game.getTournament().getShortName()+".pgn");
        return game.getPng();
    }

    @RequestMapping(value = "/completed/{shortName}", method = RequestMethod.GET)
    public String tourneyComletedGet(@PathVariable String shortName, ModelMap modelMap) {
        List<TournamentGame> games = tourneyDAO.getGamesByPgn(shortName);
        if (games.isEmpty()) {
            modelMap.addAttribute("title", "Completed games");
            modelMap.addAttribute("error", "No games yet.");
            return "error";
        }

        StringBuilder wikiTable = new StringBuilder();
        wikiTable.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
        wikiTable.append("<tr>\n" +
                "         <th>No</th>\n" +
                "         <th width=\"150px\">White player</th>\n" +
                "         <th width=\"125px\">Result</th>\n" +
                "         <th width=\"150px\">Black player</th>\n" +
                "         <th></th>\n" +
                "      </tr>");

        int i = 1;
        for (TournamentGame game: games) {
            wikiTable.append("<tr>\n" + "         <td>").append(i++).append("</td>\n").append("         <td><img src=\"/static/images/flags/").append(game.getWhitePlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getWhitePlayer().getAssocMember().getUsername()).append("</td>\n").append("         <td><center>").append(game.getResult()).append("</center></td>\n").append("         <td><img src=\"/static/images/flags/").append(game.getBlackPlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getBlackPlayer().getAssocMember().getUsername()).append("</td>\n").append("         <td><a href=\"/tourney/pgn/"+game.toString()+"\">pgn</a></td></tr>");
        }
        wikiTable.append("</table>");

        modelMap.addAttribute("title", "Completed games for " + games.get(0).getTournament().getFullName());
        modelMap.addAttribute("error", wikiTable.toString());
        return "error";
    }

    @RequestMapping(value = "/forum/{forumString}", method = RequestMethod.GET)
    public String tourneyForumGet(@PathVariable String forumString, ModelMap modelMap) {
        TournamentGame game = tourneyDAO.getGameByForumString(forumString);
        if (game == null)
            return "not-found";

        StringBuilder badTimes = new StringBuilder();
        badTimes.append("<tr><td><b>").append(game.getWhitePlayer().getAssocMember().getUsername()).append("</b></td>");
        List<String> badTimesA = Arrays.asList(game.getWhitePlayer().getAssocMember().getInsist().split(","));
        for (int i = 1; i <= 24; i++) {
            if (badTimesA.contains(Integer.toString(i))) {
                badTimes.append("<td bgcolor=\"red\">");
            }
            else
                badTimes.append("<td>");

            badTimes.append(i).append("</td>");
        }
        badTimes.append("</tr>");
        badTimes.append("<tr><td><b>").append(game.getBlackPlayer().getAssocMember().getUsername()).append("</b></td>");
        List<String> badTimesB = Arrays.asList(game.getBlackPlayer().getAssocMember().getInsist().split(","));
        for (int i = 1; i <= 24; i++) {
            if (badTimesB.contains(Integer.toString(i))) {
                badTimes.append("<td bgcolor=\"red\">");
            }
            else
                badTimes.append("<td>");

            badTimes.append(i).append("</td>");
        }
        badTimes.append("</tr>");
        badTimes.append("</table>");

        String propTime = UsefulMethods.recommendTime(game.getWhitePlayer().getAssocMember().getPreference(),
                game.getBlackPlayer().getAssocMember().getPreference());
        modelMap.addAttribute("title", game.toString());
        modelMap.addAttribute("proposedTime", propTime);
        modelMap.addAttribute("badTimes", badTimes.toString());
        modelMap.addAttribute("htmlText", game.getGameforumHtml());
        return "gameforum";
    }

    @RequestMapping(value = "/signup/{tourneyShortName}", method = RequestMethod.POST)
    public String tourneySignupPost(@PathVariable String tourneyShortName, ModelMap modelMap) {
        Tournament tournament = tourneyDAO.getByShortName(tourneyShortName);
        if (tournament == null)
            return "not-found";

        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        TournamentPlayer player = new TournamentPlayer();
        player.setFixedRating(0);
        player.setAssocMember(((Member)user));
        player.setEmailForum(true);
        player.setTournament(tournament);
        player.setTourneyGroup("");
        tourneyDAO.storePlayer(player);

        return "redirect:/tourney/signup/"+tourneyShortName;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String tourneyProcessPost(@RequestParam(value = "Full_name") String fullName, @RequestParam(value = "Short_name") String shortName,
                                     @RequestParam(value = "Max") String max, @RequestParam(value = "From") String from,
                                     @RequestParam(value = "To") String to, ModelMap modelMap) {
        Tournament newTourn = new Tournament();
        newTourn.setFullName(fullName);
        newTourn.setShortName(shortName);
        newTourn.setMaxCount(Integer.parseInt(max));
        try {
            newTourn.setSignupFrom(formatter.parse(from));
            newTourn.setSignupTo(formatter.parse(to));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tourneyDAO.store(newTourn);

        modelMap.addAttribute("title", "Sucessfull tourney creation");
        modelMap.addAttribute("error", "Your tourney signup page has been created at <a href=\"/tourney/signup/"+shortName+"\">/tourney/"+shortName+"/signup</a>.");
        return "error";
    }
}