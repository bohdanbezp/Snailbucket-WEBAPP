package net.rwchess.persistent.dao;

import net.rwchess.persistent.DownloadFile;
import net.rwchess.utils.HibernateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class DownloadFileDAOHib implements DownloadFileDAO {

    @Override
    public void store(DownloadFile downloadFile) {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction=session.beginTransaction();
        try {
            session.saveOrUpdate(downloadFile);
            transaction.commit();
        }
        catch (Exception e) {
            transaction.rollback();
        }
        finally {
            session.close();
        }
    }

    @Override
    public List<DownloadFile> getAllFiles() {
        Session session = HibernateUtils.getInstance().openSession();
        Transaction transaction=session.beginTransaction();
        String hql = "FROM DownloadFile M";
        Query query = session.createQuery(hql);

        List<DownloadFile> res = null;
        try {
            res = query.list();
            transaction.commit();
        }
        catch (IndexOutOfBoundsException e) {
            transaction.rollback();
        }
        finally {
            session.close();
        }
        return res;
    }
}
