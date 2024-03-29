package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AlreadyConnectedException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sciplore.deserialize.mapper.MrDlibXmlMapper;
import org.sciplore.deserialize.reader.XmlResourceReader;
import org.sciplore.io.StringInputStream;
import org.sciplore.queries.CitationsQueries;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.resources.Citation;
import org.sciplore.resources.Contact;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.Person;
import org.sciplore.resources.PersonHomonym;

public class DocumentCommons {

	public static final File PDF_STORE_PATH = new File("/srv/docear/documents/");

	static {
		if (!PDF_STORE_PATH.exists()) {
			PDF_STORE_PATH.mkdirs();
		}
	}

	public static File createDocumentStoreFile(String relativeFilePath, boolean overwrite) throws IOException {
		File file = new File(PDF_STORE_PATH, relativeFilePath);
		if (!overwrite && file.exists() && file.length() > 0) {
			throw new AlreadyConnectedException();
		}
		else {
			if (!file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) {
					throw new IOException("could not create directory '" + file.getParentFile() + "'");
				}
			}
			if (!file.exists() && !file.createNewFile()) {
				throw new IOException("could not create file '" + file + "'");
			}
		}
		return file;
	}

	public static Response handleDocumentPDFUpload(Session session, Document doc, int xref_id, FulltextUrl fulltext, InputStream inputStream) {
		Transaction transaction = session.beginTransaction();
		try {
			String relativeFilePath = "/" + doc.getId() + "/" + xref_id + ".pdf";
			File file;
			try {
				file = DocumentCommons.createDocumentStoreFile(relativeFilePath, false);
			}
			catch (AlreadyConnectedException e) {
				return Tools.getHTTPStatusResponse(Status.NOT_MODIFIED, "already exists");
			}

			FileOutputStream fos = new FileOutputStream(file);
			int b = 0;
			while ((b = inputStream.read()) > -1) {
				fos.write(b);
			}
			fos.flush();
			fos.close();
			inputStream.close();

			// try to index the plain text with lucene
			String hash = FulltextCommons.updatePlainText(doc, file);

			// save the document hash to the database
			DocumentsPdfHash pdfHash = new DocumentsPdfHash();
			pdfHash.setSession(session);
			pdfHash.setDocument(doc);
			pdfHash.setHash(hash);
			session.saveOrUpdate(pdfHash);
			session.flush();

			// save the fulltext information to database after download was
			// successful
			fulltext.setDocumentsPdfHash(pdfHash);
			session.saveOrUpdate(fulltext);
			session.flush();

			boolean updated = false;
			// update the xref with status indexed=1
			for (DocumentXref xref : doc.getXrefs()) {
				if (xref.getId() == xref_id) {
					xref.setIndexed(1);
					session.saveOrUpdate(xref);
					updated = true;
					break;
				}
			}
			if (updated) {
				System.out.println("updated indexed fulltext for xref.id: " + xref_id);
			}
			else {
				System.out.println("no updated fulltext for xref.id: " + xref_id);
			}

			transaction.commit();
			return Tools.getHTTPStatusResponse(Status.OK, "");
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could not store pdf");
		}
		catch (IOException e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could not store pdf");
		}
		finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	/**
	 * @param hash
	 * @param session
	 * @param doc
	 * @param fulltext
	 */
	public static void createOrUpdateFulltextHash(String hash, Session session, Document doc, FulltextUrl fulltext) {
		// save the document hash to the database
		DocumentsPdfHash pdfHash = new DocumentsPdfHash();
		pdfHash.setSession(session);
		pdfHash.setDocument(doc);
		pdfHash.setHash(hash);
		session.saveOrUpdate(pdfHash);
		session.flush();

		// save the fulltext information to database after download was
		// successful
		fulltext.setDocumentsPdfHash(pdfHash);
		session.saveOrUpdate(fulltext);
		session.flush();
	}
	
	public static boolean updateDocumentPersons(Session session, Document document, List<String> emails) {
		if (session == null || document == null || emails == null /*||  doc.getPersons().size() > 0/**/) {
			return false;
		}
		boolean isDirty = false;
		long time = System.currentTimeMillis();
		Set<DocumentPerson> persons = document.getPersons();
		for (String email : emails) {
			if(MailUtils.isValidMailAddress(email)) {
				Contact contact = Contact.getContact(session, email);
				//if no existing contact was found create new one
				if(contact == null) {
					contact = new Contact(session, email, Contact.CONTACT_TYPE_EMAIL);
					contact.setPerson(new Person(""));
					session.save(contact.getPerson());
					session.save(contact);
					session.flush();
				}
				// if no author with this contact data is already attached, create and add a new
				if(!containsContact(persons, contact)) {
					PersonHomonym homonym = new PersonHomonym(session, "");
					homonym.setPerson(contact.getPerson());
					homonym.setValid((short) 1);
					session.save(homonym);
					session.flush();
					
					DocumentPerson docPerson = new DocumentPerson(session, homonym);
					docPerson.setPersonMain(contact.getPerson());
					docPerson.setDocument(document);
					session.save(docPerson);
					session.flush();
					isDirty = true;
					
					persons.add(docPerson);
				}
				contact.getPerson().setDocidxNewDocuments(true);
				if(contact.getPerson().getDocidxIdToken() == null) {
					contact.getPerson().setDocidxIdToken(UUID.randomUUID().toString());
				}
				session.update(contact.getPerson());
				session.flush();
			}
		}
		if(isDirty) {
			//document may be used elsewhere after running this method --> refreshing it
			session.refresh(document);
//			session.update(document);
//			session.flush();
		}
		
		System.out.println("updating DocumentPersons time: " + (System.currentTimeMillis() - time));

		return isDirty;		
	}

	private static boolean containsContact(Collection<DocumentPerson> persons, Contact contact) {
		if(persons == null || contact == null || contact.getUri() == null) {
			return false;
		}
		
		for (DocumentPerson documentPerson : persons) {
			try {
				for (Contact c : documentPerson.getPersonMain().getContacts()) {
					if(contact.getUri().equals(c.getUri())) {
						return true;
					}
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static boolean updateDocumentData(Session session, Document doc, String xmlStream) {
		if (xmlStream == null || session == null || doc == null || doc.getCitations().size() > 0 || doc.getPersons().size() > 0 || doc.getAbstract() != null) {
			return false;
		}
		
		Transaction transaction = session.beginTransaction();
		try {
			
    		boolean isDirty = false;
    		XmlResourceReader reader = new XmlResourceReader(MrDlibXmlMapper.getDefaultMapper());
    		Document resource = null;
    		try {
    			InputStream is = new StringInputStream(xmlStream, "UTF-8");
    			resource = (Document) reader.parse(is);
    		}
    		catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
    		long time = System.currentTimeMillis();
    
    		if (resource != null && !CitationsQueries.areCitationsAlreadyStored(session, doc)) {
    			// transfer citations
    			for (Citation ref : resource.getCitations()) {
    				// clear citedDocument from other entities to prevent from
    				// unnecessary and costly loops during saveOrUpdate
    				Document citingDocument = getClearedDocumentFromEntities(doc);
    				Document citedDocument = getClearedDocumentFromEntities(ref.getCitedDocument());
    				
    				if (citedDocument == null || citingDocument == null) {
    					continue;
    				}
    
    				ref.setCitingDocument(citingDocument);
    				//automatic procedure
//    				ref.setCitedDocument(citedDocument);
//    				session.saveOrUpdate(ref);
    				//by hand procedure
    				Document persDoc = (Document) citedDocument.getPersistentIdentity();
    				if(persDoc != null) {
    					ref.setCitedDocument(persDoc);
    				}
    				else {
    					session.save(citedDocument);
    					ref.setCitedDocument(citedDocument);
    				}
    				session.save(ref);
    			}
    			// update abstract text in original doc entry if necessary
    			if (resource.getAbstract() != null && resource.getAbstract().trim().length() > 0) {
    				doc.setAbstract(resource.getAbstract());
    				session.update(doc);
    				isDirty = true;
    			}
    			try {
    				session.flush();
    				transaction.commit();
    			}
    			catch (Exception e) {
    				e.printStackTrace();
    				System.out.println("dirty doc: "+doc.getTitle());
    				transaction.rollback();    				
    			}
    		}
    		System.out.println("updating Reference time: " + (System.currentTimeMillis() - time));
    
    		return isDirty;
		}
		finally {
			if(transaction.isActive()) {
				transaction.rollback();
			}					
		}
	}

	public static Document getClearedDocumentFromEntities(Document document) {
		Document cleared = new Document(document.getSession(), document.getTitle());
		if (DocumentQueries.getValidCleanTitle(cleared.getTitle()) == null) {
			return null;
		}
		cleared.setAbstract(document.getAbstract());
		cleared.setDoi(document.getDoi());
		cleared.setPages(document.getPages());
		cleared.setPublisher(document.getPublisher());
		cleared.setPublishedYear(document.getPublishedYear());
		cleared.setPublishedMonth(document.getPublishedMonth());
		cleared.setPublishedDay(document.getPublishedDay());

		return cleared;
	}

}
