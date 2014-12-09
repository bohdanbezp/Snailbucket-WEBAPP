package net.rwchess.services;


import net.rwchess.persistent.Bucket;
import net.rwchess.persistent.Tournament;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.TournamentPlayer;
import net.rwchess.utils.UsefulMethods;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class PythonPairingsService {

    @Autowired
    private ServletContext servletContext;

    public List<TournamentGame> allRoundsGame(Bucket bucket, Tournament tournament,
                                              Random random) {
        List<TournamentPlayer> playerListOrig = bucket.getPlayerList();
        List<TournamentPlayer> playerList = new ArrayList<TournamentPlayer>(playerListOrig.size());
        for (TournamentPlayer player : playerListOrig) {
            playerList.add(player);
        }

        Collections.shuffle(playerList, random);
        StringBuilder players = new StringBuilder();
        for (TournamentPlayer player : playerList) {
            players.append('\'').append(player.getAssocMember().getUsername()).append("',");
        }

        PythonInterpreter interp =
                new PythonInterpreter();

        String pythonDir = servletContext.getRealPath("/WEB-INF/python");
       // interp.execfile(is);
        interp.execfile(pythonDir + "/pairings.py");
        interp.exec("pairings = generate([" + players + "])");

        int roundsCount = interp.eval("len(pairings)").asInt();

        List<TournamentGame> games = new ArrayList<TournamentGame>();

        for (int i = 1; i <= roundsCount; i++) {
            PyList pyBucket = new PyList(interp.eval("pairings[" + i + ']'));
            PyObject[] tupleArray = pyBucket.getArray();
            for (PyObject tuple : tupleArray) {
                PyTuple pyTuple = (PyTuple) tuple;

                try {
                    String white = pyTuple.getArray()[0].toString();
                    String black = pyTuple.getArray()[1].toString();
                    if (white.contains("BYE") || black.contains("BYE"))
                        continue;

                    TournamentGame game = new TournamentGame();
                    game.setTournament(tournament);
                    game.setWhitePlayer(UsefulMethods.findByName(playerList, white));
                    game.setBlackPlayer(UsefulMethods.findByName(playerList, black));
                    game.setGameforumHtml("");
                    game.setRound(i);
                    games.add(game);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
        //IOUtils.closeQuietly(is);
        return games;
    }
}
