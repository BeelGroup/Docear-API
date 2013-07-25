package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.Session;
import org.sciplore.tools.SimpleRestrictions;

@Entity
@Table(name = "algorithms")
public class Algorithm extends Resource {

	public final static Integer DATA_SOURCE_MAP = 1;
    public final static Integer DATA_SOURCE_PDF = 2;
    public final static Integer DATA_SOURCE_BIBTEX = 3;
    
    public final static Integer DATA_SOURCE_LIMITATION_ALL = 0;
    public final static Integer DATA_SOURCE_LIMITATION_LIBRARY = 1;    
	
    public final static Integer DATA_ELEMENT_MAPS = 1;
    public final static Integer DATA_ELEMENT_NODES = 2;
    
    public final static Integer DATA_ELEMENT_TYPE_TEXT = 1;
    public final static Integer DATA_ELEMENT_TYPE_CITATIONS = 2;
    
    public final static Integer ELEMENT_SELECTION_METHOD_EDITED = 1;
    public final static Integer ELEMENT_SELECTION_METHOD_CREATED = 2;
    public final static Integer ELEMENT_SELECTION_METHOD_MOVED = 3;
    public final static Integer ELEMENT_SELECTION_METHOD_OPENED = 4;
    public final static Integer ELEMENT_SELECTION_METHOD_SELECTED = 5;
    public final static Integer ELEMENT_SELECTION_METHOD_UNFOLDED = 6;
    
    public final static Integer ROOT_PATH_NO = 0;
    public final static Integer ROOT_PATH_YES = 1;
    
    public final static Integer TIME_FRAME_ALL = 0;
    public final static Integer TIME_FRAME_1HOUR = 1;
    public final static Integer TIME_FRAME_2HOURS = 2;
    public final static Integer TIME_FRAME_5HOURS = 5;
    public final static Integer TIME_FRAME_12HOURS = 12;
    public final static Integer TIME_FRAME_1DAY = 24;
    public final static Integer TIME_FRAME_2DAYS = 48;
    public final static Integer TIME_FRAME_5DAYS = 120;
    public final static Integer TIME_FRAME_14DAYS = 336;
    
    public final static Integer CHILD_NODES_NO = 0;
    public final static Integer CHILD_NODES_YES = 1;
    
    public final static Integer SIBLING_NODES_NO = 0;
    public final static Integer SIBLING_NODES_YES = 1;
    
    public final static Integer STOP_WORD_REMOVAL_NO = 0;
    public final static Integer STOP_WORD_REMOVAL_YES = 1;
    
    public final static Integer STEMMING_NO = 0;
    public final static Integer STEMMING_YES = 1;
    
    public final static Integer WEIGHTING_SCHEME_TF = 1;
    public final static Integer WEIGHTING_SCHEME_TFIDF = 2;
    
    public final static Integer WEIGHT_TF_MINDMAPS = 1;
    
    public final static Integer WEIGHT_IDF_DISABLED = 0;
    public final static Integer WEIGHT_IDF_MINDMAPS = 1;
    public final static Integer WEIGHT_IDF_DOCUMENTS = 2;
    
    public final static Integer APPROACH_CONTENT_BASED = 1;
    public final static Integer APPROACH_STEREOTYPE = 2;
    public final static Integer APPROACH_COLLABORATIVE_FILTERING = 3;    
    
    public final static Integer NODE_DEPTH_DISABLED = 0;
    public final static Integer NODE_DEPTH = 1;
    public final static Integer NODE_DEPTH_REVERSE = 2;
    
    public final static Integer NODE_DEPTH_METRIC_ABS = 0;
    public final static Integer NODE_DEPTH_METRIC_LOG = 1;
    public final static Integer NODE_DEPTH_METRIC_LOG10 = 2;
    public final static Integer NODE_DEPTH_METRIC_SQRT = 3;
    public final static Integer NODE_DEPTH_METRIC_REL = 4;
    
    public final static Integer NO_SIBLINGS_DISABLED = 0;
    public final static Integer NO_SIBLINGS = 1;
    public final static Integer NO_SIBLINGS_REVERSE = 2;
    
