package org.imirsel.nema.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Proxy;

/**
 * Represents an output resulting from a job execution.
 * 
 * @author shirk
 * @since 1.0
 */
@Entity
@Table(name="jobResult")
@Proxy(lazy=false)
public class JobResult implements Serializable {

	/**
	 * The version of this class.
	 */
	private static final long serialVersionUID = -5142973545301953290L;
	private Long id;
	private Job job;
	private String url;
	private String resultType;
	
	@Id
	@Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@JoinColumn(name = "jobId")
	@ManyToOne(fetch=FetchType.EAGER,optional=false)
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
	@Column(name="url",nullable=false,length=20000000)
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Column(name="resultType",nullable=false)
	public String getResultType() {
		return resultType;
	}
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((job == null) ? 0 : job.hashCode());
		result = prime * result
				+ ((resultType == null) ? 0 : resultType.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobResult other = (JobResult) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (job == null) {
			if (other.job != null)
				return false;
		} else if (!job.equals(other.job))
			return false;
		if (resultType == null) {
			if (other.resultType != null)
				return false;
		} else if (!resultType.equals(other.resultType))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	
}
