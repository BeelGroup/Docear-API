package org.sciplore.resources;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.sciplore.eventhandler.Required;

@Entity
@Table(name = "search_documents")
public class SearchDocuments extends Resource implements Comparable<SearchDocuments>{

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "search_documents_page_id")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@Required
	private SearchDocumentsPage searchDocumentsPage;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fulltext_url_id")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@Required
	private FulltextUrl fulltextUrl;

	@Column()
	private Date clicked;

	@Column()
	private String hashId = UUID.randomUUID().toString();

	@Column()
	private Integer originalRank;

	@Column()
	private Integer presentationRank;
	
	@Column()
	private Float relevance;
	
	public SearchDocuments() {
	}

	public SearchDocuments(Session session) {
		this.setSession(session);
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

	public SearchDocumentsPage getSearchDocumentsPage() {
		return searchDocumentsPage;
	}

	public void setSearchDocumentsPage(SearchDocumentsPage searchDocumentsPage) {
		this.searchDocumentsPage = searchDocumentsPage;
	}

	public FulltextUrl getFulltextUrl() {
		return fulltextUrl;
	}

	public void setFulltextUrl(FulltextUrl fulltextUrl) {
		this.fulltextUrl = fulltextUrl;
	}

	public Date getClicked() {
		return clicked;
	}

	public void setClicked(Date clicked) {
		this.clicked = clicked;
	}

	public String getHashId() {
		return hashId;
	}

	public void setHashId(String hashId) {
		this.hashId = hashId;
	}

	public Integer getOriginalRank() {
		return originalRank;
	}

	public void setOriginalRank(Integer originalRank) {
		this.originalRank = originalRank;
	}

	public Integer getPresentationRank() {
		return presentationRank;
	}
	
	
	/**
	 * @return returns the index of the document in the whole set (starting with 0, ending with set.size-1), this method returns presentationRank-1 for convenience
	 */
	public Integer getPresentationIndex() {
		if (presentationRank == null) {
			return presentationRank;
		}
		return presentationRank-1;
	}

	public void setPresentationRank(Integer presentationRank) {
		this.presentationRank = presentationRank;
	}

	public Float getRelevance() {
		return relevance;
	}

	public void setRelevance(Float relevance) {
		this.relevance = relevance;
	}

	@Override
	public int compareTo(SearchDocuments o) {
		return this.getPresentationRank().compareTo(o.getPresentationRank());
	}

}
