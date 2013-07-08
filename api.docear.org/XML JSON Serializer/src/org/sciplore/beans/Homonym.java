package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.Bean;

@SciBeanElements({"name_first","name_last","name_middle","name_last_prefix","name_last_suffix"})
public class Homonym extends Bean{
	
	private String href;
	private Bean name_first;
	private Bean name_middle;
	private Bean name_last;
	private Bean name_last_prefix;
	private Bean name_last_suffix;
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public Bean getName_first() {
		return name_first;
	}
	public void setName_first(Bean name_first) {
		this.name_first = name_first;
	}
	public Bean getName_middle() {
		return name_middle;
	}
	public void setName_middle(Bean name_middle) {
		this.name_middle = name_middle;
	}
	public Bean getName_last() {
		return name_last;
	}
	public void setName_last(Bean name_last) {
		this.name_last = name_last;
	}
	public Bean getName_last_prefix() {
		return name_last_prefix;
	}
	public void setName_last_prefix(Bean name_last_prefix) {
		this.name_last_prefix = name_last_prefix;
	}
	public Bean getName_last_suffix() {
		return name_last_suffix;
	}
	public void setName_last_suffix(Bean name_last_suffix) {
		this.name_last_suffix = name_last_suffix;
	}

}
