package org.imirsel.nema.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Proxy;

/**
 * Represents a Meandre flow.
 * 
 * @author shirk
 */
@Entity
@Table(name = "flow")
@Proxy(lazy = false)
public class Flow implements Serializable {
	public enum FlowType {
		INHERITS("Inherits"), FEATURE_EXTRACTION("Feature Extraction"), CLASSIFICATION(
				"Classification"), EVALUATION("Evaluation"), ANALYSIS(
				"Analysis");
		String name;

		private FlowType(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
        @Override
		public String toString() {
			return name;
		}
        public static FlowType toFlowType(String typeName) {
        	if(typeName.equals(INHERITS.getName())) {
        		return INHERITS;
        	} else if (typeName.equals(FEATURE_EXTRACTION.getName())) {
        		return FEATURE_EXTRACTION;
        	} else if (typeName.equals(CLASSIFICATION.getName())) {
        		return CLASSIFICATION;
        	} else if (typeName.equals(EVALUATION.getName())) {
        		return EVALUATION;
        	} else if (typeName.equals(ANALYSIS.getName())) {
        		return ANALYSIS;
        	} else {
        		return null;
        	}
        }
	}

	/**
	 * Version of this class.
	 */
	private static final long serialVersionUID = 8013326562531128503L;

	private Long id;
	private String name;
	private String description;
	private Date dateCreated = new Date();
	private String keyWords;
	private Boolean template = false;
	private String typeName;
	// TODO: change this to uri rather than url
	private String url;
	private Long creatorId;
	private Flow instanceOf = null;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description", nullable = false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "dateCreated", nullable = false)
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Column(name = "keyWords", nullable = false)
	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	@Column(name = "isTemplate", nullable = false)
	public Boolean isTemplate() {
		return template;
	}

	public void setTemplate(Boolean template) {
		this.template = template;
	}

	@Column(name = "typeName", nullable = false)
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
    @Transient
    public FlowType getType() {
    	return FlowType.toFlowType(typeName);
    }
    public void setType(FlowType flowType) {
    	this.typeName = flowType.getName();
    }
    
	@Column(name = "url", nullable = false)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "creatorId", nullable = false)
	public Long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Long creatorId) {
		this.creatorId = creatorId;
	}

	@JoinColumn(name = "instanceOf", nullable = true)
	@ManyToOne(fetch = FetchType.EAGER)
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
				+ ((template == null) ? 0 : template.hashCode());
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
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
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
