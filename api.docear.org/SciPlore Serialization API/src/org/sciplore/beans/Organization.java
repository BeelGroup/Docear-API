package org.sciplore.beans;

import org.sciplore.formatter.Bean;
import org.sciplore.formatter.SimpleTypeElementBean;

public class Organization extends SimpleTypeElementBean{
	
	private String id;
	private String href;
	private Bean name;
	private Bean url;
	
	public void setHref(String href) {
		this.href = href;
	}
	public String getHref() {
		return href;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public Bean getName() {
		return name;
	}
	public void setName(Bean name) {
		this.name = name;
	}
	public Bean getUrl() {
		return url;
	}
	public void setUrl(Bean url) {
		this.url = url;
	}
	
}
