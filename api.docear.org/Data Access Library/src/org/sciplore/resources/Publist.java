package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class Publist extends Resource {
	public static Publist sync(Publist p) {
		// TODO
		return p;
	}
	
	@Column(nullable = false)
	private Date lastmodified;
	private Date created;
	private String name;
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	private User user;
	
	public Publist(){}
	
	public Publist(Session s){
		this.setSession(s);
	}
	
	
	/**
	 * @return the lastmodified
	 */
	public Date getLastmodified() {
		return lastmodified;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param lastmodified the lastmodified to set
	 */
	public void setLastmodified(Date lastmodified) {
		this.lastmodified = lastmodified;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @param created the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}
	
	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}
	
	public Resource getPersistentIdentity() {
		//TODO check for identity
		return null;
	}
}
