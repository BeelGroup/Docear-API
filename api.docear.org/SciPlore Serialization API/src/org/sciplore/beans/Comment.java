package org.sciplore.beans;
import org.sciplore.formatter.SimpleTypeElementBean;


public class Comment extends SimpleTypeElementBean{
	
	private String href;
	private String created;
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
	}
	
}
