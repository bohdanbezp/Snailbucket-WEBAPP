package net.rwchess.persistent.dao;

import net.rwchess.persistent.Member;
import net.rwchess.utils.HibernateUtils;
import net.rwchess.utils.UsefulMethods;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Created by bodia on 10/14/14.
 */
public class MemberDAOHib implements MemberDAO {
    @Override
    public Member getMemberByUsername(String username) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.username = :username and M.group <> 0";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);

        Member res = null;
        try {
            res = (Member) query.list().get(0);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public Member getMemberById(Long key) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", key);

        Member res = null;
        try {
            res = (Member) query.list().get(0);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public void toggleConfirmed(String username) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.username = :username and M.group <> 0";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);

        Member res = null;
        try {
            res = (Member) query.list().get(0);
            res.setConfirmed(true);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }


    @Override
    public void store(Member member) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(member);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Member> getAllConfirmedMembers() {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.confirmed = 1 and M.group <> 0 order by M.username";
        Query query = session.createQuery(hql);

        List<Member> res = null;
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
    public List<Member> getAllMembers() {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M order by M.username";
        Query query = session.createQuery(hql);

        List<Member> res = null;
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
    public void updateWithData(String username, String passwordHash, String country, String badTimes, String hardTimes,
                               String timeControlPreferrence, String email) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.username = :username";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);

        Member m = null;
        try {
            m = (Member) query.list().get(0);
            m.setUsername(username);
            m.setPasswordHash(passwordHash);
            m.setConfirmed(false);
            m.setGroup(Member.USER);
            m.setCountry(country.toLowerCase());
            m.setInsist(this.getInsistData(badTimes, hardTimes).toString());
            m.setPreference(timeControlPreferrence);
            m.setEmail(email);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateRole(Long key, int newGroup) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", key);

        Member m = null;
        try {
            m = (Member) query.list().get(0);
            m.setGroup(newGroup);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateRR(Long key, int rr) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", key);

        Member m = null;
        try {
            m = (Member) query.list().get(0);
            m.setRr(rr);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updatePassword(Long key, String password) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", key);

        Member m = null;
        try {
            m = (Member) query.list().get(0);
            m.setPasswordHash(UsefulMethods.getMD5(password));
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateTimeorder(Long key, String timeOrder) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", key);

        Member m = null;
        try {
            m = (Member) query.list().get(0);
            m.setPreference(timeOrder);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateCountry(Long key, String country) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", key);

        Member m = null;
        try {
            m = (Member) query.list().get(0);
            m.setCountry(country);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateInsist(Long key, String badTimes, String hardTimes) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM Member M WHERE M.key = :key";
        Query query = session.createQuery(hql);
        query.setParameter("key", key);

        Member m = null;
        try {
            m = (Member) query.list().get(0);
            m.setInsist(this.getInsistData(badTimes, hardTimes).toString());
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }
    
    public InsistData getInsistData(String insist) {
    	return new InsistData(insist);
    }
    
    public InsistData getInsistData(String badTimes, String hardTimes) {
    	return new InsistData(badTimes, hardTimes);
    }
}