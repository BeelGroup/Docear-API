package org.sciplore.queries;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Mindmap;
import org.sciplore.resources.User;

public class UserQueries {
	
	@SuppressWarnings("unchecked")
	public static List<BigInteger> getUsers(Session session) {
		SQLQuery query = session.createSQLQuery("SELECT U.id FROM users U JOIN mindmaps M ON (M.user = U.id) WHERE U.allow_recommendations=1 GROUP BY U.id having COUNT(*)>=10");
		return query.list();
	}

	public static User getUserByAccessToken(Session session, String accessToken) {
		if (accessToken == null) {
			return null;
		}
		return (User) getCriteria(session, accessToken, null).setMaxResults(1).uniqueResult();
	}
	
	private static Criteria getCriteria(Session session, String accessToken, Boolean allowRecommendations) {
		Criteria criteria = session.createCriteria(User.class);
		if (accessToken != null) {
			criteria = criteria.add(Restrictions.eq("accessToken", accessToken));
		}
		if (allowRecommendations != null) {
			criteria = criteria.add(Restrictions.ge("allowRecommendations", (short) 1));
		}
		return criteria;
	}
	
	public static Integer getDaysStarted(Session session, User user) {
		SQLQuery query = (SQLQuery) session.createSQLQuery("SELECT COUNT(DISTINCT(DATE(time))) FROM users_applications U WHERE user_id=:user_id")
				.setParameter("user_id", user.getId());
		
		return ((BigInteger) query.uniqueResult()).intValue();
	}
	
	public static Integer getDaysUsed(Session session, User user) {		
		Criteria criteria = session.createCriteria(Mindmap.class);
		criteria = criteria.add(Restrictions.eq("user", user));
		criteria = criteria.setProjection(Projections.projectionList().add(Projections.min("revision"), "min").add(Projections.max("revision"), "max"));
		
		try {
			Object[] minMaxDate = (Object[]) criteria.uniqueResult();
			return (int) ((((Timestamp)minMaxDate[1]).getTime() - ((Timestamp)minMaxDate[0]).getTime()) / 3600000L / 24L);
		}
		catch(NullPointerException e) {			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