    public final static Integer NO_SIBLINGS_METRIC_ABS = 0;
    public final static Integer NO_SIBLINGS_METRIC_LOG = 1;
    public final static Integer NO_SIBLINGS_METRIC_LOG10 = 2;
    public final static Integer NO_SIBLINGS_METRIC_SQRT = 3;
    public final static Integer NO_SIBLINGS_METRIC_REL = 4;
    
    public final static Integer NO_CHILDREN_DISABLED = 0;
    public final static Integer NO_CHILDREN = 1;
    public final static Integer NO_CHILDREN_REVERSE = 2;
    
    public final static Integer NO_CHILDREN_METRIC_ABS = 0;
    public final static Integer NO_CHILDREN_METRIC_LOG = 1;
    public final static Integer NO_CHILDREN_METRIC_LOG10 = 2;
    public final static Integer NO_CHILDREN_METRIC_SQRT = 3;
    public final static Integer NO_CHILDREN_METRIC_REL = 4;
    
    public final static Integer WORD_COUNT_DISABLED = 0;
    public final static Integer WORD_COUNT = 1;
    public final static Integer WORD_COUNT_REVERSE = 2;
    
    public final static Integer WORD_COUNT_METRIC_ABS = 0;
    public final static Integer WORD_COUNT_METRIC_LOG = 1;
    public final static Integer WORD_COUNT_METRIC_LOG10 = 2;
    public final static Integer WORD_COUNT_METRIC_SQRT = 3;
    public final static Integer WORD_COUNT_METRIC_REL = 4;
    
    public final static Integer NODE_WEIGHT_NORMALIZATION_DISABLED = 0;
    public final static Integer NODE_WEIGHT_NORMALIZATION_ON_TOTAL = 1;
    public final static Integer NODE_WEIGHT_NORMALIZATION_PER_PARAMETER = 2;
    
    public final static Integer NODE_WEIGHT_COMBO_SCHEME_ADD_ALL = 0;
    public final static Integer NODE_WEIGHT_COMBO_SCHEME_MULTIPLY_ALL = 1;
    public final static Integer NODE_WEIGHT_COMBO_SCHEME_MAX = 2;
    public final static Integer NODE_WEIGHT_COMBO_SCHEME_AVG = 3;
    public final static Integer NODE_WEIGHT_COMBO_SCHEME_WEIGHTED_AVG = 4; // not used currently
    
    protected Algorithm() {
    	
    }
    
    public Algorithm(Session session) {
    	super();
    	this.setSession(session);
    }
    
    @Column()
    private Integer data_source = DATA_SOURCE_MAP;
    
    @Column()
    private Integer data_source_limitation = DATA_SOURCE_LIMITATION_ALL;
    
	@Column()
    private Integer data_element = DATA_ELEMENT_MAPS;
    
    @Column()
    private Integer data_element_type = DATA_ELEMENT_TYPE_TEXT;

	@Column()
    private String data_element_type_weighting = "1";
    
    @Column()
    private Integer element_selection_method = 0;
        
    @Column()
    private Integer element_amount = 0;
    
	@Column(nullable = true)
    private Integer root_path = ROOT_PATH_NO;
    
    @Column()
    private Integer time_frame = TIME_FRAME_ALL;
    
    @Column()
    private Integer child_nodes = CHILD_NODES_NO;
     
    @Column()
    private Integer sibling_nodes = SIBLING_NODES_NO; 
  
    @Column()
    private Integer stop_word_removal = STOP_WORD_REMOVAL_YES; 
   
    @Column()
    private Integer stemming = STEMMING_NO;
    
    @Column()
    private Integer result_amount = 100;
    
    @Column(name = "approach")
    private Integer approach = APPROACH_CONTENT_BASED;
    
    @Column(name = "weighting_scheme") 
    private Integer weightingScheme = WEIGHTING_SCHEME_TF;
    
    @Column(name = "weight_tf") 
    private Integer weightTF = WEIGHT_TF_MINDMAPS;
    
    @Column(name = "weight_idf")
    private Integer weightIDF = WEIGHT_IDF_DISABLED;
    
    @Column(name = "feature_weight_submission")
    private Boolean featureWeightSubmission;
    
