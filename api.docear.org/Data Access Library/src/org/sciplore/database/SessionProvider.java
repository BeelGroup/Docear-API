package org.sciplore.database;

import java.io.IOException;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.mrdlib.index.Indexer;
import org.sciplore.eventhandler.LuceneUpdateHandler;
import org.sciplore.eventhandler.ResourceManager;
import org.sciplore.eventhandler.SaveOrUpdateHandler;
import org.sciplore.rules.ModificationRuleManager;
import org.sciplore.rules.XmlRuleConfiguration;
import org.sciplore.utilities.config.Config;

public class SessionProvider {
	
	public static final SessionFactory sessionFactory;
	public static final MrDLibConfiguration cfg;
	public static final ResourceManager dependTable;
	public static final ModificationRuleManager ruleManager;
	private static Indexer luceneIndexer;
	
	static {
		try {
			luceneIndexer = new Indexer();
		} catch (IOException ex) {
		}
        try {         	
        	cfg = (MrDLibConfiguration) new MrDLibConfiguration(HibernateConfigurationInfo.getConfigurationFile()).configure();
        	Properties p = Config.getProperties("org.mrdlib");
        	cfg.setProperty("hibernate.connection.driver_class", p.getProperty("driver", "com.mysql.jdbc.Driver"));
        	cfg.setProperty("hibernate.connection.url", p.getProperty("url", "jdbc:mysql://localhost/mr-dlib?nicode=true&characterEncoding=UTF-8"));
        	cfg.setProperty("hibernate.connection.username", p.getProperty("username", "mr-dlib"));
        	cfg.setProperty("hibernate.connection.password", p.getProperty("password", ""));
        	cfg.setProperty("hibernate.connection.autocommit", p.getProperty("autocommit", "true"));
        	cfg.setProperty("hibernate.show_sql", p.getProperty("showSql", "false"));
        	cfg.setProperty("hibernate.dialect", p.getProperty("dialect", "org.hibernate.dialect.MySQLInnoDBDialect"));
        	cfg.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
        	cfg.setListener("save-update", SaveOrUpdateHandler.class.getName());
        	cfg.setListener("post-update", LuceneUpdateHandler.class.getName());
        	cfg.setListener("post-insert", LuceneUpdateHandler.class.getName());
        	dependTable = ResourceManager.buildNewIndex(cfg.getAnnotatedClasses());
        	ruleManager = XmlRuleConfiguration.getRuleManager("rules.xml");
        	sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {            
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
	}
	
	public static Session getNewSession() {
		return sessionFactory.openSession();
	}
	
	public static Indexer getLuceneIndexer() throws IOException{
		if(luceneIndexer == null) {
			luceneIndexer = new Indexer();
		}
		return luceneIndexer;
	}
}
