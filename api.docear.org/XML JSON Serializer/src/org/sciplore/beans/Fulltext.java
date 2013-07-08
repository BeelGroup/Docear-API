package org.sciplore.beans;

import org.sciplore.formatter.SimpleTypeElementBean;

public class Fulltext extends SimpleTypeElementBean{
	
	private String id;
	private String href;
	private String licence;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getLicence() {
		return licence;
	}
	public void setLicence(String licence) {
		this.licence = licence;
	}
	
	

}
