package org.imirsel.nema.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name="job")
public class Job {

	static public enum JobStatus {
      UNKNOWN(-1), SUBMITTED(0), STARTED(1), ENDED(2);

      private final int code;

      private JobStatus(int code) { this.code = code; }

      private int getCode() { return code; }

      @Override
	public String toString() {
         String name = null;
         switch (code) {

            case -1: {
               name = "Unknown";
            }

            case 0: {
               name = "Submitted";
            }

            case 1: {
               name = "Started";
            }

            case 2: {
               name = "Ended";
            }
         }
         return name;
      }

      static public JobStatus toJobStatus(int code) {
         JobStatus status = null;
         switch (code) {

            case -1: {
               status = JobStatus.UNKNOWN;
            }

            case 0: {
               status = JobStatus.SUBMITTED;
            }

            case 1: {
               status = JobStatus.STARTED;
            }

            case 2: {
               status = JobStatus.ENDED;
            }
         }
         return status;
      }
   }
	
	private Long id;
	private String token;
	private String name;
	private String description;
	private String host;
	private Integer port;
	private Date submitTimestamp;
	private Date startTimestamp;
	private Date endTimestamp;
	private Date updateTimestamp;
	private Integer statusCode = -1;
	private Long ownerId;
	private String ownerEmail;
	private Flow flow;
	private String executionInstanceId;
	
	@Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name="host")
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	@Column(name="port")
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	@Column(name="submitTimestamp")
	public Date getSubmitTimestamp() {
		return submitTimestamp;
	}
	public void setSubmitTimestamp(Date submitTimestamp) {
		this.submitTimestamp = submitTimestamp;
	}
	@Column(name="startTimestamp")
	public Date getStartTimestamp() {
		return startTimestamp;
	}
	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
	@Column(name="endTimestamp")
	public Date getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	@Column(name="statusCode")
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	@Column(name="token")
    public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="updateTimestamp")
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}
	public void setUpdateTimestamp(Date updateTimestamp) {
		this.updateTimestamp = updateTimestamp;
	}
	@Column(name="ownerId")
	public Long getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}
	@Column(name="ownerEmail")
	public String getOwnerEmail() {
		return ownerEmail;
	}
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	@Column(name="flowId")
	public Flow getFlow() {
		return flow;
	}
	public void setFlow(Flow flow) {
		this.flow = flow;
	}
	@Column(name="executionInstanceId")
	public String getExecutionInstanceId() {
		return executionInstanceId;
	}
	public void setExecutionInstanceId(String executionInstanceId) {
		this.executionInstanceId = executionInstanceId;
	}
	public JobStatus getJobStatus() {
	      return JobStatus.toJobStatus(statusCode);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((endTimestamp == null) ? 0 : endTimestamp.hashCode());
		result = prime
				* result
				+ ((executionInstanceId == null) ? 0 : executionInstanceId
						.hashCode());
		result = prime * result + ((flow == null) ? 0 : flow.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((ownerEmail == null) ? 0 : ownerEmail.hashCode());
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result
				+ ((startTimestamp == null) ? 0 : startTimestamp.hashCode());
		result = prime * result
				+ ((statusCode == null) ? 0 : statusCode.hashCode());
		result = prime * result
				+ ((submitTimestamp == null) ? 0 : submitTimestamp.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result
				+ ((updateTimestamp == null) ? 0 : updateTimestamp.hashCode());
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
		Job other = (Job) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (endTimestamp == null) {
			if (other.endTimestamp != null)
				return false;
		} else if (!endTimestamp.equals(other.endTimestamp))
			return false;
		if (executionInstanceId == null) {
			if (other.executionInstanceId != null)
				return false;
		} else if (!executionInstanceId.equals(other.executionInstanceId))
			return false;
		if (flow == null) {
			if (other.flow != null)
				return false;
		} else if (!flow.equals(other.flow))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (ownerEmail == null) {
			if (other.ownerEmail != null)
				return false;
		} else if (!ownerEmail.equals(other.ownerEmail))
			return false;
		if (ownerId == null) {
			if (other.ownerId != null)
				return false;
		} else if (!ownerId.equals(other.ownerId))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (startTimestamp == null) {
			if (other.startTimestamp != null)
				return false;
		} else if (!startTimestamp.equals(other.startTimestamp))
			return false;
		if (statusCode == null) {
			if (other.statusCode != null)
				return false;
		} else if (!statusCode.equals(other.statusCode))
			return false;
		if (submitTimestamp == null) {
			if (other.submitTimestamp != null)
				return false;
		} else if (!submitTimestamp.equals(other.submitTimestamp))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (updateTimestamp == null) {
			if (other.updateTimestamp != null)
				return false;
		} else if (!updateTimestamp.equals(other.updateTimestamp))
			return false;
		return true;
	}

	
}
