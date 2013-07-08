package org.sciplore.queries;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.MindmapsPdfHash;
import org.sciplore.resources.User;

public class MindmapsPdfHashQueries {	
	
	//for offline evaluator
	public static String getLatestNewDocumentForUser(Session session, User user, int maxId) {
		Query query = session.createSQLQuery("SELECT MIN(P.id) AS minid, pdfhash FROM mindmaps_pdfhash P " +
				"JOIN mindmaps M ON (M.id = P.mindmap_id) WHERE user=:user AND P.id<=:maxId " +
				"GROUP BY P.pdfhash ORDER BY minid DESC limit 1").setParameter("user", user.getId()).setParameter("maxId", maxId);
		
		return (String) ((Object[]) query.uniqueResult())[1];		
	}
	
	
	public static MindmapsPdfHash getItem(Session session, Long mindmapId, String pdfHash) {
		return (MindmapsPdfHash) getCriteria(session, mindmapId, pdfHash).uniqueResult();
	}
	
	public static boolean isRevisionAlreadyStored(Session session, Long mindmapId) {
		return !getCriteria(session, mindmapId, null).list().isEmpty();
	}
	
	private static Criteria getCriteria(Session session, Long mindmapId, String pdfHash) {
		Criteria criteria = session.createCriteria(MindmapsPdfHash.class);
		
		if (mindmapId != null) {
			criteria.add(Restrictions.eq("mindmapId", mindmapId));
		}
		
		if (pdfHash != null) {
			criteria.add(Restrictions.eq("pdfHash", pdfHash));
		}
		
		return criteria;
	}
}
