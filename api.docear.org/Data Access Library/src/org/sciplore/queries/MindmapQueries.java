package org.sciplore.queries;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Mindmap;
import org.sciplore.resources.User;

public class MindmapQueries {
	private static Criteria getMindmapCriteria(Session session, User user, String accessToken, boolean requireAuthorization, Integer id, String mindmapId, 
				Date datehi, Date datelo, Integer applicationMin, Integer mustAllowRecommendations) {
		if (user == null || user.getId() == null || user.getAccessToken() == null || user.getAccessToken().trim().length() == 0) {			
			return null;
		}
		
		Criteria criteria = session.createCriteria(Mindmap.class, "map");
		if (id != null) {
			criteria = criteria.add(Restrictions.eq("id", id));
		}
		if (mindmapId != null) {
			criteria = criteria.add(Restrictions.eq("mindmapId", mindmapId));
		}
		if (datehi != null) {
			criteria = criteria.add(Restrictions.le("revision", datehi));
		}		
		if (datelo != null) {
			criteria = criteria.add(Restrictions.gt("revision", datelo));

		}
		criteria = criteria.createCriteria("user", "user").add(Restrictions.eq("id", user.getId()));
		if (requireAuthorization) {
			criteria = criteria.add(Restrictions.eq("accessToken", accessToken));		
		}
		
		if (applicationMin != null) {
			criteria = criteria.createCriteria("map.application", "application").add(Restrictions.ge("id", applicationMin));
		}
		
		if (mustAllowRecommendations != null) {
			criteria = criteria.add(Restrictions.eq("map.allowRecommendations", true));
		}
		
		criteria = criteria.addOrder(Order.asc("map.filename")).addOrder(Order.asc("map.filepath")).addOrder(Order.desc("map.revision"));
		return criteria;
	}
	
	public static Mindmap getMindmap(Session session, User user, String accessToken, Integer id) {
		if (id == null) {			
			return null;
		}
		List<Mindmap> maps = getMindmaps(session, user, accessToken, id, null, null, null, null, null);
		if (maps.size()==0) {
			return null;
		}
		else {
			return getMindmaps(session, user, accessToken, id, null, null, null, null, null).get(0);
		}
	}
	
	public static List<Mindmap> getMindmaps(Session session, User user, String accessToken) {
		return getMindmaps(session, user, accessToken, null, null, null, null, null, null);
	}
	
	//the standard method to use
	public static List<Mindmap> getMindmaps(Session session, User user, String accessToken, Integer id, String mindmapId, Date datehi, Date datelo, 
			Integer minApplication, Integer allowRecommendations) {		
		return getMindmaps(session, user, accessToken, true, id,  mindmapId,  datehi,  datelo, minApplication, allowRecommendations);
	}
	
	public static List<Mindmap> getMindmaps(Session session, User user, String accessToken, boolean requireAuthorization, Integer id, String mindmapId, 
			Date datehi, Date datelo, Integer minApplication, Integer allowRecommendations) {		
		Criteria criteria = getMindmapCriteria(session, user, accessToken, requireAuthorization, id, mindmapId, datehi, datelo, minApplication, allowRecommendations);
		if (criteria == null) {
			return null;
		}
		
		@SuppressWarnings("unchecked")
		List<Mindmap> maps = (List<Mindmap>) criteria.list();
		
		if (maps != null) {
			System.out.println("returning: "+maps.size()+" mind maps");
		}
		else {
			System.out.println("returning: null mind maps");
		}
		
		return maps;
	}
	
	public static Mindmap getMindmap(Session session, String mindmapId, Date revision) {
		return (Mindmap) session.createCriteria(Mindmap.class)
			.add(Restrictions.eq("mindmapId", mindmapId))
			.add(Restrictions.eq("revision", revision))
			.setMaxResults(1)
			.uniqueResult();		
	}
	
	public static Mindmap getMindmap(Session session, Integer id) {
		Mindmap m = (Mindmap) session.get(Mindmap.class, id);
		return m;
	}

	
	
}
