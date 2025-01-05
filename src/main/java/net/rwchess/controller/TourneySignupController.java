package net.rwchess.controller;


import chesspresso.game.Game;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import net.rwchess.persistent.*;
import net.rwchess.persistent.dao.InsistData;
import net.rwchess.persistent.dao.MemberDAO;
import net.rwchess.persistent.dao.TournByeDAO;
import net.rwchess.persistent.dao.TourneyDAO;
import net.rwchess.services.*;
import net.rwchess.utils.Mailer;
import net.rwchess.utils.UsefulMethods;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.rwchess.utils.UsefulMethods.getLastRound;

@Controller
@RequestMapping("/tourney")
public class TourneySignupController {

    private TourneyDAO tourneyDAO;
    private MemberDAO memberDAO;
    private CheckRatingsService ratingsService;
    private PythonPairingsService pairingsService;
    private GameForumPostService gameForumPostService;
    private RemindersService remindersService;
    private PythonStandingsService standingsService;
    private final Mailer mailService;
    private final TournByeDAO tournByeDAO;

    private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy h:mm");

    public TourneySignupController(TourneyDAO tourneyDAO, MemberDAO memberDAO, CheckRatingsService ratingsService,
                                   PythonPairingsService pairingsService, GameForumPostService gameForumPostService,
                                   RemindersService remindersService, PythonStandingsService standingsService,
                                   Mailer mailService, TournByeDAO tournByeDAO) {
        this.tourneyDAO = tourneyDAO;
        this.memberDAO = memberDAO;
        this.ratingsService = ratingsService;
        this.pairingsService = pairingsService;
        this.gameForumPostService = gameForumPostService;
        this.remindersService = remindersService;
        this.standingsService = standingsService;
        this.mailService = mailService;
        this.tournByeDAO = tournByeDAO;
        startRemindersService();
        System.out.println("TourneySignupController constructor remindersService");
    }

