package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class Url extends SimpleTypeElementBean{
	public Url() {
		super();
	}
	
	public Url(String url) {
		super();
		this.setValue(url);
	}
}
