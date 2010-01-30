package org.imirsel.nema.flowservice;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.imirsel.nema.flowservice.config.MeandreJobSchedulerConfig;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Property;

/**
 * Provides Repository functions to the {@link NemaFlowService}
 * @author kumaramit01
 * @since 0.5.0
 * 
 */
public class MeandreFlowStore {
	
	private static final Logger logger = 
			Logger.getLogger(MeandreFlowStore.class.getName());
	 
	private MeandreJobSchedulerConfig meandreJobSchedulerConfig;

	 
	public void setMeandreJobSchedulerConfig(MeandreJobSchedulerConfig meandreJobSchedulerConfig) {
		this.meandreJobSchedulerConfig = meandreJobSchedulerConfig;
	}

	public MeandreJobSchedulerConfig getMeandreJobSchedulerConfig() {
		return meandreJobSchedulerConfig;
	}

	 
	public List<Component> getComponents(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getConsole(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean removeFlow(String url) {
		// TODO Auto-generated method stub
		return false;
	}

	public String createNewFlow(HashMap<String, String> paramMap, String flowURI) {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<String, Property> getComponentPropertyDataType(
			Component component, String url) {
		// TODO Auto-generated method stub
		return null;
	}
	 
		

}
