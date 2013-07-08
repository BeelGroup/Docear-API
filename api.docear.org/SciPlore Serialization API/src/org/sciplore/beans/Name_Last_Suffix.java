package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class Name_Last_Suffix extends SimpleTypeElementBean {
	
	public Name_Last_Suffix() {
	}
	
	public Name_Last_Suffix(String value) {
		this.setValue(value);
	}
}
