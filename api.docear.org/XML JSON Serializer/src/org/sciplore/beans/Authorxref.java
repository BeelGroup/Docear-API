package org.sciplore.beans;

import org.sciplore.formatter.Bean;

public class Authorxref extends Bean{
	public final static int PHOTO = 1;
	public final static int PROFILE = 2;
	public final static int HOMEPAGE = 3;
	public final static int BIOGRAPHY = 4;
	public final static int ARTICLE = 5;
	public final static int PUBLICATION = 6;
 
	
	private String href;
	private String type;
	private Bean url;

	public void setHref(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}
	
	public void setUrl(Url url) {
		this.url = url;
	}
	
	public Bean getUrl() {
		return this.url;
	}
	
	public void setType(String type) {
		Integer typ_int;
		try {
			typ_int = Integer.valueOf(type);
		} 
		catch(Exception e) {
			typ_int = 0;
		}
		switch (typ_int) {
			case Authorxref.PHOTO: this.type = "photo"; break;
			case Authorxref.PROFILE: this.type = "profile"; break;
			case Authorxref.HOMEPAGE: this.type = "homepage"; break;
			case Authorxref.BIOGRAPHY: this.type = "biography"; break;
			case Authorxref.ARTICLE: this.type = "article"; break;
			case Authorxref.PUBLICATION: this.type = "publication"; break;
			default: this.type = "unknown"; break;
		}
	}

	public String getType() {
		return type;
	}
	
	

}
