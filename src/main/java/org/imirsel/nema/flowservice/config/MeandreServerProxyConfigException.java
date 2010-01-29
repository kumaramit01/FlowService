package org.imirsel.nema.flowservice.config;

import java.io.IOException;

/**Indicates there was a misconfigured meandre server
 * 
 * @author amitku
 * @since 0.5.0
 */
public class MeandreServerProxyConfigException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6424395100840248364L;

	public MeandreServerProxyConfigException(String message) {
		super(message);
	}

	public MeandreServerProxyConfigException(IOException e) {
		super(e);
	}

}
