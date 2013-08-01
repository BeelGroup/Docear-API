package org.docear.database;

import java.util.HashMap;

public class AlgorithmArguments {
	public static final int MAX_ELEMENT_AMOUNT = 1000;
	public static final int MAX_RESULT_AMOUNT = 1000;
	
	public static final String STEMMING = "stemming";
	public static final String STOPWORDS = "stopwords";
	public static final String SIBLINGS = "siblings";
	public static final String CHILDREN = "children";
	public static final String TIMEFRAME = "timeframe";
	public static final String ROOTPATH = "rootpath";
//	public static final String ELEMENT_AMOUNT = "amount";
	public static final String ELEMENT_SELECTION_METHOD = "method";
	public static final String DATA_ELEMENT_TYPE = "type";
//	public static final String DATA_ELEMENT_TYPE_WEIGHTING = "typeWeighting";
	public static final String DATA_ELEMENT = "element";
	public static final String LIMITATION = "limitation";
	public static final String SOURCE = "source";
//	public static final String RESULT_AMOUNT = "resultAmount";
	public static final String WEIGHTING_SCHEME = "weightingScheme";
	public static final String WEIGHT_TF = "weightTF";
	public static final String WEIGHT_IDF = "weightIDF";
	public static final String NODE_INFO_SOURCE = "nodeInfoSource";
	public static final String NODE_VISIBILITY = "nodeVisibility";
	public static final String NODE_DEPTH = "nodeDepth";
	public static final String NODE_DEPTH_METRIC = "nodeDepthMetric";
	public static final String NO_SIBLINGS = "noSiblings";
	public static final String NO_SIBLINGS_METRIC = "noSiblingsMetric";
	public static final String NO_CHILDREN = "noChildren";
	public static final String NO_CHILDREN_LEVEL = "noChildrenLevel";
	public static final String NO_CHILDREN_METRIC = "noChildrenMetric";
	public static final String WORD_COUNT = "wordCount";
	public static final String WORD_COUNT_METRIC = "wordCountMetric";
	public static final String NODE_WEIGHT_NORMALIZATION = "nodeWeightNormalization";
	public static final String NODE_WEIGHT_COMBO_SCHEME = "nodeWeightComboScheme";

	private HashMap<String, Object> arguments = new HashMap<String, Object>(); 
	
	public AlgorithmArguments() {
		
	}
	
	public AlgorithmArguments(String arguments) {
		parse(arguments);
	}

	private void parse(String arguments) {
		this.arguments.put(STOPWORDS, 1);
		this.arguments.put(ELEMENT_SELECTION_METHOD, 0);
		this.arguments.put(WEIGHTING_SCHEME, 1);
		this.arguments.put(WEIGHT_IDF, 0);
		this.arguments.put(WEIGHT_TF, 1);
		this.arguments.put(DATA_ELEMENT_TYPE, 1);
//		this.arguments.put(DATA_ELEMENT_TYPE_WEIGHTING, "1");
		this.arguments.put(NODE_INFO_SOURCE, 0);
		this.arguments.put(NODE_VISIBILITY, 0);
		this.arguments.put(NODE_DEPTH, 0);
		this.arguments.put(NO_SIBLINGS, 0);
		this.arguments.put(NO_CHILDREN, 0);
		this.arguments.put(WORD_COUNT, 0);
		
		if(arguments == null || arguments.trim().length() == 0) {
			return;
		}
		for (String token : arguments.split(";")) {
			try {
				String[] kv = token.split("=");
				if(kv[1] != null && !"null".equals(kv[1])) {
					if (kv[1].contains(",")) {
						this.arguments.put(kv[0],  kv[1]);
					}
					else {
						this.arguments.put(kv[0], Integer.parseInt(kv[1]));
					}
				}				
			}
			catch(Exception e) {
				System.out.println("org.docear.database.AlgorithmArguments.parse(arguments): invalid argument pair:" + token);
			}
		}
	}
	
	// used to extract how many maps and nodes a user has 
	public void setAlgorithmForAllElements() {
		// data element is nodes --> so all maps are fetched
		this.arguments.put(DATA_ELEMENT, 2);
		// get all nodes with text
		this.arguments.put(DATA_ELEMENT_TYPE, 1);
		// do not order - get all nodes independent from created/modified/moved information
		this.arguments.put(ELEMENT_SELECTION_METHOD, 0);
	}
	
	public Object getArgument(String key) {
		return this.arguments.get(key);
	}
	
	public String toString() {
		return arguments.toString();
	}
}