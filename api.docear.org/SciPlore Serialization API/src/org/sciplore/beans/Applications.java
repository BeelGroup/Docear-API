package org.sciplore.beans;

import java.util.List;

import org.sciplore.formatter.CollectionBean;

public class Applications extends CollectionBean {
	private String href;

	
	public Applications() {
		super();
	}
	
	public Applications(List<Application> apps) {
		super();
		for(Application app : apps) {
			add(app);
		}
	}
	
	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}
	
	
}
