package org.imirsel.nema.flowmetadataservice;

/**
 * 
 * @author kumaramit01
 * @since 0.5.0
 */
public class CorruptedFlowException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 189677769986982847L;

	public CorruptedFlowException(String trace) {
		super(trace);
	}

	public CorruptedFlowException(String execStepMsg, Exception e) {
		super(e);
	}

}
