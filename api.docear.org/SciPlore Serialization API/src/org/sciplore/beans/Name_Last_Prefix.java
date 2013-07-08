package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class Name_Last_Prefix extends SimpleTypeElementBean {

	public Name_Last_Prefix() {
	}
	
	public Name_Last_Prefix(String value) {
		this.setValue(value);
	}
}
