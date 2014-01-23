/**
 * 
 */
package org.mrdlib.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hibernate.Session;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.resources.Citation;
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
	public static int RAM_BUFFER_SIZE_MB = Integer.parseInt(Config.getProperties("org.mrdlib").getProperty("RAMBufferSize", "64"));
	public static final FieldType plainTextFieldType = new FieldType(TextField.TYPE_NOT_STORED);
	private static IndexWriter iw;
	static {
		if (!DOCUMENT_PLAINTEXT_DIRECTORY.exists()) {
			DOCUMENT_PLAINTEXT_DIRECTORY.mkdirs();
		}
		plainTextFieldType.setStoreTermVectors(true);
		plainTextFieldType.freeze();
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
			lucDoc.add(new Field("text", loadPlainText(hash), plainTextFieldType));
			lucDoc.add(new TextField("hash", hash, Store.YES));
			lucDoc.add(new StoredField("fulltext_type", "PDF"));
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
			ir = DirectoryReader.open(getIndexWriter(), true); //true=apply deletes (slower/costly); false=ignore deletes(if deleted docs are no problem)
		}
		return ir;
	}
	
	private IndexWriter getIndexWriter() throws IOException {
		if(iw == null) {
			System.out.println("creating new IndexWriter with "+RAM_BUFFER_SIZE_MB+"MB as RAM buffer.");
			iw = new IndexWriter(FSDirectory.open(indexDirFile), new IndexWriterConfig(Version.LUCENE_46, new StandardAnalyzer(Version.LUCENE_46)).setRAMBufferSizeMB(RAM_BUFFER_SIZE_MB));
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
		
		ld.add(new IntField("id", d.getId(), Store.YES));				
		ld.add(new StringField("sid", Integer.toString(d.getId()), Store.YES));
		
		if (d.getType() != null) {
			ld.add(new TextField("type", d.getType(), Store.NO));
		}
		
		addPlainTextFromFile(ld, hash);
		
		if (d.getTitle() != null) {
			ld.add(new TextField("title", d.getTitle(), Store.YES));
			ld.add(new TextField("default", d.getTitle(), Store.NO));
		}
		
		try {
			if (d.getAbstract() != null) {
				ld.add(new TextField("abstract", d.getAbstract(), Store.NO));
			}
			if (d.getPublishedYear() != null) {
				String pubdate = d.getPublishedYear().toString();
				if (d.getPublishedMonth() != null) {
					pubdate += d.getPublishedMonth().toString();
					if (d.getPublishedDay() != null) {
						pubdate+= d.getPublishedDay().toString();					
					}
				}
				ld.add(new TextField("pubdate", pubdate, Store.NO));
//				ld.add(new TextField("default", pubdate, Store.NO));
			}
			
			if (d.getPublishedPlace() != null) {
				ld.add(new TextField("location", d.getPublishedPlace(), Store.NO));
			}
			
			if (d.getPublisher() != null) {
				ld.add(new TextField("publisher", d.getPublisher(), Store.NO));
			}
			
			if (d.getEdition() != null) {
				ld.add(new TextField("edition", d.getEdition().toString(), Store.NO));
			}
			
			if (d.getNumber() != null) {
				ld.add(new TextField("number", d.getNumber().toString(), Store.NO));
			}
			
			if (d.getVolume() != null) {
				ld.add(new TextField("volume", d.getVolume().toString(), Store.NO));
			}
	
			if (d.getPages() != null) {
				ld.add(new TextField("pages", d.getPages().toString(), Store.NO));
			}
			
			if (d.getSeries() != null) {
				ld.add(new TextField("series", d.getSeries(), Store.NO));
			}
			
			if (d.getDoi() != null) {
				ld.add(new TextField("doi", d.getDoi(), Store.NO));
				ld.add(new TextField("default", d.getDoi(), Store.NO));
			}
			
			if (d.getIssn() != null) {
				ld.add(new TextField("issn", d.getIssn(), Store.NO));
			}
			
			if (d.getIsbn() != null) {
				ld.add(new TextField("isbn", d.getIsbn(), Store.NO));
			}
			
			if (d.getLanguage() != null) {
				ld.add(new TextField("lang", d.getLanguage(), Store.NO));
			}
			StringBuilder refIdBuffer = new StringBuilder();
			for (Citation c : d.getCitations()) {
				org.sciplore.resources.Document cd = c.getCitedDocument();
				if(cd.getId() != null) {
					refIdBuffer.append("dcr_doc_id_").append(cd.getId()).append(" ");
				}	
			}
			if(refIdBuffer.length() > 0) {
				ld.add(new TextField("references", refIdBuffer.toString(), Store.NO));
			}
		}
		catch (Throwable e) {
			System.out.println("Exception in Indexer.createLuceneDocument():"+e.getMessage());
		}
		// TODO: header, toc, headings, body, figures, tables, formulas, acknowledgments 
		
		return ld;
	}
	
	public static void main(String args[]) throws IOException {
		Indexer idx = new Indexer();
		final Session session = SessionProvider.sessionFactory.openSession();
		for (org.sciplore.resources.Document d : DocumentQueries.getDocuments(session)) {
			idx.updateDocument(d);
			idx.commit();
		}
	}
	

}
