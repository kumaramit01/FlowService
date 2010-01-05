package org.imirsel.nema.model;

import org.junit.Test;


public class JobTest {

	@Test
	public void testJobStatus() {
		assert Job.JobStatus.ABORTED.toString() == "Aborted";
		System.out.println(Job.JobStatus.ABORTED);
	}
}
