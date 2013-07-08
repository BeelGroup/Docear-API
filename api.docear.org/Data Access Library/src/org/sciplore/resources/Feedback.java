package org.sciplore.resources;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;
import org.sciplore.tools.SciploreResponseCode;

@Entity
public class Feedback extends Resource {
	
	public Resource getPersistentIdentity() {
		return getFeedback(this);
	}
	
	public  Feedback getFeedback(Feedback f) {
		if(f.getId() != null) {
			f.load(f.getId());
			return f;
			//return getFeedback(f.getId());
		} else {
			return getFeedback(f.getDocument(), f.getType(), f.getText(), f.getCreated());
		}
	}
	
	private Feedback getFeedback(Document document, Short type, String text, Date created) {
		 
		Feedback result = (Feedback) getSession().createCriteria(Feedback.class)
					.add(Restrictions.eq("document", document))					
					.add(Restrictions.eq("type", type))					
					.add(Restrictions.eq("text", text))
					.add(Restrictions.eq("created", created))
					.uniqueResult();
		
		return result;			
	}

	public  Feedback getFeedback(Integer id) {
		return (Feedback)this.getSession().get(Feedback.class, id);
	}
	
	
	@OneToMany(mappedBy = "parent")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Feedback> children = new HashSet<Feedback>();
	private Date created;
	@ManyToOne
	@JoinColumn(name = "document_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Document document;

	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	private Feedback parent;
	private Short rating;
	private String text;
	private String title;
	private Short type;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private User user;

	private Short valid;
	
	public Feedback() {
		
	}
	
	public Feedback(Session s) {
		this.setSession(s);
	}
	
	public SciploreResponseCode create(String username,
													   String password,			   
													   Document document,
													   String title,
													   String text,
													   Short type,
													   Short rating){
		return create(username,
						     password,
						     null,
						     document,
						     title,
						     text,
						     type,
						     rating);
	}
	
	public SciploreResponseCode create(String username,
													   String password,
													   Feedback parent,
													   Document document,
													   String title,
													   String text,
													   Short type,
													   Short rating){
		User user = new User(this.getSession()).getUserByEmailOrUsername(username);
		if(user == null)
			return new SciploreResponseCode(SciploreResponseCode.USERNAME_INVALID, "Username invalid.");
		if(!user.checkCredentials(password))
			return new SciploreResponseCode(SciploreResponseCode.UNAUTHORIZED, "Password wrong.");
		if((title == null || title.isEmpty()) && (text == null || text.isEmpty()) && rating == null)
			return new SciploreResponseCode(SciploreResponseCode.BAD_REQUEST, "Post at least a title, a text or a rating");
		if(type != null && type != 1 && type != 2 && type != 3)
			return new SciploreResponseCode(SciploreResponseCode.BAD_REQUEST, "Invalid rating Type. Must be 1-3.");
				
		this.setCreated(new GregorianCalendar().getTime());
		this.setUser(user);
		this.setDocument(document);
		this.setTitle(title);
		this.setText(text);
		this.setType(type);
		this.setRating(rating);
		this.setValid((short)1);
		this.setParent(parent);
		this.save();
		
		return new SciploreResponseCode(SciploreResponseCode.OK, "Feedback created.");
	}

	public Feedback(int id) {
		super();
		this.setId(id);
	}
	
	public void addChild(Feedback c) {
		children.add(c);
	}

	/**
	 * @return the children
	 */
	public Set<Feedback> getChildren() {
		return children;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * @return the parent
	 */
	public Feedback getParent() {
		return parent;
	}

	/**
	 * @return the rating
	 */
	public Short getRating() {
		return rating;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
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
	 * @param children the children to set
	 */
	public void setChildren(Set<Feedback> children) {
		this.children = children;
	}

	/**
	 * @param created
	 *            the created to set
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * @param document
	 *            the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}


	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Feedback parent) {
		this.parent = parent;
	}

	/**
	 * @param rating the rating to set
	 */
	public void setRating(Short rating) {
		this.rating = rating;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Short type) {
		this.type = type;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @param valid
	 *            the valid to set
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}

}