    @Column()
    private Integer node_depth = NODE_DEPTH_DISABLED;
    
    @Column()
    private Integer no_siblings = NO_SIBLINGS_DISABLED;
    
    @Column()
    private Integer no_children = NO_CHILDREN_DISABLED;
    
    @Column()
    private Integer node_depth_metric = NODE_DEPTH_METRIC_ABS;
    
    @Column()
    private Integer no_siblings_metric = NO_SIBLINGS_METRIC_ABS;
    
    @Column()
    private Integer no_children_metric = NO_CHILDREN_METRIC_ABS;
    
    @Column()
    private Integer word_count = WORD_COUNT_DISABLED;
    
    @Column()
    private Integer word_count_metric = WORD_COUNT_METRIC_ABS;   
    
    @Column()
    private Integer node_weight_normalization = NODE_WEIGHT_NORMALIZATION_DISABLED;
    
    @Column()
    private Integer node_weight_combo_scheme = NODE_WEIGHT_COMBO_SCHEME_MULTIPLY_ALL;
    
    public Integer getDataSource() {
		return data_source;
	}



	/**
	 * @param data_source
	 * 		can be one of the following: {@link Algorithm.DATA_SOURCE_MAP}, {@link Algorithm.DATA_SOURCE_PDF} or {@link Algorithm.DATA_SOURCE_BIBTEX}
	 */
	public void setDataSource(Integer data_source) {
		this.data_source = data_source;
	}



	public Integer getDataSourceLimitation() {
		return data_source_limitation;
	}



	public void setDataSourceLimitation(Integer data_source_limitation) {
		this.data_source_limitation = data_source_limitation;
	}



	public Integer getDataElement() {
		return data_element;
	}



	public void setDataElement(Integer data_element) {
		this.data_element = data_element;
	}



	public Integer getDataElementType() {
		return data_element_type;
	}



	public void setDataElementType(Integer data_element_type) {
		this.data_element_type = data_element_type;
	}
	
    
    public String getDataElementTypeWeighting() {
		return data_element_type_weighting;
	}

	public void setDataElementTypeWeighting(String data_element_type_weighting) {
		this.data_element_type_weighting = data_element_type_weighting;
	}
	

	public Integer getElementSelectionMethod() {
		return element_selection_method;
	}



	public void setElementSelectionMethod(Integer element_selection_method) {
		this.element_selection_method = element_selection_method;
	}



	public Integer getElementAmount() {
		return element_amount;
	}



	public void setElementAmount(Integer element_amount) {
		this.element_amount = element_amount;
	}



	public Integer getRootPath() {
		return root_path;
	}



	public void setRootPath(Integer root_path) {
		this.root_path = root_path;
	}



	public Integer getTimeFrame() {
		return time_frame;
	}



	public void setTimeFrame(Integer time_frame) {
		this.time_frame = time_frame;
	}



	public Integer getChildNodes() {
		return child_nodes;
	}



	public void setChildNodes(Integer child_nodes) {
		this.child_nodes = child_nodes;
	}



	public Integer getSiblingNodes() {
		return sibling_nodes;
	}



	public void setSiblingNodes(Integer sibling_nodes) {
		this.sibling_nodes = sibling_nodes;
	}



	public Integer getStopWordRemoval() {
		return stop_word_removal;
	}



	public void setStopWordRemoval(Integer stop_word_removal) {
		this.stop_word_removal = stop_word_removal;
	}



	public Integer getStemming() {
		return stemming;
	}



	public void setStemming(Integer stemming) {
		this.stemming = stemming;
	}
	
	public Integer getResultAmount() {
		return result_amount;
	}

	public void setResultAmount(Integer result_amount) {
		this.result_amount = result_amount;
	}



	public Integer getApproach() {
		return approach;
	}

	public void setApproach(Integer approach) {
		this.approach = approach;
	}

	public Integer getWeightingScheme() {
		return weightingScheme;
	}

	public void setWeightingScheme(Integer weightingScheme) {
		this.weightingScheme = weightingScheme;
	}

	public Integer getWeightTF() {
		return weightTF;
	}

	public void setWeightTF(Integer weightTF) {
		this.weightTF = weightTF;
	}

