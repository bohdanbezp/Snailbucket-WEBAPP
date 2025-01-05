package net.rwchess.persistent.dao;

import net.rwchess.persistent.TournBye;
import net.rwchess.utils.HibernateUtils;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

/**
 * Hibernate implementation of TournByeDAO for handling tournament BYEs.
 */
public class TournByeDAOHib implements TournByeDAO {

    @Override
    public void store(TournBye tournBye) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(tournBye);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public List<TournBye> getByTourneyShortName(String shortName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        List<TournBye> result = null;

        try {
            String hql = "FROM TournBye tb WHERE tb.tournament IN (SELECT t FROM Tournament t WHERE t.shortName = :shortName)";
            Query query = session.createQuery(hql);
            query.setParameter("shortName", shortName);
            result = query.list();

            // Initialize lazy-loaded entities while session is still open
            for (TournBye bye : result) {
                Hibernate.initialize(bye.getPlayer().getAssocMember());
            }

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        return result;
    }


    @Override
    public List<TournBye> getByTourneyShortNameAndRound(String shortName, int round) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        List<TournBye> result = null;

        try {
            String hql = "FROM TournBye tb WHERE tb.tournament IN (SELECT t FROM Tournament t WHERE t.shortName = :shortName) AND tb.round = :round";
            Query query = session.createQuery(hql);
            query.setParameter("shortName", shortName);
            query.setParameter("round", round);

            result = query.list();

            // Manually initialize the lazy-loaded assocMember of TournamentPlayer
            for (TournBye bye : result) {
                Hibernate.initialize(bye.getPlayer().getAssocMember());
            }

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        return result;
    }


}
