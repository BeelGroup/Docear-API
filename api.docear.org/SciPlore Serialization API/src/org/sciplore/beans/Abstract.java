package org.sciplore.beans;

import org.sciplore.formatter.SimpleTypeElementBean;

public class Abstract extends SimpleTypeElementBean{
	
	private String href;
	private String id;
	
	
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

}
