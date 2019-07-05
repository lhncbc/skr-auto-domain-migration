package gov.nih.nlm.semmed.util;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

/**
 * Configures and provides access to Hibernate sessions, tied to the
 * current thread of execution.  Follows the Thread Local Session
 * pattern, see {@link http://hibernate.org/42.html}.
 */
public class HibernateSessionFactory {
	private static Log log = LogFactory.getLog(HibernateSessionFactory.class);	
	
    /** 
     * Location of hibernate.cfg.xml file.
     * NOTICE: Location should be on the classpath as Hibernate uses
     * #resourceAsStream style lookup for its configuration file. That
     * is place the config file in a Java package - the default location
     * is the default Java package.<br><br>
     * Examples: <br>
     * <code>CONFIG_FILE_LOCATION = "/hibernate.conf.xml". 
     * CONFIG_FILE_LOCATION = "/com/foo/bar/myhiberstuff.conf.xml".</code> 
     */
    private static String CONFIG_FILE_LOCATION = "/hibernate.cfg.xml";

    /** Holds a single instance of Session */
    private static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();

    /** The single instance of hibernate configuration */
    private static final Configuration cfg = new Configuration();

    /** The single instance of hibernate SessionFactory */
    private static org.hibernate.SessionFactory sessionFactory;

    /**
     * Returns the ThreadLocal Session instance.  Lazy initialize
     * the <code>SessionFactory</code> if needed.
     *
     *  @return Session
     *  @throws HibernateException
     */
    public static Session currentSession() throws HibernateException, SQLException {
        Session session = threadLocal.get();

        if (session == null) { //TODO Move this initialization to threadLocal.get ? [Alejandro]
        	log.debug("Session is null."); 
            if (sessionFactory == null) { //TODO This is not thread safe [Alejandro]
                try {
                    cfg.configure(CONFIG_FILE_LOCATION);
                    sessionFactory = cfg.buildSessionFactory(); //TODO Check whether sessionFactory is still null [Alejandro]
                }
                catch (Exception e) {
                    System.err.println("%%%% Error Creating SessionFactory %%%%");
                    e.printStackTrace();
                }
            }
            session = sessionFactory.openSession();
            threadLocal.set(session);
        }

        return session;
    }
    
    public static boolean isSessionValid() {
    	return threadLocal.get() != null;
    }

    /**
     *  Close the single hibernate session instance.
     *
     *  @throws HibernateException
     */
    public static void closeSession() throws HibernateException {
        Session session = threadLocal.get();
        threadLocal.set(null);

        if (session != null) {
        	session.disconnect();
            session.close();
        }
    }
    
    /**
     * Default constructor.
     */
    private HibernateSessionFactory() {
    }

}
