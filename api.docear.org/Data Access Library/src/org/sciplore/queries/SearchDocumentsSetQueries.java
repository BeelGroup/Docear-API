package org.sciplore.queries;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Resource;
import org.sciplore.resources.SearchDocumentsSet;

public class SearchDocumentsSetQueries {

	public static Resource getPersistentIdentity(Session session, SearchDocumentsSet searchDocumentsSet) {		
		if (searchDocumentsSet.getUser() == null || searchDocumentsSet.getCreated() == null) {
			return null;
		}
		
		Criteria crit = session.createCriteria(SearchDocumentsSet.class);
		crit.add(Restrictions.eq("user", searchDocumentsSet.getUser()));		
		crit.add(Restrictions.eq("created", searchDocumentsSet.getCreated()));
		crit.setMaxResults(1);
		
		return (SearchDocumentsSet) crit.uniqueResult();
	}
}
