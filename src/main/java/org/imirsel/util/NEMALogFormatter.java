package org.imirsel.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** This class is used to display the formatted logs.
 * 
 * @author Amit Kumar
 * Created on Jul 18, 2008 11:53:22 PM
 *
 */
public class NEMALogFormatter extends Formatter {
	
	/** The maximum lenght of a thread name */
	private final static int MAX_THREAD_NAME_LENGTH = 40;
	
	/** The new line separator */
	private final static String NEW_LINE = System.getProperty("line.separator");
	
	/** The date formater */
	private final static SimpleDateFormat FORMATER = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
	
	/** Should the class be reported */
	private boolean bClass = true;

	/** Creates the default formater */
	public NEMALogFormatter () {
		bClass = true;
	}
	
	/** Creates the default formater allowing to choos is the reporting class should be reported.
	 * 
	 * @param bShowClass Should the class be shown?
	 */
	public NEMALogFormatter ( boolean bShowClass ) {
		bClass = bShowClass;
	}
	
	/** Formats the record.
	 * 
	 * @param record The log record to format
	 * @return The formated record
	 */
	  public String format(LogRecord record) {
		  String className = record.getSourceClassName();
		  
		  String threadName = Thread.currentThread().getName();
		  if(threadName!=null && threadName.length()>MAX_THREAD_NAME_LENGTH ){
			  threadName = threadName.substring(threadName.length()-MAX_THREAD_NAME_LENGTH);
		  }
		  
		  String sTimeStamp = FORMATER.format(new Date(record.getMillis()));
		  
		  return sTimeStamp+"::"+
		   		record.getLevel()+":  "+
		   		record.getMessage()+ "  " + 
		   		((bClass)?" ["+className+"."+record.getSourceMethodName() + "]":"")+
		   		((bClass)?" <"+threadName+":"+record.getThreadID()+">":"")+
		   		NEW_LINE;
	  }
}
