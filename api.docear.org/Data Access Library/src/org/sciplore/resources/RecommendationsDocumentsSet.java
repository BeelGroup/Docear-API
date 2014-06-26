package org.sciplore.resources;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.sciplore.eventhandler.Required;
import org.sciplore.queries.RecommendationsDocumentsSetQueries;

@Entity
@Table(name = "recommendations_documents_set")

public class RecommendationsDocumentsSet extends Resource {
	public final static int TRIGGER_TYPE_MINDMAP_UPLOAD = 1;
	public final static int TRIGGER_TYPE_RECOMMENDATION_REQUEST = 2;
	public final static int TRIGGER_TYPE_AUTO_RECOMMENDATION = 3;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")    
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @Required
    private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")    
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @Required
    private Application application;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_model_id") 
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @Required
    private UserModel userModel;

    @Column()
    private Date created;
    
    @Column()
    private Integer triggerType;
    
    @Column(name = "computation_time")    
    private Long computationTime;
    
    @Column()
    private Date delivered;
    
    @Column()
    private Date received;
    
    @Column()
    private Boolean auto;
    
    @Column()
    private Long deliveryTime;
    
    @Column()
    private Boolean old;
    
    @Column()
    private Boolean offlineEvaluator;
    
    private Integer recAmountCurrent;
    private Integer recAmountPotential;
    private Integer recAmountShould;
    private Integer recOriginalRankMax;
    private Integer recOriginalRankMin;
    private Double recOriginalRankAvg;
    
    @Column(name="rec_selected_from_top_x")
    private Integer recSelectedFromTopX;
    
    private Integer recClickedCount;
    private Double recClickedCtr;    
    
    private Integer userDaysStarted;
    private Integer userDaysSinceRegistered;
    private Integer userSetsDelivered;
    private Integer offlineEvaluatorPaperPosition;
    
    private Integer userRating;
    
    @OneToMany(mappedBy = "recommentationsDocumentsSet", fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)    
	private Set<RecommendationsDocuments> recommendationsDocuments = new TreeSet<RecommendationsDocuments>();
    
    public RecommendationsDocumentsSet() {
    	
    }
    
    public RecommendationsDocumentsSet(Session session) {
    	setSession(session);
	}

	@Override
	public Resource getPersistentIdentity() {
    	RecommendationsDocumentsSet recDocSet = new RecommendationsDocumentsSet(getSession());
    	if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());			
		}
		else {
			return RecommendationsDocumentsSetQueries.getPersistentIdentity(getSession(), this);
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public UserModel getUserModel() {
		return userModel;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(Integer triggerType) {
		this.triggerType = triggerType;
	}

	public Long getComputationTime() {
		return computationTime;
	}

	public void setComputationTime(Long computationTime) {
		this.computationTime = computationTime;
	}

	public Date getDelivered() {
		return delivered;
	}

	public void setDelivered(Date delivered) {
		this.delivered = delivered;
	}

	public boolean getAuto() {
		return auto;
	}

	public void setAuto(Boolean auto) {
		this.auto = auto;
	}

	public Long getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Long deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	
	public Date getReceived() {
		return received;
	}

	public void setReceived(Date received) {
		this.received = received;
	}

	public Boolean getOld() {
		return old;
	}

	public void setOld(Boolean old) {
		this.old = old;
	}
	
	public Boolean getOfflineEvaluator() {
		return offlineEvaluator;
	}

	public void setOfflineEvaluator(Boolean offlineEvaluator) {
		this.offlineEvaluator = offlineEvaluator;
	}

	public Set<RecommendationsDocuments> getRecommendationsDocuments() {
		return recommendationsDocuments;
	}

	public void setRecommendationsDocuments(Set<RecommendationsDocuments> recommendationsDocuments) {
		this.recommendationsDocuments = recommendationsDocuments;
	}

	public Integer getRecAmountCurrent() {
		return recAmountCurrent;
	}

	public void setRecAmountCurrent(Integer recAmountCurrent) {
		this.recAmountCurrent = recAmountCurrent;
	}

	public Integer getRecAmountPotential() {
		return recAmountPotential;
	}

	public void setRecAmountPotential(Integer recAmountPotential) {
		this.recAmountPotential = recAmountPotential;
	}

	public Integer getRecAmountShould() {
		return recAmountShould;
	}

	public void setRecAmountShould(Integer recAmountShould) {
		this.recAmountShould = recAmountShould;
	}
	
	public Integer getRecOriginalRankMax() {
		return recOriginalRankMax;
	}

	public void setRecOriginalRankMax(Integer recOriginalRankMax) {
		this.recOriginalRankMax = recOriginalRankMax;
	}

	public Integer getRecOriginalRankMin() {
		return recOriginalRankMin;
	}

	public void setRecOriginalRankMin(Integer recOriginalRankMin) {
		this.recOriginalRankMin = recOriginalRankMin;
	}

	public Integer getRecSelectedFromTopX() {
		return recSelectedFromTopX;
	}

	public void setRecSelectedFromTopX(Integer recSelectedFromTopX) {
		this.recSelectedFromTopX = recSelectedFromTopX;
	}

	public Double getRecOriginalRankAvg() {
		return recOriginalRankAvg;
	}

	public void setRecOriginalRankAvg(Double recOriginalRankAvg) {
		this.recOriginalRankAvg = recOriginalRankAvg;
	}

	public Integer getRecClickedCount() {
		return recClickedCount;
	}

	public void setRecClickedCount(Integer recClickedCount) {
		this.recClickedCount = recClickedCount;
	}

	public Double getRecClickedCtr() {
		return recClickedCtr;
	}

	public void setRecClickedCtr(Double recClickedCtr) {
		this.recClickedCtr = recClickedCtr;
	}

	public Integer getUserDaysStarted() {
		return userDaysStarted;
	}

	public void setUserDaysStarted(Integer userDaysStarted) {
		this.userDaysStarted = userDaysStarted;
	}

	public Integer getUserDaysSinceRegistered() {
		return userDaysSinceRegistered;
	}

	public void setUserDaysSinceRegistered(Integer userDaysSinceRegistered) {
		this.userDaysSinceRegistered = userDaysSinceRegistered;
	}

	public Integer getUserSetsDelivered() {
		return userSetsDelivered;
	}

	public void setUserSetsDelivered(Integer userSetsDelivered) {
		this.userSetsDelivered = userSetsDelivered;
	}

	public Integer getOfflineEvaluatorPaperPosition() {
		return offlineEvaluatorPaperPosition;
	}

	public void setOfflineEvaluatorPaperPosition(Integer offlineEvaluatorPaperPosition) {
		this.offlineEvaluatorPaperPosition = offlineEvaluatorPaperPosition;
	}

	public Integer getUserRating() {
		return userRating;
	}

	public void setUserRating(Integer userRating) {
		this.userRating = userRating;
	}	
	
}