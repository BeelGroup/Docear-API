package org.neo4j.plugins;

import org.docear.Logging.DocearLogger;
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
	@Name("test")
	@Description("test specific algorithm arguments")
	@PluginTarget(GraphDatabaseService.class)
	public String getRecommendationKeywords(@Source GraphDatabaseService graphDb) throws Exception {
		String args = "stemming=0;approach=1;weightingScheme=1;weightTF=1;weightIDF=null;resultAmount=100;stopwords=0;siblings=0;children=0;"
				+ "timeframe=0;rootpath=null;amount=0;noDaysSinceMax=null;noDaysSinceChosen=null;method=1;type=1;typeWeighting=1,909;"
				+ "element=2;limitation=0;source=1;nodeInfoSource=4;nodeVisibility=0;nodeDepth=1;nodeDepthMetric=0;noSiblings=1;"
				+ "noSiblingsMetric=0;noChildren=0;noChildrenLevel=null;noChildrenMetric=null;wordCount=1;wordCountMetric=0;"
				+ "nodeWeightNormalization=2;nodeWeightComboScheme=0";

		return getRecommendationKeywords(graphDb, 2, args, null);
	}

	// path: /db/data/ext/DocearPlugin/graphdb/keywords/
	@Name("keywords")
	@Description("Get all nodes from the Neo4j graph database")
	@PluginTarget(GraphDatabaseService.class)
	public String getRecommendationKeywords(@Source GraphDatabaseService graphDb, @Parameter(name = "userId", optional = false) Integer userId,
			@Parameter(name = "algorithmArguments", optional = true) String algorithmArguments,
			@Parameter(name = "excludeHash", optional = true) String excludeHash) throws Exception {

		AlgorithmArguments args;
		try {
			if (algorithmArguments == null) {
				args = new AlgorithmArguments("");
			}
			else {
				args = new AlgorithmArguments(algorithmArguments);
			}
		}
		catch (Exception e) {
			DocearLogger.error(e);
			throw new Exception("could not parse algorithm's arguments: " + algorithmArguments);
		}

		DocearLogger.info("===> user(" + userId + "): " + algorithmArguments);
		Integer data_element_type = (Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT_TYPE);
		if (data_element_type == null) {
			data_element_type = 0;
		}

		UserModel userModel = new UserModel();
		if (data_element_type == 0 || data_element_type == 1) {
			try {
				fillKeywords(userId, args, userModel, excludeHash);
			}
			catch (Exception e) {
				DocearLogger.error(e);
			}
		}
		if (data_element_type == 0 || data_element_type == 2) {
			try {
				fillReferences(userId, args, userModel, excludeHash);
			}
			catch (Exception e) {
				DocearLogger.error(e);
			}
		}

		DocearLogger.info("before");

		if (userModel.getKeywords().isEmpty() && userModel.getReferences().isEmpty()) {
			DocearLogger.info("###### not enough data gathered for (" + args + ")");
			throw new Exception("not enough data gathered for (" + args + ")");
		}

		try {
			summarizeVariables(userModel);

			Integer no_days_since_max_nodes = null;
			Integer no_days_since_max_maps = null;

			String s = userModel.getKeywords().getVariables().get("no_days_since_max_nodes");
			if (s == null) {
				s = userModel.getReferences().getVariables().get("no_days_since_max_nodes");
			}
			if (s != null) {
				no_days_since_max_nodes = Integer.parseInt(s);
			}

			s = userModel.getVariables().getVariables().get("no_days_since_max_maps");
			if (s == null) {
				s = userModel.getReferences().getVariables().get("no_days_since_max_maps");
			}
			if (s != null) {
				no_days_since_max_maps = Integer.parseInt(s);
			}

			DocearLogger.info("####### no_days_since_max_nodes: " + no_days_since_max_nodes);
			DocearLogger.info("####### no_days_since_max_maps: " + no_days_since_max_maps);

			if ((no_days_since_max_nodes != null && no_days_since_max_nodes < 0) || (no_days_since_max_maps != null && no_days_since_max_maps < 0)) {
				DocearLogger.info("negativ variables");
			}
		}
		catch (Exception e) {
			DocearLogger.error(e);
		}

		String out = userModel.getXml();
		return out;
	}

	private void summarizeVariables(UserModel userModel) {
		userModel.addVariable("feature_count_expanded", "" + getVariableSum(userModel, "feature_count_expanded"));
		userModel.addVariable("feature_count_expanded_unique", "" + getVariableSum(userModel, "feature_count_expanded_unique"));
		userModel.addVariable("feature_count_reduced", "" + getVariableSum(userModel, "feature_count_reduced"));
		userModel.addVariable("feature_count_reduced_unique", "" + getVariableSum(userModel, "feature_count_reduced_unique"));

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
			DocearLogger.error(e);
			return null;
		}

		return value;
	}

	private void fillKeywords(Integer userId, AlgorithmArguments args, UserModel userModel, String excludeHash) throws Exception {
		ResultGenerator generator = new TFKeywordGenerator(args);
		if (((Integer) args.getArgument(AlgorithmArguments.WEIGHTING_SCHEME)) == 2 && ((Integer) args.getArgument(AlgorithmArguments.WEIGHT_IDF)) == 1) {
			generator = new TFIDFKeywordGenerator(args);
			DocearLogger.info("using TFIDF");
		}

		generator.generateResultsForUserModel(userId, userModel, excludeHash);
	}

	public void fillReferences(Integer userId, AlgorithmArguments args, UserModel userModel, String excludeHash) throws Exception {
		ResultGenerator generator = new DocearReferencesGenerator(args);
		generator.generateResultsForUserModel(userId, userModel, excludeHash);
	}
}
