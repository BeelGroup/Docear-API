package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.sciplore.eventhandler.Required;

@Entity
@Table(name = "user_models")
public class UserModel extends Resource {

	@ManyToOne
	@JoinColumn(name = "algorithm_id")
	@Cascade(CascadeType.LOCK)
	@Required
	private Algorithm algorithm;
	
	@Column(nullable = false)
	private String model;
	
	@Column(nullable = true)
	private Integer count = 0;
	
	@Column(name = "total_count")
	private Integer totalCount = 0;
	
	@Column(name = "node_count")
	private Integer nodeCount = 0;
	
	@Column(name = "ratio_keywords")
	private Double ratioKeywords;
	
	@Column(name = "ratio_references")
	private Double ratioReferences;
		
	@Column(name = "execution_time")
	private Integer executionTime = 0;
	
	//variables
	private Integer entityTotalCount;
	private Integer featureCountExpanded;
	private Integer featureCountExpandedUnique;
	private Integer featureCountReduced;
	private Integer featureCountReducedUnique;
	private Integer mindmapCountTotal;
	private Integer nodeCountBeforeExpanded;
	private Integer nodeCountExpanded;
	private Integer nodeCountTotal;
	private Integer paperCountTotal;
	private Integer linkCountTotal;
	
	private Double umSizeRelative;
	private Double umFeatureWeightMax;
	private Double umFeatureWeightMin;
	private Double umFeatureWeightAvg;
	@Column(name = "um_feature_weight_top3_avg")
	private Double umFeatureWeightTop3Avg;
	@Column(name = "um_feature_weight_top5_avg")
	private Double umFeatureWeightTop5Avg;
	@Column(name = "um_feature_weight_top10_avg")
	private Double umFeatureWeightTop10Avg;
	@Column(name = "um_feature_weight_last3_avg")
	private Double umFeatureWeightLast3Avg;
	@Column(name = "um_feature_weight_last5_avg")
	private Double umFeatureWeightLast5Avg;
	@Column(name = "um_feature_weight_last10_avg")
	private Double umFeatureWeightLast10Avg;

    
    protected UserModel() {
    	
    }
    
    public UserModel(Session session) {
    	super();
    	this.setSession(session);
    }

	@Override
	public Resource getPersistentIdentity() {
		if (this.getId() != null) {
			return this.getUserModel(this.getId());
		} 
		return null;
		
	}
	
	public UserModel getUserModel(Integer id) {
		return (UserModel)this.getSession().get(UserModel.class, id);
	}
	
	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
	

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(Integer nodeCount) {
		this.nodeCount = nodeCount;
	}

	public Double getRatioKeywords() {
		return ratioKeywords;
	}

	public void setRatioKeywords(Double ratioKeywords) {
		this.ratioKeywords = ratioKeywords;
	}

	public Double getRatioReferences() {
		return ratioReferences;
	}

	public void setRatioReferences(Double ratioReferences) {
		this.ratioReferences = ratioReferences;
	}

