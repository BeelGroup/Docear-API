package org.sciplore.queries;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentXref;
import org.sciplore.tools.Tools;

public class XrefQueries {

	@SuppressWarnings("unchecked")
	public static List<DocumentXref> getDocumentXrefs(Session session, Document document) {
		Criteria criteria = session.createCriteria(DocumentXref.class, "xref");
		if (document != null) {
			criteria = criteria.add(Restrictions.eq("document", document));
		}
		
		return criteria.list();
	}
	
	public static DocumentXref getDocumentXref(Session session, Document document, String source/*, String sourcesId*/) {
		Criteria criteria = session.createCriteria(DocumentXref.class, "xref");
		if (document != null) {
			criteria = criteria.add(Restrictions.eq("document", document));
		}
		if (source != null) {
			criteria = criteria.add(Tools.getDisjunctionFromString("source", source));
		}
//		if (sourcesId != null) {
//			criteria = criteria.add(Tools.getDisjunctionFromString("sourcesId", sourcesId));
//		}
		criteria = criteria.setMaxResults(1);
		
		return (DocumentXref) criteria.uniqueResult();
	}	
	
	public static DocumentXref getDocumentXref(Session session, DocumentXref x) {
		if(x.getId() != null) {
			return (DocumentXref) session.load(DocumentXref.class, x.getId());			
		} else {
			if(x.getDocument() == null || x.getDocument().getId() == null /*|| x.getInstitution() == null || x.getInstitution().getId() == null*/) {
				return null;
			}
			return getDocumentXref(session, x.getDocument(), x.getSource()/*, x.getSourcesId()*/);
		}
	}
	
	public static DocumentXref getDocumentXref(Session session, Integer id, String source) {		
		Criteria criteria = session.createCriteria(DocumentXref.class, "xref").add(Restrictions.eq("id", id));
		if (source != null) {
			criteria.add(Tools.getDisjunctionFromString("source", source));
		}
		
		return (DocumentXref) criteria.setMaxResults(1).uniqueResult();
	}
	

	
}
