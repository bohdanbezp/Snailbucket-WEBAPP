package net.rwchess.services;

import net.rwchess.persistent.Bucket;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.TournamentPlayer;
import net.rwchess.utils.UsefulMethods;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;

import java.util.ArrayList;
import java.util.List;

public class PythonStandingsService {

    private String pythonDir;

    public PythonStandingsService(String pythonDir) {
        this.pythonDir = pythonDir;
    }

    public static class StandingRecord {
        public int games;
        public double points;
        public double hth;
        public int won;
        public int white;
        public int rating;
        public TournamentPlayer player;
    }

    public List<StandingRecord> generateStandings(List<TournamentGame> gamesWithResult, List<TournamentPlayer> players,
                                                  Bucket bucket) {

        StringBuilder inputArr = new StringBuilder("[");

        for (TournamentPlayer player : players) {
            if (bucket.getPlayerList().contains(player)) {
                inputArr.append("('").append(player.getAssocMember().getUsername()).append("', ").append(player.getFixedRating()).append("),");
            }
        }
        inputArr.append(']');

        StringBuilder gameArr = new StringBuilder("[");
        for (TournamentGame game : gamesWithResult) {
            if (bucket.getPlayerList().contains(game.getWhitePlayer())) {
                float whiteScore = 0.0f;
                float blackScore = 0.0f;
                if (game.getResult().equals("1-0") || game.getResult().equals("+:-"))
                    whiteScore = 1f;
                else if (game.getResult().equals("0-1") || game.getResult().equals("-:+"))
                    blackScore = 1f;
                else if (game.getResult().equals("1/2-1/2") || game.getResult().equals("0.5-0.5")) {
                    whiteScore = 0.5f;
                    blackScore = 0.5f;
                }

                gameArr.append("('" + game.getWhitePlayer().getAssocMember().getUsername() + "', '" + game.getBlackPlayer().getAssocMember().getUsername() + "', " +
                        whiteScore + ", " + blackScore + "),");
            }
        }
        gameArr.append(']');

        PythonInterpreter interp =
                new PythonInterpreter();

        interp.execfile(pythonDir + "standings.py");
        interp.exec("table = calculate_standings(" + inputArr + "," + gameArr + ")");

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

        return records;
    }
}
