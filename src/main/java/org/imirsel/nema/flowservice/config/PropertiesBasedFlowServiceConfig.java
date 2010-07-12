package org.imirsel.nema.flowservice.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.imirsel.nema.flowservice.monitor.FileChangeListener;
import org.imirsel.nema.flowservice.monitor.FileMonitor;
import org.springframework.core.io.ClassPathResource;

/**
 * A {@link FlowServiceConfig} that loads the configuration from a Java
 * properties file. Reloads the configuration when changes are made to the
 * properties file.
 * 
 * @author kumaramit01
 * @author shirk
 * @since 0.5.0
 */
public class PropertiesBasedFlowServiceConfig extends FlowServiceConfigBase 
   implements FileChangeListener {
   
   private static Logger logger = Logger.getLogger(
         PropertiesBasedFlowServiceConfig.class.getName());
   
   private String propertiesFileName;
   private Properties properties;
   private File propertiesFile;
   private ClassPathResource propertiesResource;
   private MeandreServerProxyConfig head;
   private Set<MeandreServerProxyConfig> workers;
   
   public PropertiesBasedFlowServiceConfig() {
   }

   @PostConstruct
   public void init() throws ConfigException {
      workers = new HashSet<MeandreServerProxyConfig>();
      
      propertiesResource = new ClassPathResource(propertiesFileName);

      loadProperties();
      head = loadHeadConfig();
      workers = loadWorkerConfigs();

      try {
         propertiesFile = propertiesResource.getFile();
         int monitorIntervalInSeconds = 2;
         FileMonitor configFileMonitor = new FileMonitor(this,propertiesFile,
               monitorIntervalInSeconds);
         configFileMonitor.start();
      } catch (IOException e) {
         logger.warning("A file monitor will not be created for " +
               "configuration file " + propertiesFileName + " because it " +
                     "cannot be resolved to an absolute file path.");
      }
   }

   /**
    * Loads the {@link MeandreServerProxyConfig}s for the worker servers.
    * 
    * @return The {@link MeandreServerProxyConfig}s for the worker servers.
    * @throws ConfigException if a problem occurs while loading the configs.
    */
   private Set<MeandreServerProxyConfig> loadWorkerConfigs() throws ConfigException {
      Set<MeandreServerProxyConfig> workers = 
         new HashSet<MeandreServerProxyConfig>();
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
            int port;
            int maxConcurrentJob;
            try {
               port = Integer.parseInt(portList[count]);
               maxConcurrentJob = Integer
                     .parseInt(maxConcurrentJobList[count]);
            } catch (NumberFormatException e) {
               throw new ConfigException(
                     "Invalid flow service configuration file. Check " +
                     "workerserver.port and workerserver.maxConcurrentJobs " +
                     "to make sure they contain only numbers.");
            }
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
      return workers;
   }

   /**
    * Loads the {@link MeandreServerProxyConfig} for the head server.
    * 
    * @return The {@link MeandreServerProxyConfig} for the head server.
    * @throws ConfigException if a problem occurs while loading the config.
    */
   private MeandreServerProxyConfig loadHeadConfig() throws ConfigException {
      String headServer = properties.getProperty("headserver.host");
      String headServerPortStr = properties.getProperty("headserver.port");
      String headUsername = properties.getProperty("headserver.username");
      String headPassword = properties.getProperty("headserver.password");
      String headMaxCurrentJobsStr = properties
            .getProperty("headserver.maxConcurrentJobs");

      if(nullOrEmpty(headServer) || nullOrEmpty(headServerPortStr) || 
            nullOrEmpty(headUsername) || nullOrEmpty(headPassword) || 
            nullOrEmpty(headMaxCurrentJobsStr)) {
         throw new ConfigException(
               "Invalid flow service configuration file. Some headserver " +
               "properties are missing.");
      }
      
      int headServerPort;
      int headMaxCurrentJobs;
      try {
         headServerPort = Integer.parseInt(headServerPortStr);
         headMaxCurrentJobs = Integer.parseInt(headMaxCurrentJobsStr);
      } catch (NumberFormatException e) {
         throw new ConfigException(
             "Invalid flow service configuration file. Check headserver.port " +
             "and headserver.maxConcurrentJobs to make sure they are numbers.");
      }

      return new SimpleMeandreServerProxyConfig(
            headUsername, headPassword, headServer, headServerPort,
            headMaxCurrentJobs);
   }

   /**
    * Load the properties file.
    * 
    * @throws ConfigException if a problem occurs while loading the properties
    * file.
    */
   private void loadProperties()
         throws ConfigException {
      if (propertiesResource == null) {
         throw new ConfigException(
               "Could not find flow service configuration file: " + 
                                                  propertiesFileName);
      }
      InputStream is = null;
      try {
         is = propertiesResource.getInputStream();
         properties = new Properties();
         properties.load(is);
      } catch (IOException e) {
         throw new ConfigException(e);
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      if (properties == null) {
         throw new ConfigException(
               "Invalid flow service configuration file. No properties found.");
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

   /**
    * Respond to changes made to the configuration file.
    * 
    * @param file A reference to the changed configuration file.
    */
   @Override
   public void fileChanged(File file) {
      try {
         logger.info("Flow Service configuration has changed. Reloading...");
         reload();
         logger.info("Notifiying listeners of updated configuration...");
         notifyListeners();
      } catch (ConfigException e) {
         logger.warning("A problem occured while reloading the "
               + "configuration file: " + e.getMessage());
      }
   }

   /**
    * Reload the configuration file.
    * 
    * @throws ConfigException if there is a problem with the configuration file.
    */
   public void reload() throws ConfigException {
      loadProperties();
      head = loadHeadConfig();
      workers = loadWorkerConfigs();
   }
   
   private boolean nullOrEmpty(String str) {
      if(null==str || "".equals(str)) {
         return true;
      }
      return false;
   }
}
