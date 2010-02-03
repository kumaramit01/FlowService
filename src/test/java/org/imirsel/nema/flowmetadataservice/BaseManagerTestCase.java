package org.imirsel.nema.flowmetadataservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * 
 * @author kumaramit01
 * @since 0.5.0
 */
public class BaseManagerTestCase {
	protected final Logger log = Logger.getLogger(getClass().getName());
	Properties properties;
	
	public static void main(String[] args){
		BaseManagerTestCase bmt = new BaseManagerTestCase();
	}


	public BaseManagerTestCase() {
		properties = new Properties();
		String propFile = this.getClass().getSimpleName()+".properties";
		InputStream in = getClass().getResourceAsStream(propFile);
		try {
			properties.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}




}
