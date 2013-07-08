package org.sciplore.beans;

import org.sciplore.formatter.SimpleTypeElementBean;

public class Doi extends SimpleTypeElementBean {
	
	private String href;
	
	public Doi() {
	}
	
	public Doi(String value) {
		this.setValue(value);
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}

}
