package net.rwchess.services;

import net.rwchess.persistent.TournBye;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.TournamentPlayer;
import net.rwchess.utils.UsefulMethods;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import java.util.*;
import java.util.stream.Collectors;

public class PythonStandingsService {

    @Autowired
    private ServletContext servletContext;

    public static class StandingRecord {
        public int games;
        public double points;
        public double hth;
        public int won;
        public int white;
        public int rating;
        public TournamentPlayer player;

        public int getGames() {
            return games;
        }

        public double getPoints() {
            return points;
        }

        public double getHth() {
            return hth;
        }

        public int getWon() {
            return won;
        }

        public int getWhite() {
            return white;
        }

        public int getRating() {
            return rating;
        }

        public TournamentPlayer getPlayer() {
            return player;
        }
    }


    public List<StandingRecord> generateStandings(
            List<TournamentGame> gamesWithResult,
            List<TournamentGame> allGames,
            List<TournamentPlayer> players,
            List<TournBye> byes
    ) {
        int totalRounds = UsefulMethods.getLastRound(allGames);
        StringBuilder inputArr = new StringBuilder("[");

        // Initialize absences for all players
        for (TournamentPlayer player : players) {
            inputArr.append("('").append(player.getAssocMember().getUsername()).append("', ").append(player.getFixedRating()).append("),");
        }
        inputArr.append(']');

        StringBuilder gameArr = new StringBuilder("[");
        for (TournamentGame game : gamesWithResult) {
            float whiteScore = 0.0f;
            float blackScore = 0.0f;
            if (game.getResult().equals("1-0") || game.getResult().equals("+:-"))
                whiteScore = 1f;
            else if (game.getResult().equals("0-1") || game.getResult().equals("-:+"))
                blackScore = 1f;
            else if (game.getResult().equals("1/2-1/2") || game.getResult().equals("0.5-0.5")
                    || game.getResult().equals("i/2-i/2")) {
                whiteScore = 0.5f;
                blackScore = 0.5f;
            }

            gameArr.append("('").append(game.getWhitePlayer().getAssocMember().getUsername()).append("', '").append(game.getBlackPlayer().getAssocMember().getUsername()).append("', ").append(whiteScore).append(", ").append(blackScore).append("),");
        }
        gameArr.append(']');

        PythonInterpreter interp = new PythonInterpreter();

        String pythonDir = servletContext.getRealPath("/WEB-INF/python");
        interp.execfile(pythonDir + "/standings.py");
        interp.exec("table = calculate_standings(" + inputArr + ',' + gameArr + ')');

        int recCount = interp.eval("len(table)").asInt();

        List<StandingRecord> records = new ArrayList<StandingRecord>();
        PyList list = new PyList(interp.eval("table"));
        PyObject[] arr = list.getArray();
        for (int i = 0; i < recCount; i++) {
            PyTuple pyTuple = (PyTuple) arr[i];
            StandingRecord record = new StandingRecord();
            record.player = UsefulMethods.findByName(players, pyTuple.getArray()[0].toString());
            record.games = pyTuple.getArray()[1].asInt();
            record.points = pyTuple.getArray()[2].asDouble();
            record.hth = pyTuple.getArray()[3].asDouble();
            record.won = pyTuple.getArray()[4].asInt();
            record.white = pyTuple.getArray()[5].asInt();
            record.rating = pyTuple.getArray()[6].asInt();

            records.add(record);
        }

        for (TournamentGame game: gamesWithResult) {
            String result = game.getResult();
            boolean isAdjudicated = result.equals("+:-") || result.equals("-:+") || result.equals("i/2-i/2");

            StandingRecord whitePlayerRecord = findRecordWhitePlayerByGame(records, game);
            StandingRecord blackPlayerRecord = findRecordBlackPlayerByGame(records, game);

            if (isAdjudicated) {
                whitePlayerRecord.games -= 1;
                blackPlayerRecord.games -= 1;
                whitePlayerRecord.white -= 1;
                if (result.equals("+:-")) {
                    whitePlayerRecord.won -= 1;
                } else if (result.equals("-:+")) {
                    blackPlayerRecord.won -= 1;
                }
            }
        }

        for (TournBye bye : byes) {
            String playerName = bye.getPlayer().getAssocMember().getUsername();
            StandingRecord record = findRecordByPlayerName(records, playerName);  // Helper method to find record by player name
            if (record != null) {
                if ("FULL".equalsIgnoreCase(bye.getByeType())) {
                    record.points += 1.0;  // Add 1 point for FULL BYE
                } else if ("HALF".equalsIgnoreCase(bye.getByeType())) {
                    record.points += 0.5;  // Add 0.5 point for HALF BYE
                }
            }
        }

        return sortStandings(records);
    }

    private StandingRecord findRecordByPlayerName(List<StandingRecord> records, String playerName) {
        for (StandingRecord record : records) {
            if (record.player.getAssocMember().getUsername().equals(playerName)) {
                return record;
            }
        }
        return null;
    }

    public StandingRecord findRecordWhitePlayerByGame(List<StandingRecord> records, TournamentGame game) {
        String whitePlayerName = game.getWhitePlayer().getAssocMember().getUsername();

        for (StandingRecord record : records) {
            String recordPlayerName = record.player.getAssocMember().getUsername();

            if (recordPlayerName.equals(whitePlayerName)) {
                return record;
            }
        }
        return null;
    }

    public StandingRecord findRecordBlackPlayerByGame(List<StandingRecord> records, TournamentGame game) {
        String blackPlayerName = game.getBlackPlayer().getAssocMember().getUsername();

        for (StandingRecord record : records) {
            String recordPlayerName = record.player.getAssocMember().getUsername();

            if (recordPlayerName.equals(blackPlayerName)) {
                return record;
            }
        }
        return null;
    }


    public List<StandingRecord> sortStandings(List<StandingRecord> records) {
        // Sort the records list based on Swiss tournament logic without color preference
        records.sort(Comparator
                // Sort by points in descending order
                .comparingDouble(StandingRecord::getPoints)
                // If points are the same, sort by head-to-head (H2H)
                .thenComparingDouble(StandingRecord::getHth)
                // If H2H is the same, sort by number of games won
                .thenComparingInt(StandingRecord::getWon)
                // If won games are the same, sort by rating
                .thenComparingInt(StandingRecord::getRating));

        Collections.reverse(records);
        return records;
    }

}
