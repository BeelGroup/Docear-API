package org.sciplore.queries;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentsPdfHash;

public class DocumentsPdfHashQueries {
	
	public static List<String> getUsersDocumentsTitlesWithoutXref(Session session) {
		String sql = "SELECT D.title from documents D JOIN documents_pdfhash DP ON (D.id = DP.document_id) JOIN mindmaps_pdfhash MP ON (MP.pdfhash = DP.hash) " + 
					"LEFT OUTER JOIN document_xref DX ON (DX.document_id = D.id AND DX.id IS NULL)";
		Query query = session.createSQLQuery(sql);
		
		@SuppressWarnings("unchecked")
		List<String> titles = query.list();
		
		return titles;
	}

	public static DocumentsPdfHash getPdfHash(Session session, String hash) {
		if (hash==null) {
			return null;
		}
		
		Criteria criteria = session.createCriteria(DocumentsPdfHash.class);
		criteria = criteria.add(Restrictions.eq("hash", hash));
		
		return (DocumentsPdfHash) criteria.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public static List<DocumentsPdfHash> getDocumentPdfHashes(Session session, String hash) {
		if (hash==null) {
			return null;
		}
		
		Criteria criteria = session.createCriteria(DocumentsPdfHash.class);
		criteria = criteria.add(Restrictions.eq("hash", hash));
		
		return (List<DocumentsPdfHash>) criteria.list();
	}
	
	public static List<DocumentsPdfHash> getPdfHashes(Session session, Document document) {
		if (document == null || document.getId() == null) {
			return null;
		}
		
		Criteria criteria = session.createCriteria(DocumentsPdfHash.class);
		criteria = criteria.add(Restrictions.eq("document", document));
		
		return criteria.list();
	}
	
	
}
