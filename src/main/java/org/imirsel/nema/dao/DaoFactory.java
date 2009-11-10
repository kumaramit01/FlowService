package org.imirsel.nema.dao;


public interface DaoFactory {

	    FlowDao getFlowDao();
	    JobDao getJobDao();
	    JobResultDao getJobResultDao();
	    NotificationDao getNotificationDao();

}
