package org.sciplore.queries;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.GoogleDocumentQuery;
import org.sciplore.resources.User;

public class InternalQueries {
	
	public static void finishKeywords(Session session, String lockId, boolean setDate) {
		Criteria criteria = session.createCriteria(GoogleDocumentQuery.class);
		criteria = criteria.add(Restrictions.eq("lock_id", lockId));
		@SuppressWarnings("unchecked")
		List<GoogleDocumentQuery> results = criteria.list();
		
		Transaction transaction = session.beginTransaction();
		try {
    		for (GoogleDocumentQuery result : results) {
    			if (result.getQuery_date() == null) {
    				result.setQuery_date(new Date());
    			}
    			result.setLockId(null);
    			session.saveOrUpdate(result);
    		}
    		session.flush();
    		transaction.commit();
		}
		catch(Exception e) {
			transaction.rollback();
		}		
	}
	
	@SuppressWarnings("unchecked")
	public static List<GoogleDocumentQuery> retrieveKeywords(Session session, Integer count) {
		Criteria criteria = session.createCriteria(GoogleDocumentQuery.class, "googleDocQuery");
		criteria = criteria.add(Restrictions.isNull("lock_id"));		
		criteria = criteria.add(Restrictions.isNull("query_date"));
		criteria = criteria.addOrder(Order.desc("id"));		
		criteria = criteria.setMaxResults(count == null ? 30 : count);
		
		return (List<GoogleDocumentQuery>) criteria.list();
	}
		
	public static GoogleDocumentQuery getMatchingModels(Session session, String model) {
    	Criteria criteria = session.createCriteria(GoogleDocumentQuery.class, "googleDocQuery");
    	criteria = criteria.add(Restrictions.eq("model", model));
    	criteria.setMaxResults(1);
    	return (GoogleDocumentQuery) criteria.uniqueResult();    	
	}
	
	@SuppressWarnings("unchecked")
	public static List<User> getUsersAllowingResearch(Session session, Date dlo) {
		Criteria criteria = session.createCriteria(User.class, "user");
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria = criteria.createCriteria("mindmaps", "map");
		criteria = criteria.add(Restrictions.or(Restrictions.eq("allowContentResearch", true), Restrictions.or(Restrictions.eq("allowInformationRetrieval", true), Restrictions.eq("allowRecommendations", true))));
		if (dlo != null) {
			System.out.println(dlo);
			criteria = criteria.add(Restrictions.gt("revision", dlo));
		}
		criteria = criteria.createCriteria("map.application", "application").add(Restrictions.ge("id", 23));
		
		return (List<User>) criteria.list();
	}
	
	public static Date getLastKeywordsGeneratedDate(Session session) {
		Criteria criteria = session.createCriteria(GoogleDocumentQuery.class, "gdq");
		criteria = criteria.setProjection(Projections.max("created_date"));
		
		return (Date) criteria.uniqueResult();
	}
	
	
}
