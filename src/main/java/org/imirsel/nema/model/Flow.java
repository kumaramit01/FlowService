package org.imirsel.nema.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="flow")
public class Flow implements Serializable {
	
	/**
	 * Version of this class.
	 */
	private static final long serialVersionUID = 8013326562531128503L;
	
	private Long id;
    private String name;
    private String description;
    private Date dateCreated;
    private String keyWords;
    private Boolean isTemplate;
    private String url;
    private Long creatorId;
    private Flow instanceOf;
    
    @Column(name="id")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
    @Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name="dateCreated")
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	@Column(name="keyWords")
	public String getKeyWords() {
		return keyWords;
	}
	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}
	@Column(name="isTemplate")
	public Boolean getIsTemplate() {
		return isTemplate;
	}
	public void setIsTemplate(Boolean isTemplate) {
		this.isTemplate = isTemplate;
	}
	@Column(name="url")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Column(name="creatorId")
	public Long getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(Long creatorId) {
		this.creatorId = creatorId;
	}
	@Column(name="flowInstanceId")
	public Flow getInstanceOf() {
		return instanceOf;
	}
	public void setInstanceOf(Flow instanceOf) {
		this.instanceOf = instanceOf;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((creatorId == null) ? 0 : creatorId.hashCode());
		result = prime * result
				+ ((dateCreated == null) ? 0 : dateCreated.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((instanceOf == null) ? 0 : instanceOf.hashCode());
		result = prime * result
				+ ((isTemplate == null) ? 0 : isTemplate.hashCode());
		result = prime * result
				+ ((keyWords == null) ? 0 : keyWords.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Flow other = (Flow) obj;
		if (creatorId == null) {
			if (other.creatorId != null)
				return false;
		} else if (!creatorId.equals(other.creatorId))
			return false;
		if (dateCreated == null) {
			if (other.dateCreated != null)
				return false;
		} else if (!dateCreated.equals(other.dateCreated))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (instanceOf == null) {
			if (other.instanceOf != null)
				return false;
		} else if (!instanceOf.equals(other.instanceOf))
			return false;
		if (isTemplate == null) {
			if (other.isTemplate != null)
				return false;
		} else if (!isTemplate.equals(other.isTemplate))
			return false;
		if (keyWords == null) {
			if (other.keyWords != null)
				return false;
		} else if (!keyWords.equals(other.keyWords))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
    
    
}
