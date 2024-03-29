package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mrdlib.index.Indexer;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.queries.GoogleDocumentQueryQueries;
import org.sciplore.queries.XrefQueries;
import org.sciplore.resources.Contact;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.GoogleDocumentQuery;
import org.sciplore.resources.Person;
import org.w3c.dom.Element;

import rest.InternalResource;

public class InternalCommons {
	public boolean isReconnecting = false;
	
	public static Boolean RANDOM_MODEL_CREATION_IN_PROGRESS = false;

	// MODEL1: "A B C D E F G"
	// MODEL2: "H I J K L M N"
	// created new Models: "A B H I; C D J K; E F L M; G N"
	public static void createRandomUserModels() {
		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);

		try {
			// test if this is really needed --> only create if no pdfs to
			// download; else the pdf downloader might download pdfs for models
			// which are irrelelvant before the important models
			Long count = DocumentQueries.getDocumentCount(session, null, null, null, false, false, true, 0);

			if (count > 0) {
				return;
			}

			List<GoogleDocumentQuery> models = GoogleDocumentQueryQueries.getRandomItems(session, 5);

			for (int i = 0; i < models.size(); i++) {
				for (int j = i + 1; j < models.size(); j++) {
					List<String> modelCombinations = getModelCombinations(models.get(i).getModel(), models.get(j).getModel());

					for (String model : modelCombinations) {
						storeModel(session, model);
					}
				}
			}
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	private static void storeModel(Session session, String model) {
		Transaction transaction = session.beginTransaction();
		try {
			GoogleDocumentQuery gdq = new GoogleDocumentQuery(session);
			gdq.setCreated_date(new Date());
			gdq.setModel(model);
			session.saveOrUpdate(gdq);
			session.flush();
			transaction.commit();
		}
		catch (Throwable e) {
			transaction.rollback();
			e.printStackTrace();
		}
	}

	private static List<String> getModelCombinations(String model1, String model2) {
		List<String> models = new ArrayList<String>();

		List<String> sm1 = getSubModels(model1.split(" "));
		List<String> sm2 = getSubModels(model2.split(" "));

		int iterations = Math.min(sm1.size(), sm2.size());

		for (int i = 0; i < iterations; i++) {
			models.add(sm1.get(i) + " " + sm2.get(i));
		}

		return models;
	}

	private static List<String> getSubModels(String[] m) {
		List<String> subStrings = new ArrayList<String>();

		String s = "";
		for (int i = 0; i < m.length; i++) {
			if (s.length() == 0) {
				s += m[i];
			}
			else {
				subStrings.add(s + " " + m[i]);
				s = "";
			}
		}

		return subStrings;
	}

	private static String joinStrings(String[] s, int begin, int end) {
		StringBuilder sb = new StringBuilder();
		sb.append(s[begin]);

		for (int i = begin + 1; i < end; i++) {
			sb.append(" ");
			sb.append(s[i]);
		}

		return sb.toString();
	}

	public static String getLuceneDocumentContent(org.apache.lucene.document.Document doc) {
		StringBuilder sb = new StringBuilder();

		for (IndexableField field : doc.getFields()) {
			String value = doc.get(field.name());
			sb.append(field.name()).append(": ").append(value).append("\n");
		}

		return sb.toString();
	}

	public static void addUserDocumentToSpiderList(Session session, Document document) {
		try {
			String title = document.getTitle();
			title = title.trim();
			title = title.toLowerCase();

			StringBuilder sb = new StringBuilder();

			for (String s : title.split(" ")) {
				// only add tokens consisting of word characters (letters and
				// digits), ignore special characters at the borders
				if (s.length() > 0 && s.matches("^.[\\wüäöß]*.$")) {
					sb.append(s.replaceAll("[^\\wüäöß]", "")).append(" ");
				}
			}

			String normalized = sb.toString();
			// kyrillic titles don't have useable characters for us --> don't
			// add empty models or models with only a letter or a number
			if (normalized.trim().length() < 10) {
				return;
			}

			GoogleDocumentQuery query = new GoogleDocumentQuery(session);
			query.setCreated_date(new Date());
			query.setModel(normalized);
			session.saveOrUpdate(query);
		}
		catch (Exception e) {
			System.out.println(InternalCommons.class.getName() + ".addUserDocumentToSpiderList: " + e.getMessage());
		}
	}
	
	public static void executeQueries(Session session, Query... queries) {		
		Transaction transaction = session.beginTransaction();
		
		try {
			for (Query query : queries) {
				query.executeUpdate();
			}
			
			transaction.commit();
			session.flush();
		}
		catch (Exception e) {			
			System.out.println("execute native update or insert query: "+e.getMessage());
			transaction.rollback();			
		}
		finally {
			
		}
	}

	public static void cleanUpLucene(Indexer indexer, String tablename, Long counter, boolean update) {
		Session session = SessionProvider.sessionFactory.openSession();
		try {
    		session.setFlushMode(FlushMode.ALWAYS);
    		SQLQuery docs = session.createSQLQuery("SELECT document_id FROM "+tablename+" WHERE done IS NULL");		
    		List<BigInteger> result = docs.list();		
    		
    		long time = System.currentTimeMillis();    		
    		for (BigInteger docId : result) {
    			counter++;
    			if (counter % 100 == 0) {    				
    				try {										
    					IndexReader ir = Tools.getLuceneIndexer().getIndexReader();
    					System.out.println("remaining documents in lucene index: "+ir.numDocs());
    					ir.close();
    				}
    				catch (IOException e) {
    					e.printStackTrace();
    				}
    				long duration = System.currentTimeMillis()-time;
    				System.out.println("cleanLucene done: "+counter+" in "+duration+" ms --> mean: "+(duration/counter));				
    			}
    						
    			try {
    				if (update) {
    					Document document = DocumentQueries.getDocument(session, docId.intValue());
    					if (document != null) {
    						IndexReader ir = Tools.getLuceneIndexer().getIndexReader();
    						try {
        						if (getByDocumentId(docId.intValue(), ir).size() > 0) {
        							List<DocumentsPdfHash> hashes = DocumentsPdfHashQueries.getPdfHashes(session, document);
        							if (hashes != null && hashes.size()>0) {
        								indexer.updateDocument(document, hashes.get(0).getHash());
        							}
        							else {
        								indexer.updateDocument(document, null);
        							}
        						}
        						else {
        							System.out.println("lucene update skipped for document: "+docId.intValue());
        						}
    						}
    						finally {
    							ir.close();
    						}
    					}
    				}
    				else {
    					indexer.deleteDocument(docId.intValue());    					
    				}
    				
    			}
    			catch(Exception e) {
    				System.out.println("cleaningLucene - error on deleting docId: "+docId+" --> "+e.getMessage());
    			}
    			
    		}
		}
		finally  {
			session.close();
		}
	}
	
	private static Collection<org.apache.lucene.document.Document> getByDocumentId(Integer docId, IndexReader ir) throws IOException {
		org.apache.lucene.search.Query q = new TermQuery(new Term("sid", String.valueOf(docId)));
		
		Set<org.apache.lucene.document.Document> lucDocs = new HashSet<org.apache.lucene.document.Document>();
		try {
			IndexSearcher is = new IndexSearcher(ir);			
			TopDocs td = is.search(q, 100);		
			if(td.totalHits > 0) {
				for (int i = 0; i < td.scoreDocs.length; i++) {
					lucDocs.add(is.doc(td.scoreDocs[i].doc));
				}
			}					
		}
		catch (Throwable e) {
		}
		return lucDocs;
	}
	
	public static void cleanLuceneFromDuplicatedDocuments() {
		Indexer indexer;
		try {
			indexer = new Indexer();
		}
		catch (IOException e) {			
			e.printStackTrace();
			return;
		}
		Long counter = 0l;
		
//		cleanUpLucene(indexer, "tmp_lucene_removed_docs", counter, false);
//		cleanUpLucene(indexer, "tmp_lucene_removed_docs_duplicates", counter, false);
		cleanUpLucene(indexer, "tmp_lucene_modified_docs", counter, true);
	}
	
	public static void regenerateCleantitles() {
		long size = 9189048;
		
		long counter = 0;
		while (size > (counter*1000)) {			
			Session session = SessionProvider.sessionFactory.openSession();
			session.setFlushMode(FlushMode.ALWAYS);
    		SQLQuery docs = session.createSQLQuery("SELECT id, title, cleanTitle FROM documents limit "+(counter*1000)+",1000");
    		counter++;
    		List<Object[]> result = docs.list();
    		session.close();
    		
    		System.out.println("regenerateCleantitles: "+counter+" of "+size);
    		
    		for (Object[] tuple : result) {
    			String cleanTitle = DocumentQueries.getValidCleanTitle((String) tuple[1]);		
    			if (cleanTitle != null && !cleanTitle.equals((String) tuple[2])) {
    				session = SessionProvider.sessionFactory.openSession();
    				session.setFlushMode(FlushMode.ALWAYS);
    				Query query = session.createSQLQuery("UPDATE documents SET cleantitle = :cleanTitle WHERE id=:id")
    						.setParameter("cleanTitle", cleanTitle)
    						.setParameter("id", tuple[0]);    				
    				executeQueries(session, query);    				
    				session.close();
    			}
    			else if (cleanTitle == null){
    				session = SessionProvider.sessionFactory.openSession();
    				session.setFlushMode(FlushMode.ALWAYS);
					ArrayList<Query> queries = new ArrayList<Query>();
					queries.add(session.createSQLQuery("DELETE FROM documents_persons WHERE id="+tuple[0]));    					        				
					queries.add(session.createSQLQuery("DELETE FROM documents WHERE id="+tuple[0]));        				
					queries.add(session.createSQLQuery("INSERT INTO tmp_lucene_removed_docs(document_id) VALUES ("+tuple[0]+")"));
					executeQueries(session, queries.toArray(new Query[]{}));					
					session.close();
    			}
    		}
		}
				
		Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.ALWAYS);
		SQLQuery query = session.createSQLQuery("INSERT INTO tmp_lucene_removed_docs(document_id) VALUES (0)");
		query.executeUpdate();
		session.close();
	}
	
