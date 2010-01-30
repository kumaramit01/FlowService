package org.imirsel.nema.flowmetadataservice;

import java.sql.SQLException;
import java.util.Map;

import org.imirsel.meandre.client.TransmissionException;
import org.imirsel.nema.model.Component;
import org.imirsel.nema.model.Property;

/** Returns the component metadata
 * 
 * @author kumaramit01
 *
 */
public interface ComponentMetadataService{
	
	/**Returns the component property data types
	 * 
	 * @param component
	 * @param flowUri 
	 * @return HashMap of component properties and data type
	 * @throws TransmissionException 
	 * @throws SQLException 
	 */
	public Map<String,Property> getComponentPropertyDataType(final Component component, String flowUri) throws TransmissionException, SQLException;
	
	
}
