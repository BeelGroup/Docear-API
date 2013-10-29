package org.sciplore.queries;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.Person;
import org.sciplore.tools.Tools;

public class DocumentPersonQueries {
	
	@SuppressWarnings("unchecked")
	public static List<DocumentPerson> getDocumentPersons(Session session, Person person, boolean indexed) {
		return (List<DocumentPerson>) getCriteria(session, person, null, indexed).list();
	}
	
	@SuppressWarnings("unchecked")
	public static List<DocumentPerson> getDocumentPersons(Session session, Person person, String source) {
    	return (List<DocumentPerson>) getCriteria(session, person, source, null).list();
	}
	
	private static Criteria getCriteria(Session session, Person person, String source, Boolean indexed) {
		Criteria crit = session.createCriteria(DocumentPerson.class);
		if (person != null) {
    			crit.add(Restrictions.eq("personMain", person));
		}
		
//    	crit.setFetchMode("document_xref", org.hibernate.FetchMode.JOIN).createCriteria("document_xref");    	
//    	if (source != null) {
//    		crit.add(Restrictions.eq("source", source));
//    	}
//    	if (indexed != null) {
//    		crit.add(Restrictions.eq("indexed", indexed ? 1 : 0));
//    	}  	
    	
    	crit = crit.createCriteria("document").setFetchMode("xrefs", FetchMode.JOIN).createCriteria("xrefs");
    	
    	if (source != null) {
    		crit.add(Restrictions.eq("source", source));
    	}
    	if (indexed != null) {
    		crit.add(Restrictions.eq("indexed", indexed ? 1 : 0));
    	}
    	
    	return crit;
	}
	
	 
}
