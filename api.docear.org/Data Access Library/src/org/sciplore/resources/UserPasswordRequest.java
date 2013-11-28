package org.sciplore.resources;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;


@Entity
@Table(name = "user_password_request")
public class UserPasswordRequest extends Resource {

	@ManyToOne
	@JoinColumn(name = "user_id")
	@Cascade(CascadeType.LOCK)
	@Required
	private User user;
	
	@Column(nullable = false)
	private String token;
	
	private Date created;
	
	private Date used = null;
	
	protected UserPasswordRequest() {
		
	}
	
	public UserPasswordRequest(User user, String token) {
		this.user = user;
		this.token = token;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getToken() {
		return token;
	}
	
	@Override
	public Resource getPersistentIdentity() {
		if(getId() != null) {
			return (Resource) getSession().load(UserPasswordRequest.class, getId());			
		} 
		else {
			return getUserPasswordRequest(getSession(), getToken());
		}
	}
	
	public boolean isExpired(long timeout_millis) {
		if(created != null) {
			if(created.getTime() > (System.currentTimeMillis()-timeout_millis)) {
				return true;
			}
		}
		return false;
	}
	
	public void setUsed() {
		if(used == null) {
			used = new Date(System.currentTimeMillis());
		}
	}
	
	public boolean isUsed() {
		return (used != null);
	}

	public static UserPasswordRequest create(User user) {
		UUID.randomUUID().toString();
		return new UserPasswordRequest(user, UUID.randomUUID().toString());
	}
	
	public static UserPasswordRequest getUserPasswordRequest(Session session, String token) {
		return (UserPasswordRequest) session.createCriteria(UserPasswordRequest.class)
    			.add(Restrictions.like("token", token))
    			.setMaxResults(1)
    			.uniqueResult();
	}
	
}