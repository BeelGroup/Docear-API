package org.sciplore.resources;

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
@Table(name = "keywords")
public class Keyword extends Resource {
	public final static short KEYWORD_TYPE_AUTHOR = 1;
	public final static short KEYWORD_TYPE_USER = 2;
	public final static short KEYWORD_TYPE_MINDMAP = 3;
	
	public Resource getPersistentIdentity() {
		return getKeyword(this);
	}
	
	public  Keyword getKeyword(Keyword k) {
		if(k.getId() != null) {
			return getKeyword(k.getId());
		} else {
			return getKeyword(k.getDocument(), k.getKeyword(), k.getType(), k.getUser());
		}
	}
	
	public  Keyword getKeyword(Integer id) {
		return (Keyword)this.getSession().get(Keyword.class, id);
	}
	
	public  Keyword getKeyword(Document d, String k, Short t, User u) {
		return (Keyword)this.getSession().createCriteria(Keyword.class)
			.add(Restrictions.eq("document", d))
			.add(Restrictions.like("keyword", k))
			.add(Restrictions.eq("type", t))
			.add(Restrictions.eq("user", u))
			.setMaxResults(1)
			.uniqueResult();
	}
	/*
	public  Keyword sync(Keyword key) {
		Keyword k = Keyword.getKeyword(key);
		if(k == null) {
			k = key;
			if (!Tools.empty(k.getDocument())) {
				k.setDocument(Document.sync(k.getDocument()));
			}
			if (!Tools.empty(k.getUser())) {
				k.setUser(User.sync(k.getUser()));
			}
		} else {
			if (Tools.empty(k.getDocument()) && !Tools.empty(key.getDocument())) {
				k.setDocument(Document.sync(k.getDocument()));
			}
			if (Tools.empty(k.getId()) && !Tools.empty(key.getId())) {
				k.setId(key.getId());
			}
			if (Tools.empty(k.getKeyword()) && !Tools.empty(key.getKeyword())) {
				k.setKeyword(key.getKeyword());
			}
			if (Tools.empty(k.getType()) && !Tools.empty(key.getType())) {
				k.setType(key.getType());
			}
			if (Tools.empty(k.getUser()) && !Tools.empty(key.getUser())) {
				k.setUser(User.sync(key.getUser()));
			}
			if (Tools.empty(k.getValid()) && !Tools.empty(key.getValid())) {
				k.setValid(key.getValid());
			}
		}
		return k;
	}*/
	@ManyToOne
	@JoinColumn(name = "document_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Document document;
	
	@Column(nullable = false)
	private String keyword;
	@Column(nullable = false)
	private Short type;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private User user;
	
	@Column(nullable = false)
	private Short valid = 1;

	public Keyword() {
		
	}
	
	public Keyword(Session s) {
		this.setSession(s);
	}
	
	public Keyword(Session s, Document doc, String keyword) {
		this.setSession(s);
		this.keyword = keyword;
		this.document = doc;
	}
	
	public Keyword(Session s, Document doc, String keyword, Short type) {
		this.setSession(s);
		this.keyword = keyword;
		this.type = type;
		this.document = doc;
	}
	
	public Keyword(Session s, String keyword) {
		this.setSession(s);
		this.keyword = keyword;
	}
	
	public Keyword(Session s, String keyword, Short type) {
		this.setSession(s);
		this.keyword = keyword;
		this.type = type;
	}
	

	/**
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}
	/**
	 * @return the type
	 */
	public Short getType() {
		return type;
	}
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}
	/**
	 * @return the valid
	 */
	public Short getValid() {
		return valid;
	}
	/**
	 * @param document the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(Short type) {
		this.type = type;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	/**
	 * @param valid the valid to set
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}
}
