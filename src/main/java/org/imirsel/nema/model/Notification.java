package org.imirsel.nema.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

/**
 * Represents a notification to be sent to an end user.
 * 
 * @author shirk
 * @since 1.0
 */
@Entity
@Table(name="notification")
@Proxy(lazy=false)
public class Notification implements Serializable {
	public enum DeliveryStatus {
		UNSENT(-1), SUCCESS(1), FAILURE(0);
		
	    private final int code;

	    private DeliveryStatus(int code) { this.code = code; }

	    public int getCode() { return code; }
	    
	    @Override
		public String toString() {
	         String name = null;
	         switch (code) {

	            case -1: {
	               name = "Unsent";
	               break;
	            }
	         
	            case 0: {
	               name = "Failure";
	               break;
	            }

	            case 1: {
	               name = "Success";
	               break;
	            }
	            
	         }
	         return name;
	      }

	      static public DeliveryStatus toDeliveryStatus(int code) {
	         DeliveryStatus status = null;
	         switch (code) {

	            case -1: {
	               status = DeliveryStatus.UNSENT;
	               break;
	            }

	            case 0: {
	               status = DeliveryStatus.FAILURE;
	               break;
	             }
	            
	            case 1: {
	               status = DeliveryStatus.SUCCESS;
	               break;
	            }
	            
	         }
	         return status;
	      }
	}
	/**
	 * Version of this class.
	 */
	private static final long serialVersionUID = -2583085614468213225L;
	
	private Long id;
	private Long recipientId;
	private String recipientEmail;
	private Date dateCreated = new Date();
	private String message;
	private String subject;
	private String errorMessage;
	private Integer deliveryStatusCode = -1;
	
	@Id
	@Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="recipientId",nullable=false)
	public Long getRecipientId() {
		return recipientId;
	}
	public void setRecipientId(Long recipientId) {
		this.recipientId = recipientId;
	}
	@Column(name="dateCreated",nullable=false)
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	@Column(name="message",nullable=false,length=20000000)
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Column(name="deliveryStatusCode",nullable=false)
	public Integer getDeliveryStatusCode() {
		return deliveryStatusCode;
	}
	public void setDeliveryStatusCode(Integer deliveryStatusCode) {
		this.deliveryStatusCode = deliveryStatusCode;
	}
	@Transient
	public DeliveryStatus getDeliveryStatus() {
	      return DeliveryStatus.toDeliveryStatus(deliveryStatusCode);
	}
	public void setDeliveryStatus(DeliveryStatus status) {
		this.deliveryStatusCode = status.getCode();
	}
	@Column(name="recipientEmail",nullable=false)
	public String getRecipientEmail() {
		return recipientEmail;
	}
	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}
	@Column(name="subject")
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	@Column(name="errorMessage")
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Notification other = (Notification) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
