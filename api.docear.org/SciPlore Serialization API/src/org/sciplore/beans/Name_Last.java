package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class Name_Last extends SimpleTypeElementBean {

	public Name_Last() {
	}
	
	public Name_Last(String value) {
		this.setValue(value);
	}
}
