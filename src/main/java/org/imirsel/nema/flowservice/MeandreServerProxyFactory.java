package org.imirsel.nema.flowservice;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.imirsel.nema.repository.RepositoryClientConnectionPool;

@ThreadSafe
public class MeandreServerProxyFactory {

   private JobStatusMonitor jobStatusMonitor;
   
   private RepositoryClientConnectionPool repositoryClientConnectionPool;
   
   private Map<String,MeandreServerProxy> proxyInstances = 
      new HashMap<String,MeandreServerProxy>(8);
    
   public synchronized MeandreServerProxy getServerProxyInstance(MeandreServerProxyConfig config) {
      String key = keyFor(config);
      MeandreServerProxy instance = null;
      if(proxyInstances.containsKey(key)) {
         instance = proxyInstances.get(key);
      } else {
         instance = new MeandreServerProxy(config);
         instance.setJobStatusMonitor(jobStatusMonitor);
         instance.setRepositoryClientConnectionPool(repositoryClientConnectionPool);
         instance.init();
         proxyInstances.put(key, instance);
      }
      return instance;
   }
   
   private String keyFor(MeandreServerProxyConfig config) {
      return config.getHost()+":"+config.getPort();
   }
   
   public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
      this.jobStatusMonitor = jobStatusMonitor;
   }
   
   public JobStatusMonitor getJobStatusMonitor() {
      return jobStatusMonitor;
   }

   public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
      return repositoryClientConnectionPool;
   }

   public void setRepositoryClientConnectionPool(
         RepositoryClientConnectionPool repositoryClientConnectionPool) {
      this.repositoryClientConnectionPool = repositoryClientConnectionPool;
   }
   
}