    void startRemindersService() {
        remindersService.runService();
        DisplayPositionService.getInstance(tourneyDAO).run();
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String tourneyProcessGet() {
        return "tourney-create";
    }

    @RequestMapping(value = "/manage/{tourneyShortName}", method = RequestMethod.POST)
    public String tourneyManagePost(@PathVariable String tourneyShortName,
                                    @RequestParam(value = "submType") String submitVal,
                                    @RequestParam(value = "pairingsFile", required = false) MultipartFile pairingsFile,
                                    ModelMap modelMap,
                                    HttpSession session) {
        if (submitVal.startsWith("Update ratings")) {
            ratingsService.checkRatings(tourneyShortName);

            modelMap.addAttribute("title", "Notice");
            modelMap.addAttribute("error", "Please wait, the ratings are being updated right now. It takes about 1 sec per player to check ratings. Try refreshing this page after a few minutes");
            return "error";
        } else if (submitVal.startsWith("Create pairings")) {
            StringBuilder body = new StringBuilder();
            body.append("<form name=\"input\" action=\"\" method=\"post\"><input name=\"submType\" type=\"submit\" value=\"Save pairings\"></form><br/><br/>");
            body.append("<p>The following games will be created:</p><br/>");

            Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);
            List<TournamentGame> games = new ArrayList<>();
            List<TournBye> byes = new ArrayList<>();
            List<TournamentGame> existingGames = tourneyDAO.getGamesForTourney(tourneyShortName);
            Tournament tournament = tourneyDAO.getByShortName(tourneyShortName);

            int lastRound = getLastRound(existingGames);
            if (pairingsFile != null && !pairingsFile.isEmpty()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pairingsFile.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Skip headers or any line that doesn't start with a board number
                        if (line.length() > 0 && Character.isDigit(line.charAt(0))) {
                            // Split the line into parts
                            String[] parts = line.split(";");

                            // Extract data from the parsed parts
                            String player1Name = parts[4];
                            String player2Name = parts[17];
                            int round = lastRound+1;
                            if (player1Name.equals("BYE")) {
                                TournamentPlayer player2 = findPlayerByName(sorted, player2Name);
                                TournBye tournBye = new TournBye(tournament, round, player2, "FULL");
                                body.append("<li>").append(tournBye.toString()).append("</li>");
                                byes.add(tournBye);
                            } else if (player2Name.equals("BYE")) {
                                TournamentPlayer player1 = findPlayerByName(sorted, player1Name);
                                TournBye tournBye = new TournBye(tournament, round, player1, "FULL");
                                body.append("<li>").append(tournBye.toString()).append("</li>");
                                byes.add(tournBye);
                            } else {
                                TournamentPlayer player1 = findPlayerByName(sorted, player1Name);
                                TournamentPlayer player2 = findPlayerByName(sorted, player2Name);

                                if (player1 != null && player2 != null) {
                                    TournamentGame game = new TournamentGame();
                                    game.setWhitePlayer(player1);
                                    game.setBlackPlayer(player2);
                                    game.setTournament(tourneyDAO.getByShortName(tourneyShortName));
                                    game.setRound(round);
                                    games.add(game);
                                    body.append("<li>").append(game.toString()).append("</li>");
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    modelMap.addAttribute("title", "Error");
                    modelMap.addAttribute("error", "Failed to process the pairings file. Please try again.");
                    return "error";
                }
            } else {
                body.append("<p>No pairings file uploaded.</p>");
            }

            body.append("</ul><br/>");

            session.setAttribute("games", games);
            session.setAttribute("byes", byes);

            modelMap.addAttribute("title", "Notice");
            modelMap.addAttribute("error", body.toString());
            return "error";
        } else if (submitVal.startsWith("Save pairings")) {
//            if (tourneyDAO.tourneyHasPairings(tourneyShortName)) {
//                modelMap.addAttribute("title", "Error");
//                modelMap.addAttribute("error", "<p>Somebody has already created pairings for the tourney.</p>");
//                return "error";
//            }

            List<TournamentGame> games = (List<TournamentGame>) session.getAttribute("games");
            List<TournBye> byes = (List<TournBye>) session.getAttribute("byes");
            System.out.println("Games size: " + games.size());

            for (TournamentGame game : games) { // Assuming games is a class-level or method-level variable holding the pairings
                System.out.println("GAME: " + game);
                tourneyDAO.storeGame(game);
            }

            int round = games.get(0).getRound();
            for (TournBye bye: byes) {
                TournamentPlayer player = bye.getPlayer();
                mailService.sendEmail("notify@snailbucket.org", "SB Rapid 1: Round " + round + " BYE", getByeMessage(round), player.getAssocMember().getEmail());
                tournByeDAO.store(bye);
            }

            modelMap.addAttribute("title", "Notice");
            modelMap.addAttribute("error", "<p>The pairings have been created. Consult TD guide to find links to pairings.</p>");
            return "error";
        }
        return "not-found";
    }

    private static String getByeMessage(int round) {
        return "You have a BYE in round " + round + " of the SB Rapid 1.";
    }


    private TournamentPlayer findPlayerByName(List<TournamentPlayer> players, String name) {
        for (TournamentPlayer player : players) {
            if (player.getAssocMember().getUsername().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null; // Return null if no player with the given name is found.
    }

    /**
     * Displays an HTML form that lets the TD enter text-based pairings.
     * Submits (POST) to the existing `/manage/{tourneyShortName}/text-input` endpoint.
     */
    @RequestMapping(value = "/manage/{tourneyShortName}/text-input", method = RequestMethod.GET)
    public String createTextPairingsForm(@PathVariable String tourneyShortName,
                                         ModelMap modelMap) {
        Tournament tournament = tourneyDAO.getByShortName(tourneyShortName);
        if (tournament == null) {
            modelMap.addAttribute("title", "Error");
            modelMap.addAttribute("error", "Tournament not found: " + tourneyShortName);
            return "error";
        }

        // Build a small HTML form that the user can fill out in the browser.
        // This is just a demo of embedding HTML inside a String.
        // In a real app, you might use a .jsp or .thymeleaf template instead.
        StringBuilder formHtml = new StringBuilder();
        formHtml.append("<h2>Create Text-Based Pairings for Tournament: ")
                .append(tourneyShortName).append("</h2>");
        formHtml.append("<p>Enter pairings line-by-line in the following format:</p>");
        formHtml.append("<ul>");
        formHtml.append("  <li><code>PLAYER1 - PLAYER2</code> for a game</li>");
        formHtml.append("  <li><code>PLAYER1 BYE</code> for a full bye (1 point)</li>");
        formHtml.append("  <li><code>PLAYER1 HALF_BYE</code> for a half-bye (0.5 point)</li>");
        formHtml.append("</ul>");

        // The form will POST back to the same path, but method=POST
        formHtml.append("<form method=\"post\" action=\"/tourney/manage/")
                .append(tourneyShortName).append("/text-input\">")
                .append("    <br/><br/>")
                .append("    <textarea name=\"inputText\" rows=\"10\" cols=\"60\" placeholder=\"e.g.\n")
                .append("Technetium - AsDaGo\n")
                .append("Maras BYE\n")
                .append("mindlin HALF_BYE\n")
                .append("...\"></textarea>")
                .append("    <br/><br/>")
                .append("    <input type=\"submit\" value=\"Create Pairings\"/>")
                .append("</form>");

        // Put our form in the "error" attribute to reuse your "error" template
        modelMap.addAttribute("title", "Text-Based Pairings");
        modelMap.addAttribute("error", formHtml.toString());
        return "error";
    }


    @RequestMapping(value = "/manage/{tourneyShortName}", method = RequestMethod.GET)
    public String tourneyManageGet(@PathVariable String tourneyShortName, ModelMap modelMap) {
        StringBuilder body = new StringBuilder();

        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Tournament currTourney = tourneyDAO.getByShortName(tourneyShortName);

        if (user.getGroup() >= Member.ADMIN) {
            if (currTourney.getSignupTo().after(new Date())) {
                body.append("<form name=\"input\" action=\"\" method=\"post\" enctype=\"multipart/form-data\">")
                        .append("<input name=\"submType\" type=\"submit\" value=\"Update ratings\">")
                        .append("</form><br/><br/>");
            }

            // Form to create pairings and upload CSV file
            body.append("<form name=\"input\" action=\"\" method=\"post\" enctype=\"multipart/form-data\">\n" +
                    "    <input type=\"hidden\" name=\"submType\" value=\"Create pairings\">\n" +
                    "    <input type=\"file\" name=\"pairingsFile\" accept=\".csv\" required>\n" +
                    "    <input type=\"submit\" value=\"Create pairings\">\n" +
                    "</form>\n" +
                    "<br/><br/>\n");
        }

        List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);

        // Removed bucket generation and listed all players in a single space
        body.append("<h3>All Players</h3><br/>");
        body.append("<ul>");

        for (TournamentPlayer player : sorted) {
            body.append("<li><img src=\"/static/images/flags/").append(player.getAssocMember().getCountry()).append(".png\" border=\"0\"> ")
                    .append(player.getAssocMember().getUsername()).append(' ').append(player.getFixedRating()).append("</li>");
        }

        body.append("</ul>");
        body.append("<br/><br/>");

        modelMap.addAttribute("title", "Manage " + tourneyShortName);
        modelMap.addAttribute("error", body.toString());
        return "error";
    }

    @RequestMapping(value = "/signup/{tourneyShortName}", method = RequestMethod.GET)
    public String tourneySignupGet(@PathVariable String tourneyShortName, ModelMap modelMap) {
        Tournament tournament = tourneyDAO.getByShortName(tourneyShortName);
        if (tournament == null) {
            return "not-found";
        }

        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean signedUp = false;

        List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);
        boolean allBucketsNull = sorted.stream().allMatch(player -> player.getBucket() == null);

        StringBuilder signedList = new StringBuilder("<h2>Players list</h2>");

        if (allBucketsNull) {
            signedList.append("<ul>");
            for (TournamentPlayer player : sorted) {
                signedList.append("<li><img src=\"/static/images/flags/")
                        .append(player.getAssocMember().getCountry())
                        .append(".png\" border=\"0\"> ")
                        .append(player.getAssocMember().getUsername())
                        .append(' ')
                        .append(player.getFixedRating())
                        .append("</li>");
                if (!(user instanceof String)) {
                    if (player.getAssocMember().getUsername().equalsIgnoreCase(((Member) user).getUsername())) {
                        signedUp = true;
                    }
                }
            }
            signedList.append("</ul>");
        } else {
            Map<String, List<TournamentPlayer>> bucketMap = new LinkedHashMap<>();
            for (TournamentPlayer player : sorted) {
                String bucket = (player.getBucket() != null) ? player.getBucket() : "No bucket";
                bucketMap.putIfAbsent(bucket, new ArrayList<>());
                bucketMap.get(bucket).add(player);
            }
            for (Map.Entry<String, List<TournamentPlayer>> entry : bucketMap.entrySet()) {
                String bucketName = entry.getKey();
                List<TournamentPlayer> playersInBucket = entry.getValue();
                signedList.append("<h3>").append(bucketName).append("</h3>");
                signedList.append("<ul>");
                for (TournamentPlayer player : playersInBucket) {
                    signedList.append("<li><img src=\"/static/images/flags/")
                            .append(player.getAssocMember().getCountry())
                            .append(".png\" border=\"0\"> ")
                            .append(player.getAssocMember().getUsername())
                            .append(' ')
                            .append(player.getFixedRating())
                            .append("</li>");
                    if (!(user instanceof String)) {
                        if (((Member) user).getUsername().equalsIgnoreCase(player.getAssocMember().getUsername())) {
                            signedUp = true;
                        }
                    }
                }
                signedList.append("</ul>");
            }
        }

        signedList.append("<br/><br/>");

        String signupMessage;
        DateTime today = DateTime.now(DateTimeZone.forID("America/New_York"));
        DateTime signupFrom = new DateTime(tournament.getSignupFrom(), DateTimeZone.forID("America/New_York"));
        DateTime signupTo = new DateTime(tournament.getSignupTo(), DateTimeZone.forID("America/New_York"));

        if (signupFrom.isAfter(today)) {
            signupMessage = "<p>The registration will start at " + UsefulMethods.formatDateWiki(signupFrom) + "</p>";
        } else if (signupTo.isBefore(today)) {
            signupMessage = "<p>The registration has been closed at " + UsefulMethods.formatDateWiki(signupTo) + "</p>";
        } else {
            if (user instanceof String) {
                signupMessage = "<p>Log in in order to sign up.</p>";
            } else if (!signedUp) {
                signupMessage = "<p>Please read the tourney guide before signing up.</p>\n" +
                        "<form id=\"Sign\" action=\"\" method=\"post\">\n" +
                        "    <p>\n" +
                        "       <input id=\"SaveAccount\" type=\"submit\" value=\"Sign up to tourney\" />\n" +
                        "    </p>\n" +
                        "</form>";
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
        } else if (submitVal.startsWith("Submit")) {
            Long key = Long.parseLong(submitVal.substring(6));
            memberDAO.updateRole(key, Integer.parseInt(req.getParameter("Role")));
            memberDAO.updateRR(key, Integer.parseInt(req.getParameter("rr")));
            return "redirect:/tourney/players";
        } else if (submitVal.startsWith("Set new password")) {
            Long key = Long.parseLong(req.getParameter("key"));
            memberDAO.updatePassword(key, req.getParameter("password"));
            body.append("<p>Password has been updated.</p>");
        }

        modelMap.addAttribute("title", "");
        modelMap.addAttribute("error", body.toString());
        return "error";
    }

    private final static Pattern ficsGamesLinkMod = Pattern.compile("\\/cgi-bin\\/show\\.cgi\\?ID=[0-9]+;action=save");

    /**
     * Fetch game results from ficsgames.org after game finish
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/updateforums/{forumString}", method = RequestMethod.GET)
    public String updateForums(@PathVariable String forumString, ModelMap modelMap) {
        try {
            TournamentGame game = tourneyDAO.getGameByForumString(forumString);

            URL urll = new URL("https://www.ficsgames.org/cgi-bin/search.cgi?player="+game.getBlackPlayer().getAssocMember().getUsername()+"&action=History");
            URLConnection con = urll.openConnection();
            InputStream inn = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(inn, encoding);
            Matcher m = ficsGamesLinkMod.matcher(body);
            while (m.find()) {
                String s = "https://www.ficsgames.org" + m.group(0);

                URL url = new URL(s);
                String pgn = IOUtils.toString(new InputStreamReader(
                        url.openStream()));

                boolean in = false;
                char last = ' ';
                StringBuilder buff = new StringBuilder();
                for (char c : pgn.toCharArray()) {
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

                PGNReader red = new PGNReader(IOUtils.toInputStream(pgn), game.getTournament().getShortName() + ".pgn");
                try {
                    Game ga = red.parseGame();
                    ga.setTag("Round", Integer.toString(game.getRound()));
                    ga.setTag("Event", game.getTournament().getFullName());
                    result = ga.getResultStr();

                    String whiteNameFromGame = ga.getWhite().replaceAll("\\(.*?\\)", "");
                    String blackNameFromGame = ga.getBlack().replaceAll("\\(.*?\\)", "");
                    if (!whiteNameFromGame.equalsIgnoreCase(game.getWhitePlayer().getAssocMember().getUsername()) ||
                            !blackNameFromGame.equalsIgnoreCase(game.getBlackPlayer().getAssocMember().getUsername())) {
                         continue;
                    }
                    DateTimeFormatter df = DateTimeFormat.forPattern("yyyy.MM.dd HH:mm:ss");
                    DateTime gameDate = df.parseDateTime(ga.getDate() + ' ' + ga.getTag("Time"));
                    DateTime startDate = new DateTime(game.getTournament().getStartDate(), DateTimeZone.forID("America/New_York"));

                    if (gameDate.isBefore(startDate)) {
                         continue;
                    }

                    pgn = UsefulMethods.getPgnRepresentation(ga);
                    gameForumPostService.gameForumPost(game, "Game ended as " + result, "snailbot(TD)");
                    tourneyDAO.updatePgn(game, pgn);
                    tourneyDAO.updatePlayedDate(game, gameDate.toDate());
                    tourneyDAO.updateResult(game, result);
                    modelMap.addAttribute("title", "Done");
                    modelMap.addAttribute("error", "Set.");
                    return "error";
                } catch (PGNSyntaxError e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        modelMap.addAttribute("title", "Error");
        modelMap.addAttribute("error", "Not Set.");
        return "error";
    }

    @RequestMapping(value = "/manage/{tourneyShortName}/text-input", method = RequestMethod.POST)
    public String createTextPairings(@PathVariable String tourneyShortName,
                                     @RequestParam(value = "round", required = false) Integer specifiedRound,
                                     @RequestParam(value = "inputText") String inputText,
                                     ModelMap modelMap,
                                     HttpSession session) {

        // 1) Fetch the tournament
        Tournament tournament = tourneyDAO.getByShortName(tourneyShortName);
        if (tournament == null) {
            modelMap.addAttribute("title", "Error");
            modelMap.addAttribute("error", "Tournament not found: " + tourneyShortName);
            return "error";
        }

        // 2) Get existing games to figure out lastRound
        List<TournamentGame> existingGames = tourneyDAO.getGamesForTourney(tourneyShortName);
        int lastRound = getLastRound(existingGames);
        int roundToCreate = (specifiedRound != null) ? specifiedRound : (lastRound + 1);

        // 3) Retrieve all players
        List<TournamentPlayer> allPlayers = tourneyDAO.getAllPlayersListSorted(tourneyShortName);

        // 4) Prepare containers for the new games/byes
        List<TournamentGame> newGames = new ArrayList<>();
        List<TournBye> newByes = new ArrayList<>();

        // 5) Prepare a list to collect errors
        List<String> parseErrors = new ArrayList<>();

        // 6) Parse input text line-by-line
        String[] lines = inputText.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue; // skip blank lines
            }

            // Check if it's a game line: "PLAYER1 - PLAYER2"
            if (line.contains(" - ")) {
                // e.g. "PlayerA - PlayerB"
                String[] parts = line.split("-", 2); // split only once
                if (parts.length != 2) {
                    parseErrors.add("Invalid game line (missing dash): " + line);
                    continue;
                }
                String player1Name = parts[0].trim();
                String player2Name = parts[1].trim();

                TournamentPlayer white = findPlayerByName(allPlayers, player1Name);
                TournamentPlayer black = findPlayerByName(allPlayers, player2Name);

                if (white == null || black == null) {
                    parseErrors.add("Cannot find player(s) in line: " + line);
                    continue;
                }

                // Create a new game
                TournamentGame game = new TournamentGame();
                game.setWhitePlayer(white);
                game.setBlackPlayer(black);
                game.setTournament(tournament);
                game.setRound(roundToCreate);
                newGames.add(game);

            } else {
                // Could be "PLAYER1 BYE" or "PLAYER1 HALF_BYE"
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    parseErrors.add("Invalid bye line (missing token): " + line);
                    continue;
                }

                // Last token is either BYE or HALF_BYE
                String lastToken = parts[parts.length - 1].trim().toUpperCase();
                // The rest is the player's name
                String playerName = String.join(" ",
                        Arrays.copyOf(parts, parts.length - 1)).trim();

                TournamentPlayer player = findPlayerByName(allPlayers, playerName);
                if (player == null) {
                    parseErrors.add("Cannot find player in line: " + line);
                    continue;
                }

                String byeType;
                if ("BYE".equals(lastToken)) {
                    byeType = "FULL";
                } else if ("HALF_BYE".equals(lastToken)) {
                    byeType = "HALF";
                } else {
                    parseErrors.add("Unrecognized bye type (must be BYE or HALF_BYE) in line: " + line);
                    continue;
                }

                TournBye tournBye = new TournBye(tournament, roundToCreate, player, byeType);
                newByes.add(tournBye);
            }
        }

        // 7) Check if there were any parse errors
        if (!parseErrors.isEmpty()) {
            // We do NOT store anything if there's at least one error.
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("<h3>Failed to create pairings due to errors:</h3>");
            errorMsg.append("<ul>");
            for (String err : parseErrors) {
                errorMsg.append("<li>").append(err).append("</li>");
            }
            errorMsg.append("</ul>");
            errorMsg.append("<p>Please correct these lines and try again.</p>");

            modelMap.addAttribute("title", "Error Parsing Pairings");
            modelMap.addAttribute("error", errorMsg.toString());
            return "error";
        }

        // 8) If no errors, store the new games and byes in the DB
        for (TournamentGame g : newGames) {
            tourneyDAO.storeGame(g);
        }
        for (TournBye b : newByes) {
            tournByeDAO.store(b);
        }

        // 9) Build a success message
        StringBuilder msg = new StringBuilder();
        msg.append("<h3>Successfully created round ")
                .append(roundToCreate)
                .append(" pairings:</h3>")
                .append("<ul>");
        for (TournamentGame g : newGames) {
            msg.append("<li>Game: ")
                    .append(g.getWhitePlayer().getAssocMember().getUsername())
                    .append(" - ")
                    .append(g.getBlackPlayer().getAssocMember().getUsername())
                    .append("</li>");
        }
        for (TournBye b : newByes) {
            msg.append("<li>Bye: ")
                    .append(b.getPlayer().getAssocMember().getUsername())
                    .append(" (").append(b.getByeType()).append(")</li>");
        }
        msg.append("</ul>");

        modelMap.addAttribute("title", "Text Pairings Created");
        modelMap.addAttribute("error", msg.toString());  // reusing "error" for the display
        return "error";  // or your own template
    }

    @RequestMapping(value = "/players", method = RequestMethod.GET)
    public String tourneyPlayers(ModelMap modelMap) {
        List<Member> members = memberDAO.getAllMembers();

        StringBuilder playersTable = new StringBuilder();
        playersTable.append("<p>Click \"UpdateXX\" button to update user's role or change password. Sort by clicking on column headers.</p><form name=\"input\" action=\"\" method=\"post\"><table class=\"tablesorter\" cellspacing=\"1\"><thead>" +
                "<tr><th>USERNAME</th><th>EMAIL</th><th>CONFIRMED</th><th>SIGNED_UP</th><th>ROLE</th><th>RR</th><th>UPDATE</th></tr></thead><tbody>");
        for (Member member : members) {
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

    Pattern ficsGamesLink = Pattern.compile("https:\\/\\/(www\\.)?ficsgames\\.org\\/cgi-bin\\/show\\.cgi\\?ID=[0-9]+;action=save");

    @RequestMapping(value = "/forum/{forumString}", method = RequestMethod.POST)
    public String tourneyForumPost(@PathVariable String forumString, ModelMap modelMap, @RequestParam Map<String, String> params) throws Exception {
        TournamentGame game = tourneyDAO.getGameByForumString(forumString);
        if (game == null)
            return "not-found";


        Member user = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (game.getRound() > UsefulMethods.getCurrentRound()) {
//            modelMap.addAttribute("title", "Error");
//            modelMap.addAttribute("error", "You cannot negotiate about next round yet.");
//            return "error";
//        }

        String content = params.get("contents");
        String buttonName = params.get("name");

        if (buttonName.equals("Submit text")) {
            Matcher matcher = ficsGamesLink.matcher(content);
            List<String> links = new ArrayList<>();

// Find all matching links and add them to the list
            while (matcher.find()) {
                links.add(matcher.group());
            }

            if (links.size() > 0) {
                StringBuilder pngs = new StringBuilder();

                int whiteWins = 0;
                int blackWins = 0;
                int draws = 0;

                String expectedWhitePlayer = game.getWhitePlayer().getAssocMember().getUsername();
                String expectedBlackPlayer = game.getBlackPlayer().getAssocMember().getUsername();

                for (String link : links) {
                    URL url = new URL(link);
                    String pgn = IOUtils.toString(new InputStreamReader(url.openStream()));

                    boolean in = false;
                    char last = ' ';
                    StringBuilder buff = new StringBuilder();
                    for (char c : pgn.toCharArray()) {
                        if (c == ']' && in) in = false;
                        if (in) continue;
                        if (c == '[' && last == '{') in = true;
                        last = c;
                        buff.append(c);
                    }
                    pgn = buff.toString().replace("{[]}", "");
                    pngs.append('\n');
                    pngs.append(pgn);

                    PGNReader reader = new PGNReader(IOUtils.toInputStream(pgn), game.getTournament().getShortName() + ".pgn");
                    Game ga;
                    try {
                        ga = reader.parseGame();
                    } catch (PGNSyntaxError e) {
                        modelMap.addAttribute("title", "Error");
                        modelMap.addAttribute("error", "PGN Syntax Error: " + e.getMessage());
                        return "error";
                    }

                    String whitePlayer = ga.getWhite();
                    String blackPlayer = ga.getBlack();
                    String result = ga.getResultStr();
                    String timeControl = ga.getTag("TimeControl");

                    // Verify opponent names
                    if (!(extractBaseName(whitePlayer).equalsIgnoreCase(expectedWhitePlayer) &&
                            extractBaseName(blackPlayer).equalsIgnoreCase(expectedBlackPlayer)) &&
                            !(extractBaseName(whitePlayer).equalsIgnoreCase(expectedBlackPlayer) &&
                                    extractBaseName(blackPlayer).equalsIgnoreCase(expectedWhitePlayer))) {
                        modelMap.addAttribute("title", "Error");
                        modelMap.addAttribute("error", "You posted the wrong game.");
                        return "error";
                    }

                    // Count the results based on actual players, not the color in the game
                    if (result.equals("1-0")) {
                        if (extractBaseName(whitePlayer).equalsIgnoreCase(expectedWhitePlayer)) {
                            whiteWins++;
                        } else {
                            blackWins++;
                        }
                    } else if (result.equals("0-1")) {
                        if (extractBaseName(blackPlayer).equalsIgnoreCase(expectedBlackPlayer)) {
                            blackWins++;
                        } else {
                            whiteWins++;
                        }
                    } else if (result.equals("1/2-1/2")) {
                        draws++;
                    } else {
                        modelMap.addAttribute("title", "Error");
                        modelMap.addAttribute("error", "Unknown game result: " + result);
                        return "error";
                    }
                }

                // Determine the final result based on points
                String finalResult;
                if (whiteWins > blackWins) {
                    finalResult = "1-0";
                } else if (blackWins > whiteWins) {
                    finalResult = "0-1";
                } else {
                    finalResult = "1/2-1/2";
                }

// Process the result and update the database
                gameForumPostService.gameForumPost(game, "Game ended as " + finalResult, "snailbot(TD)");
                tourneyDAO.updatePgn(game, pngs.toString());
                tourneyDAO.updatePlayedDate(game, new Date());
                tourneyDAO.updateResult(game, finalResult);
            } else {
                if (content.startsWith("Game adjudicated as") &&
                        !content.endsWith(".") && user.getGroup() >= Member.TD) {
                    String result = content.substring("Game adjudicated as ".length()).trim();
                    tourneyDAO.updateResult(game, result);
                }

                gameForumPostService.gameForumPost(game, content, user.getUsername());
            }

            return "redirect:/tourney/forum/" + forumString;

        }


        String tag = "";
        if (user.getGroup() >= Member.TD && !forumString.contains(user.getUsername()))
            tag = "(TD)";

        if (!content.isEmpty()) {
            gameForumPostService.gameForumPost(game, content, user.getUsername() + tag);
        } else if (buttonName.equals("Set clock")) {
            if (!params.get("month").equals("0") && (forumString.contains(user.getUsername()) || user.getGroup() >= Member.TD)) {
                DateTime now = DateTime.now();
                DateTime dateTime = new DateTime(now.getYear(), Integer.parseInt(params.get("month")),
                        Integer.parseInt(params.get("day")), Integer.parseInt(params.get("hour")),
                        Integer.parseInt(params.get("minute")), DateTimeZone.forID("America/New_York"));
                if (dateTime.isBefore(new DateTime(game.getTournament().getStartDate(), DateTimeZone.forID("America/New_York")))) {
                    modelMap.addAttribute("title", "Error");
                    modelMap.addAttribute("error", "The date before tourney start.");
                    return "error";
                }

                content = gameForumPostService.dateSetPost(params.get("month"), params.get("day"),
                        params.get("hour"), params.get("minute"), game);
                gameForumPostService.gameForumPost(game, content, user.getUsername() + tag);
            }
        } else if (buttonName.equals("Unset clock")) {
            if (forumString.contains(user.getUsername()) || user.getGroup() >= Member.TD) {
                content = gameForumPostService.dateUnsetPost(game);
                gameForumPostService.gameForumPost(game, content, user.getUsername() + tag);
            }
        }

        return "redirect:/tourney/forum/" + forumString;
    }

    public static String extractBaseName(String input) {
        return input.replaceAll("\\s*\\(.*?\\)", "").trim();
    }

    SimpleDateFormat forumFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm",
            Locale.US);


    @RequestMapping(value = "/standing/{shortName}", method = RequestMethod.GET)
    public String tourneyStandingGet(@PathVariable String shortName, ModelMap modelMap) {
        List<TournamentGame> gameWithResults = tourneyDAO.getGamesByResult(shortName);
        List<TournamentGame> allGames = tourneyDAO.getGamesForTourney(shortName);
        List<TournamentPlayer> players = tourneyDAO.getAllPlayersList(shortName);
        List<TournBye> byes = tournByeDAO.getByTourneyShortName(shortName);

        if (players == null || players.isEmpty()) {
            modelMap.addAttribute("title", "Standings");
            modelMap.addAttribute("error", "No standings yet.");
            return "error";
        }

        StringBuilder wikiTable = new StringBuilder();
        wikiTable.append("<p><i>Standings were created using the ")
                .append("<a href=\"https://www.vegachess.com/ns/home\" target=\"_blank\">Vega Chess</a>")
                .append(" software.</i></p><br/>");
        List<PythonStandingsService.StandingRecord> records
                = standingsService.generateStandings(gameWithResults, allGames, players, byes);

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
            wikiTable.append("<tr>\n" + "         <td>").append(i++).append("</td>\n")
                    .append("         <td><img src=\"/static/images/flags/").append(record.player.getAssocMember().getCountry())
                    .append(".png\"/> ").append(record.player.getAssocMember().getUsername()).append("</td>\n")
                    .append("         <td>").append(record.points).append("</td>\n")
                    .append("         <td>").append(record.hth).append("</td>\n")
                    .append("         <td>").append(record.won).append("</td>\n")
                    .append("         <td>").append(record.white).append("</td>\n")
                    .append("         <td>").append(record.rating).append("</td>\n")
                    .append("         <td>").append(record.games).append("</td>\n")
                    .append("      </tr>");
        }
        wikiTable.append("</table><br/>");

        modelMap.addAttribute("title", "Standings for " + players.get(0).getTournament().getFullName());
        modelMap.addAttribute("error", wikiTable);
        return "error";
    }


    @RequestMapping(value = "/pending/{shortName}", method = RequestMethod.GET)
    public String tourneyPendingGet(@PathVariable String shortName, ModelMap modelMap) {
        List<TournamentGame> games = tourneyDAO.getGamesByDate(shortName);
        List<TournamentPlayer> players = tourneyDAO.getAllPlayersList(shortName);

        if (players == null || players.isEmpty()) {
            modelMap.addAttribute("title", "Pending games");
            modelMap.addAttribute("error", "No games yet.");
            return "error";
        }

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
                "         <th>Round</th>\n" +
                "      </tr>");

        int i = 1;
        for (TournamentGame scheduledGame : games) {
            String sched = forumFormatter.format(scheduledGame.getSecheduled());

            if (scheduledGame.getSecheduled().getYear() < 100) {
                sched = "<img src=\"/static/images/sn.gif\" width=\"20\"/>";
            }

            DateTime now = DateTime.now(DateTimeZone.forID("America/New_York"));
            Duration p2 = new Duration(now, new DateTime(scheduledGame.getSecheduled(), DateTimeZone.forID("America/New_York")));
            if (p2.getStandardHours() <= 4) {
                if (p2.getStandardHours() > 0) {
                    String[] arr = DurationFormatUtils.formatDuration(p2.getMillis(), "H#m").split("#");
                    sched += " (in " + arr[0] + (arr[0].equals("1") ? " hour " : " hours ");
                    sched += arr[1] + (arr[1].equals("1") ? " minute)" : " minutes)");
                } else if (p2.getStandardHours() == 0) {
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
                    .append("</td>\n").append("<td><center>").append(timeControl.replaceAll("_", " ")).append("</center></td>")
                    .append("<td><center>").append(scheduledGame.getRound()).append("</center></td></tr>");
        }
        wikiTable.append("</table>");

        modelMap.addAttribute("title", "Pending games for " + games.get(0).getTournament().getFullName());
        modelMap.addAttribute("error", wikiTable);
        return "error";
    }


    @RequestMapping(value = "/pairings/all/{shortName}", method = RequestMethod.GET)
    public String tourneyPairingsAll(@PathVariable String shortName, ModelMap modelMap) {

        List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(shortName);
        if (sorted == null || sorted.isEmpty()) {
            modelMap.addAttribute("title", "Pairings All Rounds");
            modelMap.addAttribute("error", "No pairings yet.");
            return "error";
        }

        StringBuilder wikiTable = new StringBuilder();
        wikiTable.append("<p><i>Pairings were created using the ")
                .append("<a href=\"https://www.vegachess.com/ns/home\" target=\"_blank\">Vega Chess</a>")
                .append(" software.</i></p><br/>");

        List<TournamentGame> games = tourneyDAO.getGamesForTourney(shortName);

        if (games == null || games.isEmpty()) {
            modelMap.addAttribute("title", "Pairings All Rounds");
            modelMap.addAttribute("error", "No pairings yet.");
            return "error";
        }

        Map<Integer, List<TournamentGame>> gamesByRound = games.stream()
                .collect(Collectors.groupingBy(TournamentGame::getRound));

        // Looping through each round
        for (Map.Entry<Integer, List<TournamentGame>> entry : gamesByRound.entrySet()) {
            int round = entry.getKey();
            List<TournBye> byes = tournByeDAO.getByTourneyShortNameAndRound(shortName, round);
            List<TournamentGame> gamesInRound = entry.getValue();

            wikiTable.append("<h1>Round ").append(round).append("</h1>");
            wikiTable.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
            wikiTable.append("<tr>\n" +
                    "         <th>No</th>\n" +
                    "         <th width=\"150px\">White player</th>\n" +
                    "         <th width=\"125px\">Date/Result</th>\n" +
                    "         <th width=\"150px\">Black player</th>\n" +
                    "      </tr>");

            int id = 1;
            for (TournamentGame game : gamesInRound) {
                String result = "";
                if (game.getSecheduled() != null) {
                    if (game.getSecheduled().getYear() < 100)
                        result = "<img src=\"/static/images/sn.gif\" width=\"20\"/>";
                    else
                        result = forumFormatter.format(game.getSecheduled());
                }
                if (game.getResult() != null) {
                    result = game.getResult();

                    if (game.getPng() != null) {
                        result = "<a href=\"/tourney/pgn/" + game.getGameForumString() + "\">" + result + "</a>";
                    }
                }

                wikiTable.append("<tr>\n")
                        .append("         <td>").append(id++).append("</td>\n")
                        .append("         <td><img src=\"/static/images/flags/").append(game.getWhitePlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getWhitePlayer().getAssocMember().getUsername()).append("</td>\n")
                        .append("         <td><center>").append(result).append("</center></td>\n")
                        .append("         <td><img src=\"/static/images/flags/").append(game.getBlackPlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getBlackPlayer().getAssocMember().getUsername()).append("</td>\n")
                        .append("      </tr>");
            }

            if (byes != null && !byes.isEmpty()) {
                for (TournBye bye : byes) {
                    wikiTable.append("<tr>\n")
                            .append("         <td>").append(id++).append("</td>\n")
                            .append("         <td><img src=\"/static/images/flags/")
                            .append(bye.getPlayer().getAssocMember().getCountry())
                            .append(".png\" border=\"0\"> ")
                            .append(bye.getPlayer().getAssocMember().getUsername())
                            .append("</td>\n")
                            .append("         <td><center>+:-</center></td>\n")
                            .append("         <td>BYE</td><td></td>\n")
                            .append("      </tr>");
                }
            }

            wikiTable.append("</table><br/>");
        }

        modelMap.addAttribute("title", "Pairings All Rounds");
        modelMap.addAttribute("error", wikiTable);
        return "error";
    }


    Pattern gameForumPattern = Pattern.compile("([^:]+):R([0-9]+)");

    @RequestMapping(value = "/pairings/{tourneyShortName}", method = RequestMethod.GET)
    public String tourneyPairingsGet(@PathVariable String tourneyShortName, ModelMap modelMap) {
        List<TournamentGame> allGames = tourneyDAO.getGamesForTourney(tourneyShortName);
        int round = UsefulMethods.getLastRound(allGames);
        List<TournamentPlayer> sorted = tourneyDAO.getAllPlayersListSorted(tourneyShortName);
        if (sorted == null || sorted.isEmpty()) {
            modelMap.addAttribute("title", "Pairings");
            modelMap.addAttribute("error", "No pairings yet.");
            return "error";
        }

        boolean allBucketsNull = sorted.stream().allMatch(p -> p.getBucket() == null);

        StringBuilder content = new StringBuilder();
        content.append("<p><i>Pairings were created using the ")
                .append("<a href=\"https://www.vegachess.com/ns/home\" target=\"_blank\">Vega Chess</a>")
                .append(" software.</i></p><br/>");

        List<TournamentGame> games = tourneyDAO.getGamesForRound(tourneyShortName, round);
        if (games == null || games.isEmpty()) {
            modelMap.addAttribute("title", "Pairings");
            modelMap.addAttribute("error", "No pairings yet.");
            return "error";
        }

        List<TournBye> byes = tournByeDAO.getByTourneyShortNameAndRound(tourneyShortName, round);

        if (allBucketsNull) {
            content.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
            content.append("<tr>")
                    .append("<th>No</th>")
                    .append("<th width=\"150px\">White player</th>")
                    .append("<th width=\"125px\">Date/Result</th>")
                    .append("<th width=\"150px\">Black player</th>")
                    .append("<th></th>")
                    .append("</tr>");

            int id = 1;
            for (TournamentGame game : games) {
                String result = "";
                if (game.getSecheduled() != null) {
                    if (game.getSecheduled().getYear() < 100) {
                        result = "<img src=\"/static/images/sn.gif\" width=\"20\"/>";
                    } else {
                        result = forumFormatter.format(game.getSecheduled());
                    }
                }
                if (game.getResult() != null) {
                    result = game.getResult();
                    if (game.getPng() != null) {
                        result = "<a href=\"/tourney/pgn/" + game.getGameForumString() + "\">" + result + "</a>";
                    }
                }
                content.append("<tr>")
                        .append("<td>").append(id++).append("</td>")
                        .append("<td><img src=\"/static/images/flags/")
                        .append(game.getWhitePlayer().getAssocMember().getCountry())
                        .append(".png\" border=\"0\"> ")
                        .append(game.getWhitePlayer().getAssocMember().getUsername())
                        .append("</td>")
                        .append("<td><center>").append(result).append("</center></td>")
                        .append("<td><img src=\"/static/images/flags/")
                        .append(game.getBlackPlayer().getAssocMember().getCountry())
                        .append(".png\" border=\"0\"> ")
                        .append(game.getBlackPlayer().getAssocMember().getUsername())
                        .append("</td>")
                        .append("<td><a href=\"/tourney/forum/")
                        .append(tourneyShortName).append(":R").append(round).append('_')
                        .append(game.getWhitePlayer().getAssocMember().getUsername()).append('-')
                        .append(game.getBlackPlayer().getAssocMember().getUsername())
                        .append("\">Game forum</a></td>")
                        .append("</tr>");
            }

            if (byes != null && !byes.isEmpty()) {
                for (TournBye byeItem : byes) {
                    content.append("<tr>")
                            .append("<td>").append(id++).append("</td>")
                            .append("<td><img src=\"/static/images/flags/")
                            .append(byeItem.getPlayer().getAssocMember().getCountry())
                            .append(".png\" border=\"0\"> ")
                            .append(byeItem.getPlayer().getAssocMember().getUsername())
                            .append("</td>")
                            .append("<td><center>+:-</center></td>")
                            .append("<td>BYE</td><td></td>")
                            .append("</tr>");
                }
            }

            content.append("</table><br/>");
            modelMap.addAttribute("title", "Pairings for " + games.get(0).getTournament().getFullName() + " Round " + round);
            modelMap.addAttribute("error", content);
            return "error";
        } else {
            Map<String, List<TournamentGame>> bucketGames = new LinkedHashMap<>();
            Map<String, List<TournBye>> bucketByes = new LinkedHashMap<>();
            for (TournamentGame g : games) {
                String bucketW = g.getWhitePlayer().getBucket();
                String bucketB = g.getBlackPlayer().getBucket();
                String bucket = (bucketW != null) ? bucketW : bucketB;
                if (bucket == null) {
                    bucket = "No bucket";
                }
                bucketGames.putIfAbsent(bucket, new ArrayList<>());
                bucketGames.get(bucket).add(g);
            }
            if (byes != null) {
                for (TournBye bItem : byes) {
                    String bucket = bItem.getPlayer().getBucket();
                    if (bucket == null) {
                        bucket = "No bucket";
                    }
                    bucketByes.putIfAbsent(bucket, new ArrayList<>());
                    bucketByes.get(bucket).add(bItem);
                }
            }

            for (String bucketName : bucketGames.keySet()) {
                content.append("<h3>Bucket: ").append(bucketName).append("</h3>");
                content.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
                content.append("<tr>")
                        .append("<th>No</th>")
                        .append("<th width=\"150px\">White player</th>")
                        .append("<th width=\"125px\">Date/Result</th>")
                        .append("<th width=\"150px\">Black player</th>")
                        .append("<th></th>")
                        .append("</tr>");
                int id = 1;
                for (TournamentGame g : bucketGames.get(bucketName)) {
                    String result = "";
                    if (g.getSecheduled() != null) {
                        if (g.getSecheduled().getYear() < 100) {
                            result = "<img src=\"/static/images/sn.gif\" width=\"20\"/>";
                        } else {
                            result = forumFormatter.format(g.getSecheduled());
                        }
                    }
                    if (g.getResult() != null) {
                        result = g.getResult();
                        if (g.getPng() != null) {
                            result = "<a href=\"/tourney/pgn/" + g.getGameForumString() + "\">" + result + "</a>";
                        }
                    }
                    content.append("<tr>")
                            .append("<td>").append(id++).append("</td>")
                            .append("<td><img src=\"/static/images/flags/")
                            .append(g.getWhitePlayer().getAssocMember().getCountry())
                            .append(".png\" border=\"0\"> ")
                            .append(g.getWhitePlayer().getAssocMember().getUsername())
                            .append("</td>")
                            .append("<td><center>").append(result).append("</center></td>")
                            .append("<td><img src=\"/static/images/flags/")
                            .append(g.getBlackPlayer().getAssocMember().getCountry())
                            .append(".png\" border=\"0\"> ")
                            .append(g.getBlackPlayer().getAssocMember().getUsername())
                            .append("</td>")
                            .append("<td><a href=\"/tourney/forum/")
                            .append(tourneyShortName).append(":R").append(round).append('_')
                            .append(g.getWhitePlayer().getAssocMember().getUsername()).append('-')
                            .append(g.getBlackPlayer().getAssocMember().getUsername())
                            .append("\">Game forum</a></td>")
                            .append("</tr>");
                }
                if (bucketByes.containsKey(bucketName)) {
                    for (TournBye byeItem : bucketByes.get(bucketName)) {
                        content.append("<tr>")
                                .append("<td>").append(id++).append("</td>")
                                .append("<td><img src=\"/static/images/flags/")
                                .append(byeItem.getPlayer().getAssocMember().getCountry())
                                .append(".png\" border=\"0\"> ")
                                .append(byeItem.getPlayer().getAssocMember().getUsername())
                                .append("</td>")
                                .append("<td><center>+:-</center></td>")
                                .append("<td>BYE</td><td></td>")
                                .append("</tr>");
                    }
                }
                content.append("</table><br/>");
            }

            modelMap.addAttribute("title", "Pairings for " + games.get(0).getTournament().getFullName() + " Round " + round);
            modelMap.addAttribute("error", content.toString());
            return "error";
        }
    }



    @RequestMapping(value = "/pgn/all/{shortName}", method = RequestMethod.GET, produces = "application/pgn")
    @ResponseBody
    public String tourneyPgnGetAll(@PathVariable String shortName, HttpServletResponse response) {
        List<TournamentGame> games = tourneyDAO.getGamesByPgn(shortName);
        StringBuilder sb = new StringBuilder();
        for (TournamentGame game: games) {
            sb.append(game.getPng()).append("\n\n");
        }

        response.setContentType("application/pgn");
        response.setHeader("Content-Disposition", "attachment; filename=" + shortName + ".pgn");
        return sb.toString();
    }

    @RequestMapping(value = "/pgn/{forumString}", method = RequestMethod.GET, produces = "application/pgn")
    @ResponseBody
    public String tourneyPgnGet(@PathVariable String forumString, HttpServletResponse response) {
        TournamentGame game = tourneyDAO.getGameByForumString(forumString);
        if (game == null)
            return null;

        response.setContentType("application/pgn");
        response.setHeader("Content-Disposition", "attachment; filename=" + game.getWhitePlayer().getAssocMember().getUsername()
                + '_' + game.getBlackPlayer().getAssocMember().getUsername() + '_' + game.getTournament().getShortName() + ".pgn");
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
        wikiTable.append("<p>All games can be downloaded here - <a href=\"/tourney/pgn/all/"+games.get(0).getTournament().getShortName()+"\">"+games.get(0).getTournament().getShortName()
        +".pgn</a></p>");
        wikiTable.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
        wikiTable.append("<tr>\n" +
                "         <th>No</th>\n" +
                "         <th width=\"150px\">White player</th>\n" +
                "         <th width=\"125px\">Result</th>\n" +
                "         <th width=\"150px\">Black player</th>\n" +
                "         <th></th>\n" +
                "      </tr>");

        int i = 1;
        for (TournamentGame game : games) {
            wikiTable.append("<tr>\n" + "         <td>").append(i++).append("</td>\n").append("         <td><img src=\"/static/images/flags/").append(game.getWhitePlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getWhitePlayer().getAssocMember().getUsername()).append("</td>\n").append("         <td><center>").append(game.getResult()).append("</center></td>\n").append("         <td><img src=\"/static/images/flags/").append(game.getBlackPlayer().getAssocMember().getCountry()).append(".png\" border=\"0\"> ").append(game.getBlackPlayer().getAssocMember().getUsername()).append("</td>\n").append("         <td><a href=\"/tourney/pgn/").append(game.getGameForumString()).append("\">pgn</a></td></tr>");
        }
        wikiTable.append("</table>");

        modelMap.addAttribute("title", "Completed games for " + games.get(0).getTournament().getFullName());
        modelMap.addAttribute("error", wikiTable.toString());
        return "error";
    }

    @RequestMapping(value = "/forum/{forumString}", method = RequestMethod.GET)
    public String tourneyForumGet(@PathVariable String forumString, ModelMap modelMap) {
        TournamentGame game = tourneyDAO.getGameByForumString(forumString);
        System.out.println("game " + game);
        if (game == null)
            return "not-found";

        StringBuilder badTimes = new StringBuilder();
        StringBuilder whitePlayerRow = this.getPlayerTimesRow(game.getWhitePlayer().getAssocMember().getUsername(), memberDAO.getInsistData(game.getWhitePlayer().getAssocMember().getInsist()));
        badTimes.append(whitePlayerRow);
        StringBuilder blackPlayerRow = this.getPlayerTimesRow(game.getBlackPlayer().getAssocMember().getUsername(), memberDAO.getInsistData(game.getBlackPlayer().getAssocMember().getInsist()));
        badTimes.append(blackPlayerRow);


        String propTime = UsefulMethods.recommendTime(game.getWhitePlayer().getAssocMember().getPreference(),
                game.getBlackPlayer().getAssocMember().getPreference());
        modelMap.addAttribute("title", game.toString());
        modelMap.addAttribute("tourneyShort", forumString.split(":")[0]);
        modelMap.addAttribute("proposedTime", propTime);
        modelMap.addAttribute("badTimes", badTimes.toString());
        modelMap.addAttribute("htmlText", game.getGameforumHtml());
        modelMap.addAttribute("round", game.getRound());
        return "gameforum";
    }    

    @RequestMapping(value = "/signup/{tourneyShortName}", method = RequestMethod.POST)
    public String tourneySignupPost(@PathVariable String tourneyShortName, ModelMap modelMap) {
        Tournament tournament = tourneyDAO.getByShortName(tourneyShortName);
        if (tournament == null)
            return "not-found";

        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((Member) user).getUsername(); // Assuming getUsername() exists in Member class

        // Define a map of usernames to their corresponding fixed ratings
        Map<String, Integer> ratingsMap = new HashMap<>();
        ratingsMap.put("Technetium", 2546);
        ratingsMap.put("Maras", 2226);
        ratingsMap.put("mindlin", 2079);
        ratingsMap.put("AsDaGo", 2051);
        ratingsMap.put("HaStudent", 2003);
        ratingsMap.put("pchesso", 1984);
        ratingsMap.put("mccannj", 1894);
        ratingsMap.put("morphology", 1806);
        ratingsMap.put("carlosvalero", 1762);
        ratingsMap.put("WitPion", 1517);
        ratingsMap.put("Bodia", 1432);
        ratingsMap.put("IAHMCOL", 1304);

        // Determine the fixed rating for the player, default to 0 if not in the list
        int fixedRating = ratingsMap.getOrDefault(username, 0);

        TournamentPlayer player = new TournamentPlayer();
        player.setFixedRating(fixedRating);
        player.setAssocMember(((Member) user));
        player.setEmailForum(true);
        player.setTournament(tournament);
        player.setTourneyGroup("");
        tourneyDAO.storePlayer(player);

        return "redirect:/tourney/signup/" + tourneyShortName;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String tourneyProcessPost(@RequestParam(value = "Full_name") String fullName, @RequestParam(value = "Short_name") String shortName,
                                     @RequestParam(value = "Max") String max, @RequestParam(value = "From") String from,
                                     @RequestParam(value = "To") String to, @RequestParam(value = "Start") String start,
                                     @RequestParam(value = "End") String end,
                                     ModelMap modelMap) {
        Tournament newTourn = new Tournament();
        newTourn.setFullName(fullName);
        newTourn.setShortName(shortName);
        newTourn.setMaxCount(Integer.parseInt(max));
        try {
            newTourn.setSignupFrom(formatter.parse(from+" 3:00"));
            newTourn.setSignupTo(formatter.parse(to+" 3:00"));
            newTourn.setStartDate(formatter.parse(start+" 3:00"));
            newTourn.setEndDate(formatter.parse(end+" 3:00"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tourneyDAO.store(newTourn);

        modelMap.addAttribute("title", "Sucessfull tourney creation");
        modelMap.addAttribute("error", "Your tourney signup page has been created at <a href=\"/tourney/signup/" + shortName + "\">/tourney/" + shortName + "/signup</a>.");
        return "error";
    }
    
    private StringBuilder getPlayerTimesRow(String playerName, InsistData insistDataA) {
    	List<String> badTimesA = Arrays.asList(insistDataA.getBadTimes().split(","));
        List<String> hardTimesA = Arrays.asList(insistDataA.getHardTimes().split(","));
    	StringBuilder rowBuilder = new StringBuilder();
    	rowBuilder.append("<tr><td><b>").append(playerName).append("</b></td>");
    	
        for (int i = 1; i <= 24; i++) {
            if (badTimesA.contains(Integer.toString(i))) {
                rowBuilder.append("<td bgcolor=\"red\">");
            } else if (hardTimesA.contains(Integer.toString(i))) {
            	rowBuilder.append("<td bgcolor=\"orange\">");
            } else {
                rowBuilder.append("<td>");
            }
            rowBuilder.append(i).append("</td>");
        }
        rowBuilder.append("</tr>");
        
        return rowBuilder;
    }
}