	public Integer getWeightIDF() {
		return weightIDF;
	}

	public void setWeightIDF(Integer weightIDF) {
		this.weightIDF = weightIDF;
	}

	public Algorithm getAlgorithm(Integer id) {
		return (Algorithm)this.getSession().get(Algorithm.class, id);
	}
	
	public Boolean getFeatureWeightSubmission() {
		return featureWeightSubmission;
	}

	public void setFeatureWeightSubmission(Boolean featureWeightSubmission) {
		this.featureWeightSubmission = featureWeightSubmission;
	}
	
	public Integer getNodeDepth() {
		return node_depth;
	}

	public void setNodeDepth(Integer nodeDepth) {
		this.node_depth = nodeDepth;
	}
	
	public Integer getNodeDepthMetric() {
		return node_depth_metric;
	}

	public void setNodeDepthMetric(Integer nodeDepthMetric) {
		this.node_depth_metric = nodeDepthMetric;
	}
	
	public Integer getNoSiblings() {
		return no_siblings;
	}

	public void setNoSiblings(Integer noSiblings) {
		this.no_siblings = noSiblings;
	}

	public Integer getNoSiblingsMetric() {
		return no_siblings_metric;
	}

	public void setNoSiblingsMetric(Integer noSiblingsMetric) {
		this.no_siblings_metric = noSiblingsMetric;
	}
	
	public Integer getNoChildren() {
		return no_children;
	}

	public void setNoChildren(Integer noChildren) {
		this.no_children = noChildren;
	}
	
	public Integer getNoChildrenMetric() {
		return no_children_metric;
	}

	public void setNoChildrenMetric(Integer noChildrenMetric) {
		this.no_children_metric = noChildrenMetric;
	}
	
	public Integer getWordCount() {
		return word_count;
	}

	public void setWordCount(Integer wordCount) {
		this.word_count = wordCount;
	}
	
	public Integer getWordCountMetric() {
		return word_count_metric;
	}

	public void setWordCountMetric(Integer wordCountMetric) {
		this.word_count_metric = wordCountMetric;
	}
	
	public Integer getNodeWeightNormalization() {
		return node_weight_normalization;
	}

	public void setNodeWeightNormalization(Integer nodeWeightNormalization) {
		this.node_weight_normalization = nodeWeightNormalization;
	}
	
	public Integer getNodeWeightComboScheme() {
		return node_weight_combo_scheme;
	}

