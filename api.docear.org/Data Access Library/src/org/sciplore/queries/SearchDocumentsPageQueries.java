package org.sciplore.queries;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.SearchDocumentsPage;
import org.sciplore.resources.SearchDocumentsSet;

public class SearchDocumentsPageQueries {

	@SuppressWarnings("unchecked")
	public static SearchDocumentsPage getSearchDocumentsPage(Session session, SearchDocumentsSet searchDocumentsSet, int page) throws Exception {
		Criteria criteria = session.createCriteria(SearchDocumentsPage.class)
				.add(Restrictions.eq("searchDocumentsSet", searchDocumentsSet))
				.add(Restrictions.eq("page", page));
		List<SearchDocumentsPage> list = criteria.list();
		
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

}
