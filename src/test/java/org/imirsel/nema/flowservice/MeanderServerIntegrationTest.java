package org.imirsel.nema.flowservice;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.context.ManagedSessionContext;
import org.imirsel.nema.dao.impl.FlowDaoImpl;
import org.imirsel.nema.dao.impl.JobDaoImpl;
import org.imirsel.nema.model.Flow;
import org.imirsel.nema.model.Job;
import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.util.FlowTypeUtils;

public class MeanderServerIntegrationTest {

	static JobDaoImpl jobDao = new org.imirsel.nema.dao.impl.JobDaoImpl();
	static FlowDaoImpl flowDao = new org.imirsel.nema.dao.impl.FlowDaoImpl();
	static SessionFactory sessionFactory;
	static {
		AnnotationConfiguration cfg = new AnnotationConfiguration()
	    .addAnnotatedClass(org.imirsel.nema.model.JobResult.class)
	    .addAnnotatedClass(org.imirsel.nema.model.Job.class)
	    .addAnnotatedClass(org.imirsel.nema.model.Flow.class)
	    .setProperty("hibernate.connection.url", "jdbc:mysql://128.174.154.145:3306/flowservice?autoReconnect=true")
	    .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
	    .setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
	    .setProperty("hibernate.connection.username", "nema_user")
        .setProperty("hibernate.hbm2ddl.auto", "update")
	    .setProperty("hibernate.connection.password", "reduxer101")
	    .setProperty("hibernate.current_session_context_class", "managed");
		
		sessionFactory = cfg.buildSessionFactory();

		ManagedSessionContext.bind(sessionFactory.openSession());
		
		jobDao.setSessionFactory(sessionFactory);
		flowDao.setSessionFactory(sessionFactory);
	}
	
	public static void main(String[] args) throws MeandreServerException {
        MeandreServer server = new MeandreServer();
        server.setHost("128.174.154.145");
        server.setPort(1714);
        server.setMaxConcurrentJobs(1);
        server.init();
        
		Flow flow = new Flow();
		flow.setCreatorId(100L);
		flow.setDateCreated(new Date());
        flow.setUrl("http://test.org/helloworld/");
        flow.setName("Bugger off");
        flow.setDescription("bite me");
        flow.setKeyWords("test flow");
        flow.setTemplate(false);
        flow.setType(FlowTypeUtils.DEFAULT_TYPE);

        Session currentSession = sessionFactory.getCurrentSession();
        Transaction transaction = currentSession.beginTransaction();
        flowDao.makePersistent(flow);
        transaction.commit();
        
        
        transaction = currentSession.beginTransaction();


		Job job = new Job();
		job.setToken(String.valueOf(System.currentTimeMillis()));
		job.setName("Test " + new Date());
		job.setDescription("A test job.");
		job.setFlow(flow);
		job.setOwnerId(200L);
		job.setOwnerEmail("nobody@loopback.net");
		job.setNumTries(0);
		job.setToken("token-"+System.currentTimeMillis());
		
		jobDao.makePersistent(job);
		
		transaction.commit();
		
		server.executeJob(job);
		
		
		ManagedSessionContext.unbind(sessionFactory);
        
	}
}
