package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class Name_First extends SimpleTypeElementBean {

	public Name_First() {
	}
	
	public Name_First(String value) {
		this.setValue(value);
	}

}
