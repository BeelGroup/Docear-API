package org.docear.lucene;

import java.io.IOException;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.docear.database.AlgorithmArguments;
import org.docear.graphdb.GraphDbWorker;
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
	public void fillKeywords(Integer userId, AlgorithmArguments args, UserModel userModel, String excludeHash) throws Exception {
		long sTime = System.currentTimeMillis();		
		Directory directory = buildLuceneDocumentForUser(userId, args, userModel, excludeHash);

		IndexReader tfReader = IndexReader.open(directory);
		// reader.getUniqueTermCount();
		TermEnum tfTerms = tfReader.terms();
				
		while (tfTerms.next()) {
			Term term = tfTerms.term();
			String termText = term.text();
			try {
				Float.parseFloat(termText);
			} catch (Exception e) {
				try {
					// get all documents that contain the term
					TermDocs docs = tfReader.termDocs(term);
					double termNodeWeight = 0;
					
					while (docs.next()) { 			
						// get term frequencies for the field the term belongs to in the document
						TermFreqVector tvf = tfReader.getTermFreqVector(docs.doc(), term.field());
						Integer freqInField = tvf.getTermFrequencies()[tvf.indexOf(termText)];
						termNodeWeight += freqInField * nodeWeightsTotal.get(tvf.getField());
					}
					docs.close();
					
					Keywords keywds = userModel.getKeywords();
					Keyword keywdForTerm = keywds.getKeywordByTerm(termText);		
					// if the term already exists update its weight
					if (keywdForTerm != null)
						keywdForTerm.setWeight(keywdForTerm.getWeight() + termNodeWeight);
					else
						keywds.addKeyword(termText, termNodeWeight);					
				} 
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		tfReader.close();
		setIDFFromMindmaps(userModel.getKeywords());

		System.out.println("tfidf-terms for user: "+userId+" (" + (System.currentTimeMillis() - sTime) + ")");	
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
