package net.rwchess.persistent.dao;

import net.rwchess.persistent.WikiPage;
import net.rwchess.utils.HibernateUtils;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import java.util.Stack;

/**
 * Created by bodia on 10/14/14.
 */
public class WikiPageDAOHib implements WikiPageDAO {
    @Override
    public WikiPage getWikiPageByName(String name) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM WikiPage M WHERE M.name = :name";
        Query query = session.createQuery(hql);
        query.setParameter("name", name);

        WikiPage res = null;
        try {
            res = (WikiPage) query.list().get(0);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
        return res;
    }

    @Override
    public void store(WikiPage wikiPage) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.saveOrUpdate(wikiPage);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void toggleProtectTd(String pageName) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM WikiPage M WHERE M.name = :name";
        Query query = session.createQuery(hql);
        query.setParameter("name", pageName);

        WikiPage res = null;
        try {
            res = (WikiPage) query.list().get(0);
            res.setTdProtected(!res.isTdProtected());
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }
    }

    @Override
    public void updatePageWithText(String pageName, String rawText, Stack<String> newHistory) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction = session.beginTransaction();
        String hql = "FROM WikiPage M WHERE M.name = :name";
        Query query = session.createQuery(hql);
        query.setParameter("name", pageName);

        WikiPage res = null;
        try {
            res = (WikiPage) query.list().get(0);
            res.setRawText(rawText);
            res.setHistory(newHistory);
            transaction.commit();
        } catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        } finally {
            session.close();
        }

    }
}
