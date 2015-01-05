package net.rwchess.persistent.dao;

import net.rwchess.persistent.Member;
import net.rwchess.persistent.Tournament;
import net.rwchess.persistent.TournamentGame;
import net.rwchess.persistent.TournamentPlayer;
import net.rwchess.utils.HibernateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TourneyDAOHib implements TourneyDAO {
    @Override
    public void store(Tournament tournament) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(tournament);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void storePlayer(TournamentPlayer player) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(player);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public Tournament getByShortName(String shortName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Tournament M WHERE M.shortName = :shortName";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortName);

        Tournament res = null;
        try {
            res = (Tournament) query.list().get(0);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public List<Tournament> getAllTourneys() {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Tournament M";
        Query query = session.createQuery(hql);

        List<Tournament> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public List<TournamentPlayer> getAllPlayersList(String shortName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Tournament M WHERE M.shortName = :shortName";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortName);

        List<Tournament> tournaments = query.list();
        if (tournaments == null || tournaments.isEmpty())
            return null;

        Tournament tournament = tournaments.get(0);

        hql = "FROM TournamentPlayer M WHERE M.tournament = :tournament";
        query = session.createQuery(hql);
        query.setParameter("tournament", tournament);

        List<TournamentPlayer> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public List<TournamentPlayer> getAllPlayersListSorted(String shortName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Tournament M WHERE M.shortName = :shortName";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortName);
        Tournament tournament = (Tournament) query.list().get(0);

        hql = "FROM TournamentPlayer M WHERE M.tournament = :tournament  order by M.fixedRating desc";
        query = session.createQuery(hql);
        query.setParameter("tournament", tournament);

        List<TournamentPlayer> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public void updateRating(String username, int rating) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentPlayer M WHERE M.assocMember.username = :username";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);

        TournamentPlayer m = null;
        try {
            m = (TournamentPlayer) query.list().get(0);
            m.setFixedRating(rating);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    Pattern gameForumPattern = Pattern.compile("([^:]+):R([0-9]+)_(.*)-(.*)");

    @Override
    public TournamentGame getGameByForumString(String forumString) {
        Matcher m = gameForumPattern.matcher(forumString);
        if (m.matches()) {
            String tourneyShort = m.group(1);
            int round = Integer.parseInt(m.group(2));
            String white = m.group(3);
            String black = m.group(4);

            Session session = HibernateUtils.getInstance().openSession();
            Transaction transaction = session.beginTransaction();
            String hql = "FROM Tournament M WHERE M.shortName = :shortName";
            Query query = session.createQuery(hql);
            query.setParameter("shortName", tourneyShort);
            Tournament tournament = (Tournament) query.list().get(0);

            hql = "FROM TournamentPlayer M WHERE M.assocMember.username = :username";
            query = session.createQuery(hql);
            query.setParameter("username", white);
            TournamentPlayer whitePlayer = (TournamentPlayer) query.list().get(0);

            hql = "FROM TournamentPlayer M WHERE M.assocMember.username = :username";
            query = session.createQuery(hql);
            query.setParameter("username", black);
            TournamentPlayer blackPlayer = (TournamentPlayer) query.list().get(0);

            hql = "FROM TournamentGame M WHERE M.tournament = :tournament and " +
                    "M.round = :round and M.whitePlayer = :whitePlayer and " +
                    "M.blackPlayer = :blackPlayer";
            query = session.createQuery(hql);
            query.setParameter("tournament", tournament);
            query.setParameter("round", round);
            query.setParameter("whitePlayer", whitePlayer);
            query.setParameter("blackPlayer", blackPlayer);

            TournamentGame res = null;
            try {
                res = (TournamentGame) query.list().get(0);
                transaction.commit();
            } catch (IndexOutOfBoundsException e) {
                transaction.rollback();
            } finally {
                session.close();
            }
            return res;
        }

        return null;
    }

    @Override
    public boolean tourneyHasPairings(String shortName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Tournament M WHERE M.shortName = :shortName";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortName);
        Tournament tournament = (Tournament) query.list().get(0);

        hql = "FROM TournamentGame M WHERE M.tournament = :tournament";
        query = session.createQuery(hql);
        query.setParameter("tournament", tournament);

        boolean res = false;
        try {
            res = !query.list().isEmpty();
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public void storeGame(TournamentGame game) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(game);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateScheduledDate(TournamentGame game, Date scheduled) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setSecheduled(scheduled);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateHtml(TournamentGame game, String html) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setGameforumHtml(html);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateResult(TournamentGame game, String result) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setResult(result);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateWhiteLastPost(TournamentGame game, Date date) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setWhiteLastPost(date);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateBlackLastPost(TournamentGame game, Date date) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setBlackLastPost(date);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updatePlayedDate(TournamentGame game, Date date) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setPlayed(date);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateInitContReminderSent(TournamentGame game, boolean val) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setInitContReminderSent(val);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateFirstReminderSent(TournamentGame game, boolean val) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setFirstReminder(val);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updatePreGameReminderSent(TournamentGame game, boolean val) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setPreGameReminderSent(val);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updatePgn(TournamentGame game, String png) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", game.getKey());

        TournamentGame m = null;
        try {
            m = (TournamentGame) query.list().get(0);
            m.setPng(png);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public boolean isSignedUp(Member member) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentPlayer M WHERE M.assocMember = :assocMember";
        Query query = session.createQuery(hql);
        query.setParameter("assocMember", member);

        List<TournamentPlayer> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        if (res == null)
            return false;

        return !res.isEmpty();
    }

    @Override
    public List<TournamentGame> getGamesForRound(String shortTourneyName, int round) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.tournament.shortName = :shortName and M.round = :round";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortTourneyName);
        query.setParameter("round", round);

        List<TournamentGame> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public List<TournamentGame> getGamesByDate(String shortTourneyName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.tournament.shortName = :shortName and M.secheduled <> null and " +
                "M.result = null" +
                " order by M.secheduled asc";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortTourneyName);

        List<TournamentGame> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public List<TournamentGame> getGamesByResult(String shortTourneyName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.tournament.shortName = :shortName and M.result <> null";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortTourneyName);

        List<TournamentGame> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public List<TournamentGame> getGamesByPgn(String shortTourneyName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.tournament.shortName = :shortName and M.png <> null";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortTourneyName);

        List<TournamentGame> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public List<TournamentGame> getGamesForTourney(String shortTourneyName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM TournamentGame M WHERE M.tournament.shortName = :shortName";
        Query query = session.createQuery(hql);
        query.setParameter("shortName", shortTourneyName);

        List<TournamentGame> res = null;
        try {
            res = query.list();
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    public void initTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }
}
