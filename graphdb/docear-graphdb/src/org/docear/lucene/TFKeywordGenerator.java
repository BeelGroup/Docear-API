package org.docear.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
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
	private GraphDbWorker worker;
	private final AlgorithmArguments args;
	
	protected Document doc; // the document that contains node info for the user
	
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
			// access the field, where this term comes from
			String fieldName = term.field();
			Fieldable fieldFromDoc = doc.getFieldable(fieldName);
			try {
				Float.parseFloat(termText);
			} catch (Exception e) {
				try {
					TermDocs docs = reader.termDocs(term);
					double frequency = 0;
					while (docs.next()) {
						frequency += (docs.freq() * fieldFromDoc.getBoost());
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

			doc = getDocument(userId, args, userModel, excludePdfHash);
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
			Field fieldToAdd = new Field("field".concat(String.valueOf(fieldNumber++)), nodeInfo.getText(), Field.Store.YES, Field.Index.ANALYZED);
			// root nodes acquire a depth of 1
			// if NODE_DEPTH is set to 1 term weight will be divided by node depth
			if (new Integer(1).equals(args.getArgument(AlgorithmArguments.NODE_DEPTH)))
				fieldToAdd.setBoost(new Float(1.0) / (nodeInfo.getDepth() + 1));
			// if NODE_DEPTH is set to 2 term weight will be multiplied with node depth
			else if (new Integer(2).equals(args.getArgument(AlgorithmArguments.NODE_DEPTH)))
				fieldToAdd.setBoost(nodeInfo.getDepth() + 1);
			// if NODE_DEPTH is set to 0 node depth is not taken into account
			else fieldToAdd.setBoost(new Integer(1));
			doc.add(fieldToAdd);
		}
		return doc;
	}
	
	@Override
	public void generateResultsForUserModel(int userId, UserModel userModel, String excludePdfHash) throws Exception {
		fillKeywords(userId, args, userModel, excludePdfHash);
		
	}
	
}
