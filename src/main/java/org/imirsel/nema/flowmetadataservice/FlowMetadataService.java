package org.imirsel.nema.flowmetadataservice;

import java.util.HashMap;
import java.util.List;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.model.Component;

public interface FlowMetadataService {
	/**Returns the list of components that make a flow
	 * 
	 * @param flowUrl
	 * @return
	 */
	public List<Component> getComponents(String flowUrl) throws TransmissionException;

	/**Returns the uri of the new flow this would create after
	 * merging the flowUri and the parameterMap
	 * 
	 * @param paramMap
	 * @param flowUri
	 * @return
	 * @throws MeandreCommunicationException 
	 * @throws CorruptedFlowException 
	 */
	public String createNewFlow(final HashMap<String, String> paramMap, String flowUri) throws MeandreServerException, CorruptedFlowException;
	
	/** Removes the flow
	 * 
	 * @param uri
	 * @return
	 * @throws TransmissionException
	 */
	public boolean removeFlow(String uri) throws MeandreServerException;

	/**Gets the console
	 * 
	 * @param uri
	 * @return
	 * @throws TransmissionException
	 * @throws MeandreServerException 
	 */
	public String getConsole(String uri) throws  MeandreServerException;
}
