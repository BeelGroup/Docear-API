package org.sciplore.beans;

import org.sciplore.formatter.SimpleTypeElementBean;

public class Title extends SimpleTypeElementBean{
	
	private String href;
	
	public Title() {
		super();
	}
	
	public Title(String title) {
		this.setValue(title);
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
