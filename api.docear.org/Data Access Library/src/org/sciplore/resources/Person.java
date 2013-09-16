package org.sciplore.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Formula;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;


@Entity
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
public class Person extends Resource {
	
	public Resource getPersistentIdentity() {
		return getPerson(this);
	}
	
	public Person getPerson(Person p) {
		if (p.getId() != null) {
			p.load();
			return p;
		} else {
			String nameComplete = p.createNameComplete();
			System.out.println("Person Name Complete: '" + nameComplete+"'");
			//Session session = SessionProvider.sessionFactory.openSession();
			Person person = (Person)getSession().createCriteria(PersonHomonym.class)														   
										       .add(Restrictions.eq("nameComparable", nameComplete))
										       .setProjection(Projections.property("person"))
										       .setMaxResults(1).uniqueResult();
			//session.close();
			return person;
		}
		
	}
	
	public  Person getPerson(Integer id) {		
		Person p = (Person)this.getSession().get(Person.class, id);		
		return p;
	}
	
	public  Person getPerson(String name) { // FIXME
		return (Person)this.getSession().createCriteria(Person.class)
			.add(Restrictions.like("nameFirst", name))
			.setMaxResults(1)
			.uniqueResult();
	}
		
	@OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Contact> contacts = new HashSet<Contact>();
	private Date dob;
	private Date dod;
	@OneToMany(mappedBy = "personHomonym")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<DocumentPerson> documents = new HashSet<DocumentPerson>();
	@OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<PersonHomonym> homonyms = new HashSet<PersonHomonym>();
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "institution_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Institution institution;
	
	@Formula(value= "CONCAT_WS('', name_first, name_middle, name_last_prefix, name_last, name_last_suffix)")
	private String nameComplete;	
	private String nameFirst;
	private String nameLast;
	private String nameMiddle;
	private String nameLastPrefix;
	private String nameLastSuffix;
	
	@Column(nullable = false)
	private Short valid = 1;
	private Short gender;
	@OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<PersonXref> xrefs = new HashSet<PersonXref>();
	
	public Person() {
	}
	
	public Person(Session s) {
		this.setSession(s);
	}
	
	public Person(Session s, Integer id) {
		this.setSession(s);
		this.setId(id);
	}

	public Person(Session s, Integer id, String nameLast, String nameMiddle, String nameFirst) {
		this.setSession(s);
		this.setId(id);
		this.nameLast = nameLast;
		this.nameMiddle = nameMiddle;
		this.nameFirst = nameFirst;
	}
	
	public Person(String name) {
		setName(name);
	}

	public void addContact(Contact contact) {
		contacts.add(contact);
	}
	
	public void addDocument(DocumentPerson dp) {
		documents.add(dp);
	}

	public void addHomonym(PersonHomonym homonym) {
		homonyms.add(homonym);
	}

	public void addHomonym(String homonym) {
		homonyms.add(new PersonHomonym(this.getSession(), homonym));
	}

	public void addXref(PersonXref x) {
		xrefs.add(x);
	}

	/**
	 * @return the contacts
	 */
	public Set<Contact> getContacts() {
		return contacts;
	}

	/**
	 * @return the dob
	 */
	public Date getDob() {
		return dob;
	}
	
	public String getDocIdxToken() {
		//TODO: implement 
		return null;
	}
	
	public List<Document> getDocumentsIndexed() {
		//TODO: implement 
		return Collections.emptyList();
	}

	/**
	 * @return the documents
	 */
	public List<DocumentPerson> getDocumentsFromSource(String source) {
		
		System.out.println("debug id: "+getId());
		System.out.println("debug source: "+source);
		@SuppressWarnings("unchecked")
		List<DocumentPerson> documents = (List<DocumentPerson>) this.getSession().createCriteria(DocumentPerson.class)
														.add(Restrictions.eq("person_id", this.getId()))
														.setFetchMode("document_xref", org.hibernate.FetchMode.JOIN)
														.createCriteria("document_xref")
														.add(Restrictions.eq("source", source))
														.list();
								
		return documents;
	}
	
	/**
	 * @return the documents
	 */
	public Set<DocumentPerson> getDocuments() {
		return documents;
	}