	public void setNodeWeightComboScheme(Integer nodeWeightComboScheme) {
		this.node_weight_combo_scheme = nodeWeightComboScheme;
	}
	
	
	public Algorithm getAlgorithm(Integer useStemming, Integer useStopWordRemoval, Integer useSiblingNodes, Integer childNodes
			, Integer timeFrame, Integer useRootPath, Integer elementAmount, Integer elementSelectionMethod, Integer dataElementType, String dataElementTypeWeighting, Integer dataElement
			, Integer dataSourceLimitation, Integer dataSource, Integer resultAmount, Integer approach, Integer weightingScheme, Integer weightTF, Integer weightIDF
			, Integer nodeDepth, Integer nodeDepthMetric, Integer noSiblings, Integer noSiblingsMetric, Integer noChildren, Integer noChildrenMetric
			, Integer wordCount, Integer wordCountMetric, Integer nodeWeightNormalization, Integer nodeWeightComboScheme) {
		
		return (Algorithm)this.getSession().createCriteria(Algorithm.class)
		.add(SimpleRestrictions.eq("stemming", useStemming))
		.add(SimpleRestrictions.eq("stop_word_removal", useStopWordRemoval))
		.add(SimpleRestrictions.eq("sibling_nodes", useSiblingNodes))
		.add(SimpleRestrictions.eq("child_nodes", childNodes))
		.add(SimpleRestrictions.eq("time_frame", timeFrame))
		.add(SimpleRestrictions.eq("root_path", useRootPath))
		.add(SimpleRestrictions.eq("element_amount", elementAmount))
		.add(SimpleRestrictions.eq("element_selection_method", elementSelectionMethod))
		.add(SimpleRestrictions.eq("data_element_type", dataElementType))
		.add(SimpleRestrictions.eq("data_element_type_weighting", dataElementTypeWeighting))
		.add(SimpleRestrictions.eq("data_element", dataElement))
		.add(SimpleRestrictions.eq("data_source_limitation", dataSourceLimitation))
		.add(SimpleRestrictions.eq("data_source", dataSource))
		.add(SimpleRestrictions.eq("result_amount", resultAmount))
		.add(SimpleRestrictions.eq("approach", approach))
		.add(SimpleRestrictions.eq("weightingScheme", weightingScheme))
		.add(SimpleRestrictions.eq("weightTF", weightTF))
		.add(SimpleRestrictions.eq("weightIDF", weightIDF))
		.add(SimpleRestrictions.eq("node_depth", nodeDepth))
		.add(SimpleRestrictions.eq("node_depth_metric", nodeDepthMetric))
		.add(SimpleRestrictions.eq("no_siblings", noSiblings))
	    .add(SimpleRestrictions.eq("no_siblings_metric", noSiblingsMetric))
		.add(SimpleRestrictions.eq("no_children", noChildren))
	    .add(SimpleRestrictions.eq("no_children_metric", noChildrenMetric))
	    .add(SimpleRestrictions.eq("word_count", wordCount))
	    .add(SimpleRestrictions.eq("word_count_metric", wordCountMetric))
	    .add(SimpleRestrictions.eq("node_weight_normalization", nodeWeightNormalization))
	    .add(SimpleRestrictions.eq("node_weight_combo_scheme", nodeWeightComboScheme))
		.setMaxResults(1)
		.uniqueResult();
	}

    
    @Override
	public Resource getPersistentIdentity() {
    	if (this.getId() != null) {
			return this.getAlgorithm(this.getId());
		} else {
			return this.getAlgorithm(getStemming(), getStopWordRemoval(), getSiblingNodes(), getChildNodes()
					, getTimeFrame(), getRootPath(), getElementAmount(), getElementSelectionMethod(), getDataElementType(), getDataElementTypeWeighting(), getDataElement()
					, getDataSourceLimitation(), getDataSource(), getResultAmount(), getApproach(), getWeightingScheme(), getWeightTF(), getWeightIDF()
					, getNodeDepth(), getNodeDepthMetric(), getNoSiblings(), getNoSiblingsMetric(), getNoChildren(), getNoChildrenMetric(), getWordCount(), getWordCountMetric()
					, getNodeWeightNormalization(), getNodeWeightComboScheme());
		}
    }
    
    public String toString() {
    	return "stemming="+getStemming()+";"
    			+"approach="+getApproach()+";"
    			+"weightingScheme="+getWeightingScheme()+";"
    			+"weightTF="+getWeightTF()+";"
    			+"weightIDF="+getWeightIDF()+";"
    			+"resultAmount="+getResultAmount()+";"
    			+"stopwords="+getStopWordRemoval()+";"
    			+"siblings="+getSiblingNodes()+";"
    			+"children="+getChildNodes()+";"
    			+"timeframe="+getTimeFrame()+";"
    			+"rootpath="+getRootPath()+";"
    			+"amount="+getElementAmount()+";"
    			+"method="+getElementSelectionMethod()+";"
    			+"type="+getDataElementType()+";"
    			+"typeWeighting="+getDataElementTypeWeighting()+";"
    			+"element="+getDataElement()+";"
    			+"limitation="+getDataSourceLimitation()+";"
    			+"source="+getDataSource()+";"
    			+"nodeDepth="+getNodeDepth()+";"
    			+"nodeDepthMetric="+getNodeDepthMetric()+";"
    			+"noSiblings="+getNoSiblings()+";"
    			+"noSiblingsMetric="+getNoSiblingsMetric()+";"    			
    			+"noChildren="+getNoChildren()+";" 
				+"noChildrenMetric="+getNoChildrenMetric()+";"
				+"wordCount="+getWordCount()+";" 
				+"wordCountMetric="+getWordCountMetric()+";"
				+"nodeWeightNormalization="+getNodeWeightNormalization()+";"
				+"nodeWeightComboScheme="+getNodeWeightComboScheme();
    }

}
