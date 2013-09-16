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
 * Resource class for contacts.
 *
 * @author Mario Lipinski &lt;<a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>&gt;
 * @see Resource
 */
@Entity
@Table(name = "contacts")
public class Contact extends Resource {
	/**
	 * Bitmask for private contact type.
	 */
	public static final short CONTACT_TYPE_PRIVATE = 1;
	/**
	 * Bitmask for business contact type.
	 */
	public static final short CONTACT_TYPE_BUSINESS = 2;
	/**
	 * Bitmask for email contact type.
	 */
	public static final short CONTACT_TYPE_EMAIL = 4;
	
	/**
	 * Bitmask for homepage contact type.
	 */
	public static final short CONTACT_TYPE_HOMEPAGE = 8;
	/**
	 * Bitmask for private email contact type.
	 * 
	 * @see #CONTACT_TYPE_PRIVATE
	 * @see #CONTACT_TYPE_EMAIL
	 */
	public static final short CONTACT_TYPE_PRIVATE_EMAIL = CONTACT_TYPE_PRIVATE | CONTACT_TYPE_EMAIL;
	/**
	 * Bitmask for private homepage contact type.
	 * 
	 * @see #CONTACT_TYPE_PRIVATE
	 * @see #CONTACT_TYPE_HOMEPAGE
	 */
	public static final short CONTACT_TYPE_PRIVATE_HOMEPAGE = CONTACT_TYPE_PRIVATE | CONTACT_TYPE_HOMEPAGE;
	/**
	 * Bitmask for business email contact type.
	 * 
	 * @see #CONTACT_TYPE_BUSINESS
	 * @see #CONTACT_TYPE_EMAIL
	 */
	public static final short CONTACT_TYPE_BUSINESS_EMAIL = CONTACT_TYPE_BUSINESS | CONTACT_TYPE_EMAIL;
	/**
	 * Bitmask for business homepage type.
	 * 
	 * @see #CONTACT_TYPE_BUSINESS
	 * @see #CONTACT_TYPE_HOMEPAGE
	 */
	public static final short CONTACT_TYPE_BUSINESS_HOMEPAGE = CONTACT_TYPE_BUSINESS | CONTACT_TYPE_HOMEPAGE;
	
	public Resource getPersistentIdentity() {
		return getContact(this);
	}
	
	/**
	 * Returns a Contact object from the database from a Contact object.
	 *
	 * @param c the contact object
	 * @return the contact object from database or null if not found
	 */
	public  Contact getContact(Contact c) {
		if(c.getId() != null) {
			return getContact(getSession(), c.getId());
		} else {
			return getContact(getSession(), c.getUri());
		}
	}
	
	/**
	 * Returns a Contact object from the database for an identifier.
	 *
	 * @param id the identifier
	 * @return the Contact object from the database or null if not found
	 */
	public static Contact getContact(Session session, Integer id) {
		return (Contact) session.get(Contact.class, id);
	}
	
	/**
	 * Returns a Contact object fromt he database for an uri.
	 *	 
	 * @param uri the URI
	 * @return the Contact object from the database or null if not found
	 * @see Person
	 */
	public static Contact getContact(Session session, String uri){
		return (Contact) session.createCriteria(Contact.class)		
		.add(Restrictions.eq("uri", uri))
		.setMaxResults(1)
		.uniqueResult();
	}
	
	/**
	 * Returns a Contact object fromt he database for a {@link Person} and an uri.
	 *
	 * @param p the {@link Person}
	 * @param uri the URI
	 * @return the Contact object from the database or null if not found
	 * @see Person
	 */
//	public  Contact getContact(Person p, String uri) {
//		return (Contact) this.getSession().createCriteria(Contact.class)
//			.add(Restrictions.eq("person", p))
//			.add(Restrictions.eq("uri", uri))
//			.setMaxResults(1)
//			.uniqueResult();
//	}
	
	/**
	 * Synchronizes a Contact object with a record from the database. If the 
	 * object does not exist, it is added to the database.
	 * In any case related objects are synchronized as well.
	 * 
	 * @param co the Contact
	 * @return the synchronized Contact which is stored in the database
	 
	public  Contact sync(Contact co) {
		Contact c = getContact(co);
		if (c == null) {
			c = co;
			if (!Tools.empty(c.getPerson())) {
				c.setPerson(Person.sync(c.getPerson()));
			}
		} else {
			if (Tools.empty(c.getDescription()) && !Tools.empty(co.getDescription())) {
				c.setDescription(co.getDescription());
			}
			if (Tools.empty(c.getId()) && !Tools.empty(co.getId())) {
				c.setId(co.getId());
			}
			if (Tools.empty(c.getPerson()) && !Tools.empty(co.getPerson())) {
				c.setPerson(Person.sync(co.getPerson()));
			}
			if (Tools.empty(c.getType()) && !Tools.empty(co.getType())) {
				c.setType(co.getType());
			}
			if (Tools.empty(c.getUri()) && !Tools.empty(co.getUri())) {
				c.setUri(co.getUri());
			}
			if (Tools.empty(c.getValid()) && !Tools.empty(co.getValid())) {
				c.setValid(co.getValid());
			}
		}
		return c;
	}*/
	
	private String description;	
	@ManyToOne
	@JoinColumn(name = "person_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Person person;
	@Column(nullable = false)
	private Short type;
	@Column(nullable = false)
	private String uri;
	@Column(nullable = false)
	private Short valid = 1;
	
	/**
	 * Contruct an empty Contact object.
	 */
	public Contact() {
	}
	
	public Contact(Session s) {
		this.setSession(s);
	}
	
	/**
	 * Construct a Contact object from {@link Person}, URI and type.
	 * 
	 * @param person the {@link Person}
	 * @param uri the URI
	 * @param type the type
	 * @see Person
	 */
	public Contact(Session s, Person person, String uri, Short type) {
		this.setSession(s);
		this.person = person;
		this.uri = uri;
		this.type = type;		
	}

	/**
	 * Construct a Contact object from URI and Type.
	 * 
	 * @param uri the URI
	 * @param type the type
	 */
	public Contact(Session s, String uri, Short type) {
		this.setSession(s);
		this.uri = uri;
		this.type = type;
	}

	/**
	 * Returns the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	

	/**
	 * Returns the {@link Person}.
	 * 
	 * @return the {@link Person}
	 * @see Person
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * Returns the type.
	 * 
	 * @return the type
	 */
	public Short getType() {
		return type;
	}

	/**
	 * Returns the URI.
	 * 
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Returns information about the validity of the record.
	 * 
	 * @return information about the validity of the record
	 */
	public Short getValid() {
		return valid;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	

	/**
	 * Sets the {@link Person}.
	 * 
	 * @param person the {@link Person}
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the type
	 */
	public void setType(Short type) {
		this.type = type;
	}

	/**
	 * Sets the URI.
	 * 
	 * @param uri the uri
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Sets information about the validity of the record.
	 * @param valid information about the validity of the record.
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}
}
