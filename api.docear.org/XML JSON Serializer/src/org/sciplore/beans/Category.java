package org.sciplore.beans;

import org.sciplore.formatter.Bean;

public class Category extends Bean{
	
	private String href;
	private String type;
	private Bean organization;
	private Bean id;
	
	
	public void setHref(String href) {
		this.href = href;
	}
	public String getHref() {
		return href;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Bean getOrganization() {
		return organization;
	}
	public void setOrganization(Bean organization) {
		this.organization = organization;
	}
	public Bean getId() {
		return id;
	}
	public void setId(Bean id) {
		this.id = id;
	}
	
	
	

}
