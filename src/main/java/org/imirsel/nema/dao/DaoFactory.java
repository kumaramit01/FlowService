package org.imirsel.nema.dao;

/**
 * Factory interface for instantiating flow service related DAOs.
 * 
 * @author shirk
 * @since 0.4.0
 */
public interface DaoFactory {

   /**
    * Return a {@link FlowDao} instance.
    * 
    * @return A {@link FlowDao} instance.
    */
   FlowDao getFlowDao();

   /**
    * Return a {@link JobDao} instance.
    * 
    * @return A {@link JobDao} instance.
    */
   JobDao getJobDao();

   /**
    * Return a {@link JobResultDao} instance.
    * 
    * @return A {@link JobResultDao} instance.
    */
   JobResultDao getJobResultDao();

   /**
    * Return a {@link NotificationDao} instance.
    * 
    * @return A {@link NotificationDao} instance.
    */
   NotificationDao getNotificationDao();

}
