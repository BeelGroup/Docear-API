package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;

@Entity
public class PersonXref extends Resource {
		
	public  PersonXref getPersonXref(PersonXref x) {
		if(x.getId() != null) {
			return (PersonXref) getSession().load(PersonXref.class, x.getId());
		} else {
			return getPersonXref(x.getPerson(), x.getSource(), x.getSourcesId(), x.getType());
		}
	}
	
	public  PersonXref getPersonXref(Integer id) {
		return (PersonXref)this.getSession().get(PersonXref.class, id);
	}
	
	public  PersonXref getPersonXref(Person p, String s, String source_id, Short type) {
		if(p == null || p.getId() == null) {
			return null;
		}
		return (PersonXref)this.getSession().createCriteria(PersonXref.class)
			.add(Restrictions.eq("person", p))
			.add(Restrictions.eq("source", s))
			.add((source_id == null || source_id.isEmpty()) ? Restrictions.isNull("sourcesId") : Restrictions.eq("sourcesId", source_id))
			.add(Restrictions.eq("type", type))
			.setMaxResults(1)
			.uniqueResult();
	}
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "person_id")
	@Required
	private Person person;
	@Column(nullable = false)
	private String source;
	private String sourcesId;
	private Short type;
	
	public PersonXref(){}
	
	public PersonXref(Session s){
		this.setSession(s);
	}
	
	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @return the sourcesId
	 */
	public String getSourcesId() {
		return sourcesId;
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @param sourcesId the sourcesId to set
	 */
	public void setSourcesId(String sourcesId) {
		this.sourcesId = sourcesId;
	}

	public void setType(Short type) {
		this.type = type;
	}

	public Short getType() {
		return type;
	}
	
	public Resource getPersistentIdentity() {
		return getPersonXref(this);
	}
}
