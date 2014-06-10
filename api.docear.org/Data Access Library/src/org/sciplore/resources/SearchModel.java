package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.sciplore.eventhandler.Required;

@Entity
@Table(name = "search_models")
public class SearchModel extends Resource {

	@ManyToOne
	@JoinColumn(name = "user_model_id")
	@Cascade(CascadeType.LOCK)
	@Required
	private UserModel userModel;
	
	@Column(nullable = false)
	private String model;
		
	@Column(nullable = true)
	private Integer varSize = 0;
	
	@Column(name = "execution_time")
	private Integer executionTime = 0;
			
	@Column(nullable = true)
	private Boolean old = null;
	
    public SearchModel(Session session) {
    	super();
    	this.setSession(session);
    }

	@Override
	public Resource getPersistentIdentity() {
		if (this.getId() != null) {
			return this.getSearchModel(this.getId());
		} 
		return null;
		
	}
	
	public SearchModel getSearchModel(Integer id) {
		return (SearchModel)this.getSession().get(SearchModel.class, id);
	}

	public UserModel getUserModel() {
		return userModel;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Integer executionTime) {
		this.executionTime = executionTime;
	}

	public Integer getVarSize() {
		return varSize;
	}

	public void setVarSize(Integer varSize) {
		this.varSize = varSize;
	}

	public Boolean getOld() {
		return old;
	}

	public void setOld(Boolean old) {
		this.old = old;
	}
	
		
}
