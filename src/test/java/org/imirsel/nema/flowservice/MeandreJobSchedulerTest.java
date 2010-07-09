package org.imirsel.nema.flowservice;

import java.util.HashSet;
import java.util.Set;

import org.imirsel.nema.dao.DaoFactory;
import org.imirsel.nema.flowservice.config.ConfigChangeListener;
import org.imirsel.nema.flowservice.config.FlowServiceConfig;
import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.config.SimpleMeandreServerProxyConfig;
import org.imirsel.nema.repository.RepositoryClientConnectionPool;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MeandreJobSchedulerTest {

   Mockery context = new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);
   }};
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
   }

   @Before
   public void setUp() throws Exception {
      FlowServiceConfig serviceConfig = new FlowServiceConfig() {
         Set<MeandreServerProxyConfig> workers = new HashSet<MeandreServerProxyConfig>();
         MeandreServerProxyConfig head = null;
         
         {
            String headHost = "localhost";
            String headPass = "admin";
            String headUser = "admin";
            int headPort = 1714;
            int maxConcurrentJobs = 1;

            head = new SimpleMeandreServerProxyConfig(
                  headUser, headPass, headHost, headPort, maxConcurrentJobs);
            
            String host1 = "128.174.154.145";
            String host2 = "128.174.154.145";
            String host3 = "128.174.154.145";
            String password = "admin";
            String username = "admin";

            int port1 = 11514;
            int port2 = 11614;
            int port3 = 11714;

            MeandreServerProxyConfig config1 = new SimpleMeandreServerProxyConfig(
                  username, password, host1, port1, maxConcurrentJobs);
            MeandreServerProxyConfig config2 = new SimpleMeandreServerProxyConfig(
                  username, password, host2, port2, maxConcurrentJobs);
            MeandreServerProxyConfig config3 = new SimpleMeandreServerProxyConfig(
                  username,password,host3,port3,maxConcurrentJobs);

            workers.add(config1);
            workers.add(config2);
            workers.add(config3);
         }
         
         public MeandreServerProxyConfig getHeadConfig() {
            return head;
         }

         public Set<MeandreServerProxyConfig> getWorkerConfigs() {
            return workers;
         }

         @Override
         public void addChangeListener(ConfigChangeListener listener) {
            throw new RuntimeException("Method not supported.");
         }

         @Override
         public void removeChangeListener(ConfigChangeListener listener) {
            throw new RuntimeException("Method not supported.");
         }
      };
      
      RepositoryClientConnectionPool mockRepositoryClient  = 
         context.mock(RepositoryClientConnectionPool.class);
      DaoFactory mockDaoFactory = context.mock(DaoFactory.class);
      
      MeandreJobScheduler jobScheduler = new MeandreJobScheduler();
      MeandreLoadBalancer loadBalancer = new RoundRobinLoadBalancer();
      MeandreServerProxyFactory proxyFactory = new MeandreServerProxyFactory();

      proxyFactory.setRepositoryClientConnectionPool(mockRepositoryClient);
      jobScheduler.setLoadBalancer(loadBalancer);
      jobScheduler.setMeandreServerProxyFactory(proxyFactory);
      jobScheduler.setWorkerConfigs(serviceConfig.getWorkerConfigs());
      jobScheduler.setDaoFactory(mockDaoFactory);
      jobScheduler.init();

   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void emptyTest() {
   }

}
