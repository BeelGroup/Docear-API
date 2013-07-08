package org.sciplore.queries;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.DocumentsBibtexPdfHash;

public class DocumentsBibtexPdfHashQueries {
	public static DocumentsBibtexPdfHash getDocumentsBibtexPdfHash(Session session, Integer bibtexId, Integer pdfHashhId) {
		if (bibtexId==null || pdfHashhId==null) {
			return null;
		}
		
		Criteria criteria = session.createCriteria(DocumentsBibtexPdfHash.class);
		criteria = criteria.add(Restrictions.eq("documentsBibtex.id", bibtexId));
		criteria = criteria.add(Restrictions.eq("documentsPdfHash.id", pdfHashhId));
		return (DocumentsBibtexPdfHash) criteria.uniqueResult();
	}
}
