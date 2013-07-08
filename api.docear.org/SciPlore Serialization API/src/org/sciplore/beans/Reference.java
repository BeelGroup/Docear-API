package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.Bean;

@SciBeanElements({"document","occurences"})
public class Reference extends Bean {
	
	private String id;
	private String href;
	private Bean document;
	private Bean occurences;
	
	
	public Bean getDocument() {
		return document;
	}
	public void setDocument(Bean document) {
		this.document = document;
	}
	public Bean getOccurences() {
		return occurences;
	}
	public void setOccurences(Bean occurences) {
		this.occurences = occurences;
	}
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
	
}
