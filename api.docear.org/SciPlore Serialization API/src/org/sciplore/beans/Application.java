package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.Bean;

@SciBeanElements({"versions"})
public class Application extends Bean {
	
	private String id;
	private String href;
	private String name;
	private ApplicationVersions versions;
	
	public ApplicationVersions getVersions() {
		if(versions == null) {
			versions = new ApplicationVersions();
		}
		return versions;
	}
	
	public void setVersions(ApplicationVersions versions) {
		this.versions = versions;
		activateElement("versions");
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}	
	
}
