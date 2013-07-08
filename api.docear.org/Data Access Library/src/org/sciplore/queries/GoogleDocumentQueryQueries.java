package org.sciplore.queries;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.GoogleDocumentQuery;

public class GoogleDocumentQueryQueries {
	
	public static Integer getItemCount(Session session) {
		Criteria criteria = session.createCriteria(GoogleDocumentQuery.class);
		criteria.setProjection(Projections.rowCount());
		return (Integer) criteria.uniqueResult();
	}
	
	public static List<GoogleDocumentQuery> getRandomItems(Session session, int count) {
		Criteria criteria = session.createCriteria(GoogleDocumentQuery.class);
		criteria.add(Restrictions.sqlRestriction("1=1 order by rand()"));
		criteria.setMaxResults(count);
		return (List<GoogleDocumentQuery>) criteria.list();
	}
}

