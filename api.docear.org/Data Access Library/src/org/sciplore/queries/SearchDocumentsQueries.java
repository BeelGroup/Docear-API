package org.sciplore.queries;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Document;
import org.sciplore.resources.SearchDocuments;
import org.sciplore.resources.SearchDocumentsSet;
import org.sciplore.resources.User;

public class SearchDocumentsQueries {

	@SuppressWarnings("unchecked")
	public static SearchDocuments getSearchDocument(Session session, String hashId, User user) throws Exception {
		Criteria criteria = session.createCriteria(SearchDocuments.class).add(Restrictions.eq("hashId", hashId));
		for (SearchDocuments result : (List<SearchDocuments>) criteria.list()) {
			if (result.getSearchDocumentsSet().getUser().getId() == user.getId()) {
				return result;
			}
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public static List<Document> getSearchDocumentsForUser(Session session, User user) {
		Criteria criteria = session.createCriteria(SearchDocumentsSet.class).add(Restrictions.eq("user", user));
		criteria.createCriteria("searchDocuments");
		criteria.setProjection(Projections.distinct(Projections.property("document")));
		return criteria.list();
	}

	public static SearchDocuments getSearchDocuments(Session session, SearchDocuments searchDoc) {
		if (searchDoc.getId() != null) {
			return (SearchDocuments) session.load(SearchDocuments.class, searchDoc.getId());			
		}
		else {
			return null;
		}
	}
		
	private static Criteria getCriteria(Session session, String hashId, User user, Document document, Date delivered) {
		Criteria criteria = session.createCriteria(SearchDocumentsSet.class, "searchDocSet");

		if (user != null) {
			criteria = criteria.add(Restrictions.eq("searchDocSet.user", user));
		}
		
		if (delivered != null) {
			criteria = criteria.add(Restrictions.eq("searchDocSet.delivered", delivered));
		}
		
		criteria.createCriteria("searchDocuments", "searchDoc");

		if (document != null) {
			criteria = criteria.add(Restrictions.eq("searchDoc.document", document));
		}
		
		if (hashId != null) {
			criteria = criteria.add(Restrictions.eq("searchDoc.hash_id", hashId));
			criteria = criteria.setFetchSize(1);
		}

		return criteria;
	}
	
}
