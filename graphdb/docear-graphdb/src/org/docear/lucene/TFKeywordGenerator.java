package org.docear.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.docear.database.AlgorithmArguments;
import org.docear.graphdb.GraphDbWorker;
import org.docear.lucene.NodeWeightCalculator.ParameterMetric;
import org.docear.query.ResultGenerator;
import org.docear.structs.NodeInfo;
import org.docear.xml.Keyword;
import org.docear.xml.Keywords;
import org.docear.xml.UserModel;

public class TFKeywordGenerator implements ResultGenerator {	
	private static final String FIELD = "field";
	
	private GraphDbWorker worker;
	private final AlgorithmArguments args;
	
	protected Map<String, Double> nodeWeightsTotal = new HashMap<String, Double>();
	
	public TFKeywordGenerator(AlgorithmArguments args) {
		this(args, LuceneController.getCurrentController().getGraphDbWorker());
	}
	
	public TFKeywordGenerator(AlgorithmArguments args, GraphDbWorker graphDbWorker) {		
		if(args == null) {
			throw new NullPointerException();
		}
		this.args = args;
		worker = graphDbWorker;
	}

	protected GraphDbWorker getWorker() {
		return worker;
	}
	
	public void fillKeywords(Integer userId, AlgorithmArguments args, UserModel userModel, String excludePdfHash) throws Exception {
		Directory directory = buildLuceneDocumentForUser(userId, args, userModel, excludePdfHash);

		IndexReader reader = IndexReader.open(directory);
		// reader.getUniqueTermCount();
		TermEnum terms = reader.terms();
		
		while (terms.next()) {
			Term term = terms.term();
			String termText = term.text();

			try {
				Float.parseFloat(termText);
			} catch (Exception e) {
				try {
					// get all documents that contain the term
					TermDocs docs = reader.termDocs(term);
					double termNodeWeight = 0;
					
					while (docs.next()) { 			
						// get term frequencies for the field the term belongs to in the document
						TermFreqVector tvf = reader.getTermFreqVector(docs.doc(), term.field());
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
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		reader.close();
	}
	
	protected Directory buildLuceneDocumentForUser(int userId, AlgorithmArguments args, UserModel userModel, String excludePdfHash) throws Exception {
		if (args == null) {
			args = new AlgorithmArguments("");
		}

		RAMDirectory directory = new RAMDirectory();
		DocearAnalyzer analyzer = new DocearAnalyzer(Version.LUCENE_35
				, new Integer(1).equals(args.getArgument(AlgorithmArguments.STOPWORDS))
				, new Integer(1).equals(args.getArgument(AlgorithmArguments.STEMMING)));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
		try {
			IndexWriter writer = new IndexWriter(directory, config);

			Document doc = getDocument(userId, args, userModel, excludePdfHash);
			if (doc == null) {
				writer.close();
				throw new Exception("not enough data gathered for (" + args + ")");
			}
			writer.addDocument(doc, analyzer);						
			writer.close();
			
    		userModel.getKeywords().addVariable("feature_count_expanded", ""+analyzer.getOriginalTermsCount());    		;
    		userModel.getKeywords().addVariable("feature_count_expanded_unique", ""+analyzer.getOriginalUniqueTermsCount());
    		userModel.getKeywords().addVariable("feature_count_reduced", ""+analyzer.getReducedTermsCount());
    		userModel.getKeywords().addVariable("feature_count_reduced_unique", ""+analyzer.getReducedUniqueTermsCount());
    
    		return directory;

		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Document getDocument(int userId, AlgorithmArguments args, UserModel userModel, String excludePdfHash) {		
		ArrayList<NodeInfo> nodesInfo = worker.getUserNodesInfo(userId, args, userModel, excludePdfHash);		
		
		if(nodesInfo == null || nodesInfo.size() == 0) {
			return null;
		}
		
		Document doc = new Document();
		
		int fieldNumber = 1;
		Double maxFieldWeight = 1.0;
		
		for (Iterator<NodeInfo> iter = nodesInfo.iterator(); iter.hasNext();) {
			NodeInfo nodeInfo = iter.next();
			
			// give a unique name to the document field
			String fieldName = FIELD.concat(String.valueOf(fieldNumber++));
			Field fieldToAdd = new Field(fieldName, nodeInfo.getText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);
			Double fieldWeight = 1.0;
	
			if (nodeInfo.getDepth() != null) 				
				fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 
						1.0 + nodeInfo.getDepth(), 
						NodeWeightCalculator.ParameterMetric.values()[(Integer)args.getArgument(AlgorithmArguments.NODE_DEPTH_METRIC)],
						NodeWeightCalculator.ParameterOperator.get((Integer)args.getArgument(AlgorithmArguments.NODE_DEPTH)));
			if (nodeInfo.getNoOfSiblings() != null) 
				fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 
						1.0 + nodeInfo.getNoOfSiblings(), 
						NodeWeightCalculator.ParameterMetric.values()[(Integer)args.getArgument(AlgorithmArguments.NO_SIBLINGS_METRIC)],
						NodeWeightCalculator.ParameterOperator.get((Integer)args.getArgument(AlgorithmArguments.NO_SIBLINGS)));
			if (nodeInfo.getNoOfChildren() != null) 
				fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 
						1.0 + nodeInfo.getNoOfChildren(), 
						NodeWeightCalculator.ParameterMetric.values()[(Integer)args.getArgument(AlgorithmArguments.NO_CHILDREN_METRIC)],
						NodeWeightCalculator.ParameterOperator.get((Integer)args.getArgument(AlgorithmArguments.NO_CHILDREN)));
			
			nodeWeightsTotal.put(fieldName, fieldWeight);
			
			// compare with max node weight (if node weight normalization is set)
			// check that at least one parameter is considered (although normnalization should not be set otherwise)
			if (new Integer(1).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION)) &&
					(nodeInfo.getDepth() != null || nodeInfo.getNoOfSiblings() != null || nodeInfo.getNoOfChildren() != null)) 
				maxFieldWeight = fieldWeight>maxFieldWeight ? fieldWeight : maxFieldWeight;
			
			doc.add(fieldToAdd);
		}

		// replace absolute weight with its normalized value
		if (new Integer(1).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION))) 
			for (Map.Entry<String, Double> entry : nodeWeightsTotal.entrySet()) 
			    entry.setValue(entry.getValue() / maxFieldWeight);
		
		return doc;
	}
	
	@Override
	public void generateResultsForUserModel(int userId, UserModel userModel, String excludePdfHash) throws Exception {
		fillKeywords(userId, args, userModel, excludePdfHash);
		
	}
	
}
