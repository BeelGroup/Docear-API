package org.sciplore.queries;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.RecommendationsUsersSettings;
import org.sciplore.resources.User;

public class RecommendationsUsersSettingsQueries {
	
	public static RecommendationsUsersSettings getRecommendationsUsersSettings(Session session, User user) {
		return (RecommendationsUsersSettings) getCriteria(session, user).uniqueResult();
	}
	
	private static Criteria getCriteria(Session session, User user) {
		Criteria criteria = session.createCriteria(RecommendationsUsersSettings.class);
		
		if (user != null) {
			criteria.add(Restrictions.eq("user", user));
		}
		
		return criteria;
	}
}

