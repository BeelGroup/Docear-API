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
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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
public class Searcher {
	
	public class DocumentHashItem {
		public Integer documentId;
		public String pdfHash;
		public int rank;
		public Float relevance;
	}
	
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
//		ir = IndexReader.open(FSDirectory.open(indexDir), true);
		ir = SessionProvider.getLuceneIndexer().getIndexReader();
		is = new IndexSearcher(ir);
		
		logger.info("Lucene index initialized.");
	}
	
	public List<DocumentHashItem> search(String search) throws ParseException, IOException {
		return search(search, "title", 100);
	}
	
	public List<DocumentHashItem> search(String search, String field, int max) throws ParseException, IOException {		
		Query q = new QueryParser(Version.LUCENE_34, field, new StandardAnalyzer(Version.LUCENE_34)).parse(search);		
		
		return search(q, max);
	}
	
	public List<DocumentHashItem> search(Query query, int max) throws ParseException, IOException {
		List<DocumentHashItem> r = new ArrayList<DocumentHashItem>();
				
		TopDocs td = is.search(query, max);		
		int rank = 1;
		for (ScoreDoc sd : td.scoreDocs) {			
			DocumentHashItem item = new DocumentHashItem();
			try {
				item.documentId = Integer.parseInt((is.doc(sd.doc).get("id")));
				item.pdfHash = is.doc(sd.doc).get("hash");				
				item.rank = rank++;
				item.relevance =sd.score;
				r.add(item);
			}
			catch (Exception e) {
				logger.info("Exception in org.mrdlib.index.Searcher.search(): "+e.getMessage());
			}
		}
		return r;
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
		is.close();
//		ir.close();
		return this;
	}
}
