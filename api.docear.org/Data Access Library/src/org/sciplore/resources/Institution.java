package org.sciplore.resources;

import java.util.HashSet;
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
import org.hibernate.criterion.Restrictions;

@Entity
@Table(name = "institutions")
public class Institution extends Resource {
	public final static short INSTITUTION_TYPE_MISC = 4;
	public final static short INSTITUTION_TYPE_PUBLISHER = 2;
	public final static short INSTITUTION_TYPE_SCHOOL = 3;
	public final static short INSTITUTION_TYPE_UNIVERSITY = 1;

	public Resource getPersistentIdentity() {
		return getInstitution(this);
	}
	
	public  Institution getInstitution(Institution i) {
		if(i.getId() != null && i.getSession() != null) {
			return (Institution) getSession().load(Institution.class, i.getId());
		} else {
			return getInstitution(i.getName());
		}
	}
	public  Institution getInstitution(Integer id) {
		return (Institution)this.getSession().get(Institution.class, id);
	}
	public  Institution getInstitution(String name) {
		return (Institution)this.getSession().createCriteria(Institution.class)
			.add(Restrictions.like("name", name))
			.setMaxResults(1)
			.uniqueResult();
	}
		
	private String address;

	@OneToMany(mappedBy = "parent")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Institution> children;

	private String city;
	private String country;
	@OneToMany(mappedBy = "institution")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Document> documents;
	
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "parent")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Institution parent;

	private String postalcode;

	private String state;
	
	private Short type; // University/Scool; Publisher; Conference; Workshop

	private String url;

	@Column(nullable = false)
	private Short valid = 1;
	
	@OneToMany(mappedBy = "institution")
	private Set<DocumentXref> xrefs = new HashSet<DocumentXref>();
	
	private String xrefBaseUrl;

	public Institution() {
	}
	
	public Institution(Session s) {
		this.setSession(s);
	}
	
	public Institution(Session s, Integer id) {
		this.setSession(s);
		this.setId(id);
	}
	
	public Institution(Session s, String name) {
		this.setSession(s);
		this.name = name;
	}
	
	public Institution(Session s, String name, Short type) {
		this.setSession(s);
		this.name = name;
		this.type = type;
	}
	
	public void addChild(Institution c) {
		children.add(c);
	}
	
	public void addDocument(Document d) {
		documents.add(d);
	}
	
	/**
	 * @return the adress
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the children
	 */
	public Set<Institution> getChildren() {
		return children;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the documents
	 */
	public Set<Document> getDocuments() {
		return documents;
	}

	

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the parent
	 */
	public Institution getParent() {
		return parent;
	}

	/**
	 * @return the postalcode
	 */
	public String getPostalcode() {
		return postalcode;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return the type
	 */
	public Short getType() {
		return type;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the valid
	 */
	public Short getValid() {
		return valid;
	}

	/**
	 * @param adress the adress to set
	 */
	public Institution setAddress(String address) {
		this.address = address;
		return this;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(Set<Institution> children) {
		this.children = children;
	}

	/**
	 * @param city the city to set
	 */
	public Institution setCity(String city) {
		this.city = city;
		return this;
	}

	/**
	 * @param country the country to set
	 */
	public Institution setCountry(String country) {
		this.country = country;
		return this;
	}

	/**
	 * @param documents the documents to set
	 */
	public void setDocuments(Set<Document> documents) {
		this.documents = documents;
	}



	/**
	 * @param name the name to set
	 */
	public Institution setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * @param parent the parent to set
	 */
	public Institution setParent(Institution parent) {
		this.parent = parent;
		return this;
	}

	/**
	 * @param postalcode the postalcode to set
	 */
	public Institution setPostalcode(String postalcode) {
		this.postalcode = postalcode;
		return this;
	}
	
	/**
	 * @param state the state to set
	 */
	public Institution setState(String state) {
		this.state = state;
		return this;
	}

	/**
	 * @param type the type to set
	 */
	public Institution setType(Short type) {
		this.type = type;
		return this;
	}

	/**
	 * @param url the url to set
	 */
	public Institution setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @param valid the valid to set
	 */
	public Institution setValid(Short valid) {
		this.valid = valid;
		return this;
	}
	
	public void addXref(DocumentXref x) {
		x.setInstitution(this);
		xrefs.add(x);
	}

	public void setXrefs(Set<DocumentXref> xrefs) {
		this.xrefs = xrefs;
	}
	public Set<DocumentXref> getXrefs() {
		return xrefs;
	}
	public void setXrefBaseUrl(String xrefBaseUrl) {
		this.xrefBaseUrl = xrefBaseUrl;
	}
	public String getXrefBaseUrl() {
		return xrefBaseUrl;
	}
	public String toString() {
		return name;
	}

}
