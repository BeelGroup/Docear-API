package org.sciplore.beans;

import org.sciplore.formatter.CollectionBean;

public class Recommendations extends CollectionBean {

    private String href;
    private String descriptor;
    

    public Recommendations() {
	
    }
        
    public void setHref(String href) {
	this.href = href;
    }

    public String getHref() {
	return href;
    }

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String desciptor) {
		this.descriptor = desciptor;
	}
}