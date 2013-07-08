package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class Log extends Resource {
	
	public static Log sync(Log l) {
		// TODO
		return l;
	}
	
	public Resource getPersistentIdentity() {
		//TODO check for identity
		return null;
	}
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	private Application application;
	private String details;
	@Column(nullable = false)
	private Short event;
	
	private String ip;
	@Column(nullable = false)
	private String table;
	@Column(nullable = false)
	private Date time;
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	private User user;
	
	public Log(){}
	
	public Log(Session s){
		this.setSession(s);
	}
	
	/**
	 * @return the application
	 */
	public Application getApplication() {
		return application;
	}
	/**
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}
	/**
	 * @return the event
	 */
	public Short getEvent() {
		return event;
	}
	
	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}
	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}
	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	/**
	 * @param application the application to set
	 */
	public void setApplication(Application application) {
		this.application = application;
	}
	/**
	 * @param details the details to set
	 */
	public void setDetails(String details) {
		this.details = details;
	}
	/**
	 * @param event the event to set
	 */
	public void setEvent(Short event) {
		this.event = event;
	}
	
	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	/**
	 * @param table the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
}
