package org.sciplore.queries;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.RecommendationsDocumentsSet;
import org.sciplore.resources.User;

public class RecommendationsDocumentsSetQueries {

	public static RecommendationsDocumentsSet getLatestUnusedRecommendationsSet(Session session, User user) {
		Criteria crit = session.createCriteria(RecommendationsDocumentsSet.class);
		crit.add(Restrictions.eq("user", user));
		crit.addOrder(Order.desc("id"));
		crit.add(Restrictions.eq("offlineEvaluator", false));
		crit.setMaxResults(1);
		RecommendationsDocumentsSet latest = (RecommendationsDocumentsSet) crit.uniqueResult();

		crit = session.createCriteria(RecommendationsDocumentsSet.class);
		crit.add(Restrictions.eq("user", user));
		crit.add(Restrictions.isNull("deliveryTime"));
		crit.add(Restrictions.eq("offlineEvaluator", false));
		crit.addOrder(Order.desc("id"));
		crit.setMaxResults(1);
		RecommendationsDocumentsSet latestUnused = (RecommendationsDocumentsSet) crit.uniqueResult();
		if (latest.getId() != latestUnused.getId()) {
			latestUnused.setOld(true);
		}

		return latestUnused;
	}
	
	public static RecommendationsDocumentsSet getLatestRecommendationsSet(Session session, User user, int triggerType) {
		Criteria crit = session.createCriteria(RecommendationsDocumentsSet.class);
		crit.add(Restrictions.eq("user", user));
		crit.add(Restrictions.eq("triggerType", triggerType));
		crit.add(Restrictions.eq("offlineEvaluator", false));
		crit.addOrder(Order.desc("id"));
		crit.setMaxResults(1);
		
		return (RecommendationsDocumentsSet) crit.uniqueResult();
	}

	public static RecommendationsDocumentsSet getPersistentIdentity(Session session, RecommendationsDocumentsSet recDocSet) {
		if (recDocSet.getUser() == null || recDocSet.getCreated() == null) {
			return null;
		}
		
		Criteria crit = session.createCriteria(RecommendationsDocumentsSet.class);
		crit.add(Restrictions.eq("user", recDocSet.getUser()));		
		crit.add(Restrictions.eq("created", recDocSet.getCreated()));
		crit.add(Restrictions.eq("offlineEvaluator", recDocSet.getOfflineEvaluator()));
		crit.setMaxResults(1);
		
		return (RecommendationsDocumentsSet) crit.uniqueResult();
	}
	
	public static Integer getCountDeliveredBefore(Session session, User user) {
		Criteria crit = session.createCriteria(RecommendationsDocumentsSet.class);
		crit.add(Restrictions.eq("user", user));
		crit.add(Restrictions.eq("offlineEvaluator", false));
		crit.add(Restrictions.isNotNull("delivered"));
		crit.setProjection(Projections.rowCount());
		
		//substract 1 since the current dataset should not be counted (delivered is already set)
		return ((Number) crit.uniqueResult()).intValue()-1;
	}

}

