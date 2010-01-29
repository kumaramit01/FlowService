package org.imirsel.nema.flowmetadataservice;

public class CorruptedFlowException extends Exception {

	public CorruptedFlowException(String trace) {
		super(trace);
	}

	public CorruptedFlowException(String execStepMsg, Exception e) {
		super(e);
	}

}
