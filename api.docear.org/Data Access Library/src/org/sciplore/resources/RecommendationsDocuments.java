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
@Table(name = "recommendations_documents")
public class RecommendationsDocuments extends Resource {
	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "document_id")
	// @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	// @Required
	// private Document document;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fulltext_url_id")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@Required
	private FulltextUrl fulltextUrl;

	@Column()
	private Date clicked;

	@Column()
	private String hash_id = UUID.randomUUID().toString();

	@Column()
	private Integer original_rank;

	@Column()
	private Integer presentation_rank;

	@Column()
	private Float relevance;
	
	private Integer shownBefore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recommendations_documents_set_id")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@Required
	private RecommendationsDocumentsSet recommentationsDocumentsSet;										

	public RecommendationsDocuments() {
	}

	public RecommendationsDocuments(Session session) {
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
		return hash_id;
	}

	public void setHashId(String hash_id) {
		this.hash_id = hash_id;
	}

	public Integer getOriginal_rank() {
		return original_rank;
	}

	public void setOriginalRank(Integer original_rank) {
		this.original_rank = original_rank;
	}

	public Integer getPresentationRank() {
		return presentation_rank;
	}

	public void setPresentationRank(Integer presentation_rank) {
		this.presentation_rank = presentation_rank;
	}

	public Float getRelevance() {
		return relevance;
	}

	public void setRelevance(Float relevance) {
		this.relevance = relevance;
	}
	
	

	public Integer getShownBefore() {
		return shownBefore;
	}

	public void setShownBefore(Integer shownBefore) {
		this.shownBefore = shownBefore;
	}

	public RecommendationsDocumentsSet getRecommentationsDocumentsSet() {
		return recommentationsDocumentsSet;
	}

	public void setRecommentationsDocumentsSet(RecommendationsDocumentsSet recommentationsDocumentsSet) {
		this.recommentationsDocumentsSet = recommentationsDocumentsSet;
	}

}
