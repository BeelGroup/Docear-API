package org.sciplore.resources;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.sciplore.eventhandler.Required;
import org.sciplore.queries.RecommendationsEvaluatorCacheQueries;

@Entity
@Table(name = "recommendations_evaluator_chache")
public class RecommendationsEvaluatorCache extends Resource {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")	
	@Required
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "latest_mindmaps_pdfhash_id")	
	@Required
	private MindmapsPdfHash latestNewMindmapsPdfHash;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public MindmapsPdfHash getLatestNewMindmapsPdfHash() {
		return latestNewMindmapsPdfHash;
	}

	public void setLatestNewMindmapsPdfHash(MindmapsPdfHash latestNewMindmapsPdfHash) {
		this.latestNewMindmapsPdfHash = latestNewMindmapsPdfHash;
	}

	@Override
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		if (this.getUser() != null) {
			return RecommendationsEvaluatorCacheQueries.getRecommendationsEvaluatorCacheQueries(getSession(), this.getUser());
		}
		return null;
	}

}
