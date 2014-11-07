package net.rwchess.utils;

import cz.hejl.chesswalk.ChessClient;
import cz.hejl.chesswalk.LoginException;
import net.rwchess.persistent.TournamentPlayer;
import net.rwchess.persistent.dao.TourneyDAO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckRatingsService {

    private TourneyDAO tourneyDAO;

    public CheckRatingsService() {
    }

    public CheckRatingsService(TourneyDAO tourneyDAO) {
        this.tourneyDAO = tourneyDAO;
    }

    public void checkRatings(final String tourney) {
        Runnable checkRunnable = new Runnable() {
            @Override
            public void run() {
                List<TournamentPlayer> players = tourneyDAO.getAllPlayersList(tourney);

                ChessClient.RatingListener ratingListener = new ChessClient.RatingListener() {
                    @Override
                    public void ratingChecked(String player, int rating) {
                        tourneyDAO.updateRating(player, rating);
                    }
                };

                List<String> usernames = new ArrayList<String>();

                for (TournamentPlayer player : players) {
                    usernames.add(player.getAssocMember().getUsername());
                }

                ChessClient chessClient = new ChessClient(ratingListener, usernames);
                try {
                    chessClient.login("guest", "");
                    for (String player : usernames) {
                        chessClient.finger(player);
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                        }
                    }
                    chessClient.cancel();

                } catch (LoginException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(checkRunnable).start();
    }
}
