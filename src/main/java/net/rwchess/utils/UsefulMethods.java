package net.rwchess.utils;

import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.game.GameListener;
import chesspresso.move.Move;
import chesspresso.pgn.PGN;
import info.bliki.wiki.model.WikiModel;
import net.rwchess.persistent.Bucket;
import net.rwchess.persistent.Member;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.TournamentPlayer;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by bodia on 10/12/14.
 */
public final class UsefulMethods {

    public static ScheduledExecutorService utilExecutor = Executors.newScheduledThreadPool(2);

    private static final WikiModel wikiModel = new WikiModel(
            "/wikiImg/${image}",
            "/wiki/${title}") {
        public boolean isMathtranRenderer() {
            return true;
        }
    };

    private static DateTimeFormatter dateFormat;

    private UsefulMethods() {
    } // provides non-instensability

    /**
     * Gets a plain password string and returns MD5 hash
     */
    public static String getMD5(String passwd) {
        return DigestUtils.md5Hex(passwd.getBytes());
    }

    public static String getHtml(String rawText) {
        return wikiModel.render(rawText);
    }

    public static DateTimeFormatter getWikiDateFormatter() {
        if (dateFormat == null) {
            dateFormat = DateTimeFormat.forPattern("H:mm, d MMMMM yyyy z")
                    .withLocale(Locale.US);
        }

        return dateFormat;
    }


    public static TournamentPlayer findByName(List<TournamentPlayer> players, String username) {
        for (TournamentPlayer player : players) {
            if (player.getAssocMember().getUsername().equalsIgnoreCase(username))
                return player;
        }

        return null;
    }

    public static List<List<TournamentPlayer>> getBuckets(List<TournamentPlayer> players, int plsInBucket) {
        int buckets = players.size() / plsInBucket + 1;

        List<List<TournamentPlayer>> metaBuckets = new ArrayList<List<TournamentPlayer>>();

        int plIndex = 0;
        for (int i = 0; i < buckets; i++) {
            List<TournamentPlayer> bucket = new ArrayList<TournamentPlayer>();

            for (int j = 0; j < plsInBucket; j++) {
                if (players.size() > plIndex)
                    bucket.add(players.get(plIndex++));
            }
            metaBuckets.add(bucket);
        }
        return metaBuckets;
    }

    public static String getMembersTableHtml(List<Member> members, List<String> aliveUsers) {
        StringBuilder buff = new StringBuilder();
        //int maxRows = members.size()/4;
        buff.append("<table border=\"0\">");
        int coloumn = 0;

        for (Member m : members) {
            if (coloumn == 0)
                buff.append("<tr>");
            else if (coloumn == 3) {
                buff.append("</tr>");
                coloumn = 0;
            }

            buff.append("<td width=\"25%\">");
            buff.append("<img src=\"/static/images/flags/" + "").append(m.getCountry()).append(".png\" border=\"0\"/> ");

            if (notContainsAlive(aliveUsers, m.getUsername())) {
                buff.append("<a href=\"/wiki/User:").append(m.getUsername()).append("\" style=\"color: RED\">").append(m.getUsername()).append("</a>");
            } else
                buff.append("<a href=\"/wiki/User:").append(m.getUsername()).append("\">").append(m.getUsername()).append("</a>");

            buff.append("</td>");
            coloumn++;
        }
        buff.append("</table>");
        return buff.toString();
    }

    public static String groupIdToString(int id) {
        if (id == 1)
            return "user";
        else if (id == 2)
            return "td";
        else if (id == 0)
            return "banned";
        else
            return "admin";
    }

    private static boolean notContainsAlive(List<String> aliveUsers, String username) {
//        for (String u: aliveUsers) {
//            if (u.equals(username))
//                return false;
//        }
//        return true;
        return false;
    }

