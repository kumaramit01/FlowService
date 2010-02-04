package org.imirsel.nema.flowservice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.imirsel.meandre.client.TransmissionException;
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

	/**Returns list of flow components
	 *  
	 * @param flowURI
	 * @return list of components
	 * @throws MeandreServerException 
	 */
	public List<Component> getComponents(String flowURI) throws MeandreServerException {
		return this.getMeandreJobSchedulerConfig().getHead().getFlowMetadataService().getComponents(flowURI);
	}

	/** Returns console as string
	 * 
	 * @param URI
	 * @return the console as string
	 * @throws MeandreServerException 
	 */
	public String getConsole(String URI) throws MeandreServerException {
		return this.getMeandreJobSchedulerConfig().getHead().getFlowMetadataService().getConsole(URI);
	}

	/**Removes the flow
	 * 
	 * @param URI
	 * @return the success true/false
	 * @throws MeandreServerException
	 */
	public boolean removeFlow(String URI) throws MeandreServerException {
		return this.getMeandreJobSchedulerConfig().getHead().getFlowMetadataService().removeFlow(URI);
	}

	/** Returns the new flow
	 * 
	 * @param paramMap
	 * @param flowURI
	 * @return the file path to the flow in the nt format
 	 * @throws MeandreServerException
	 * @throws CorruptedFlowException
	 */
	public String createNewFlow(HashMap<String, String> paramMap, String flowURI) throws MeandreServerException {
		return this.getMeandreJobSchedulerConfig().getHead().
		getFlowMetadataService().createNewFlow(paramMap, flowURI);
	}

	/**
	 * 
	 * @param component
	 * @param flowURI
	 * @return The Map of Component property names and the data types
	 * @throws MeandreServerException
	 */
	public Map<String, Property> getComponentPropertyDataType(
			Component component, String flowURI) throws MeandreServerException {
		try {
			return this.getMeandreJobSchedulerConfig().getHead().getComponentMetadataService().getComponentPropertyDataType(component, flowURI);
		} catch (TransmissionException e) {
			throw new MeandreServerException(e.getMessage());
		} catch (SQLException e) {
			throw new MeandreServerException(e.getMessage());
		}
		
	}
	 
		

}
