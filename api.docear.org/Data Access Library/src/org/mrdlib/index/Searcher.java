/**
 * 
 */
package org.mrdlib.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.sciplore.database.SessionProvider;
import org.sciplore.utilities.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *
 * @author Mario Lipinski <a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>
 */
/**
 * @author stefan
 *
 */
public class Searcher {	
	private final static Logger logger = LoggerFactory.getLogger(Searcher.class);
	private String indexDir = Config.getProperties("org.mrdlib").getProperty("indexDir");
	private IndexReader ir;
	private IndexSearcher is;

	public Searcher() throws CorruptIndexException, LockObtainFailedException, IOException {
		logger.info("Initializing Lucene index.");
		File indexDir = new File(this.indexDir);
		if (!indexDir.isDirectory()) {
			throw new FileNotFoundException("Index directory does not exist.");
		}
		ir = SessionProvider.getLuceneIndexer().getIndexReader();
		is = new IndexSearcher(ir);
		
		logger.info("Lucene index initialized.");
	}
	
	public List<DocumentHashItem> search(String search) throws ParseException, IOException {
		return search(search, "text", 0, 100, null);
	}
			
	public List<DocumentHashItem> search(String search, String defaultField, int offset, int number, Integer returnNumber) throws ParseException, IOException {	
		if (defaultField == null) {
			defaultField = "text";
		}
		
		Query query = new QueryParser(Version.LUCENE_46, defaultField, new StandardAnalyzer(Version.LUCENE_46)).parse(search);
		return search(query, offset, number, returnNumber);
	}
	
	public List<DocumentHashItem> search(Query query, int offset, int number) throws ParseException, IOException {
		return search(query, offset, number, null);
	}
	
	/**
	 * @param query
	 * @param offset
	 * @param number number of docs that should be retrieved from lucene (important for paginator)
	 * @param returnNumber number of docs that should be returned
	 * @return List of DocumentHashItem retrieved from Lucene
	 * @throws ParseException
	 * @throws IOException
	 */
	public List<DocumentHashItem> search(Query query, int offset, int number, Integer returnNumber) throws ParseException, IOException {
		List<DocumentHashItem> documentHashItem = new ArrayList<DocumentHashItem>();		
		TopDocs td = is.search(query, number);
		
		if (returnNumber == null) {
			returnNumber = number;
		}
				
		int i = 0;
		
		ScoreDoc[] docs = td.scoreDocs;
		int documentsAvailable = docs.length;
		for (ScoreDoc sd : td.scoreDocs) {
			if (i++ >= returnNumber+offset) {
				break;
			}
			
			DocumentHashItem item = new DocumentHashItem();			
			if (i > offset) {
    			try {
    				item.documentId = Integer.parseInt((is.doc(sd.doc).get("id")));
    				item.pdfHash = is.doc(sd.doc).get("hash");				
    				item.rank = i;
    				item.relevance = sd.score;
    				item.documentsAvailable = documentsAvailable;
    				documentHashItem.add(item);
    			}
    			catch (Exception e) {
    				logger.info("Exception in org.mrdlib.index.Searcher.search(): "+e.getMessage());
    			}
			}
		}
		System.out.println("LuceneQuery added "+i+" results to result list");
		return documentHashItem;
	}
	
	public Double[] getIDF(String[] terms, String field) throws Exception {				
		Double idf[] = new Double[terms.length];
		IndexReader reader = SessionProvider.getLuceneIndexer().getIndexReader();	
		for (int i=0; i<terms.length; i++) {
			int termDocs = reader.docFreq(new Term(field, terms[i]));
			if (termDocs == 0) {
				idf[i] = (double) 0;
			}
			else {
				idf[i] = Math.log(((double) reader.numDocs() / ((double) termDocs)));
			}
		}
		
		return idf;
	}
	
	public Double getIDF(String term, String field) throws Exception {
		String[] terms = new String[] {term};

		return getIDF(terms, field)[0];		
	}
	
	public Searcher close() throws IOException {		
		ir.close();
		return this;
	}
}
