package org.imirsel.nema.flowservice.config;

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;

/**
 * A {@link MeandreServerProxyConfig} meant to simply be instantiated or
 * injected with the configuration properties.
 * 
 * @author shirk
 * @since 0.4.0
 */
public class SimpleMeandreServerProxyConfig implements MeandreServerProxyConfig{
	private String host;
	private int port;
	private int maxConcurrentJobs;
	private String username;
	private String password;

	/**
	 * Create a new instance with the given configuration properties.
	 * 
	 * @param username The username required to connect to the Meandre server.
	 * @param password The password that corresponds to the username.
	 * @param host Address of the machine the Meandre server is running on.
	 * @param port Port the Meandre server is running on.
	 * @param maxConcurrentJobs The maximum number of concurrently executing
	 * jobs that should be allowed on this server. This number should largely
	 * correspond to the number of processors in the machine.
	 */
	public SimpleMeandreServerProxyConfig(String username,String password, String host,
			int port, int maxConcurrentJobs) {
		this.host = host;
		this.maxConcurrentJobs = maxConcurrentJobs;
		this.username = username;
		this.password = password;
		this.port = port;
		this.maxConcurrentJobs = maxConcurrentJobs;
	}

	/**
	 * @see MeandreServerProxyConfig#getHost()
	 */
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @see MeandreServerProxyConfig#getPort()
	 */
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @see MeandreServerProxyConfig#getMaxConcurrentJobs()
	 */
	public int getMaxConcurrentJobs() {
		return maxConcurrentJobs;
	}

	public void setMaxConcurrentJobs(int maxConcurrentJobs) {
		this.maxConcurrentJobs = maxConcurrentJobs;
	}

	/**
	 * @see MeandreServerProxyConfig#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((host == null) ? 0 : host.hashCode());
      result = prime * result + port;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      SimpleMeandreServerProxyConfig other = (SimpleMeandreServerProxyConfig) obj;
      if (host == null) {
         if (other.host != null)
            return false;
      } else if (!host.equals(other.host))
         return false;
      if (port != other.port)
         return false;
      return true;
   }
	
}