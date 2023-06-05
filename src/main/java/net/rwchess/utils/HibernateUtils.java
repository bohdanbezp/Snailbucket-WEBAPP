package net.rwchess.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Created by bodia on 10/14/14.
 */
public class HibernateUtils {

    private static SessionFactory sessionFactory;


    public static SessionFactory getInstance() {
        if (sessionFactory == null) {
            Configuration configuration = new Configuration().configure();
            sessionFactory = configuration.buildSessionFactory();
        }

        return sessionFactory;
    }
}
