package org.imirsel.nema.flowservice;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class RoundRobinLoadBalancerTest {

   private RoundRobinLoadBalancer loadBalancer;
   private MeandreServer server1;
   private MeandreServer server2;
   private MeandreServer server3;
   private MeandreServer server4;

   @Before
   public void setUp() throws Exception {
      loadBalancer = new RoundRobinLoadBalancer();
      server1 = new MeandreServer("192.168.0.1", 10000, 1);
      server2 = new MeandreServer("192.168.0.2", 10000, 1);
      server3 = new MeandreServer("192.168.0.3", 10000, 1);
      server4 = new MeandreServer("192.168.0.4", 10000, 1);
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
      Set<MeandreServer> serverSet = new HashSet<MeandreServer>();
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
