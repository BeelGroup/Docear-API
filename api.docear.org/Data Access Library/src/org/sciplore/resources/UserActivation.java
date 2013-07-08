package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class UserActivation extends BaseResource {
	public static UserActivation sync(UserActivation a) {
		// TODO
		return a;
	}
	
	@Column(nullable = false)
	private String code;
	@OneToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	private User user;
	@Id
	@Column(name = "user_id")
	private Integer userId;
	
	public UserActivation(){}
	
	public UserActivation(Session s){
		this.setSession(s);
	}
	
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return userId;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
