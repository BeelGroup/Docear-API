package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;


@Entity
@Table(name = "google_document_queries")
public class GoogleDocumentQuery extends Resource {
	
	public final static Integer CREATED_BY_PAPER_TITLE = 0;
	public final static Integer CREATED_BY_RECOMMENDATION = 10;


	@Column(nullable = false)
	private String model;
	
	@Column(nullable = true)
	private Date query_date;
	
	@Column(nullable = true)
	private String lock_id;
	
	@Column(nullable = false)
	private Date created_date;
	
	private Integer priority;
		
	protected GoogleDocumentQuery() {
    	
    }
    
    public GoogleDocumentQuery(Session session) {
    	super();
    	this.setSession(session);
    }
    
    
    @Override
	public Resource getPersistentIdentity() {
		if (this.getId() != null) {
			return this.getGoogleDocumentQuery(this.getId());
		} else {
			return this.getGoogleDocumentQuery(getModel());
		}
	}
    
    public String getLockId() {
		return lock_id;
	}

	public void setLockId(String lock_id) {
		this.lock_id = lock_id;
	}

	public Date getCreated_date() {
		return created_date;
	}

	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}

	public Date getQuery_date() {
		return query_date;
	}

	public void setQuery_date(Date query_date) {
		this.query_date = query_date;
	}
    
    public GoogleDocumentQuery getGoogleDocumentQuery(Integer id) {
		return (GoogleDocumentQuery)this.getSession().get(GoogleDocumentQuery.class, id);
	}
    
    public GoogleDocumentQuery getGoogleDocumentQuery(String model) {
		if(model == null) {
			return null;
		}
		return (GoogleDocumentQuery)this.getSession().createCriteria(GoogleDocumentQuery.class)
		.add(Restrictions.eq("model", model))
		.setMaxResults(1)
		.uniqueResult();
	}
	
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	
}

