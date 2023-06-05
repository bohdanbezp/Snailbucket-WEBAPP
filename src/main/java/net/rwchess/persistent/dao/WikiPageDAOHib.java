package net.rwchess.persistent.dao;

import net.rwchess.persistent.WikiPage;
import net.rwchess.utils.HibernateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Stack;

public class WikiPageDAOHib implements WikiPageDAO {

    private static WikiPageDAOHib cached;

    public static WikiPageDAOHib getCachedDao() {
        if (cached == null)
            cached = new WikiPageDAOHib();

        return cached;
    }

    @Override
    public WikiPage getWikiPageByName(String name) {
        WikiPage res = null;
        try (Session session = HibernateUtils.getInstance().openSession()) {
            Transaction transaction = session.beginTransaction();
            String hql = "FROM WikiPage M WHERE M.name = :name";
            Query query = session.createQuery(hql);
            query.setParameter("name", name);

            try {
                res = (WikiPage) query.uniqueResult();
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
            }
        }
        return res;
    }

    @Override
    public void store(WikiPage wikiPage) {
        try (Session session = HibernateUtils.getInstance().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.saveOrUpdate(wikiPage);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
            }
        }
    }

    @Override
    public void toggleProtectTd(String pageName) {
        try (Session session = HibernateUtils.getInstance().openSession()) {
            Transaction transaction = session.beginTransaction();
            String hql = "FROM WikiPage M WHERE M.name = :name";
            Query query = session.createQuery(hql);
            query.setParameter("name", pageName);

            WikiPage res = null;
            try {
                res = (WikiPage) query.uniqueResult();
                res.setTdProtected(!res.isTdProtected());
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
            }
        }
    }

    @Override
    public void updatePageWithText(String pageName, String rawText, Stack<String> newHistory) {
        try (Session session = HibernateUtils.getInstance().openSession()) {
            Transaction transaction = session.beginTransaction();
            String hql = "FROM WikiPage M WHERE M.name = :name";
            Query query = session.createQuery(hql);
            query.setParameter("name", pageName);

            WikiPage res = null;
            try {
                res = (WikiPage) query.uniqueResult();
                res.setRawText(rawText);
                res.setHistory(newHistory);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
            }
        }
    }
}
