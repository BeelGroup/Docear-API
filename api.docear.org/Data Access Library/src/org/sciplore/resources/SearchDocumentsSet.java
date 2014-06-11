package org.sciplore.resources;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.sciplore.eventhandler.Required;
import org.sciplore.queries.SearchDocumentsSetQueries;

@Entity
@Table(name = "search_documents_set")

public class SearchDocumentsSet extends Resource {
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")    
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @Required
    private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")    
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @Required
    private Application application;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_model_id") 
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @Required
    private SearchModel searchModel;
	
	@Column()
	private String query;
	
    @Column()
    private Date created;
   
    @Column(name = "computation_time")    
    private Long computationTime;

	@Column()
    private Date delivered;
	
	@Column()
    private Date deliveryTime;
	
    @Column()
    private Date received;
   
    @Column()
    private Integer userRating;
    
    @Column()
    private Integer varAmountShould;
    
    @OneToMany(mappedBy = "searchDocumentsSet", fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)    
	private Set<SearchDocuments> searchDocuments = new HashSet<SearchDocuments>();
    
    public SearchDocumentsSet() {
    	
    }
    
    public SearchDocumentsSet(Session session) {
    	setSession(session);
	}

	@Override
	public Resource getPersistentIdentity() {    	
    	if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());			
		}
		else {
			return SearchDocumentsSetQueries.getPersistentIdentity(getSession(), this);
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public SearchModel getSearchModel() {
		return searchModel;
	}

	public void setSearchModel(SearchModel searchModel) {
		this.searchModel = searchModel;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Long getComputationTime() {
		return computationTime;
	}

	public void setComputationTime(Long computationTime) {
		this.computationTime = computationTime;
	}

	public Date getDelivered() {
		return delivered;
	}

	public void setDelivered(Date delivered) {
		this.delivered = delivered;
	}

	public Date getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Date deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public Date getReceived() {
		return received;
	}

	public void setReceived(Date received) {
		this.received = received;
	}

	public Integer getUserRating() {
		return userRating;
	}

	public void setUserRating(Integer userRating) {
		this.userRating = userRating;
	}

	public Integer getVarAmountShould() {
		return varAmountShould;
	}

	public void setVarAmountShould(Integer varAmountShould) {
		this.varAmountShould = varAmountShould;
	}

	public Set<SearchDocuments> getSearchDocuments() {
		return searchDocuments;
	}

	public void setSearchDocuments(Set<SearchDocuments> searchDocuments) {
		this.searchDocuments = searchDocuments;
	}	
}