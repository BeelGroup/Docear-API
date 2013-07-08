/**
 * 
 */
package org.mrdlib.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.resources.Citation;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.Keyword;
import org.sciplore.resources.PersonHomonym;
import org.sciplore.utilities.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 * @author Mario Lipinski <a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>
 */
public class Indexer {
		
	private final static Logger logger = LoggerFactory.getLogger(Indexer.class);
	private String indexDir = Config.getProperties("org.mrdlib").getProperty("indexDir");
	public static File DOCUMENT_PLAINTEXT_DIRECTORY = new File(Config.getProperties("org.mrdlib").getProperty("plainTextDir"));
	private static IndexWriter iw;
	static {
		if (!DOCUMENT_PLAINTEXT_DIRECTORY.exists()) {
			DOCUMENT_PLAINTEXT_DIRECTORY.mkdirs();
		}
	}
	
	private final File indexDirFile;
	

	public Indexer() throws IOException {
		logger.info("Initializing Lucene index writer.");
		indexDirFile = new File(this.indexDir);
		if (!indexDirFile.isDirectory()) {
			throw new FileNotFoundException("Index directory does not exist.");
		}
		logger.info("Lucene Indexer initialized.");
	}
	
	public Indexer addDocument(org.sciplore.resources.Document d) throws CorruptIndexException, IOException {
		return addDocument(d, null);
	}
	
	public Indexer addDocument(org.sciplore.resources.Document d, String hash) throws CorruptIndexException, IOException {
		synchronized (this) {
			//getIndexWriter().updateDocument(new Term("id", d.getId().toString()), createLuceneDocument(d, hash));
			getIndexWriter().addDocument(createLuceneDocument(d, hash));
			getIndexWriter().commit();
			//updateDocument(d, hash);
			//System.err.println("Lucene: added new doc with fulltext("+String.valueOf(hash)+").");
			return this;
		}
	}
	
	public Indexer deleteDocument(org.sciplore.resources.Document d) throws Exception {
		return deleteDocument(d.getId());
	}
	
	public Indexer deleteDocument(int documentId) throws Exception {
		synchronized (this) {			
			getIndexWriter().deleteDocuments(new Term("sid", Integer.toString(documentId)));
			getIndexWriter().commit();
			IndexReader ir = getIndexReader();
//			System.out.println("remaining documents in lucene index: " + ir.numDocs());
			ir.close();
			//System.err.println("Lucene: deleted doc.");
			return this;
		}
	}
	
	public Indexer updateDocument(org.sciplore.resources.Document d) throws IOException {
		return updateDocument(d, null);
	}
	
	public Indexer updateDocument(org.sciplore.resources.Document d, String hash) throws IOException {
		synchronized (this) {
			//find all 
			Collection<Document> docs = getByDocumentId(d.getId());
						
			//indicator to show whether the document d was already updated
			boolean addDoc = true;
			
			//remove all the documents with the same document id
			getIndexWriter().deleteDocuments(new Term("sid", Integer.toString(d.getId())));
			
			//loop through each found document and update the fields
			for (Iterator<Document> iterator = docs.iterator(); iterator.hasNext();) {
				Document storedDoc = iterator.next();
				//look for the document hash
				String storedHash = storedDoc.get("hash");
				//ignore documents with empty hash values because they would be replaced by the update anyway
				if(storedHash != null) {
					//replace all documents with the given hash by a newly created/updated document 
					getIndexWriter().updateDocument(new Term("hash", storedHash), createLuceneDocument(d, storedHash));
					//in case the document which shall be updated is already up-to-date, adjust the indicator 
					if(hash==null || storedHash.equals(hash)) {
						addDoc = false;
					}
				}				
			}
			// add the currently updated document if necessary
			if(addDoc) {
				getIndexWriter().addDocument(createLuceneDocument(d, hash));
			}			
			
			getIndexWriter().commit();
			
			return this;
		}
	}
	
	private Indexer close() throws CorruptIndexException, IOException {
		synchronized (this) {
			getIndexWriter().close();			
			iw = null;
			return this;
		}
	}
	
