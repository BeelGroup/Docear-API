package org.sciplore.resources;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

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

@Entity
@Table(name = "search_documents_set")

public class SearchDocumentsSet extends Resource {
	
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

    @Column()
    private Integer userRating;
    
    @Column()
    private int documentsAvailable;
   
    @OneToMany(mappedBy = "searchDocumentsSet", fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)    
	private Set<SearchDocumentsPage> searchDocumentsPage = new TreeSet<SearchDocumentsPage>();
    
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
			return null;
		}
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

	public Integer getUserRating() {
		return userRating;
	}

	public void setUserRating(Integer userRating) {
		this.userRating = userRating;
	}

	public Set<SearchDocumentsPage> getSearchDocumentsPage() {
		return searchDocumentsPage;
	}

	public void setSearchDocumentsPage(Set<SearchDocumentsPage> searchDocumentsPage) {
		this.searchDocumentsPage = searchDocumentsPage;
	}

	public int getDocumentsAvailable() {
		return documentsAvailable;
	}

	public void setDocumentsAvailable(int documentsAvailable) {
		this.documentsAvailable = documentsAvailable;
	}
	
}