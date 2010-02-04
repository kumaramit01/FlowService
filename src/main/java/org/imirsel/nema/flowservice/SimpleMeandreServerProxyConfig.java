package org.imirsel.nema.flowservice;

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfig;

public class SimpleMeandreServerProxyConfig implements MeandreServerProxyConfig{
	private String host;
	private int port;
	private int maxConcurrentJobs;
	private String username;
	private String password;

	public SimpleMeandreServerProxyConfig(int maxConcurrentJobs, String username,
			String password) {
		this.maxConcurrentJobs = maxConcurrentJobs;
		this.username = username;
		this.password = password;
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
}