	public static void main(String[] args) {
		cleanLuceneFromDuplicatedDocuments();
	}
	
	public static void hmaReconnectThread(final boolean triggeredByCaptcha) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {					
					if (triggeredByCaptcha) {
						File file = new File("/home/stefan/hidemyass/captcha_error.log");
						FileWriter fw;
						try {
							fw = new FileWriter(file, true);
							DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
							fw.write(df.format(new Date()) + "\n");
							fw.close();
						}
						catch (IOException e1) {
							e1.printStackTrace();							
						}
					}
					
					String commands[] = {"python", "/home/stefan/hidemyass/HideMyAssReconnecter.py"};
					Process p = Runtime.getRuntime().exec(commands);
					p.waitFor();
					
					BufferedReader reader=new BufferedReader(new InputStreamReader(p.getInputStream()));
					BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
					while ((line = errorReader.readLine()) != null) {
						System.out.println(line);
					}
					synchronized (InternalResource.HMA_GOOGLE_REQUESTS) {
						InternalResource.HMA_GOOGLE_REQUESTS = 0;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * @param session
	 * @param docPerson
	 */
	public static void removeFulltextFromIndex(Session session, DocumentPerson docPerson) {
		List<DocumentsPdfHash> pdfHashes = DocumentsPdfHashQueries.getPdfHashes(session, docPerson.getDocument());
		for (DocumentsPdfHash documentsPdfHash : pdfHashes) {
			try {
				// delete plaintext file
				File f = new File(Indexer.DOCUMENT_PLAINTEXT_DIRECTORY, documentsPdfHash.getHash()+".zip");
				f.delete();

				//update index
				FulltextCommons.requestPlainTextUpdate(docPerson.getDocument(), documentsPdfHash.getHash());
				
				for (DocumentXref xref : XrefQueries.getDocumentXrefs(session, documentsPdfHash.getDocument())) {
					//atomic update xref with indexed state 2 or sth
					xref.setIndexed(2);
					session.update(xref);
					session.flush();
				}
			}
			catch(Exception e) {
				System.out.println("util.InternalCommons.removeFulltextFromIndex(session, docPerson): "+e.getMessage());
			}
		}
	}
	
	public static String buildDocumentIndexListXML(List<DocumentPerson> documentList, Person person, Contact contact) {

		org.w3c.dom.Document dom = getNewXMLDocument();
		if(dom != null) {
			Element root = dom.createElement("document-index-settings");
			dom.appendChild(root);
			
			Element author = dom.createElement("author");
			author.setAttribute("email", contact.getUri());
			author.setAttribute("token", person.getDocidxIdToken());
			author.setAttribute("ignoreAll", Boolean.toString(!person.getDocidxAllow()));
			author.setAttribute("sendNotification", Boolean.toString(person.getDocidxNotify()));
			root.appendChild(author);
			
			Element nameFirst = dom.createElement("firstname");
			nameFirst.setTextContent(normalizeStr(person.getNameFirst()));
			author.appendChild(nameFirst);
			
			Element nameMiddle = dom.createElement("middlename");
			nameMiddle.setTextContent(normalizeStr(person.getNameMiddle()));
			author.appendChild(nameMiddle);
			
			Element nameLast = dom.createElement("lastname");
			String lastName = normalizeStr(person.getNameLastPrefix()) +" ";
			lastName += normalizeStr(person.getNameLast()) + " ";
			lastName += normalizeStr(person.getNameLastSuffix());
			nameLast.setTextContent(normalizeStr(lastName));
			author.appendChild(nameLast);
			
			Element list = dom.createElement("documents");
			for (DocumentPerson document : documentList) {
				Element doc = dom.createElement("document");
				doc.setAttribute("id", Integer.toString(document.getId()));
				String url = "";
				Iterator<FulltextUrl> urls = document.getDocument().getFulltextUrls().iterator();
				if(urls.hasNext()) {
					url = urls.next().getUrl();
				}				
				doc.setAttribute("url", String.valueOf(url));
				
				Element title = dom.createElement("title");
				title.setTextContent(document.getDocument().getTitle());
				doc.appendChild(title);
				
				list.appendChild(doc);
			}
			root.appendChild(list);
			
			return getXMLStr(dom);
		}
		return "";
	}
	
	public static String buildDownloadListXML(List<Object[]> xrefList) {

		org.w3c.dom.Document dom = getNewXMLDocument();
		if(dom != null) {
			Element root = dom.createElement("xrefs");
			dom.appendChild(root);
			
			for (Object[] xref : xrefList) {
				Element doc = dom.createElement("xref");
				doc.setAttribute("id", String.valueOf(xref[0]));
				doc.setAttribute("document_id", String.valueOf(xref[1]));				
				doc.setAttribute("url", String.valueOf(xref[2]));				
				root.appendChild(doc);
			}
			
			return getXMLStr(dom);
		}
		return "";
	}

	public static String buildMailerChunkXML(Collection<Object[]> chunk) {
		org.w3c.dom.Document dom = getNewXMLDocument();
		if(dom != null) {
			Element root = dom.createElement("mailer_chunk");
			dom.appendChild(root);
			Element currentReceiver = null;
			
			Set<String> documentUrls = new HashSet<String>();
			
			for (Object[] row : chunk) {
				String address = String.valueOf(row[0]);
				String title = String.valueOf(row[1]);
				String pid = String.valueOf(row[2]);
				String token = String.valueOf(row[3]);
				String url = String.valueOf(row[4]);
				if(currentReceiver == null || !currentReceiver.getAttribute("address").equals(address)) {
					currentReceiver = dom.createElement("receiver");
					currentReceiver.setAttribute("address", address);
					currentReceiver.setAttribute("pid", pid);
					currentReceiver.setAttribute("token", token);					
					root.appendChild(currentReceiver);
					documentUrls.clear();
				}
				
				if (!documentUrls.contains(url)) {
					Element doc = dom.createElement("document");				
					doc.setAttribute("url", url);				
					doc.setTextContent(title);
				
					currentReceiver.appendChild(doc);
					documentUrls.add(url);
				}
			}
			
			return getXMLStr(dom);
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized Collection<Object[]> getNextMailerChunk(Session session, int chunkSize) {		
		List<Object []> chunk = new ArrayList<Object[]>();
		
		String sql = "SELECT p.id, c.uri, p.docidx_id_token, docidx_last_notified, p.docidx_last_displayed AS email "
				+ "FROM persons p JOIN contacts c ON (p.id=c.person_id) "
				+ "WHERE (p.docidx_last_notified IS NULL OR DATE_ADD(p.docidx_last_notified, INTERVAL 3 MONTH)<NOW()) "
				+ "AND (p.docidx_last_displayed IS NULL OR DATE_ADD(p.docidx_last_displayed, INTERVAL 3 MONTH)<NOW()) "
				+ "AND (p.docidx_notify IS NULL OR p.docidx_notify=1) AND p.docidx_new_documents=1 LIMIT :chunksize";		
		Query query = session.createSQLQuery(sql);
		query.setParameter("chunksize", chunkSize);
		
		List<Object[]> persons = query.list();
		
		for (Object[] tuple : persons) {
			long person_id = Long.valueOf(String.valueOf(tuple[0]));
			String email = (tuple[1]==null ? null : String.valueOf(tuple[1]));
			String docidx_id_token = (tuple[2]==null ? null : String.valueOf(tuple[2]));
			String docidx_last_notified = (tuple[3]==null ? null : String.valueOf(tuple[3]));
			String docidx_last_displayed = (tuple[4]==null ? null : String.valueOf(tuple[4]));
			
			sql = "SELECT :email, d.title, :person_id, :docidx_id_token, dx.sources_id FROM documents d "
					+ "JOIN documents_persons dp ON (dp.document_id = d.id AND dp.person_id = :person_id) "
					+ "JOIN document_xref dx ON "
					+ "(dp.document_id=dx.document_id AND dx.indexed=1 ";
			if (docidx_last_notified != null) {
				sql += "AND :docidx_last_notified < dx.last_attempt ";
			}
			if (docidx_last_displayed != null) {
				sql += "AND :docidx_last_displayed < dx.last_attempt ";
			}			
			sql += ") WHERE title IS NOT NULL";
			
			query = session.createSQLQuery(sql);
			query.setParameter("person_id", person_id);
			query.setParameter("email", email);
			query.setParameter("docidx_id_token", docidx_id_token);
			if (docidx_last_notified != null) {
				query.setParameter("docidx_last_notified", docidx_last_notified);
			}
			if (docidx_last_displayed != null) {
				query.setParameter("docidx_last_displayed", docidx_last_displayed);
			}
			
			List<Object[]> chunkSnippet = query.list();
			if (chunkSnippet == null || chunkSnippet.size() ==0) {
				Transaction transaction = session.beginTransaction();
				try {
					sql = "UPDATE persons SET docidx_new_documents=0 WHERE id = :person_id";
					query = session.createSQLQuery(sql);
					query.setParameter("person_id", person_id);
					query.executeUpdate();
					
					transaction.commit();
					session.flush();
				}
				catch (Exception e) {
					transaction.rollback();
					e.printStackTrace();
				}
			}
			else {
				chunk.addAll(chunkSnippet);
			}
		}
		
		return chunk;
//		String sql = "Select p.uri, d.title, p.id, p.docidx_id_token, dx.sources_id FROM " + 
//		"(SELECT p.id, p.docidx_last_notified, p.docidx_last_displayed, c.uri, p.docidx_id_token FROM persons p JOIN contacts c ON (p.id=c.person_id) WHERE (p.docidx_last_notified IS NULL OR DATE_ADD(p.docidx_last_notified, INTERVAL 3 MONTH)<NOW()) AND (p.docidx_last_displayed IS NULL OR DATE_ADD(p.docidx_last_displayed, INTERVAL 3 MONTH)<NOW()) AND (p.docidx_notify IS NULL OR p.docidx_notify=1) AND <=1 Limit :chunksize) p " +   
//		"LEFT JOIN documents_persons dp ON (p.id=dp.person_id) " + 
//		"LEFT JOIN document_xref dx ON (dp.document_id=dx.document_id AND dx.indexed=1 AND (p.docidx_last_notified IS NULL OR p.docidx_last_notified < dx.last_attempt) AND (p.docidx_last_displayed IS NULL OR p.docidx_last_displayed < dx.last_attempt)) " + 
//		"LEFT JOIN documents d ON (dx.document_id=d.id) " +
//		"WHERE title IS NOT NULL " + 
//		"ORDER BY p.id";
//		Query query = session.createSQLQuery(sql);
//		
//		query.setParameter("chunksize", chunkSize);
//		
//		return query.list();
	}	
	
	public static org.w3c.dom.Document getNewXMLDocument() {
		
		// instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        return db.newDocument();	        
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    }
	    return null;
		
	}

	/**
	 * @param dom
	 */
	public static String getXMLStr(org.w3c.dom.Document dom) {
		StringWriter out = new StringWriter();
		Transformer transf;
		try {
			transf = TransformerFactory.newInstance().newTransformer();
			transf.setOutputProperty(OutputKeys.INDENT, "yes");
			transf.setOutputProperty(OutputKeys.METHOD, "xml");
		    transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		    
		    transf.transform(new DOMSource(dom), new StreamResult(out));
		    
		    return out.toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static String normalizeStr(String str) {
		return str == null ? "" : str.trim();
	}
}
