package org.imirsel.nema.dao.impl;

import java.util.List;

import org.imirsel.nema.dao.JobDao;
import org.imirsel.nema.model.Job;
import org.junit.Test;

public class JobDaoTest extends BaseDaoTestCase {
	
	
	private JobDao jobDao;


	@Test
	public void testGetJob(){
        // query with jdbc template to get expected value
        String query = "select id from job where id=1";
        String id  = (String)this.jdbcTemplate.queryForObject(query, String.class);
        assertNotNull("Job of id 1 was not found", id);
        Job job = this.jobDao.findById(1L,false);
        assertNotNull("Job of id 1 was not found by service", job);
	}
	
	@Test
	public void testJobDaoGet(){
		List<Job> jobList=this.getJobDao().getJobsByOwnerId(1l);
		System.out.println(jobList.size());
		assertTrue(jobList.size()>1);
	}
	
	
	public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}
	

}