	private boolean addPlainTextFromFile(Document lucDoc, String hash) {
		if(hash != null) {
			lucDoc.add(new Field("text", loadPlainText(hash), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
			lucDoc.add(new Field("hash", hash, Field.Store.YES, Field.Index.NOT_ANALYZED));
			lucDoc.add(new Field("fulltext_type", "PDF", Field.Store.YES, Field.Index.NO));
			return true;
		}
		return false;
	}

	private String loadPlainText(String hash) {
		StringBuilder builder = new StringBuilder();
		try {
			File file = new File(DOCUMENT_PLAINTEXT_DIRECTORY, hash+".zip");
			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
			ZipEntry plainText;
			while((plainText = zis.getNextEntry()) != null) {
				if((hash+".txt").equals(plainText.getName())) {
					InputStreamReader reader = new InputStreamReader(zis);
					int chr = -1;
					while((chr = reader.read()) > -1 ) {
						builder.append((char)chr);
					}					
					break;
				}
			}
			zis.close();
		} catch (Exception ex) {
			logger.warn(ex.getMessage());
			builder.delete(0, builder.length());
		}	
		return builder.toString();
	}

	private Collection<Document> getByDocumentId(Integer docId) throws IOException {
		Query q = new TermQuery(new Term("sid", String.valueOf(docId)));
		
		Set<Document> lucDocs = new HashSet<Document>();
		try {
			IndexReader ir = getIndexReader();
			IndexSearcher is = new IndexSearcher(ir);
			try {
				TopDocs td = is.search(q, 100);		
				if(td.totalHits > 0) {
					for (int i = 0; i < td.scoreDocs.length; i++) {
						lucDocs.add(is.doc(td.scoreDocs[i].doc));
					}
				}
			}
			finally {
				is.close();
				ir.close();
			}
		}
		catch (Throwable e) {
		}
		return lucDocs;
	}
	
	public IndexReader getIndexReader() throws IOException {
		IndexReader ir = null;
		if(ir == null) {
			//ir = IndexReader.open(FSDirectory.open(indexDirFile), true);
			ir = IndexReader.open(getIndexWriter(), true); //true=apply deletes (slower/costly); false=ignore deletes(if deleted docs are no problem)
		}
		return ir;
	}
	
	private IndexWriter getIndexWriter() throws IOException {
		if(iw == null) {
			iw = new IndexWriter(FSDirectory.open(indexDirFile), new IndexWriterConfig(Version.LUCENE_34, new StandardAnalyzer(Version.LUCENE_34)).setRAMBufferSizeMB(64));
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		return iw;
	}
		
	public Indexer commit() throws CorruptIndexException, IOException {
		synchronized (this) {
			getIndexWriter().commit();
			return this;
		}
	}
	
	private Document createLuceneDocument(org.sciplore.resources.Document d, String hash) {
		Document ld = new Document();
		ld.add(new NumericField("id", Store.YES, false).setIntValue(d.getId()));		
		ld.add(new Field("sid", Integer.toString(d.getId()), Store.YES, Index.NOT_ANALYZED));
		
		if (d.getType() != null) {
			ld.add(new Field("type", d.getType(), Store.NO, Index.ANALYZED));
		}
		
		addPlainTextFromFile(ld, hash);
		
		if (d.getTitle() != null) {
			ld.add(new Field("title", d.getTitle(), Store.YES, Index.ANALYZED));
			ld.add(new Field("default", d.getTitle(), Store.YES, Index.ANALYZED));
		}
		
		try {
			if (d.getAbstract() != null) {
				ld.add(new Field("abstract", d.getAbstract(), Store.NO, Index.ANALYZED));
			}
			
//			for (DocumentPerson dp : d.getPersons()) {
//				if (dp.getType() == DocumentPerson.DOCUMENTPERSON_TYPE_AUTHOR) {
//					PersonHomonym ph = dp.getPersonHomonym();
//					ld.add(new Field("author", ph.getNameFirst() + " " + ph.getNameMiddle() + " " + ph.getNameLastPrefix() + " " + ph.getNameLast() + " " + ph.getNameLastSuffix(), Store.YES, Index.ANALYZED));
//					ld.add(new Field("default", ph.getNameFirst() + " " + ph.getNameMiddle() + " " + ph.getNameLastPrefix() + " " + ph.getNameLast() + " " + ph.getNameLastSuffix(), Store.YES, Index.ANALYZED));
//					for (PersonHomonym p : dp.getPersonMain().getHomonyms()) {
//						ld.add(new Field("author", p.getNameFirst() + " " + p.getNameMiddle() + " " + p.getNameLastPrefix() + " " + p.getNameLast() + " " + p.getNameLastSuffix(), Store.YES, Index.ANALYZED));
//						ld.add(new Field("default", p.getNameFirst() + " " + p.getNameMiddle() + " " + p.getNameLastPrefix() + " " + p.getNameLast() + " " + p.getNameLastSuffix(), Store.YES, Index.ANALYZED));
//					}
//			
//					if (dp.getPersonMain().getInstitution() != null) {
//						ld.add(new Field("affiliation", dp.getPersonMain().getInstitution().getName(), Store.YES, Index.ANALYZED));
//					}
//				}
//			}
			
//			for (Keyword k : d.getKeywords()) {
//				ld.add(new Field("keyword", k.getKeyword(), Store.NO, Index.ANALYZED));
//				ld.add(new Field("default", k.getKeyword(), Store.NO, Index.ANALYZED));
//			}
			
//			if (d.getVenue() != null) {
//				ld.add(new Field("publishedin", d.getVenue().getName(), Store.YES, Index.ANALYZED));
//			}
			
			if (d.getPublishedYear() != null) {
				String pubdate = d.getPublishedYear().toString();
				if (d.getPublishedMonth() != null) {
					pubdate += d.getPublishedMonth().toString();
					if (d.getPublishedDay() != null) {
						pubdate+= d.getPublishedDay().toString();					
					}
				}
				ld.add(new Field("pubdate", pubdate, Store.NO, Index.ANALYZED));
//				ld.add(new Field("default", pubdate, Store.NO, Index.ANALYZED));
			}
			
			if (d.getPublishedPlace() != null) {
				ld.add(new Field("location", d.getPublishedPlace(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getPublisher() != null) {
				ld.add(new Field("publisher", d.getPublisher(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getEdition() != null) {
				ld.add(new Field("edition", d.getEdition().toString(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getNumber() != null) {
				ld.add(new Field("number", d.getNumber().toString(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getVolume() != null) {
				ld.add(new Field("volume", d.getVolume().toString(), Store.NO, Index.ANALYZED));
			}
	
			if (d.getPages() != null) {
				ld.add(new Field("pages", d.getPages().toString(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getSeries() != null) {
				ld.add(new Field("series", d.getSeries(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getDoi() != null) {
				ld.add(new Field("doi", d.getDoi(), Store.NO, Index.ANALYZED));
				ld.add(new Field("default", d.getDoi(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getIssn() != null) {
				ld.add(new Field("issn", d.getIssn(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getIsbn() != null) {
				ld.add(new Field("isbn", d.getIsbn(), Store.NO, Index.ANALYZED));
			}
			
			if (d.getLanguage() != null) {
				ld.add(new Field("lang", d.getLanguage(), Store.NO, Index.ANALYZED));
			}
			
	
//			for (DocumentXref x : d.getXrefs()) {
//				ld.add(new Field("oid", x.getSource() + "/" + x.getSourcesId(), Store.NO, Index.ANALYZED));
//			}
			StringBuilder refIdBuffer = new StringBuilder();
			for (Citation c : d.getCitations()) {
				String authors = "";
				org.sciplore.resources.Document cd = c.getCitedDocument();
				if(cd.getId() != null) {
					refIdBuffer.append("dcr_doc_id_").append(cd.getId()).append(" ");
				}
//				for (DocumentPerson dp : cd.getPersons()) {
//					if (dp.getType() == DocumentPerson.DOCUMENTPERSON_TYPE_AUTHOR) {
//						PersonHomonym ph = dp.getPersonHomonym();
//						ld.add(new Field("reflist", ph.getNameFirst() + " " + ph.getNameMiddle() + " " + ph.getNameLastPrefix() + " " + ph.getNameLast() + " " + ph.getNameLastSuffix(), Store.YES, Index.ANALYZED));
//						for (PersonHomonym p : dp.getPersonMain().getHomonyms()) {
//							ld.add(new Field("reflist", p.getNameFirst() + " " + p.getNameMiddle() + " " + p.getNameLastPrefix() + " " + p.getNameLast() + " " + p.getNameLastSuffix(), Store.YES, Index.ANALYZED));
//						}
//					}
//				}
//				ld.add(new Field("reflist", cd.getTitle() + " " + authors + " " + cd.getPublishedYear() + " " + cd.getDoi(), Store.NO, Index.ANALYZED)); // TODO more info into string
				
			}
			if(refIdBuffer.length() > 0) {
				ld.add(new Field("references", refIdBuffer.toString(), Store.NO, Index.ANALYZED));
			}
		}
		catch (Throwable e) {
			System.out.println("Exception in Indexer.createLuceneDocument():"+e.getMessage());
		}
		// TODO: header, toc, headings, body, figures, tables, formulas, acknowledgments 
		
		return ld;
	}
	
	/********************************************
	 * search functionality
	 ********************************************/
	
	/**
	 * @param search
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public List<DocumentHashItem> search(String search) throws ParseException, IOException {
		return search(search, 100);
	}
	
	/**
	 * @param search
	 * @param max
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public List<DocumentHashItem> search(String search, int max) throws ParseException, IOException {
		List<DocumentHashItem> r = new ArrayList<DocumentHashItem>();
		try {
			if(search.startsWith("luceneMergeTo:")) {
				String[] tok = search.trim().split(":");
				getIndexWriter().forceMerge(Integer.parseInt(tok[1]), false);
				return r;
			}
		} catch (Exception e) {
		}
		
		IndexReader ir = getIndexReader();
		IndexSearcher is = new IndexSearcher(ir);		
		try {
			Query q = new QueryParser(Version.LUCENE_34, "title", new StandardAnalyzer(Version.LUCENE_34)).parse(search);
			TopDocs td = is.search(q, max);
			int rank = 1;
			for (ScoreDoc sd : td.scoreDocs) {
				DocumentHashItem item = new DocumentHashItem();
				try {
					item.documentId = Integer.parseInt((is.doc(sd.doc).get("id")));
					item.pdfHash = is.doc(sd.doc).get("hash");
					item.rank = rank++;
					r.add(item);
				}
				catch (Exception e) {
					logger.info("Exception in org.mrdlib.index.Searcher.search(): "+e.getMessage());
				}
			}
		}
		finally {
			is.close();
			ir.close();
		}
		return r;
	}
	
	public static void main(String args[]) throws IOException {
		Indexer idx = new Indexer();
		final Session session = SessionProvider.sessionFactory.openSession();
		for (org.sciplore.resources.Document d : DocumentQueries.getDocuments(session)) {
			idx.updateDocument(d);
			idx.commit();
		}
	}
	
	public class DocumentHashItem {
		public Integer documentId;
		public String pdfHash;
		public int rank;
	}
	

}
