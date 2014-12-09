package net.rwchess.services;

import chesspresso.game.Game;
import chesspresso.move.Move;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.dao.TourneyDAO;
import net.rwchess.utils.UsefulMethods;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by bodia on 12/2/14.
 */
public class DisplayPositionService {

    public static PosInfo info;

    public static class PosInfo {
        public String lastMove;
        public String fen;
        public String gameString;
        public String forumString;

        public String toString() {
            return "lastMove=" + lastMove + " fen=" + fen + " gameString="+gameString +
                    " forumString="+ forumString;
        }
    }

    private static DisplayPositionService instance;
    private TourneyDAO tourneyDAO;

    private DisplayPositionService(TourneyDAO tourneyDAO) {
        if (tourneyDAO != null)
            this.tourneyDAO = tourneyDAO;
    }

    public static DisplayPositionService getInstance(TourneyDAO tourneyDAO) {
        if (instance == null)
            instance = new DisplayPositionService(tourneyDAO);

        return instance;
    }

    public void run() {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                Process p = null;
                try {
                    info = getRandomFen();
                    String comm = "python2 /home/bodia/server/jetty/webapps/ROOT/WEB-INF/python/imagePos.py " +
                            ""+info.fen.split(" ")[0]+" /home/bodia/server/jetty/webapps/ROOT/wikiImg/fen.png /home/bodia/server/jetty/webapps/ROOT/WEB-INF/python/";
                    p = Runtime.getRuntime().exec(comm);
                    p.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (PGNSyntaxError pgnSyntaxError) {
                    pgnSyntaxError.printStackTrace();
                }
            }
        };

        UsefulMethods.utilExecutor.scheduleAtFixedRate(r, 0, 12, TimeUnit.HOURS);
    }

    private PosInfo getRandomFen() throws IOException, PGNSyntaxError {
        List<TournamentGame> games = tourneyDAO.getGamesByPgn("bucket1");
        StringBuilder sb = new StringBuilder();
        for (TournamentGame game: games) {
            sb.append(game.getPng()).append("\n\n");
        }

        PGNReader red = new PGNReader(IOUtils.toInputStream(sb), "bucket1.pgn");
        List<Game> gamesList = new ArrayList<Game>();
        Game game = red.parseGame();
        while (game != null) {
            int movesN = game.getNumOfMoves();
            if (movesN < 15) {
                game = red.parseGame();
                continue;
            }

            gamesList.add(game);

            game = red.parseGame();
        }

        Random rnd = new Random();

        Game selGame = gamesList.get(rnd.nextInt(gamesList.size()-1));

        selGame.gotoNode(rnd.nextInt(2*selGame.getNumOfMoves()-16)+16);

        PosInfo posInfo = new PosInfo();
        posInfo.lastMove = selGame.getCurrentMoveNumber() + (selGame.getLastMove().isWhiteMove() ? "." : "...") + selGame.getLastMove().getSAN();
        posInfo.fen = selGame.getPosition().getFEN();
        posInfo.gameString = "Round " + selGame.getRound() + ":<br/>"  + selGame.getWhite() + " vs. " + selGame.getBlack();
        posInfo.forumString = "bucket1:R" + selGame.getRound() + '_' + selGame.getWhite() + '-' + selGame.getBlack();

        return posInfo;

    }
}
