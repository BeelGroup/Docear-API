package org.sciplore.queries;

import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.RecommendationsRatingsLabel;

public class RecommendationsRatingsLabelQueries {

	public static RecommendationsRatingsLabel getRecommendationsRatingsLabel(Session session, String value) {
		return (RecommendationsRatingsLabel) getCriteria(session, value).setMaxResults(1).uniqueResult();
	}
	
	private static Criteria getCriteria(Session session, String value) {
		Criteria criteria = session.createCriteria(RecommendationsRatingsLabel.class);
		
		if (value != null) {
			criteria.add(Restrictions.eq("value", value));
		}
		
		return criteria;
	}
	
	public static RecommendationsRatingsLabel getRandomLabel(Session session) {
		Criteria criteria = session.createCriteria(RecommendationsRatingsLabel.class);		
		@SuppressWarnings("unchecked")
		List<RecommendationsRatingsLabel> labels = criteria.list();
				
		Integer index = new Random().nextInt(labels.size());
		return labels.get(index);
	}
}
