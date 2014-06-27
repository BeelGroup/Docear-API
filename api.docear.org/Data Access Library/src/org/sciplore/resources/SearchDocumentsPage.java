package org.sciplore.resources;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
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
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.sciplore.eventhandler.Required;
import org.sciplore.queries.SearchDocumentsPageQueries;

@Entity
@Table(name = "search_documents_page")

public class SearchDocumentsPage extends Resource {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "search_documents_set_id")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@Required
	private SearchDocumentsSet searchDocumentsSet;		
	
	@Column()
	private Integer page;
	
    @Column()
    private Integer documentsPerPage;
   
    @Column
    private Date created;
    
    @Column(name = "computation_time")    
    private Long computationTime;

	@Column()
    private Date delivered;
	
    @Column()
    private Date received;
  
    @OneToMany(mappedBy = "searchDocumentsPage", fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @Sort(type=SortType.COMPARATOR, comparator=SearchDocuments.class)
    @OrderBy(clause = "presentationRank ASC")
	private SortedSet<SearchDocuments> searchDocuments = new TreeSet<SearchDocuments>(new Comparator<SearchDocuments>() {
		@Override
		public int compare(SearchDocuments o1, SearchDocuments o2) {
			return o1.getPresentationRank().compareTo(o2.getPresentationRank());
		}
	});
    
    public SearchDocumentsPage() {
    	
    }
    
    public SearchDocumentsPage(Session session) {
    	setSession(session);
	}

	@Override
	public Resource getPersistentIdentity() {   	
    	if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());			
		}
		else if (getSearchDocuments() != null && getPage() != null) {
			try {
				return SearchDocumentsPageQueries.getSearchDocumentsPage(getSession(), getSearchDocumentsSet(), getPage());
			}
			catch (Exception e) {				
				e.printStackTrace();
			}
		}
    	
    	return null;
	}

	public SearchDocumentsSet getSearchDocumentsSet() {
		return searchDocumentsSet;
	}

	public void setSearchDocumentsSet(SearchDocumentsSet searchDocumentsSet) {
		this.searchDocumentsSet = searchDocumentsSet;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}
	
	public Integer getDocumentsPerPage() {
		return documentsPerPage;
	}

	public void setDocumentsPerPage(Integer documentsPerPage) {
		this.documentsPerPage = documentsPerPage;
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

	public Date getReceived() {
		return received;
	}

	public void setReceived(Date received) {
		this.received = received;
	}

	public Set<SearchDocuments> getSearchDocuments() {
		return searchDocuments;
	}

	public void setSearchDocuments(SortedSet<SearchDocuments> searchDocuments) {
		this.searchDocuments = searchDocuments;
	}
}