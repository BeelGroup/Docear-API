package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class Name_Middle extends SimpleTypeElementBean {
	
	public Name_Middle() {
	}
	
	public Name_Middle(String value) {
		this.setValue(value);
	}
}
