package org.imirsel.nema.flowmetadataservice;

import java.sql.SQLException;
import java.util.Map;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.flowservice.MeandreServerException;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Property;

/** Returns the component metadata
 * 
 * @author kumaramit01
 * @since 0.5.0
 */
public interface ComponentMetadataService{
	
	/**Returns the component property data types
	 * 
	 * @param component
	 * @param flowURI the URI identifying the flow
	 * @return HashMap of component properties and data type
	 * @throws TransmissionException 
	 * @throws SQLException 
	 * @throws MeandreServerException 
	 */
	public Map<String,Property> getComponentPropertyDataType(final Component component, String flowURI) throws TransmissionException, SQLException, MeandreServerException;
	
	
}
