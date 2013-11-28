package org.sciplore.queries;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Document;
import org.sciplore.resources.RecommendationsDocuments;
import org.sciplore.resources.RecommendationsDocumentsSet;
import org.sciplore.resources.User;

public class RecommendationsDocumentsQueries {

	public static RecommendationsDocuments getRecommendationsDocument(Session session, String hashId, User user) throws Exception {
		Criteria criteria = session.createCriteria(RecommendationsDocuments.class).add(Restrictions.eq("hash_id", hashId));
		for (RecommendationsDocuments rec : (List<RecommendationsDocuments>) criteria.list()) {
			if (rec.getRecommentationsDocumentsSet().getUser().getId() == user.getId()) {
				return rec;
			}
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Document> getRecommendationsDocumentsForUser(Session session, User user) {
		Criteria criteria = session.createCriteria(RecommendationsDocumentsSet.class).add(Restrictions.eq("user", user));
		criteria.createCriteria("recommendationsDocuments");
		criteria.setProjection(Projections.distinct(Projections.property("document")));
		return criteria.list();
	}

	public static RecommendationsDocuments getRecommendationsDocuments(Session session, RecommendationsDocuments recDoc) {
		if (recDoc.getId() != null) {
			return (RecommendationsDocuments) session.load(RecommendationsDocuments.class, recDoc.getId());			
		}
		else {
			return null;
		}
	}
		
	private static Criteria getCriteria(Session session, String hashId, User user, Document document, Date delivered) {
		Criteria criteria = session.createCriteria(RecommendationsDocumentsSet.class, "recDocSet");

		if (user != null) {
			criteria = criteria.add(Restrictions.eq("recDocSet.user", user));
		}
		
		if (delivered != null) {
			criteria = criteria.add(Restrictions.eq("recDocSet.delivered", delivered));
		}
		
		criteria.createCriteria("recommendationsDocuments", "recDoc");

		if (document != null) {
			criteria = criteria.add(Restrictions.eq("recDoc.document", document));
		}
		
		if (hashId != null) {
			criteria = criteria.add(Restrictions.eq("recDoc.hash_id", hashId));
			criteria = criteria.setFetchSize(1);
		}

		return criteria;
	}
	
	public static Long getShownBefore(Session session, RecommendationsDocuments recDoc) {
		Criteria crit = session.createCriteria(RecommendationsDocuments.class);
		crit.add(Restrictions.eq("fulltextUrl", recDoc.getFulltextUrl()));
		crit.createAlias("recommentationsDocumentsSet", "S");
		crit.add(Restrictions.isNotNull("S.delivered"));
		crit.setProjection(Projections.rowCount());
		
		return (Long) crit.uniqueResult();
	}
}
