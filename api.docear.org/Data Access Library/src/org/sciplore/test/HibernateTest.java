package org.sciplore.test;

import org.hibernate.Session;
import org.sciplore.database.SessionProvider;
import org.sciplore.resources.ResourceException;

public class HibernateTest {

    public static void main(String args[]) throws ResourceException {
    	test();
		
    	//synchTest1();
    	//synchTest2();
//    	synchTest3();
//    	synchTest4();
    }

	private static void test() {		
		Session session = SessionProvider.sessionFactory.openSession();
    	
    	//List<Document> docs = DocumentQueries.getDocumentsBySourceId(session, "dblp,arxiv", 20, 10);
		//List<Document> docs = DocumentQueries.getRecentDocuments(session, "arxiv, dblp", 20);
//		long count = (DocumentQueries.getDocuments(session, null, null, null, null)).size();
//		System.out.println("count: "+count);
//		
//    	for (Document d : docs) {
//    		System.out.println("document: "+d.getId()+" : "+d.getTitle());
//    	}
//    	System.out.println("count: "+docs.size());
		
		//System.out.println("latestPub arxiv: "+DocumentQueries.getLatestPublicationDate(session, "arxiv"));
//		System.out.println("latestPub dblp: "+DocumentQueries.getLatestPublicationDate(session, "dblp"));
//		System.out.println("latestPub both: "+DocumentQueries.getLatestPublicationDate(session, "dblp,arxiv"));
		
		
//		doc.setId(5);
//		DocumentXref xref = XrefQueries.getDocumentXref(session, doc, "arxiv,dblp");
		//DocumentXref xref = XrefQueries.getDocumentXref(session, 10, "dblp");
//		System.out.println("debug xref: "+xref.getId());
		
		session.close();
	}
    
//    private static void synchTest1() {
//    	Session session = SessionProvider.sessionFactory.openSession();
//    	try {
//    		System.out.println("\r\n\r\n===================== TEST 1 ========================\r\n");
//			Person person = new Person(session);
//			person.setNameLast("Genzmehr");
//			person.setNameFirst("Marcel");
//			
//			PersonHomonym homonym = new PersonHomonym(session);
//			homonym.setNameFirst("Marcel");
//			homonym.setNameLast("Genzmehr");
//			homonym.setValid((short)1);
//			homonym.setPerson(person);		
//			person.addHomonym(homonym);
//			
//			Document document = new Document(session);
//			document.setValid((short)1);
//			document.setTitle("Von Fishis, MaGs und anderem Kleingetier.");
//			
//			DocumentPerson docPerson = new DocumentPerson(session);
//			docPerson.setPersonHomonym(homonym);
//			docPerson.setDocument(document);
//			
//			person.addDocument(docPerson);
//			
//			session.saveOrUpdate(person);
//		}
//		finally {
//			session.close();
//		}
//    }
//    
//    private static void synchTest2() {
//    	Session session = SessionProvider.sessionFactory.openSession();
//    	Person person = new Person(session);
//    	try {
//    		System.out.println("\r\n\r\n===================== TEST 2 ========================\r\n");
//			
//			person.setNameLast("Genzmehr");
//			person.setNameFirst("Marcel");
//			person.setDob(new java.util.Date(System.currentTimeMillis()));
//			
//			PersonHomonym homonym = new PersonHomonym(session);
//			homonym.setNameFirst("Marcel");
//			homonym.setNameLast("Genzmehr");
//			homonym.setNameLastPrefix("van");
//			homonym.setValid((short)1);
//			homonym.setPerson(person);		
//			person.addHomonym(homonym);
//			
//			Document document = new Document(session);
//			document.setValid((short)1);
//			document.setTitle("Von Fishis, MaGs und anderem Kleingetier. 2. Auflage");
//			
//			DocumentPerson docPerson = new DocumentPerson(session);
//			docPerson.setPersonHomonym(homonym);
//			docPerson.setDocument(document);
//			
//			person.addDocument(docPerson);
//			
//			session.saveOrUpdate(person);
//			session.flush();
//		}
//		finally {
//			session.close();
//		}
//    	
//    }
//    
//    private static void synchTest3() {
//    	Session session = SessionProvider.sessionFactory.openSession();
//    	try {
//    		System.out.println("\r\n\r\n===================== TEST 3 ========================\r\n");
//    		
//    		Document document = new Document(session);
//			document.setValid((short)1);
//			document.setTitle("Von Fishis, MaGs und anderem Kleingetier. 3. Auflage");
//    		
//    		Person person = new Person(session);
//			person.setNameLast("3");
//			person.setNameFirst("Autor");
//			
//			PersonHomonym homonym = new PersonHomonym(session);
//			homonym.setNameFirst("Autor");
//			homonym.setNameLast("3");
//			homonym.setValid((short)1);
//			homonym.setPerson(person);		
//			
//			person.addHomonym(homonym);
//			
//			
//			DocumentPerson docPerson = new DocumentPerson(session);
//			docPerson.setPersonHomonym(homonym);
//			docPerson.setDocument(document);
//			
//			document.addPerson(docPerson);
//			
//			session.saveOrUpdate(document);
//			session.flush();
//		}
//		finally {
//			session.close();
//		}
//    }
//    
//    
//    private static void synchTest4() {
//    	Session session = SessionProvider.sessionFactory.openSession();
//    	try {
//    		System.out.println("\r\n\r\n===================== TEST 4 ========================\r\n");
//    		
//    		Document document = new Document(session);
//			document.setValid((short)1);
//			document.setTitle("Von Fishis, MaGs und anderem Kleingetier. 4. Auflage");
//    		
//    		Person person = new Person(session);
//			person.setNameLast("3");
//			person.setNameFirst("Autor");
//			
//			PersonHomonym homonym = new PersonHomonym(session);
//			homonym.setNameFirst("Autor");
//			homonym.setNameLast("3");
//			homonym.setValid((short)1);
//			homonym.setPerson(person);		
//			
//			person.addHomonym(homonym);
//			
//			
//			DocumentPerson docPerson = new DocumentPerson(session);
//			docPerson.setPersonHomonym(homonym);
//			docPerson.setDocument(document);
//			
//			document.addPerson(docPerson);
//		
//			session.saveOrUpdate(document);
//			session.flush();
//		}
//		finally {
//			session.close();
//		}
//    }
    
}