package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.CollectionBean;

@SciBeanElements({"Authorxref"})
public class Authorxrefs extends CollectionBean {
	
	private String href;

	public void setHref(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}

}
