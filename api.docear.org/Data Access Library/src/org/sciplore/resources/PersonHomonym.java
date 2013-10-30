package org.sciplore.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Formula;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;

@Entity
@Table(name = "person_homonyms")
public class PersonHomonym extends Resource {
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "person_id")
	@Required
	private Person person;
	
	@OneToMany(mappedBy = "personHomonym")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<DocumentPerson> documents = new HashSet<DocumentPerson>();
	
	@Formula(value= "CONCAT_WS(' ', name_first, name_middle, name_last_prefix, name_last, name_last_suffix)")
	private String nameComplete;
	private String nameComparable;
	private String nameFirst;
	private String nameLast;
	private String nameMiddle;
	private String nameLastPrefix;
	private String nameLastSuffix;
	
	
	@Column(nullable = false)
	private Short valid;
	
	public PersonHomonym() {
		
	}
	
	public PersonHomonym(Session s) {
		this.setSession(s);
	}
	
	public PersonHomonym(Session s, String name) {
		this.setSession(s);
		this.nameFirst = name;
	}
		
	@SuppressWarnings("unchecked")
	public  PersonHomonym getPersonHomonym(PersonHomonym p) {
		if(p.getId() != null) {
			p.load();
			return p;
		} else {
			String nameComplete = p.createNameComplete();			
			System.out.println("PersonHomonym Name Complete: " + nameComplete);			
			boolean changed = false;
			do{
				List<PersonHomonym> homonyms = (List<PersonHomonym>)getSession().createCriteria(PersonHomonym.class)														   
											       .add(Restrictions.eq("nameComparable", nameComplete))
											       .add(Restrictions.ne("nameComparable", ""))
											       .list();
				for(PersonHomonym homonym : homonyms){
					changed = false;
					for(PersonHomonym equalHomonym :  getEqualHomonyms(homonyms, homonym)){
						changed = true;
						for(DocumentPerson docPers : equalHomonym.getDocuments()){
							docPers.setSession(getSession());
							docPers.setPersonHomonym(homonym);
							docPers.save();
							homonym.getDocuments().add(docPers);
						}
						getSession().delete(equalHomonym);
						System.out.println("Duplicate deleted");
					}
					if(changed){
						homonym.setSession(getSession());
						homonym.save();
						break;
					}
				}
			}while(changed);
			return (PersonHomonym)getSession().createCriteria(PersonHomonym.class)														   
										       .add(Restrictions.eq("nameComparable", nameComplete))
										       .setMaxResults(1)
										       .uniqueResult();
		}
	}
	
	
	private List<PersonHomonym> getEqualHomonyms(List<PersonHomonym> homonyms, PersonHomonym homonym){
		List<PersonHomonym> equalHomonyms = new ArrayList<PersonHomonym>();
		for(PersonHomonym h : homonyms){
			if(   h.getId() != homonym.getId() 
			   && equalString(h.getNameFirst(),homonym.getNameFirst())
			   && equalString(h.getNameLast(),homonym.getNameLast())
			   && equalString(h.getNameLastPrefix(),homonym.getNameLastPrefix())
			   && equalString(h.getNameLastSuffix(),homonym.getNameLastSuffix())
			   && equalString(h.getNameMiddle(),homonym.getNameMiddle())
			   && equalPerson(h.getPerson(), homonym.getPerson())){
				equalHomonyms.add(h);
			}
		}
		return equalHomonyms;
	}
	
	private static boolean equalPerson(Person p1, Person p2) {
		if(p1 == null && p2 == null) return true;
		if(p1 != null && p2 == null) return false;
		if(p1 == null && p2 != null) return false;
		return p1.getId() == p2.getId();
	}

	public static boolean equalString(String s1, String s2){
		if(s1 == null && s2 == null) return true;
		if(s1 != null && s2 == null) return false;
		if(s1 == null && s2 != null) return false;
		return s1.equalsIgnoreCase(s2);		
	}
	
	public  PersonHomonym getPersonHomonym(Integer id) {
		return (PersonHomonym) this.getSession().get(PersonHomonym.class, id);
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
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	/**
	 * @return the valid
	 */
	public Short getValid() {
		return valid;
	}
	
	/**
	 * @param nameFirst the nameFirst to set
	 */
	public void setNameFirst(String nameFirst) {
		this.nameFirst = nameFirst;
		this.nameComparable = this.createNameComplete();
	}
	/**
	 * @param nameLast the nameLast to set
	 */
	public void setNameLast(String nameLast) {
		this.nameLast = nameLast;
		this.nameComparable = this.createNameComplete();
	}
	/**
	 * @param nameMiddle the nameMiddle to set
	 */
	public void setNameMiddle(String nameMiddle) {
		this.nameMiddle = nameMiddle;
		this.nameComparable = this.createNameComplete();
	}
	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * @param valid the valid to set
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}

	public Set<DocumentPerson> getDocuments() {
		return documents;
	}

	public void setDocuments(Set<DocumentPerson> documents) {
		this.documents = documents;
	}

	public String getNameComplete() {
		return nameComplete;
	}

	public String getNameLastPrefix() {
		return nameLastPrefix;
	}

	public void setNameLastPrefix(String nameLastPrefix) {
		this.nameLastPrefix = nameLastPrefix;
		this.nameComparable = this.createNameComplete();
	}

	public String getNameLastSuffix() {
		return nameLastSuffix;
	}

	public void setNameLastSuffix(String nameLastSuffix) {
		this.nameLastSuffix = nameLastSuffix;
		this.nameComparable = this.createNameComplete();
	}
	
	public static String createNameComplete(Person p) {
		String nameComplete = "";
		if(p.getNameFirst() != null){
			nameComplete += p.getNameFirst() + " ";
		}
		if(p.getNameMiddle() != null){
			nameComplete += p.getNameMiddle() + " ";
		}
		if(p.getNameLastPrefix() != null){
			nameComplete += p.getNameLastPrefix() + " ";
		}
		if(p.getNameLast() != null){
			nameComplete += p.getNameLast() + " ";
		}
		if(p.getNameLastSuffix() != null){
			nameComplete += p.getNameLastSuffix() + " ";
		}
		nameComplete = nameComplete.trim();
		return nameComplete;
	}
	
	public String createNameComplete() {
		String nameComplete = "";
		if(this.getNameFirst() != null){
			nameComplete += this.getNameFirst() + " ";
		}
		if(this.getNameMiddle() != null){
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

	public void setNameComparable(String nameComparable) {
		this.nameComparable = nameComparable;
	}

	public String getNameComparable() {
		return nameComparable;
	}
	
	public Resource getPersistentIdentity() {
		return getPersonHomonym(this);
	}
}
