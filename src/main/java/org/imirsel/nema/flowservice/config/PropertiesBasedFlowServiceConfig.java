package org.imirsel.nema.flowservice.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.imirsel.nema.flowservice.MeandreServerException;
import org.springframework.core.io.ClassPathResource;

/**
 * A {@link FlowServiceConfig} intended to work in concert with a Spring
 * configuration file to inject the requisite properties.
 * 
 * @author kumaramit01
 * @since 0.5.0
 * 
 */
public class PropertiesBasedFlowServiceConfig implements
      FlowServiceConfig {

   private static final String MEANDRE_SERVER_PROPERTIES_FILE_NAME = 
      "meandreserver.properties";
   
   private Properties properties;
   private MeandreServerProxyConfig head;
   private final Set<MeandreServerProxyConfig> workers = new HashSet<MeandreServerProxyConfig>();

   public PropertiesBasedFlowServiceConfig() {
   }

   @PostConstruct
   public void init() throws ConfigException {
      ClassPathResource resource = new ClassPathResource(
            MEANDRE_SERVER_PROPERTIES_FILE_NAME);
      if (resource == null) {
         throw new ConfigException(
               "Could not find " + MEANDRE_SERVER_PROPERTIES_FILE_NAME + ".");
      }
      InputStream is = null;
      try {
         is = resource.getInputStream();
         properties = new Properties();
         properties.load(is);
      } catch (IOException e) {
         throw new ConfigException(e);
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }
      }
      if (properties == null) {
         throw new ConfigException(
               "invalid meandre property list");
      }

      String headServerPortStr = properties.getProperty("headserver.port");
      String headMaxCurrentJobsStr = properties
            .getProperty("headserver.maxConcurrentJobs");
      String headUsername = properties.getProperty("headserver.username");
      String headPassword = properties.getProperty("headserver.password");
      String headServer = properties.getProperty("headserver.host");

      int headServerPort = Integer.parseInt(headServerPortStr);
      int headMaxCurrentJobs = Integer.parseInt(headMaxCurrentJobsStr);

      head = new SimpleMeandreServerProxyConfig(
            headUsername, headPassword, headServer, headServerPort,
            headMaxCurrentJobs);

      String hosts = properties.getProperty("workerserver.hosts");
      String ports = properties.getProperty("workerserver.ports");
      String usernames = properties.getProperty("workerserver.usernames");
      String passwords = properties.getProperty("workerserver.passwords");
      String maxConcurrentJobs = properties
            .getProperty("workerserver.maxConcurrentJobs");

      String[] hostList = hosts.split(",");
      String[] portList = ports.split(",");
      String[] usernameList = usernames.split(",");
      String[] passwordList = passwords.split(",");
      String[] maxConcurrentJobList = maxConcurrentJobs.split(",");

      if (hostList.length != 0 && (hostList.length == portList.length)
            && (hostList.length == usernameList.length)
            && (hostList.length == passwordList.length)
            && (hostList.length == maxConcurrentJobList.length)) {
         int count = 0;
         for (String host : hostList) {
            int port = Integer.parseInt(portList[count]);
            int maxConcurrentJob = Integer
                  .parseInt(maxConcurrentJobList[count]);
            String username = usernameList[count];
            String password = passwordList[count];
            MeandreServerProxyConfig config = new SimpleMeandreServerProxyConfig(
                  username, password, host, port, maxConcurrentJob);
            workers.add(config);
            count++;
         }
      } else {
         throw new ConfigException(
               "invalid meandre property list");
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.imirsel.nema.flowservice.config.FlowServiceConfig#getHead()
    */
   @Override
   public MeandreServerProxyConfig getHeadConfig() {
      return head;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.imirsel.nema.flowservice.config.FlowServiceConfig#getServers()
    */
   @Override
   public Set<MeandreServerProxyConfig> getWorkerConfigs() {
      return workers;
   }

}
