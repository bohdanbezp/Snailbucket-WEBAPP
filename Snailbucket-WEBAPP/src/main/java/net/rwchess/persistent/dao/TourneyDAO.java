package net.rwchess.persistent.dao;

import net.rwchess.persistent.Member;
import net.rwchess.persistent.Tournament;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.TournamentPlayer;

import java.util.Date;
import java.util.List;

/**
 * Created by bodia on 10/18/14.
 */
public interface TourneyDAO {
    public void store(Tournament tournament);
    public void storePlayer(TournamentPlayer player);

    public Tournament getByShortName(String shortName);

    public List<Tournament> getAllTourneys();

    public List<TournamentPlayer> getAllPlayersList(String shortName);

    public List<TournamentPlayer> getAllPlayersListSorted(String shortName);

    public boolean isSignedUp(Member member);

    public List<TournamentGame> getGamesForRound(String shortTourneyName, int round);
    public List<TournamentGame> getGamesByDate(String shortTourneyName);
    public List<TournamentGame> getGamesByResult(String shortTourneyName);
    public List<TournamentGame> getGamesByPgn(String shortTourneyName);
    public List<TournamentGame> getGamesForTourney(String shortTourneyName);

    public void updateRating(String username, int rating);

    public TournamentGame getGameByForumString(String forumString);

    public boolean tourneyHasPairings(String shortName);

    public void storeGame(TournamentGame game);

    public void updateScheduledDate(TournamentGame game, Date scheduled);
    public void updateHtml(TournamentGame game, String html);
    public void updatePgn(TournamentGame game, String png);

    public void updateResult(TournamentGame game, String result);

    public void updateWhiteLastPost(TournamentGame game, Date date);
    public void updateBlackLastPost(TournamentGame game, Date date);

    public void updateInitContReminderSent(TournamentGame game, boolean val);
    public void updateFirstReminderSent(TournamentGame game, boolean val);
    public void updatePreGameReminderSent(TournamentGame game, boolean val);
}
