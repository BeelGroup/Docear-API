package org.sciplore.resources;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.sciplore.queries.RecommendationsRatingsLabelQueries;


@Entity
@Table(name = "recommendations_ratings_labels")

public class RecommendationsRatingsLabel extends Resource {
	public static int TYPE_NORMAL = 1;
	public static int TYPE_FREE = 2;
	
	private Integer type;
	private String value;
	
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	

	@Override
	public Resource getPersistentIdentity() {
		return RecommendationsRatingsLabelQueries.getRecommendationsRatingsLabel(getSession(), getValue());
	}

}
