package org.imirsel.nema.flowservice;

/**
 * Given a set of {@link MeandreServer}s, determines which server will get
 * the next job request.
 *
 * @author shirk
 * @since 1.0
 */
public interface MeandreLoadBalancer {

   //~ Methods -----------------------------------------------------------------

   /**
    * Return the {@link MeandreServer} to receive the next job execution 
    * request.
    *
    * @return The next eligible {@link MeandreServer} to receive a job, or 
    * <code>null</code> if no servers are available.
    */
   public MeandreServer nextAvailableServer();

   /**
    * Test if there are any servers currently available for processing a job.
    *
    * @return True if at least one server is available.
    */
   public boolean hasAvailableServer();

   /**
    * Register a new {@link MeandreServer} with the load balancer.
    *
    * @param server The {@link MeandreServer} to add.
    */
   public void addServer(MeandreServer server);

   /**
    * Remove a {@link MeandreServer} from the load balancer.
    *
    * @param server The server to remove.
    * @throws IllegalStateException if the server is currently processing a
    * job.
    */
   public void removeServer(MeandreServer server) throws IllegalStateException;
   
   /**
    * Number of servers the load balancer is aware of.
    * 
    * @return Number of servers the load balancer is aware of.
    */
   public int size();

}
