package net.rwchess.persistent.dao;

import net.rwchess.persistent.TournBye;
import java.util.List;

/**
 * DAO interface for managing BYE entries for tournaments.
 */
public interface TournByeDAO {
    public void store(TournBye tournBye);

    public List<TournBye> getByTourneyShortName(String shortName);

    public List<TournBye> getByTourneyShortNameAndRound(String shortName, int round);
}
