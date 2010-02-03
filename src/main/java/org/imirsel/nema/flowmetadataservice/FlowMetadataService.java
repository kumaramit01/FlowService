package org.imirsel.nema.flowmetadataservice;

import java.util.HashMap;
import java.util.List;

import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.model.Component;

/**
 * Methods and functions to create new flow and retrieve flow components.
 * @author kumaramit01
 * @since 0.5.0
 *
 */
public interface FlowMetadataService {
	/**Returns the list of components that make a flow
	 * 
	 * @param flowUrl
	 * @return List<Component> List of the Components
	 * @throws MeandreServerException 
	 */
	public List<Component> getComponents(String flowUrl) throws  MeandreServerException;

	/**Returns the uri of the new flow this would create after
	 * merging the flowUri and the parameterMap
	 * 
	 * @param paramMap
	 * @param flowUri
	 * @return URI The flow URI
	 * @throws MeandreServerException 
	 * @throws CorruptedFlowException 
	 */
	public String createNewFlow(final HashMap<String, String> paramMap, String flowUri) throws MeandreServerException;
	
	/** Removes the flow
	 * 
	 * @param URI
	 * @return  success returns true/false
	 * @throws MeandreServerException 
	 */
	public boolean removeFlow(String URI) throws MeandreServerException;

	/**Gets the console
	 * 
	 * @param URI
	 * @return console The string 
	 * @throws MeandreServerException 
	 */
	public String getConsole(String URI) throws  MeandreServerException;
}
