package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.sciplore.eventhandler.Required;
import org.sciplore.queries.RecommendationsUsersSettingsQueries;

@Entity
@Table(name = "recommendations_users_settings")

public class RecommendationsUsersSettings extends Resource {
	
	@OneToOne
	@JoinColumn(name = "user_id")
	@Required
    private User user;
	
	@ManyToOne
	@JoinColumn(name = "recommendations_labels_id")
	@Required
	private RecommendationsLabel recommendationLabel;
	
	@ManyToOne
	@JoinColumn(name = "recommendations_ratings_labels_id")
	@Required
	private RecommendationsRatingsLabel recommendationRatingLabel;
	
	@Column(name = "use_prefix")
	private Boolean usePrefix;
	
	@Column(name = "highlight")
	private Boolean highlight;

	
	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public RecommendationsLabel getRecommendationLabel() {
		return recommendationLabel;
	}

	public void setRecommendationLabel(RecommendationsLabel recommendationLabel) {
		this.recommendationLabel = recommendationLabel;
	}
	
	public RecommendationsRatingsLabel getRecommendationRatingLabel() {
		return recommendationRatingLabel;
	}

	public void setRecommendationRatingLabel(RecommendationsRatingsLabel recommendationRatingLabel) {
		this.recommendationRatingLabel = recommendationRatingLabel;
	}


	public Boolean getUsePrefix() {
		return usePrefix;
	}


	public void setUsePrefix(Boolean usePrefix) {
		this.usePrefix = usePrefix;
	}


	public Boolean getHighlight() {
		return highlight;
	}


	public void setHighlight(Boolean highlight) {
		this.highlight = highlight;
	}


	@Override
	public synchronized Resource getPersistentIdentity() {
		return RecommendationsUsersSettingsQueries.getRecommendationsUsersSettings(getSession(), getUser());
	}


}
