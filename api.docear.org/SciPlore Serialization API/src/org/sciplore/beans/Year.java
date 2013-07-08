package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class Year extends SimpleTypeElementBean{
	
	public Year() {
		
	}
	
	public Year(String value) {
		this.setValue(value);
	}
}
