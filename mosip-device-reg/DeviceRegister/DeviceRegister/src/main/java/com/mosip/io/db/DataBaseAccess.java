package com.mosip.io.db;

import java.util.List;
import java.util.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DataBaseAccess {
	public SessionFactory factory;
	Session session;
	public String env = System.getProperty("env.user");
	private static Logger logger = Logger.getLogger(DataBaseAccess.class.getName());
	
	public boolean executeQuery(String queryString, String dbName) {
		int res = 0;
		try {
			res = getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString).executeUpdate();
		} catch (HibernateException e) {
			logger.info(e.getMessage());
		}finally {
		session.getTransaction().commit();
		session.close();
		factory.close();
		logger.info("==========session  closed=============");
		}
		return (res>0) ? true : false;
	}
	
	public boolean validateDataInDb(String queryString, String dbName) {
		int size=0;
		try {
			size = getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString).list().size();
		} catch (HibernateException e) {
			logger.info(e.getMessage());
		}finally {
			session.close();
			factory.close();
			logger.info("==========session  closed=============");
		}
		return (size == 1) ? true : false;
	}
	
	
	public long validateDBCount(String queryStr, String dbName) {
		long count = 0;
		try {
			count = ((long)(getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryStr).getSingleResult()));
			logger.info("obtained objects count from DB is : " + count);
		} catch (HibernateException e) {
			logger.info(e.getMessage());
		}finally {
			session.close();
			factory.close();
			logger.info("==========session  closed=============");
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getDbData(String queryString, String dbName) {

		List<String> data = null;
		try {
			data = getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString).list();
		} catch (HibernateException e) {
			logger.info(e.getMessage());
		}finally {
			session.close();
			factory.close();
			logger.info("==========session  closed=============");
		}
		return  data;

	}
	
	@SuppressWarnings("unchecked")
	public List<Object> getData(String queryString, String dbName) {

		List<Object> data = null;
		try {
			data = getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString).list();
		} catch (HibernateException e) {
			logger.info(e.getMessage());
		}finally {
			session.close();
			factory.close();
			logger.info("==========session  closed=============");
		}
		return  data;

	}

	
	public Session getDataBaseConnection(String dbName) {

		String dbConfigXml ="/dbFiles/"+ dbName+env.toLowerCase()+".cfg.xml";
		try {
		factory = new Configuration().configure(dbConfigXml).buildSessionFactory();
		session = factory.getCurrentSession();
		} 
		catch (HibernateException e) {
			logger.info("Exception in Database Connection with following message: ");
			logger.info(e.getMessage());
		}
		session.beginTransaction();
		logger.info("==========session  begins=============");
		return session;
	}
	
	
}
