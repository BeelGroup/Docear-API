package org.sciplore.queries;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.RecommendationsEvaluatorCache;
import org.sciplore.resources.User;

public class RecommendationsEvaluatorCacheQueries {
	
	public static RecommendationsEvaluatorCache getRecommendationsEvaluatorCacheQueries(Session session, User user) {
		return (RecommendationsEvaluatorCache) getCriteria(session, user).setMaxResults(1).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public static List<RecommendationsEvaluatorCache> getAllTuples(Session session) {
		return (List<RecommendationsEvaluatorCache>) getCriteria(session, null).list();
	}
	
	private static Criteria getCriteria(Session session, User user) {
		Criteria crit = session.createCriteria(RecommendationsEvaluatorCache.class);
		if (user != null) {
			crit.add(Restrictions.eq("user", user));
		}
		
		return crit;
	}
	
	

}