	/**
	 * @return the homonyms
	 */
	public Set<PersonHomonym> getHomonyms() {
		return homonyms;
	}

	
	/**
	 * @return the institution
	 */
	public Institution getInstitution() {
		return institution;
	}

	/**
	 * @return the nameFirst
	 */
	public String getNameFirst() {
		return nameFirst;
	}

	/**
	 * @return the nameLast
	 */
	public String getNameLast() {
		return nameLast;
	}

	/**
	 * @return the nameMiddle
	 */
	public String getNameMiddle() {
		return nameMiddle;
	}
	
	/**
	 * @return the valid
	 */
	public Short getValid() {
		return valid;
	}
	

	/**
	 * @return the gender (0=female, 1=male)
	 */
	public Short getGender() {
		return gender;
	}
	
	/**
	 * @return the xrefs
	 */
	public Set<PersonXref> getPersonXrefs() {
		return xrefs;
	}

	/**
	 * @param contacts the contacts to set
	 */
	public void setContacts(Set<Contact> contacts) {
		this.contacts = contacts;
	}

	/**
	 * @param dob the dob to set
	 */
	public void setDob(Date dob) {
		this.dob = dob;
	}

	/**
	 * @param documents the documents to set
	 */
	public void setDocuments(Set<DocumentPerson> documents) {
		this.documents = documents;
	}

	/**
	 * @param homonyms the homonyms to set
	 */
	public void setHomonyms(Set<PersonHomonym> homonyms) {
		this.homonyms = homonyms;
	}
	
	

	/**
	 * @param institution the institution to set
	 */
	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Person setName(String name) {
		nameFirst = name;
		return this;
	}

	/**
	 * @param nameFirst the nameFirst to set
	 */
	public void setNameFirst(String nameFirst) {
		this.nameFirst = nameFirst;
	}

	/**
	 * @param nameLast the nameLast to set
	 */
	public void setNameLast(String nameLast) {
		this.nameLast = nameLast;
	}

	/**
	 * @param nameMiddle the nameMiddle to set
	 */
	public void setNameMiddle(String nameMiddle) {
		this.nameMiddle = nameMiddle;
	}
	
	/**
	 * @param valid the valid to set
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}
	
	/**
	 * @param set Gender (0=female, 1=male)
	 */
	public void setGender(Short gender) {
		this.gender = gender;
	}

	/**
	 * @param xrefs the xrefs to set
	 */
	public void setXrefs(Set<PersonXref> xrefs) {
		this.xrefs = xrefs;
	}

	public void setNameLastPrefix(String nameLastPrefix) {
		this.nameLastPrefix = nameLastPrefix;
	}

	public String getNameLastPrefix() {
		return nameLastPrefix;
	}

	public void setNameLastSuffix(String nameLastSuffix) {
		this.nameLastSuffix = nameLastSuffix;
	}

	public String getNameLastSuffix() {
		return nameLastSuffix;
	}
	
	public String getNameComplete() {
		return nameComplete;
	}
	
	protected void setNameComplete(String nameComplete) {
		this.nameComplete = nameComplete;
	}

	public void setDod(Date dod) {
		this.dod = dod;
	}

	public Date getDod() {
		return dod;
	}

	public String toString() {
		String name = new String();
		if(getNameFirst() != null) {
			name += getNameFirst();
		}
		if(getNameMiddle() != null) {
			if(name.compareTo("") != 0) {
				name += " ";
			}
			name += getNameMiddle();
		}
		if(getNameLast() != null) {
			if(name.compareTo("") != 0) {
				name += " ";
			}
			name += getNameLast();
		}
		return name;
	}

	public String createNameComplete() {
		String nameComplete = "";
		if(this.getNameFirst() != null){
			nameComplete += this.getNameFirst() + " ";
		}
		if(this.getNameMiddle() != null) {
			nameComplete += this.getNameMiddle() + " ";
		}
		if(this.getNameLastPrefix() != null){
			nameComplete += this.getNameLastPrefix() + " ";
		}
		if(this.getNameLast() != null){
			nameComplete += this.getNameLast() + " ";
		}
		if(this.getNameLastSuffix() != null){
			nameComplete += this.getNameLastSuffix() + " ";
		}
		nameComplete = nameComplete.trim();
		return nameComplete;
	}	
}
