package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.tools.Tools;

@Entity
@Table(name = "venues")
public class Venue extends Resource {
	public final static short VENUE_TYPE_JOURNAL = 1;
	public final static short VENUE_TYPE_CONFERENCE = 2;
	public final static short VENUE_TYPE_BOOK = 3;
	public final static short VENUE_TYPE_PROCEEDINGS = 4;
	public final static short VENUE_TYPE_MISC = 5;
	
	public  Venue getVenue(Venue v) {
		if(v.getId() != null) {
			return getVenue(v.getId());
		} else {
			return getVenue(v.getName());
		}
	}
	
	public  Venue getVenue(Integer id) {
		return (Venue)this.getSession().get(Venue.class, id);
	}
	
	public  Venue getVenue(String name) {
		return (Venue)this.getSession().createCriteria(Venue.class)
				.add(Restrictions.like("name", name))
				.setMaxResults(1)
				.uniqueResult();
	}

	public  Venue sync(Venue venue) {
		Venue v = getVenue(venue);
		if(v == null) {
			v = venue;
		} else {
			if (Tools.empty(v.getAbbreviation()) && !Tools.empty(venue.getAbbreviation())) {
				v.setAbbreviation(venue.getAbbreviation());
			}
			if (Tools.empty(v.getAddress()) && !Tools.empty(venue.getAddress())) {
				v.setAddress(venue.getAddress());
			}
			if (Tools.empty(v.getId()) && !Tools.empty(venue.getId())) {
				v.setId(venue.getId());
			}
			if (Tools.empty(v.getName()) && !Tools.empty(venue.getName())) {
				v.setName(venue.getName());
			}
			if (Tools.empty(v.getType()) && !Tools.empty(venue.getType())) {
				v.setType(venue.getType());
			}
			if (Tools.empty(v.getValid()) && !Tools.empty(venue.getValid())) {
				v.setValid(venue.getValid());
			}
		}
		return v;
	}

	private String abbreviation;
	private String address;
	
	@Column(nullable = false)
	private String name = ""; // FIXME	
	private Short type;
	@Column(nullable = false)
	private Short valid = 1;

	public Venue() {
	}
	
	public Venue(Session s) {
		this.setSession(s);
	}
	
	public Venue(Session s, String name) {
		this.setSession(s);
		this.name = name;
	}

	public Venue(Session s, String name, Short type) {
		this.setSession(s);
		this.name = name;
		this.type = type;
	}

	/**
	 * @return the abbreviation
	 */
	public String getAbbreviation() {
		return abbreviation;
	}
	
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the type
	 */
	public Short getType() {
		return type;
	}
	
	/**
	 * @return the valid
	 */
	public Short getValid() {
		return valid;
	}
	
	/**
	 * @param abbreviation the abbreviation to set
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}
	
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(Short type) {
		this.type = type;
	}
	
	/**
	 * @param valid the valid to set
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}
	
	public Resource getPersistentIdentity() {
		return getVenue(this);
	}
}
