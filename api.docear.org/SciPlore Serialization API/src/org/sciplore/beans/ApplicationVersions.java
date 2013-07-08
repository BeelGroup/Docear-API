package org.sciplore.beans;

import java.util.List;

import org.sciplore.annotation.SciBeanAlias;
import org.sciplore.formatter.CollectionBean;

@SciBeanAlias("versions")
public class ApplicationVersions extends CollectionBean {
	
	private String href;
	
	public ApplicationVersions() {
		super();
	}
	
	public ApplicationVersions(List<ApplicationVersion> versions) {
		super();
		for(ApplicationVersion version : versions) {
			add(version);
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
