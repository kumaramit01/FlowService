package org.imirsel.nema.flowservice.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.core.io.ClassPathResource;

/**
 * A {@link FlowServiceConfig} that loads the configuration from a Java
 * properties file.
 * 
 * @author kumaramit01
 * @since 0.5.0
 */
public class PropertiesBasedFlowServiceConfig implements
      FlowServiceConfig {

   private String propertiesFileName;
   private Properties properties;
   private MeandreServerProxyConfig head;
   private Set<MeandreServerProxyConfig> workers;

   public PropertiesBasedFlowServiceConfig() {
   }

   @PostConstruct
   public void init() throws ConfigException {
      workers = new HashSet<MeandreServerProxyConfig>();
      ClassPathResource resource = new ClassPathResource(
            propertiesFileName);
      if (resource == null) {
         throw new ConfigException(
               "Could not find " + propertiesFileName + ".");
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
               "Invalid flow service configuration file.");
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
               "Invalid flow service configuration file.");
      }

   }

   /**
    * @see FlowServiceConfig#getHeadConfig()
    */
   @Override
   public MeandreServerProxyConfig getHeadConfig() {
      return head;
   }

   /**
    * @see FlowServiceConfig#getWorkerConfigs()
    */
   @Override
   public Set<MeandreServerProxyConfig> getWorkerConfigs() {
      return workers;
   }

   /**
    * Return the name of the properties file that contains the configuration.
    * 
    * @return Name of the properties file that contains the configuration.
    */
   public String getPropertiesFileName() {
      return propertiesFileName;
   }

   /**
    * Inject the name of the properties file that contains the configuration.
    * 
    * @param propertiesFileName The name of the properties file that
    *  contains the configuration.
    */
   public void setPropertiesFileName(String propertiesFileName) {
      this.propertiesFileName = propertiesFileName;
   }

}
