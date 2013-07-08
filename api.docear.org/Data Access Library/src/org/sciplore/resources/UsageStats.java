package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.sciplore.eventhandler.Required;

/**
 * This class represents the usage_stats table
 */
@Entity
public class UsageStats extends Resource{

	
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
	
	@Column(nullable = false)
	private Date time;
	
	public UsageStats(){}
	
	public UsageStats(Session s){
		this.setSession(s);
	}
	
	public UsageStats(Session s, Application application, User user, Short event,
			String eventdata, Date time) {
		super();
		this.setSession(s);
		this.application = application;
		this.user = user;
		this.event = event;
		this.eventdata = eventdata;
		this.time = time;
		this.save();
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

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	public Resource getPersistentIdentity() {
		//TODO check for identity
		return null;
	}

}
