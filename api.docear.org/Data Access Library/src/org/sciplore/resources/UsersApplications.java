package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.sciplore.eventhandler.Required;

@Entity
public class UsersApplications extends Resource {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@Required
	private User user;

	String ip;
		
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id")
	@Required
	private Application application;
	
	Date time;
	
	@Override
	public Resource getPersistentIdentity() {
		return null;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

}