    public static String getPgnRepresentation(Game game) {
        final StringBuffer pgnBuffer = new StringBuffer();

        pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_EVENT + ' ' + PGN.TOK_QUOTE).append(game.getEvent()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_SITE + ' ' + PGN.TOK_QUOTE).append(game.getSite()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_DATE + ' ' + PGN.TOK_QUOTE).append(game.getDate()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_ROUND + ' ' + PGN.TOK_QUOTE).append(game.getRound()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_WHITE + ' ' + PGN.TOK_QUOTE).append(game.getWhite()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_BLACK + ' ' + PGN.TOK_QUOTE).append(game.getBlack()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_RESULT + ' ' + PGN.TOK_QUOTE).append(game.getResultStr()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');

        if (game.getWhiteEloStr() != null)
            pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_WHITE_ELO + ' ' + PGN.TOK_QUOTE).append(game.getWhiteElo()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        if (game.getBlackEloStr() != null)
            pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_BLACK_ELO + ' ' + PGN.TOK_QUOTE).append(game.getBlackElo()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        if (game.getEventDate() != null)
            pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_EVENT_DATE + ' ' + PGN.TOK_QUOTE).append(game.getEventDate()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        if (game.getECO() != null)
            pgnBuffer.append(PGN.TOK_TAG_BEGIN + PGN.TAG_ECO + ' ' + PGN.TOK_QUOTE).append(game.getECO()).append(PGN.TOK_QUOTE).append(PGN.TOK_TAG_END).append('\n');
        game.gotoStart();
        // print leading comments before move 1
        String comment = game.getComment();
        if (comment != null) {
            pgnBuffer.append(PGN.TOK_COMMENT_BEGIN).append(comment).append(PGN.TOK_COMMENT_END).append(' ');
        }

        game.traverse(new GameListener() {
            private boolean needsMoveNumber = true;

            public void notifyMove(Move move, short[] nags, String comment,
                                   int plyNumber, int level) {
                if (needsMoveNumber) {
                    if (move.isWhiteMove()) {
                        pgnBuffer.append(Chess.plyToMoveNumber(plyNumber)).append('.').append(' ');
                    } else {
                        pgnBuffer.append(Chess.plyToMoveNumber(plyNumber)).append("...").append(' ');
                    }
                }
                pgnBuffer.append(move.toString()).append(' ');

                if (nags != null) {
                    for (short nag : nags) {
                        pgnBuffer.append(String.valueOf(PGN.TOK_NAG_BEGIN)).append(String.valueOf(nag)).append(' ');
                    }
                }
                if (comment != null)
                    pgnBuffer.append(PGN.TOK_COMMENT_BEGIN).append(comment).append(PGN.TOK_COMMENT_END).append(' ');
                needsMoveNumber = !move.isWhiteMove() || (comment != null);
            }

            public void notifyLineStart(int level) {
                pgnBuffer.append(String.valueOf(PGN.TOK_LINE_BEGIN));
                needsMoveNumber = true;
            }

            public void notifyLineEnd(int level) {
                pgnBuffer.append(String.valueOf(PGN.TOK_LINE_END)).append(' ');
                needsMoveNumber = true;
            }
        }, true);

        pgnBuffer.append(game.getResultStr());

        return pgnBuffer.toString();
    }

    private static String[] sortables = {
            "<li id=\"45_45\" class=\"ui-state-default\"> 45 45  <img src=\"/static/images/clock.png\"/></li>\n",
            "<li id=\"120_30\" class=\"ui-state-default\">120 30 <img src=\"/static/images/clock.png\"/></li>\n",
            "<li id=\"90_30\" class=\"ui-state-default\"> 90 30  <img src=\"/static/images/clock.png\"/></li>\n",
            "<li id=\"75_0\" class=\"ui-state-default\"> 75 0   <img src=\"/static/images/clock.png\"/></li>\n",
            "<li id=\"50_10\" class=\"ui-state-default\"> 50 10  <img src=\"/static/images/clock.png\"/></li>"
    };

    public static Bucket getBucket(List<Bucket> buckets, TournamentGame game) {
        for (Bucket bucket: buckets) {
            if (bucket.getPlayerList().contains(game.getWhitePlayer())
                    || bucket.getPlayerList().contains(game.getBlackPlayer()))
                return bucket;
        }
        return null;
    }

