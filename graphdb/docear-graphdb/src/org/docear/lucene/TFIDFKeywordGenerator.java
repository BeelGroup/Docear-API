package org.docear.lucene;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.docear.Logging.DocearLogger;
import org.docear.database.AlgorithmArguments;
import org.docear.graphdb.GraphDbWorker;
import org.docear.graphdb.QuerySession;
import org.docear.xml.Keyword;
import org.docear.xml.Keywords;
import org.docear.xml.UserModel;

public class TFIDFKeywordGenerator extends TFKeywordGenerator {
	
	public TFIDFKeywordGenerator(AlgorithmArguments args) {
		this(args, LuceneController.getCurrentController().getGraphDbWorker());
	}
	
	public TFIDFKeywordGenerator(AlgorithmArguments args, GraphDbWorker graphDbWorker) {
		super(args, graphDbWorker);
	}
	
	@Override
	public void fillKeywords(QuerySession session, Integer userId, AlgorithmArguments args, UserModel userModel, String excludePdfHash) throws Exception {
		long sTime = System.currentTimeMillis();		

		super.fillKeywords(session, userId, args, userModel, excludePdfHash);
		setIDFFromMindmaps(userModel.getKeywords());

		DocearLogger.info("tfidf-terms for user: "+userId+" (" + (System.currentTimeMillis() - sTime) + ")");	
	}
	

	private double getIDFFromMindmaps(IndexReader reader, String field, String term) throws IOException {		
		return Math.log((reader.numDocs() / ((double) reader.docFreq(new Term(field, term)))));
	}

	private void setIDFFromMindmaps(final Keywords keywords) throws Exception {
		IndexReader dfReader = LuceneController.getCurrentController().getIndexReader();
		try {	
			for (Keyword keyword : keywords.getKeywords()) {
    			double weight = keyword.getWeight() * getIDFFromMindmaps(dfReader, "text", keyword.getTerm());
    			keyword.setWeight(weight);
    		}
		}
		finally {
			dfReader.close();
		}
	}
}
