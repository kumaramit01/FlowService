/**
 * 
 */
package org.imirsel.nema.flowservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.imirsel.nema.flowservice.config.ConfigException;
import org.imirsel.nema.flowservice.config.PropertiesBasedFlowServiceConfig;
import org.junit.Test;
/**
 * @author kumaramit01
 * @since 0.5.0
 */
public class PropertyMeandreJobSchedulerConfigTest {

	
	/**
	 * Test method for {@link org.imirsel.nema.flowservice.config.PropertiesBasedFlowServiceConfig#init()}.
	 */
	@Test
	public void testInit() {
		PropertiesBasedFlowServiceConfig pmc = new PropertiesBasedFlowServiceConfig();
		try {
			pmc.init();
		} catch (ConfigException e) {
			fail(e.getMessage());
         e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.imirsel.nema.flowservice.config.PropertiesBasedFlowServiceConfig#getHeadConfig()}.
	 */
	@Test
	public void testGetHead() {
		PropertiesBasedFlowServiceConfig pmc = new PropertiesBasedFlowServiceConfig();
		try {
			pmc.init();
		} catch (ConfigException e) {
			fail(e.getMessage());
         e.printStackTrace();
		}
		assertEquals(pmc.getHeadConfig().getHost().equalsIgnoreCase("128.174.154.145"),true);
	}

	/**
	 * Test method for {@link org.imirsel.nema.flowservice.config.PropertiesBasedFlowServiceConfig#getWorkerConfigs()}.
	 */
	@Test
	public void testGetServers() {
		PropertiesBasedFlowServiceConfig pmc = new PropertiesBasedFlowServiceConfig();
		try {
			pmc.init();
		} catch (ConfigException e) {
			fail(e.getMessage());
         e.printStackTrace();
		}
		assertEquals(pmc.getWorkerConfigs().size(),2);
	}

}