    /*
    http://snailbucket.org/wiki/Matching_time_controls_algorithm
     */
    public static String recommendTime(String player1Pref, String player2Pref) {
        Set<String> A = new HashSet<String>();
        Set<String> B = new HashSet<String>();
        List<String> listA = Arrays.asList(player1Pref.trim().replaceAll("45 45", "45_45").replaceAll(" ", "").split(","));
        List<String> listB = Arrays.asList(player2Pref.trim().replaceAll("45 45", "45_45").replaceAll(" ", "").split(","));
        for (String str : listA) {
            A.add(str.trim().replaceAll("^\\s+", ""));
        }

        for (String str : listB) {
            B.add(str.trim().replaceAll("^\\s+", ""));
        }

        A.retainAll(B); // intersect

        String bestTc = "";
        int bestVal = Integer.MAX_VALUE;
        for (String tc : A) {
            int of = listA.indexOf(tc) + listB.indexOf(tc);
            if (of < bestVal) {
                bestVal = of;
                bestTc = tc;
            } else if (of == bestVal) {
                if (Integer.parseInt(tc.replaceAll("75_0", "75_00").replaceAll("_", ""))
                        < Integer.parseInt(bestTc.replaceAll("75_0", "75_00").replaceAll("_", ""))) {
                    bestVal = of;
                    bestTc = tc;
                }
            }
        }
        return bestTc;
    }

    public static int getCurrentRound() {
        DateTime dateTime = new DateTime(2014, 11, 4, 19, 0, DateTimeZone.forID("America/Los_Angeles"));

        return Weeks.weeksBetween(dateTime, DateTime.now(DateTimeZone.forID("America/Los_Angeles"))).getWeeks() + 1;
    }

    public static List<String> sortToSortables(String preference) {
        StringBuilder sortable1 = new StringBuilder();
        StringBuilder sortable2 = new StringBuilder();
        for (String pref : preference.split(",")) {
            pref = pref.replaceAll("^\\s+", "").trim();
            if (pref.equals("120_30"))
                sortable1.append(sortables[1]);
            else if (pref.equals("90_30"))
                sortable1.append(sortables[2]);
            else if (pref.equals("75_0"))
                sortable1.append(sortables[3]);
            else if (pref.equals("50_10"))
                sortable1.append(sortables[4]);
            else if (pref.equals("45_45"))
                sortable1.append(sortables[0]);
        }

        if (!preference.contains("120_30"))
            sortable2.append(sortables[1]);
        if (!preference.contains("90_30"))
            sortable2.append(sortables[2]);
        if (!preference.contains("75_0"))
            sortable2.append(sortables[3]);
        if (!preference.contains("50_10"))
            sortable2.append(sortables[4]);

        if (sortable1.toString().isEmpty())
            sortable1.append(sortables[0]);

        return Arrays.asList(sortable1.toString(), sortable2.toString());
    }

    public static List<TournamentGame> getBucketGames(Bucket bucket, List<TournamentGame> games) {
        List<TournamentGame> result = new ArrayList<TournamentGame>();

        for (TournamentGame game : games) {
            if (findByName(bucket.getPlayerList(), game.getWhitePlayer().getAssocMember().getUsername()) != null
                    || findByName(bucket.getPlayerList(), game.getBlackPlayer().getAssocMember().getUsername()) != null)
                result.add(game);
        }

        return result;
    }

    public static String insistToSelectable(String insist) {
        StringBuilder content = new StringBuilder();
        List<String> insis = Arrays.asList(insist.split(","));
        List<String> insists = new ArrayList<String>(insis.size());
        for (String str : insis) {
            insists.add(str.replaceAll("^\\s+", "").trim());
        }

        for (int i = 1; i <= 24; i++) {
            String app = "";
            if (insists.contains(Integer.toString(i)))
                app = " ui-selected";

            content.append("<li class=\"ui-widget-content").append(app).append("\">").append(i).append("</li>");
        }
        return content.toString();
    }
}
