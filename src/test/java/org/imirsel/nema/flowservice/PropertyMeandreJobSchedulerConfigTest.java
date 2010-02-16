/**
 * 
 */
package org.imirsel.nema.flowservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.imirsel.nema.flowservice.config.MeandreServerProxyConfigException;
import org.imirsel.nema.flowservice.config.PropertyMeandreJobSchedulerConfig;
import org.junit.Test;
/**
 * @author kumaramit01
 * @since 0.5.0
 */
public class PropertyMeandreJobSchedulerConfigTest {

	
	/**
	 * Test method for {@link org.imirsel.nema.flowservice.config.PropertyMeandreJobSchedulerConfig#init()}.
	 */
	@Test
	public void testInit() {
		PropertyMeandreJobSchedulerConfig pmc = new PropertyMeandreJobSchedulerConfig();
		try {
			pmc.init();
		} catch (MeandreServerProxyConfigException e) {
			fail(e.getMessage());
		} catch (MeandreServerException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.imirsel.nema.flowservice.config.PropertyMeandreJobSchedulerConfig#getHead()}.
	 */
	@Test
	public void testGetHead() {
		PropertyMeandreJobSchedulerConfig pmc = new PropertyMeandreJobSchedulerConfig();
		try {
			pmc.init();
		} catch (MeandreServerProxyConfigException e) {
			fail(e.getMessage());
		} catch (MeandreServerException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		assertEquals(pmc.getHead().getMeandreServerProxyConfig().getHost().equalsIgnoreCase("128.174.154.145"),true);
	}

	/**
	 * Test method for {@link org.imirsel.nema.flowservice.config.PropertyMeandreJobSchedulerConfig#getServers()}.
	 */
	@Test
	public void testGetServers() {
		PropertyMeandreJobSchedulerConfig pmc = new PropertyMeandreJobSchedulerConfig();
		try {
			pmc.init();
		} catch (MeandreServerProxyConfigException e) {
			fail(e.getMessage());
		} catch (MeandreServerException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		assertEquals(pmc.getServers().size(),2);
	}

}
