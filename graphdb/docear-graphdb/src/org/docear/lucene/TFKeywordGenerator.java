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
import org.docear.query.ResultGenerator;
import org.docear.structs.NodeInfo;
import org.docear.xml.UserModel;

public class TFKeywordGenerator implements ResultGenerator {	
	private static final String FIELD = "field";
	
	private GraphDbWorker worker;
	private final AlgorithmArguments args;
	
	protected Map<String, Double> nodeWeights = new HashMap<String, Double>();
	
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
					double frequency = 0;
					
					while (docs.next()) { 
							// get term frequencies for each field in the document
							TermFreqVector[] tvf = reader.getTermFreqVectors(docs.doc());
							for (int i=0; i<tvf.length; i++)
							{
								// if the term exists in this field
								int termIndex = tvf[i].indexOf(termText);
								if (termIndex > -1) {
									// get the frequency of the term in the document field multiplied the node weight
									Integer freq = tvf[i].getTermFrequencies()[termIndex];
									frequency += freq * nodeWeights.get(tvf[i].getField());
								}
							
							}
					}
					docs.close();
					userModel.getKeywords().addKeyword(termText, frequency);					
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
		for (Iterator<NodeInfo> iter = nodesInfo.iterator(); iter.hasNext();) {
			NodeInfo nodeInfo = iter.next();
			
			// give a unique name to the document field
			String fieldName = FIELD.concat(String.valueOf(fieldNumber++));
			Field fieldToAdd = new Field(fieldName, nodeInfo.getText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);
			Double fieldWeight = 1.0;
			
			if (args.getArgument(AlgorithmArguments.NODE_WEIGHTING_SCHEME) != null) {
				switch ((Integer)args.getArgument(AlgorithmArguments.NODE_WEIGHTING_SCHEME)) {
				case 1: //only node depth considered
					// add 1.0 to the weight so that no cases with division by zero occur
					fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 1.0 + nodeInfo.getDepth(), 
							new Integer(1).equals(args.getArgument(AlgorithmArguments.NODE_DEPTH)) ? NodeWeightCalculator.ParameterOperator.DIVIDE : NodeWeightCalculator.ParameterOperator.MULTIPLY);
					break;
				case 2: //only no siblings considered
					fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 1.0 + nodeInfo.getNoOfSiblings(), 
							new Integer(1).equals(args.getArgument(AlgorithmArguments.NO_SIBLINGS)) ? NodeWeightCalculator.ParameterOperator.DIVIDE : NodeWeightCalculator.ParameterOperator.MULTIPLY);
					break;
				case 3: //only no children considered
					fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 1.0 + nodeInfo.getNoOfChildren(), 
							new Integer(1).equals(args.getArgument(AlgorithmArguments.NO_CHILDREN)) ? NodeWeightCalculator.ParameterOperator.DIVIDE : NodeWeightCalculator.ParameterOperator.MULTIPLY);
					break;
				case 4: //combined case
					fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 1.0 + nodeInfo.getDepth(), 
							new Integer(1).equals(args.getArgument(AlgorithmArguments.NODE_DEPTH)) ? NodeWeightCalculator.ParameterOperator.DIVIDE : NodeWeightCalculator.ParameterOperator.MULTIPLY);
					fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 1.0 + nodeInfo.getNoOfSiblings(), 
							new Integer(1).equals(args.getArgument(AlgorithmArguments.NO_SIBLINGS)) ? NodeWeightCalculator.ParameterOperator.DIVIDE : NodeWeightCalculator.ParameterOperator.MULTIPLY);
					fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 1.0 + nodeInfo.getNoOfChildren(), 
							new Integer(1).equals(args.getArgument(AlgorithmArguments.NO_CHILDREN)) ? NodeWeightCalculator.ParameterOperator.DIVIDE : NodeWeightCalculator.ParameterOperator.MULTIPLY);
				}
				
				nodeWeights.put(fieldName, fieldWeight);
			}
			
			doc.add(fieldToAdd);
		}
		return doc;
	}
	
	@Override
	public void generateResultsForUserModel(int userId, UserModel userModel, String excludePdfHash) throws Exception {
		fillKeywords(userId, args, userModel, excludePdfHash);
		
	}
	
}
