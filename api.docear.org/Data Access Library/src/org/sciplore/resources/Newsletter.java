package org.sciplore.resources;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.sciplore.eventhandler.Required;

@Entity
public class Newsletter extends Resource {
	
	public static Newsletter sync(Newsletter n) {
		// TODO
		return n;
	}
	
	
	private Boolean newsGeneral;
	private Boolean newsSearch;
	private Boolean newsSplmm;
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private User user;
	
	public Newsletter(){}
	
	public Newsletter(Session s){
		this.setSession(s);
	}
	
	
	/**
	 * @return the newsGeneral
	 */
	public Boolean getNewsGeneral() {
		return newsGeneral;
	}
	/**
	 * @return the newsSearch
	 */
	public Boolean getNewsSearch() {
		return newsSearch;
	}
	/**
	 * @return the newsSplmm
	 */
	public Boolean getNewsSplmm() {
		return newsSplmm;
	}
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * @param newsGeneral the newsGeneral to set
	 */
	public void setNewsGeneral(Boolean newsGeneral) {
		this.newsGeneral = newsGeneral;
	}
	/**
	 * @param newsSearch the newsSearch to set
	 */
	public void setNewsSearch(Boolean newsSearch) {
		this.newsSearch = newsSearch;
	}
	/**
	 * @param newsSplmm the newsSplmm to set
	 */
	public void setNewsSplmm(Boolean newsSplmm) {
		this.newsSplmm = newsSplmm;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		return null;
	}
}
