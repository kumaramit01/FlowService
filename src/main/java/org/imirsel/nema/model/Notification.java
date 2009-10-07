package org.imirsel.nema.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;

@Entity
@Table(name="notification")
public class Notification implements Serializable {
	/**
	 * Version of this class.
	 */
	private static final long serialVersionUID = -2583085614468213225L;
	
	private Long id;
	private Long recipientId;
	private Date dateCreated;
	private String message;
	private Job job;
	
	@Id
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="recipientId")
	public Long getRecipientId() {
		return recipientId;
	}
	public void setRecipientId(Long recipientId) {
		this.recipientId = recipientId;
	}
	@Column(name="dateCreated")
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	@Column(name="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="jobId")
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}


	
	
	
}
