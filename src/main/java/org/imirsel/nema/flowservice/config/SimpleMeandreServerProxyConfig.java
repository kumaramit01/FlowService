package org.imirsel.nema.flowservice.config;

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;

public class SimpleMeandreServerProxyConfig implements MeandreServerProxyConfig{
	private String host;
	private int port;
	private int maxConcurrentJobs;
	private String username;
	private String password;

	public SimpleMeandreServerProxyConfig(String username,String password, String host,
			int port, int maxConcurrentJobs) {
		this.host = host;
		this.maxConcurrentJobs = maxConcurrentJobs;
		this.username = username;
		this.password = password;
		this.port = port;
		this.maxConcurrentJobs = maxConcurrentJobs;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.flowservice.MeandreServerProxyConfig#getHost()
	 */
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.flowservice.MeandreServerProxyConfig#getPort()
	 */
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.flowservice.MeandreServerProxyConfig#getMaxConcurrentJobs()
	 */
	public int getMaxConcurrentJobs() {
		return maxConcurrentJobs;
	}

	public void setMaxConcurrentJobs(int maxConcurrentJobs) {
		this.maxConcurrentJobs = maxConcurrentJobs;
	}

	/* (non-Javadoc)
	 * @see org.imirsel.nema.flowservice.MeandreServerProxyConfig#getUsername()
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