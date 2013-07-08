package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;

/**
 * This class represents the rec_stats table
 */
@Entity
public class RecStats extends Resource{
	
	
	
	@ManyToOne
	@JoinColumn(name = "application")
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Application application;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private User user;
	
	@Column(nullable = false)
	private Short event;
	
	private String eventdata;
	
	private Short source;
	
	private Short algorithm;
	
	private Integer originalRank;
	
	private Integer splmmRank;
	
	private String title;
	
	private String keyword;
	
	@Column(nullable = false)
	private Date delivered;
	
	private Date clicked;
	
	public RecStats(){}
	
	public RecStats(Session s){
		this.setSession(s);
	}
	
	public RecStats(Session s, 
					Application application, 
					User user, 
					Short event,					
					String eventdata, 
					Short source,
					Short algorithm,
					Integer originalRank,
					Integer splmmRank,
					String title,
					String keyword,
					Date delivered) {
		super();
		this.setSession(s);
		this.application = application;
		this.user = user;
		this.event = event;
		this.eventdata = eventdata;
		this.source = source;
		this.algorithm = algorithm;
		this.originalRank = originalRank;
		this.splmmRank = splmmRank;
		this.title = title;
		this.keyword = keyword;
		this.delivered = delivered;		
		this.save();
	}
	
	public RecStats getRecStatById(int id){
		return(RecStats)this.getSession().createCriteria(RecStats.class)
		.add(Restrictions.like("id", id))		
		.setMaxResults(1)
		.uniqueResult();
	}
	

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Short getEvent() {
		return event;
	}

	public void setEvent(Short event) {
		this.event = event;
	}

	public String getEventdata() {
		return eventdata;
	}

	public void setEventdata(String eventdata) {
		this.eventdata = eventdata;
	}

	public Short getSource() {
		return source;
	}

	public void setSource(Short source) {
		this.source = source;
	}

	public Short getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(Short algorithm) {
		this.algorithm = algorithm;
	}

	public Integer getOriginalRank() {
		return originalRank;
	}

	public void setOriginalRank(Integer originalRank) {
		this.originalRank = originalRank;
	}

	public Integer getSplmmRank() {
		return splmmRank;
	}

	public void setSplmmRank(Integer splmmRank) {
		this.splmmRank = splmmRank;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		if(keyword.length() > 128){
			keyword = keyword.substring(0, 127);
		}
		this.keyword = keyword;
	}

	public Date getDelivered() {
		return delivered;
	}

	public void setDelivered(Date delivered) {
		this.delivered = delivered;
	}

	/**
	 * @param eventdataClicked the eventdataClicked to set
	 */
	public void setClicked(Date clicked) {
		this.clicked = clicked;
	}

	/**
	 * @return the eventdataClicked
	 */
	public Date getClicked() {
		return clicked;
	}
	
	public Resource getPersistentIdentity() {
		//TODO check for identity
		return null;
	}

}
