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

/**
 * Resource class for Alerts.
 * 
 * @author Mario Lipinski &lt;<a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>&gt;
 * @see Resource
 */
@Entity
@Table(name="alerts")
public class Alert extends Resource {
	
	public Resource getPersistentIdentity() {
		return getAlert(this);
	}
	
	/**
	 * Returns an Alert object from the database for an Alert object.
	 * 
	 * @param a the Alert object
	 * @return the Alert object from the database, null if not in database
	 */
	public Alert getAlert(Alert a) {
		if(a.getId() != null) {
			return getAlert(a.getId());
		} else {
			return getAlert(a.getUser(), a.getDocument());
		}
	}
	
	/**
	 * Gets the Alert object from the database.
	 * 
	 * @param id The identifier of the Alert object
	 * @return the Alert object, null if not in database
	 */
	public Alert getAlert(Integer id) {
    	return (Alert) this.getSession().get(Alert.class, id);
	}
	
	/**
	 * Gets the Alert object from the database for a {@link User} and a {@link Document}.
	 * 
	 * @param u the {@link User}
	 * @param d the {@link Document}
	 * @return the Alert object, null if not in database
	 * @see User
	 * @see Document
	 */
	public Alert getAlert(User u, Document d) {
		if(u == null || u.getId() == null || d == null || d.getId() == null) {
			return null;
		} else {
			return (Alert)this.getSession().createCriteria(Alert.class)
			.add(Restrictions.eq("user", u))
			.add(Restrictions.eq("document", d))
			.setMaxResults(1)
			.uniqueResult();
		}
	}
	
	/**
	 * Synchronizes an Alert object with a record from the database. If the 
	 * object does not exist, it is added to the database.
	 * In any case related objects are synchronized as well.
	 * 
	 * @param alert the Alert
	 * @return the synchronized Alert which is stored in the database
	 
	public Alert sync(Alert alert) {
		Alert a = getAlert(alert);
		if(a == null) {
			a = alert;
			if(a.getDocument() != null) {
				a.setDocument(Document.sync(a.getDocument()));
			}
			if(a.getUser() != null) {
				a.setUser(User.sync(a.getUser()));
			}
		} else {
			if(Tools.empty(a.getDocument()) && !Tools.empty(alert.getDocument())) {
				a.setDocument(Document.sync(alert.getDocument()));
			}
			if(Tools.empty(a.getFrequency()) && !Tools.empty(alert.getFrequency())) {
				a.setFrequency(alert.getFrequency());
			}
			if(Tools.empty(a.getFulltextAvailable()) && !Tools.empty(alert.getFulltextAvailable())) {
				a.setFulltextAvailable(alert.getFulltextAvailable());
			}
			if(Tools.empty(a.getId()) && !Tools.empty(alert.getId())) {
				a.setId(alert.getId());
			}
			if(Tools.empty(a.getMetadataChanges()) && !Tools.empty(alert.getMetadataChanges())) {
				a.setMetadataChanges(alert.getMetadataChanges());
			}
			if(Tools.empty(a.getNewUsageData()) && !Tools.empty(alert.getNewUsageData())) {
				a.setNewUsageData(alert.getNewUsageData());
			}
			if(Tools.empty(a.getRelatedDocuments()) && !Tools.empty(alert.getRelatedDocuments())) {
				a.setRelatedDocuments(alert.getRelatedDocuments());
			}
			if(Tools.empty(a.getUser()) && !Tools.empty(alert.getUser())) {
				a.setUser(User.sync(alert.getUser()));
			}
			if(Tools.empty(a.getValid()) && !Tools.empty(alert.getValid())) {
				a.setValid(alert.getValid());
			}
		}
		return a;
	}*/
	@ManyToOne
	@JoinColumn(name = "document_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Document document;
	@Column(nullable = false)
	private Short frequency;
	@Column(nullable = false)
	private Boolean fulltextAvailable;
	
	@Column(nullable = false)
	private Boolean metadataChanges;

	@Column(nullable = false)
	private Boolean newUsageData;

	@Column(nullable = false)
	private Boolean relatedDocuments;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private User user;
	
	@Column(nullable = false)
	private Short valid=1;
	
	public Alert(){}
	
	public Alert(Session s){
		this.setSession(s);
	}
	
	/**
	 * Returns the associated {@link Document}.
	 * 
	 * @return the {@link Document}
	 * @see Document
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * Returns the frequency how often to alert the user. TODO: what metrics are used here? 
	 * 
	 * @return the frequency
	 */
	public Short getFrequency() {
		return frequency;
	}
	
	/**
	 * Returns whether to alert on the fulltext becoming available.
	 * 
	 * @return whether to alert on the fulltext becoming available
	 */
	public Boolean getFulltextAvailable() {
		return fulltextAvailable;
	}	
	
	
	/**
	 * Returns wheter to alert on metadata changes.
	 * 
	 * @return wheter to alert on metadata changes
	 */
	public Boolean getMetadataChanges() {
		return metadataChanges;
	}
	
	/**
	 * Returns whether to alert on new usage data becoming available.
	 * 
	 * @return whether to alert on new usage data becoming available
	 */
	public Boolean getNewUsageData() {
		return newUsageData;
	}
	
	/**
	 * Returns whether to alert on new related documents becoming available.
	 * 
	 * @return whether to alert on new related documents becoming available
	 */
	public Boolean getRelatedDocuments() {
		return relatedDocuments;
	}
	
	/**
	 * Returns the {@link User}.
	 * 
	 * @return the {@link User}
	 * @see User
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * Returns information about the validity of the record.
	 * 
	 * @return the validity
	 */
	public Short getValid() {
		return valid;
	}
	
	/**
	 * Sets the {@link Document}.
	 * 
	 * @param document the {@link Document}
	 * @see Document
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/**
	 * Sets the frequency how often to alert the user.
	 * 
	 * @param frequency the frequency
	 */
	
	public void setFrequency(Short frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * Sets whether to alert on the fulltext becoming available.
	 * 
	 * @param fulltextAvailable whether to alert on the fulltext becoming available
	 */
	public void setFulltextAvailable(Boolean fulltextAvailable) {
		this.fulltextAvailable = fulltextAvailable;
	}
	
	
	
	/**
	 * Sets whether to alert on metadata changes.
	 * 
	 * @param metadataChanges whether to alert on metadata chanes
	 */
	public void setMetadataChanges(Boolean metadataChanges) {
		this.metadataChanges = metadataChanges;
	}
	
	/**
	 * Sets whether to alert on new usage data becoming available.
	 * 
	 * @param newUsageData whether to alert on new usage data becoming available
	 */
	public void setNewUsageData(Boolean newUsageData) {
		this.newUsageData = newUsageData;
	}
	
	/**
	 * Sets whether to alert on new related documents becoming available.
	 * 
	 * @param relatedDocuments whether to alert on new related documents becoming available
	 */
	public void setRelatedDocuments(Boolean relatedDocuments) {
		this.relatedDocuments = relatedDocuments;
	}
	
	/**
	 * Sets the {@link User}.
	 * 
	 * @param user the {@link User}
	 * @see User
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Sets information about the validity of the record.
	 * 
	 * @param valid the information about the validity of the record
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}

}
