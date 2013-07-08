package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class ReleaseDate extends SimpleTypeElementBean {
	public ReleaseDate() {
		super();
	}
	
	public ReleaseDate(String date) {
		super();
		setValue(date);
	}
}
