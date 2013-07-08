package org.sciplore.resources;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.sciplore.queries.RecommendationsLabelQueries;


@Entity
@Table(name = "recommendations_labels")

public class RecommendationsLabel extends Resource {
	public static int TYPE_ORGANIC = 1;
	public static int TYPE_SPONSORED = 2;
	
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
		return RecommendationsLabelQueries.getRecommendationsLabel(getSession(), getValue());
	}

}
