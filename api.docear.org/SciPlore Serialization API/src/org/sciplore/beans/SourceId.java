package org.sciplore.beans;

import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanImplicitValue("value")
public class SourceId extends SimpleTypeElementBean{
	
	public SourceId() {
		super();
	}
	
	public SourceId(String value) {
		super();
		this.setValue(value);
	}

}
