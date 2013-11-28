package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;

/**
 * Resource class for the relationship between {@link Document}s and {@link Person}s.
 *
 * @author Mario Lipinski &lt;<a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>&gt;
 * @see Resource
 */
@Entity
@Table(name = "documents_persons")
public class DocumentPerson extends Resource {
	/**
	 * Type: Author.
	 */
	public final static short DOCUMENTPERSON_TYPE_AUTHOR = 1;
	/**
	 * Type: Editor.
	 */
	public final static short DOCUMENTPERSON_TYPE_EDITOR = 2;
	
	/**
	 * Constructs an empty {@code DocumentPerson} object. 
	 */
	public DocumentPerson() {
	}
	
	public DocumentPerson(Session s) {
		this.setSession(s);		
	}
	
	/**
	 * Constructs a {@code DocumentPerson} object with a {@link Person}.
	 * @param p
	 */
	public DocumentPerson(Session s, PersonHomonym p) {
		this.setSession(s);
		personHomonym = p;
	}

	/**
	 * Constructs a {@code DocumentPerson} object with a {@link Person} and type.
	 * 
	 * @param p the {@link Person}
	 * @param type the type
	 * @see Person
	 */
	public DocumentPerson(Session s, PersonHomonym p, Short type) {
		this.setSession(s);
		personHomonym = p;
		this.type = type;
	}

	/**
	 * Constructs a {@code DocumentPerson} object with a {@link Person}, type and rank.
	 * 
	 * @param p the {@link Person}
	 * @param type the type
	 * @param rank the rank
	 * @see Person
	 */
	public DocumentPerson(Session s, PersonHomonym p, Short type, Short rank) {
		this.setSession(s);
		personHomonym = p;
		this.type = type;
		this.rank = rank;
	}
	
	@ManyToOne
	@Fetch(FetchMode.JOIN) // FIXME
	@JoinColumn(name = "document_id")
	@Required
	private Document document;
	
	@ManyToOne
	@Fetch(FetchMode.JOIN) // FIXME
	@JoinColumn(name = "person_homonym_id")
	@Required
	private PersonHomonym personHomonym;
	
	@ManyToOne
	@Fetch(FetchMode.JOIN) // FIXME
	@JoinColumn(name = "person_id")
	@Required
	private Person personMain;

	@Column(nullable = false)
	private Short rank = 0;
	@Column(nullable = false)
	private Short type = 1;
	@Column(nullable = false)
	private Short valid = 1;
	
	private Boolean docidxAllow;
	
	private Boolean docidxWrongTitle;
	
	private Boolean docidxIsCollection;
	
	
	public DocumentPerson getDocumentPerson(DocumentPerson dp) {
		if(dp.getId() != null) {
			return (DocumentPerson) getSession().load(DocumentPerson.class, dp.getId());			
		} 
		else {
			return getDocumentPerson(dp.getDocument(), dp.getPersonHomonym());
		}
	}
	
	public DocumentPerson getDocumentPerson(Integer id) {
		return (DocumentPerson)this.getSession().get(DocumentPerson.class, id);
	}
	
	public DocumentPerson getDocumentPerson(Document d, PersonHomonym p) {
		if(d != null && p != null && d.getId() != null && p.getId() != null) {
			return (DocumentPerson)this.getSession().createCriteria(DocumentPerson.class)
				.add(Restrictions.eq("document", d))
				.add(Restrictions.eq("personHomonym", p))
				.setMaxResults(1)
				.uniqueResult();
		}		
		return null;
	}
	
	/**
	 * Returns the {@link Document}.
	 * 
	 * @return the {@link Document}
	 * @see Document
	 */
	public Document getDocument() {
		return document;
	}
	
	public Resource getPersistentIdentity() {
		return getDocumentPerson(this);
	}	

	/**
	 * Returns the {@link Person}.
	 * 
	 * @return the {@link Person}.
	 * @see Person
	 */
	public PersonHomonym getPersonHomonym() {
		return personHomonym;
	}

	/**
	 * Returns the rank.
	 * 
	 * @return the rank
	 */
	public Short getRank() {
		return rank;
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
	 * Sets the {@link Person}.
	 * 
	 * @param person the {@Person}
	 * @see Person
	 */
	public void setPersonHomonym(PersonHomonym person) {
		this.personHomonym = person;
	}

	/**
	 * Sets the rank.
	 * 
	 * @param rank the rank
	 */
	public void setRank(Short rank) {
		this.rank = rank;
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
	 * Sets information about the validity of the record.
	 * 
	 * @param valid the validity
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}
	
	public boolean getDocidxAllow() {
		return docidxAllow==null ? Person.DEFAULT_ALLOW : docidxAllow;
	}

	public void setDocidxAllow(Boolean docidxAllow) {
		this.docidxAllow = docidxAllow;
	}
	
	public boolean getDocidxWrongTitle() {
		return docidxWrongTitle==null ? false : docidxWrongTitle;
	}

	public void setDocidxWrongTitle(Boolean isTitleWrong) {
		this.docidxWrongTitle = isTitleWrong;
	}
	
	public boolean getDocidxIsCollection() {
		return docidxIsCollection==null ? false : docidxIsCollection;
	}

	public void setDocidxIsCollection(Boolean isCollection) {
		this.docidxIsCollection = isCollection;
	}

	public Person getPersonMain() {
		return personMain;
	}

	public void setPersonMain(Person personMain) {
		this.personMain = personMain;
	}
}
