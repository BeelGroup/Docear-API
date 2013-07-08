package org.neo4j.plugins;

import org.docear.database.AlgorithmArguments;
import org.docear.graphdb.DocearReferencesGenerator;
import org.docear.lucene.TFIDFKeywordGenerator;
import org.docear.lucene.TFKeywordGenerator;
import org.docear.query.ResultGenerator;
import org.docear.xml.UserModel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;


public class DocearPlugin extends ServerPlugin {
	// path: http://localhost:7474/db/data/ext/DocearPlugin/graphdb/keywords/
	@Name("keywords")
	@Description("Get all nodes from the Neo4j graph database")
	@PluginTarget(GraphDatabaseService.class)
	public String getRecommendationKeywords(@Source GraphDatabaseService graphDb, @Parameter(name = "userId", optional = false) Integer userId,
			@Parameter(name = "algorithmArguments", optional = true) String algorithmArguments,
			@Parameter(name = "excludeHash", optional = true) String excludeHash) throws Exception {
				
		AlgorithmArguments args;
		if (algorithmArguments == null) {
			args = new AlgorithmArguments("");
		}
		else {
			args = new AlgorithmArguments(algorithmArguments);
		}

		System.out.println("===> user(" + userId + "): " + algorithmArguments);		
		Integer data_element_type = (Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT_TYPE);
		
		UserModel userModel = new UserModel();		
		if (data_element_type == 0 || data_element_type == 1) {
			try {
				fillKeywords(userId, args, userModel, excludeHash);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		if (data_element_type == 0 || data_element_type == 2) {
			try {
				fillReferences(userId, args, userModel, excludeHash);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("before");
						
		if (userModel.getKeywords().isEmpty() && userModel.getReferences().isEmpty()) {
			throw new Exception("not enough data gathered for (" + args + ")");
		}
		
		summarizeVariables(userModel);
		
		String out = userModel.getXml();
		return out;
	}
	
	private void summarizeVariables(UserModel userModel) {				
		userModel.addVariable("feature_count_expanded", ""+getVariableSum(userModel, "feature_count_expanded"));
		userModel.addVariable("feature_count_expanded_unique", ""+getVariableSum(userModel, "feature_count_expanded_unique"));
		userModel.addVariable("feature_count_reduced", ""+getVariableSum(userModel, "feature_count_reduced"));
		userModel.addVariable("feature_count_reduced_unique", ""+getVariableSum(userModel, "feature_count_reduced_unique"));
		
	}
	
	private Integer getVariableSum(UserModel userModel, String varName) {
		int value = 0;
		
		try {
    		String s = userModel.getKeywords().getVariables().get(varName);
    		if (s != null) {
    			value += Integer.parseInt(s);
    		}
    		s = userModel.getReferences().getVariables().get(varName);
    		if (s != null) {
    			value += Integer.parseInt(s);
    		}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return value;
	}

	private void fillKeywords(Integer userId, AlgorithmArguments args, UserModel userModel, String excludeHash) throws Exception {
		ResultGenerator generator = new TFKeywordGenerator(args);
		if (((Integer) args.getArgument(AlgorithmArguments.WEIGHTING_SCHEME)) == 2 && ((Integer) args.getArgument(AlgorithmArguments.WEIGHT_IDF)) == 1) {
			generator = new TFIDFKeywordGenerator(args);
			System.out.println("using TFIDF");
		}

		generator.generateResultsForUserModel(userId, userModel, excludeHash);
	}	
	
	public void fillReferences(Integer userId, AlgorithmArguments args, UserModel userModel, String excludeHash) throws Exception {
		ResultGenerator generator = new DocearReferencesGenerator(args);
		generator.generateResultsForUserModel(userId, userModel, excludeHash);
	}
}
