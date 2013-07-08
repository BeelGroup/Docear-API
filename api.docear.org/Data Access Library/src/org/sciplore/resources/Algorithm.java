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

	public Algorithm getAlgorithm(Integer useStemming, Integer useStopWordRemoval, Integer useSiblingNodes, Integer childNodes
			, Integer timeFrame, Integer useRootPath, Integer elementAmount, Integer elementSelectionMethod, Integer dataElementType, String dataElementTypeWeighting, Integer dataElement
			, Integer dataSourceLimitation, Integer dataSource, Integer resultAmount, Integer approach, Integer weightingScheme, Integer weightTF, Integer weightIDF) {
		
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
					, getDataSourceLimitation(), getDataSource(), getResultAmount(), getApproach(), getWeightingScheme(), getWeightTF(), getWeightIDF());
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
    			+"source="+getDataSource();
    }

}
