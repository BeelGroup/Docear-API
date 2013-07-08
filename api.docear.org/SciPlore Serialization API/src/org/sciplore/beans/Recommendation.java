package org.sciplore.beans;

import org.sciplore.formatter.Bean;

public class Recommendation extends Bean {

    private String href;
    private String created;
    private String fulltext;
    private String clicked;
    private String prefix;
    private Boolean highlighted;
    
    private Bean bean;

    public String getHref() {
	return href;
    }

    public void setHref(String href) {
	this.href = href;
    }

    public void setDocument(Bean bean) {
	this.bean = bean;
    }
    
    public Bean getDocument() {
	return bean;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getFulltext() {
        return fulltext;
    }

    public void setFulltext(String url) {
        this.fulltext = url;
    }

    public String getClicked() {
        return clicked;
    }

    public void setClicked(String clicked) {
        this.clicked = clicked;
    }

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getHighlighted() {		
		return highlighted.toString();
	}

	public void setHighlighted(String highlighted) {
		this.highlighted = highlighted.toLowerCase().equals("true");
	}
    
/**************
 * create a setter for every accepted Bean type
 */
//    public void setJob(Bean bean) {
//	this.bean = bean;
//    }

}
