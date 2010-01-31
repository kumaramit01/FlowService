package org.imirsel.nema.flowservice;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author shirk
 * @since 0.4.0
 */
public class RoundRobinLoadBalancerTest {

   private RoundRobinLoadBalancer loadBalancer;
   private MeandreServerProxy server1;
   private MeandreServerProxy server2;
   private MeandreServerProxy server3;
   private MeandreServerProxy server4;

   @Before
   public void setUp() throws Exception {
      loadBalancer = new RoundRobinLoadBalancer();
      
      Set<MeandreServerProxy> servers = new HashSet<MeandreServerProxy>();
      
      
      String host ="192.168.0.1";
      String host1 ="192.168.0.2";
      String host2 ="192.168.0.3";
      String host3 ="192.168.0.4";
      String password = "admin";
      String username ="admin";
      int port = 1714;
      int maxConcurrentJobs =1;

      MeandreServerProxyConfig config = new 
      MeandreServerProxyConfig(username,password,host,port,maxConcurrentJobs);

      MeandreServerProxyConfig config1 = new 
      MeandreServerProxyConfig(username,password,host1,port,maxConcurrentJobs);
      MeandreServerProxyConfig config2 = new 
      MeandreServerProxyConfig(username,password,host2,port,maxConcurrentJobs);
      MeandreServerProxyConfig config3 = new 
      MeandreServerProxyConfig(username,password,host3,port,maxConcurrentJobs);

		
      
      
      server1 = new MeandreServerProxy(config);
      server2 = new MeandreServerProxy(config1);
      server3 = new MeandreServerProxy(config2);
      server4 = new MeandreServerProxy(config3);
   }

   @Test
   public void testAddServer() {
      loadBalancer.addServer(server1);
      assertTrue(loadBalancer.size() == 1);
      loadBalancer.addServer(server2);
      assertTrue(loadBalancer.size() == 2);
      loadBalancer.addServer(server3);
      assertTrue(loadBalancer.size() == 3);
      loadBalancer.addServer(server4);
      assertTrue(loadBalancer.size() == 4);
   }

   @Test
   public void testPreventDuplicateServers() {
      loadBalancer.addServer(server1);
      assertTrue(loadBalancer.size() == 1);
      loadBalancer.addServer(server2);
      assertTrue(loadBalancer.size() == 2);
      // add a server that is already present
      loadBalancer.addServer(server2);
      // make sure size hasn't changed
      assertTrue(loadBalancer.size() == 2);
   }

   @Test
   public void testRemoveServer() {
      loadBalancer.addServer(server1);
      loadBalancer.addServer(server2);
      loadBalancer.addServer(server3);
      loadBalancer.addServer(server4);

      assertTrue(loadBalancer.size() == 4);
      loadBalancer.removeServer(server1);
      assertTrue(loadBalancer.size() == 3);
      loadBalancer.removeServer(server2);
      assertTrue(loadBalancer.size() == 2);
      loadBalancer.removeServer(server3);
      assertTrue(loadBalancer.size() == 1);
      loadBalancer.removeServer(server4);
      assertTrue(loadBalancer.size() == 0);
   }

   @Test
   public void testNextAvailableServer() {
      loadBalancer.addServer(server1);
      loadBalancer.addServer(server2);
      loadBalancer.addServer(server3);
      loadBalancer.addServer(server4);

      int max = 4;
      Set<MeandreServerProxy> serverSet = new HashSet<MeandreServerProxy>();
      for (int i = 0; i <= max; i++) {
         serverSet.add(loadBalancer.nextAvailableServer());
      }
      assertTrue(serverSet.size() == 4);

      loadBalancer.removeServer(server1);
      max = 3;
      serverSet.clear();
      for (int i = 0; i <= max; i++) {
         serverSet.add(loadBalancer.nextAvailableServer());
      }
      assertTrue(serverSet.size() == 3);

      loadBalancer.removeServer(server2);
      max = 2;
      serverSet.clear();
      for (int i = 0; i <= max; i++) {
         serverSet.add(loadBalancer.nextAvailableServer());
      }
      assertTrue(serverSet.size() == 2);

      loadBalancer.removeServer(server3);
      max = 1;
      serverSet.clear();
      for (int i = 0; i <= max; i++) {
         serverSet.add(loadBalancer.nextAvailableServer());
      }
      assertTrue(serverSet.size() == 1);

      loadBalancer.removeServer(server4);
      assertNull(loadBalancer.nextAvailableServer());
   } // end method testNextAvailableServer

   @Test
   public void testHasAvailableServer() {
      loadBalancer.addServer(server1);
      assertTrue(loadBalancer.hasAvailableServer());
   }

}
