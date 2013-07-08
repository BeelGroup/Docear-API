package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.Bean;

@SciBeanElements({"organization","sourceid","sourceurl","releasedate"})
public class Xref extends Bean{
	
	private String id;
	private String href;
	private Bean organization;
	private Bean sourceid;
	private Bean url;
	private Bean releaseDate;
	private Bean categories;
	
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
	
	public Bean getOrganization() {
		return organization;
	}
	
	public void setOrganization(Bean institution) {
		this.organization = institution;
	}
	
	public Bean getSourceid() {
		return sourceid;
	}
	
	public void setSourceid(Bean sourceid) {
		this.sourceid = sourceid;
	}
	
	public Bean getUrl() {
		return url;
	}
	
	public void setUrl(Bean url) {
		this.url = url;
	}
	
	public void setReleaseDate(Bean releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	public Bean getReleaseDate() {
		return releaseDate;
	}

	public void setCategories(Bean categories) {
		this.categories = categories;
	}

	public Bean getCategories() {
		return categories;
	}
	
	
	
}
