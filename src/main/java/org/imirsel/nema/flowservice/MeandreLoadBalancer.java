package org.imirsel.nema.flowservice;

/**
 * Given a set of {@link MeandreServerProxy}s, determines which server will get
 * the next job request.
 *
 * @author shirk
 * @since 0.4.0
 */
public interface MeandreLoadBalancer {

   //~ Methods -----------------------------------------------------------------

   /**
    * Return the {@link MeandreServerProxy} to receive the next job execution 
    * request.
    *
    * @return The next eligible {@link MeandreServerProxy} to receive a job, or 
    * <code>null</code> if no servers are available.
    */
   public MeandreServerProxy nextAvailableServer();

   /**
    * Test if there are any servers currently available for processing a job.
    *
    * @return True if at least one server is available.
    */
   public boolean hasAvailableServer();

   /**
    * Register a new {@link MeandreServerProxy} with the load balancer.
    *
    * @param server The {@link MeandreServerProxy} to add.
    */
   public void addServer(MeandreServerProxy server);

   /**
    * Remove a {@link MeandreServerProxy} from the load balancer.
    *
    * @param server The server to remove.
    * @throws IllegalStateException if the server is currently processing a
    * job.
    */
   public void removeServer(MeandreServerProxy server) throws IllegalStateException;
   
   /**
    * Number of servers the load balancer is aware of.
    * 
    * @return Number of servers the load balancer is aware of.
    */
   public int size();

}
