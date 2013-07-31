package org.docear.lucene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.docear.xml.Keyword;
import org.docear.xml.Keywords;
import org.docear.xml.UserModel;

public class TFKeywordGenerator implements ResultGenerator {		
	private GraphDbWorker worker;
	private final AlgorithmArguments args;
	
	protected Map<String, Double> nodeWeightsPerNodeDepth = new HashMap<String, Double>();
	protected Map<String, Double> nodeWeightsPerNoSiblings = new HashMap<String, Double>();
	protected Map<String, Double> nodeWeightsPerNoChildren = new HashMap<String, Double>();
	protected Map<String, Double> nodeWeightsPerWordCount = new HashMap<String, Double>();
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
				} 
				catch (IOException ex) {
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
		List<NodeInfo> nodesInfo = worker.getUserNodesInfo(userId, args, userModel, excludePdfHash);		
		
		if(nodesInfo == null || nodesInfo.size() == 0) {
			return null;
		}
		
		Document doc = new Document();
		
		Double maxFieldWeight = 0.0;
		
		if (!new Integer(0).equals((Integer)args.getArgument(AlgorithmArguments.NODE_DEPTH))) {
			nodeWeightsPerNodeDepth = NodeWeightCalculator.calculateWeights(nodesInfo, 
					NodeWeightCalculator.ParameterType.NODE_DEPTH,
					NodeWeightCalculator.ParameterMetric.get((Integer)args.getArgument(AlgorithmArguments.NODE_DEPTH_METRIC)));
			// reverse the values if set
			if (new Integer(2).equals((Integer)args.getArgument(AlgorithmArguments.NODE_DEPTH))) 
				nodeWeightsPerNodeDepth = NodeWeightCalculator.reverseWeights(nodeWeightsPerNodeDepth);
			if (new Integer(2).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION))) 
				nodeWeightsPerNodeDepth = NodeWeightCalculator.divideWeightsWith(nodeWeightsPerNodeDepth, NodeWeightCalculator.getMaxValue(nodeWeightsPerNodeDepth));
		}
		
		if (!new Integer(0).equals((Integer)args.getArgument(AlgorithmArguments.NO_SIBLINGS))) {
			nodeWeightsPerNoSiblings = NodeWeightCalculator.calculateWeights(nodesInfo, 
				NodeWeightCalculator.ParameterType.NO_SIBLINGS,
				NodeWeightCalculator.ParameterMetric.get((Integer)args.getArgument(AlgorithmArguments.NO_SIBLINGS_METRIC)));
			// reverse the values if set
			if (new Integer(2).equals((Integer)args.getArgument(AlgorithmArguments.NO_SIBLINGS))) 
				nodeWeightsPerNoSiblings = NodeWeightCalculator.reverseWeights(nodeWeightsPerNoSiblings);
			if (new Integer(2).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION))) 
				nodeWeightsPerNoSiblings = NodeWeightCalculator.divideWeightsWith(nodeWeightsPerNoSiblings, NodeWeightCalculator.getMaxValue(nodeWeightsPerNoSiblings));
		}
		
		if (!new Integer(0).equals((Integer)args.getArgument(AlgorithmArguments.NO_CHILDREN))) {
			nodeWeightsPerNoChildren = NodeWeightCalculator.calculateWeights(nodesInfo, 
					NodeWeightCalculator.ParameterType.NO_CHILDREN,
					NodeWeightCalculator.ParameterMetric.get((Integer)args.getArgument(AlgorithmArguments.NO_CHILDREN_METRIC)));
			// reverse the values if set
			if (new Integer(2).equals((Integer)args.getArgument(AlgorithmArguments.NO_CHILDREN))) 
				nodeWeightsPerNoChildren = NodeWeightCalculator.reverseWeights(nodeWeightsPerNoChildren);
			if (new Integer(2).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION))) 
				nodeWeightsPerNoChildren = NodeWeightCalculator.divideWeightsWith(nodeWeightsPerNoChildren, NodeWeightCalculator.getMaxValue(nodeWeightsPerNoChildren));
		}
		
		if (!new Integer(0).equals((Integer)args.getArgument(AlgorithmArguments.WORD_COUNT))) {
			nodeWeightsPerWordCount = NodeWeightCalculator.calculateWeights(nodesInfo, 
					NodeWeightCalculator.ParameterType.WORD_COUNT,
					NodeWeightCalculator.ParameterMetric.get((Integer)args.getArgument(AlgorithmArguments.WORD_COUNT_METRIC)));
			// reverse the values if set
			if (new Integer(2).equals((Integer)args.getArgument(AlgorithmArguments.WORD_COUNT))) 
				nodeWeightsPerWordCount = NodeWeightCalculator.reverseWeights(nodeWeightsPerWordCount);
			if (new Integer(2).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION))) 
				nodeWeightsPerWordCount = NodeWeightCalculator.divideWeightsWith(nodeWeightsPerWordCount, NodeWeightCalculator.getMaxValue(nodeWeightsPerWordCount));
		}
		
		boolean noParamUsed = nodeWeightsPerNodeDepth.size()==0 && nodeWeightsPerNoSiblings.size()==0 && nodeWeightsPerNoChildren.size()==0 && nodeWeightsPerWordCount.size()==0;
		
		// calculate the node weights appropriately
		for (Iterator<NodeInfo> iter = nodesInfo.iterator(); iter.hasNext();) {
			NodeInfo nodeInfo = iter.next();
			
			Field fieldToAdd = new Field(nodeInfo.getId(), nodeInfo.getText(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);
			Double fieldWeight = 0d;
	
			if (args.getArgument(AlgorithmArguments.NODE_WEIGHT_COMBO_SCHEME) != null) { // combination of factors{ 			
				switch((Integer)args.getArgument(AlgorithmArguments.NODE_WEIGHT_COMBO_SCHEME)) {
				case 1: // 1=multiply all 
					fieldWeight = 1d; // in case we perform multiplications set the initial weight to one
				case 0: // 0=add all
					if (nodeInfo.getDepth() != null) 
						fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 
								nodeWeightsPerNodeDepth.get(nodeInfo.getId()),
								NodeWeightCalculator.ParameterOperator.get((Integer)args.getArgument(AlgorithmArguments.NODE_WEIGHT_COMBO_SCHEME)));
					if (nodeInfo.getNoOfSiblings() != null) 
						fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 
								nodeWeightsPerNoSiblings.get(nodeInfo.getId()),
								NodeWeightCalculator.ParameterOperator.get((Integer)args.getArgument(AlgorithmArguments.NODE_WEIGHT_COMBO_SCHEME)));
					if (nodeInfo.getNoOfChildren() != null) 
						fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 
								nodeWeightsPerNoChildren.get(nodeInfo.getId()),
								NodeWeightCalculator.ParameterOperator.get((Integer)args.getArgument(AlgorithmArguments.NODE_WEIGHT_COMBO_SCHEME)));
					if (nodeInfo.getWordCount() != null) 
						fieldWeight = NodeWeightCalculator.applyParameter(fieldWeight, 
								nodeWeightsPerWordCount.get(nodeInfo.getId()),
								NodeWeightCalculator.ParameterOperator.get((Integer)args.getArgument(AlgorithmArguments.NODE_WEIGHT_COMBO_SCHEME)));
					break;
				case 2: // 2=keep max
					if (nodeInfo.getDepth() != null) fieldWeight = nodeWeightsPerNodeDepth.get(nodeInfo.getId());
					if (nodeInfo.getNoOfSiblings() != null) fieldWeight = Math.max(fieldWeight, nodeWeightsPerNoSiblings.get(nodeInfo.getId()));
					if (nodeInfo.getNoOfChildren() != null) fieldWeight = Math.max(fieldWeight, nodeWeightsPerNoChildren.get(nodeInfo.getId()));
					if (nodeInfo.getWordCount() != null) fieldWeight = Math.max(fieldWeight, nodeWeightsPerWordCount.get(nodeInfo.getId()));
					break;
				case 3: // 3=keep avg
					int parametersNumber = 0;
					if (nodeInfo.getDepth() != null) {
						fieldWeight = nodeWeightsPerNodeDepth.get(nodeInfo.getId());
						parametersNumber++;
					}
					if (nodeInfo.getNoOfSiblings() != null) {
						fieldWeight += nodeWeightsPerNoSiblings.get(nodeInfo.getId());
						parametersNumber++;
					}
					if (nodeInfo.getNoOfChildren() != null) {
						fieldWeight += nodeWeightsPerNoChildren.get(nodeInfo.getId());
						parametersNumber++;
					}
					if (nodeInfo.getWordCount() != null) {
						fieldWeight += nodeWeightsPerWordCount.get(nodeInfo.getId());
						parametersNumber++;
					}
					fieldWeight /= parametersNumber;
				}
				nodeWeightsTotal.put(nodeInfo.getId(), fieldWeight);			
			}
			else if (noParamUsed) //if no parameter is used, the node weight is set to one for all nodes
				nodeWeightsTotal.put(nodeInfo.getId(), 1d);
			else { // if only one parameter is set replace the total weights with the parameter values for all nodes
				if (nodeWeightsPerNodeDepth.size() > 0) 
					fieldWeight = nodeWeightsPerNodeDepth.get(nodeInfo.getId());
				else if (nodeWeightsPerNoSiblings.size() > 0) 
					fieldWeight = nodeWeightsPerNoSiblings.get(nodeInfo.getId());
				else if (nodeWeightsPerNoChildren.size() > 0) 
					fieldWeight = nodeWeightsPerNoChildren.get(nodeInfo.getId());
				else 
					fieldWeight = nodeWeightsPerWordCount.get(nodeInfo.getId());
				nodeWeightsTotal.put(nodeInfo.getId(), fieldWeight);	
			}
				
			// compare with max node weight (if node weight normalization is set)
			// check that at least one parameter is considered (although normalization should not be set otherwise)
			if (new Integer(1).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION)) && !noParamUsed) 
				maxFieldWeight = fieldWeight > maxFieldWeight ? fieldWeight : maxFieldWeight;
			
			doc.add(fieldToAdd);
		}

		// replace absolute weight with its normalized value
		if (args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION) != null &&
				new Integer(1).equals(args.getArgument(AlgorithmArguments.NODE_WEIGHT_NORMALIZATION)) && !noParamUsed && maxFieldWeight != 1) 
				for (Map.Entry<String, Double> entry : nodeWeightsTotal.entrySet()) 
					entry.setValue(entry.getValue() / maxFieldWeight);
		
		return doc;
	}
	
	@Override
	public void generateResultsForUserModel(int userId, UserModel userModel, String excludePdfHash) throws Exception {
		fillKeywords(userId, args, userModel, excludePdfHash);
		
	}
	
}
