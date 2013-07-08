package org.sciplore.queries;

import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.RecommendationsLabel;

public class RecommendationsLabelQueries {

	public static RecommendationsLabel getRecommendationsLabel(Session session, String value) {
		return (RecommendationsLabel) getCriteria(session, value).setMaxResults(1).uniqueResult();
	}
	
	private static Criteria getCriteria(Session session, String value) {
		Criteria criteria = session.createCriteria(RecommendationsLabel.class);
		
		if (value != null) {
			criteria.add(Restrictions.eq("value", value));
		}
		
		return criteria;
	}
	
	public static RecommendationsLabel getRandomLabel(Session session) {
		Criteria criteria = session.createCriteria(RecommendationsLabel.class);
		criteria = criteria.add(Restrictions.eq("type", RecommendationsLabel.TYPE_ORGANIC));
		@SuppressWarnings("unchecked")
		List<RecommendationsLabel> labels = criteria.list();
				
		Integer index = new Random().nextInt(labels.size());
		return labels.get(index);
	}
}
