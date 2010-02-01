package org.imirsel.nema.flowservice.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.imirsel.nema.flowservice.MeandreServerProxy;
import org.imirsel.nema.flowservice.monitor.JobStatusMonitor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * A {@link MeandreJobSchedulerConfig} intended to work in concert with a
 * Spring configuration file to inject the requisite properties.
 *
 * @author kumaramit01
 * @since 0.5.0
 * 
 */
public class PropertyMeandreJobSchedulerConfig implements
		MeandreJobSchedulerConfig {
	
	private JobStatusMonitor jobStatusMonitor;
	private Properties properties;
	private MeandreServerProxy head;
	private final Set<MeandreServerProxy> proxySet = new HashSet<MeandreServerProxy>();
	
	public PropertyMeandreJobSchedulerConfig(){
	
		
	}
	
	@PostConstruct
	public void init() throws MeandreServerProxyConfigException{
		Resource resource=new ClassPathResource("meandreserver.properties");
		if(resource==null){
			throw new MeandreServerProxyConfigException("could not find meandreserver.properties");
		}
		InputStream is=null;
		try {
			is=resource.getInputStream();
			properties= new Properties();
			properties.load(is);
		} catch (IOException e) {
			throw new MeandreServerProxyConfigException(e);
		}finally{
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(properties==null){
			throw new MeandreServerProxyConfigException("invalid meandre property list");
		}
		
		String _headServerPort=properties.getProperty("headserver.port");
		String _headMaxCurrentJobs=properties.getProperty("headserver.maxConcurrentJobs");
		String headUsername=properties.getProperty("headserver.username");
		String headPassword=properties.getProperty("headserver.password");
		String headServer=properties.getProperty("headserver.host");
		
		int headServerPort = Integer.parseInt(_headServerPort);
		int headMaxCurrentJobs= Integer.parseInt(_headMaxCurrentJobs);
		
		MeandreServerProxyConfig configuration = new MeandreServerProxyConfig(
				headUsername,headPassword,headServer,headServerPort, headMaxCurrentJobs);
		head = new MeandreServerProxy(configuration);
		head.setJobStatusMonitor(jobStatusMonitor);
		head.init();
		
		
		String hosts = properties.getProperty("workerserver.hosts");
		String ports = properties.getProperty("workerserver.ports");
		String usernames = properties.getProperty("workerserver.usernames");
		String passwords = properties.getProperty("workerserver.passwords");
		String maxConcurrentJobs = properties.getProperty("workerserver.maxConcurrentJobs");
		
		String[] hostList = hosts.split(",");
		String[] portList = ports.split(",");
		String[] usernameList = usernames.split(",");
		String[] passwordList = passwords.split(",");
		String[] maxConcurrentJobList = maxConcurrentJobs.split(",");
		
		
		
		if(hostList.length!=0 &&(hostList.length==portList.length) 
				&& (hostList.length==usernameList.length) 
				&&( hostList.length==passwordList.length) 
				&&( hostList.length==maxConcurrentJobList.length)){
				int count=0;
				for(String host:hostList){
					int port = Integer.parseInt(portList[count]);
					int maxConcurrentJob = Integer.parseInt(maxConcurrentJobList[count]);
					String username = usernameList[count];
					String password = passwordList[count];
					MeandreServerProxyConfig config = new 
					MeandreServerProxyConfig(username,password,host,port,maxConcurrentJob);
					MeandreServerProxy proxy = new MeandreServerProxy(config);
					proxy.setJobStatusMonitor(jobStatusMonitor);
					proxy.init();
					proxySet.add(proxy);
					count++;
				}
		}else{
			throw new MeandreServerProxyConfigException("invalid meandre property list");
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.imirsel.nema.flowservice.config.MeandreJobSchedulerConfig#getHead()
	 */
	@Override
	public MeandreServerProxy getHead() {
		return head;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.flowservice.config.MeandreJobSchedulerConfig#getServers()
	 */
	@Override
	public Set<MeandreServerProxy> getServers() {
		return proxySet;
	}

	public JobStatusMonitor getJobStatusMonitor() {
		return jobStatusMonitor;
	}

	public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
		this.jobStatusMonitor = jobStatusMonitor;
	}

}
