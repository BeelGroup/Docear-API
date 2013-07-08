package org.sciplore.queries;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentsBibtex;
import org.sciplore.resources.DocumentsPdfHash;

public class DocumentsBibtexQueries {

	@SuppressWarnings("unchecked")
	public static List<DocumentsBibtex> getDocumentsBibtex(Session session, Document document) {
		Criteria criteria = session.createCriteria(DocumentsBibtex.class);
		criteria = criteria.add(Restrictions.eq("document", document));
		
		return (List<DocumentsBibtex>) criteria.list();
	}
	
	public static DocumentsBibtex getDocumentsBibtex(Session session, Document document, String bibtex) {
		Criteria criteria = session.createCriteria(DocumentsBibtex.class);
		criteria = criteria.add(Restrictions.eq("document", document));
		criteria = criteria.add(Restrictions.eq("bibtex", bibtex));
		
		return (DocumentsBibtex) criteria.uniqueResult();
	}
	
	public static Collection<DocumentsBibtex> getDocumentsBibtexByHash(Session session, String hash) {
		Criteria criteria = session.createCriteria(DocumentsPdfHash.class);
		criteria = criteria.add(Restrictions.eq("hash", hash));
		criteria = criteria.setProjection(Projections.distinct(Projections.property("document")));
		Document doc = (Document) criteria.uniqueResult();
		
		if (doc == null) {
			return null;
		}
		
		return doc.getDocumentsBibtex();
	}
	
}
