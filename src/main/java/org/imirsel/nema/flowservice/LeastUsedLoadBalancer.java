package org.imirsel.nema.flowservice;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import net.jcip.annotations.ThreadSafe;

/**
 * A {@link MeandreLoadBalancer} implementation that uses a least used
 * algorithm to deal out servers. To determine the least used server, they
 * are compared based on their load. Load is calculated as a percentage by 
 * adding the number of running and aborting jobs then dividing that sum by 
 * the max number of concurrent jobs.
 *
 * @author shirk
 * @since 0.9.0
 */
@ThreadSafe
public class LeastUsedLoadBalancer implements MeandreLoadBalancer {

   private static final Logger logger = 
      Logger.getLogger(LeastUsedLoadBalancer.class.getName());
   
   private Set<MeandreServerProxy> workers = new HashSet<MeandreServerProxy>();
   
   /**
    * @see MeandreLoadBalancer#addServer(MeandreServerProxy)
    */
   @Override
   public synchronized void addServer(MeandreServerProxy server) {
      workers.add(server);
      logger.fine("Added server " + server + " to load balancer");
   }

   /**
    * @see MeandreLoadBalancer#removeServer(MeandreServerProxy)
    */
   @Override
   public synchronized void removeServer(MeandreServerProxy server) {
      workers.remove(server);
      logger.fine("Removed server " + server + " from load balancer");
   }
   
   /**
    * @see MeandreLoadBalancer#hasAvailableServer()
    */
   @Override
   public synchronized boolean hasAvailableServer() {
      Iterator<MeandreServerProxy> proxyIterator = workers.iterator();
      while(proxyIterator.hasNext()) {
         MeandreServerProxy server = proxyIterator.next();
         if(server.isAcceptingJobs() && !server.isBusy()) {
            return true;
         }
      }
      return false;
   }

   /**
    * @see MeandreLoadBalancer#nextAvailableServer()
    */
   @Override
   public synchronized MeandreServerProxy nextAvailableServer() {
      logger.fine("Finding the next available server.");
      Iterator<MeandreServerProxy> proxyIterator = workers.iterator();
      MeandreServerProxy leastUsedServer = null;
      double leastUsedLoad = 0.0;
      while(proxyIterator.hasNext()) {
         MeandreServerProxy server = proxyIterator.next();
         if(server.isAcceptingJobs() && !server.isBusy()) {
            double load = (server.getNumJobsRunning() + 
                  server.getNumJobsAborting()) / 
                  (double) server.getConfig().getMaxConcurrentJobs();
            if(leastUsedServer == null) {
               leastUsedServer = server;
               leastUsedLoad = load;
            } else if(load < leastUsedLoad) {
               leastUsedServer = server;
               leastUsedLoad = load;
            }
         }
      }
      if(leastUsedServer!=null) {
         logger.fine("Next available server: " + leastUsedServer + 
               ". Load: " + leastUsedLoad);
      }
      return leastUsedServer;
   }

   /**
    * @see MeandreLoadBalancer#size()
    */
   @Override
   public synchronized int size() {
      return workers.size();
   }

}