	public Integer getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Integer executionTime) {
		this.executionTime = executionTime;
	}

	public Integer getEntityTotalCount() {
		return entityTotalCount;
	}

	public void setEntityTotalCount(Integer entityTotalCount) {
		this.entityTotalCount = entityTotalCount;
	}

	public Integer getFeatureCountExpanded() {
		return featureCountExpanded;
	}

	public void setFeatureCountExpanded(Integer featureCountExpanded) {
		this.featureCountExpanded = featureCountExpanded;
	}

	public Integer getFeatureCountExpandedUnique() {
		return featureCountExpandedUnique;
	}

	public void setFeatureCountExpandedUnique(Integer featureCountExpandedUnique) {
		this.featureCountExpandedUnique = featureCountExpandedUnique;
	}

	public Integer getFeatureCountReduced() {
		return featureCountReduced;
	}

	public void setFeatureCountReduced(Integer featureCountReduced) {
		this.featureCountReduced = featureCountReduced;
	}

	public Integer getFeatureCountReducedUnique() {
		return featureCountReducedUnique;
	}

	public void setFeatureCountReducedUnique(Integer featureCountReducedUnique) {
		this.featureCountReducedUnique = featureCountReducedUnique;
	}

	public Integer getMindmapCountTotal() {
		return mindmapCountTotal;
	}

	public void setMindmapCountTotal(Integer mindmapCountTotal) {
		this.mindmapCountTotal = mindmapCountTotal;
	}

	public Integer getNodeCountBeforeExpanded() {
		return nodeCountBeforeExpanded;
	}

	public void setNodeCountBeforeExpanded(Integer nodeCountBeforeExpanded) {
		this.nodeCountBeforeExpanded = nodeCountBeforeExpanded;
	}

	public Integer getNodeCountExpanded() {
		return nodeCountExpanded;
	}

	public void setNodeCountExpanded(Integer nodeCountExpanded) {
		this.nodeCountExpanded = nodeCountExpanded;
	}

	public Integer getNodeCountTotal() {
		return nodeCountTotal;
	}

	public void setNodeCountTotal(Integer nodeCountTotal) {
		this.nodeCountTotal = nodeCountTotal;
	}

	public Integer getPaperCountTotal() {
		return paperCountTotal;
	}

	public void setPaperCountTotal(Integer paperCountTotal) {
		this.paperCountTotal = paperCountTotal;
	}
	
	public Integer getLinkCountTotal() {
		return linkCountTotal;
	}

	public void setLinkCountTotal(Integer linkCountTotal) {
		this.linkCountTotal = linkCountTotal;
	}

	public Double getUmSizeRelative() {
		return umSizeRelative;
	}

	public void setUmSizeRelative(Double umSizeRelative) {
		this.umSizeRelative = umSizeRelative;
	}

	public Double getUmFeatureWeightMax() {
		return umFeatureWeightMax;
	}

	public void setUmFeatureWeightMax(Double umFeatureWeightMax) {
		this.umFeatureWeightMax = umFeatureWeightMax;
	}

	public Double getUmFeatureWeightMin() {
		return umFeatureWeightMin;
	}

	public void setUmFeatureWeightMin(Double umFeatureWeightMin) {
		this.umFeatureWeightMin = umFeatureWeightMin;
	}

	public Double getUmFeatureWeightAvg() {
		return umFeatureWeightAvg;
	}

	public void setUmFeatureWeightAvg(Double umFeatureWeightAvg) {
		this.umFeatureWeightAvg = umFeatureWeightAvg;
	}

	public Double getUmFeatureWeightTop3Avg() {
		return umFeatureWeightTop3Avg;
	}

	public void setUmFeatureWeightTop3Avg(Double umFeatureWeightTop3Avg) {
		this.umFeatureWeightTop3Avg = umFeatureWeightTop3Avg;
	}

	public Double getUmFeatureWeightTop5Avg() {
		return umFeatureWeightTop5Avg;
	}

	public void setUmFeatureWeightTop5Avg(Double umFeatureWeightTop5Avg) {
		this.umFeatureWeightTop5Avg = umFeatureWeightTop5Avg;
	}

	public Double getUmFeatureWeightTop10Avg() {
		return umFeatureWeightTop10Avg;
	}

	public void setUmFeatureWeightTop10Avg(Double umFeatureWeightTop10Avg) {
		this.umFeatureWeightTop10Avg = umFeatureWeightTop10Avg;
	}

	public Double getUmFeatureWeightLast3Avg() {
		return umFeatureWeightLast3Avg;
	}

	public void setUmFeatureWeightLast3Avg(Double umFeatureWeightLast3Avg) {
		this.umFeatureWeightLast3Avg = umFeatureWeightLast3Avg;
	}

	public Double getUmFeatureWeightLast5Avg() {
		return umFeatureWeightLast5Avg;
	}

	public void setUmFeatureWeightLast5Avg(Double umFeatureWeightLast5Avg) {
		this.umFeatureWeightLast5Avg = umFeatureWeightLast5Avg;
	}

	public Double getUmFeatureWeightLast10Avg() {
		return umFeatureWeightLast10Avg;
	}

	public void setUmFeatureWeightLast10Avg(Double umFeatureWeightLast10Avg) {
		this.umFeatureWeightLast10Avg = umFeatureWeightLast10Avg;
	}
		
}
