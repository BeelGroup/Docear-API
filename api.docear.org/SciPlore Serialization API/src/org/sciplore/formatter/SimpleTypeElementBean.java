package org.sciplore.formatter;


public abstract class SimpleTypeElementBean extends Bean {
	
	private String value;

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		if(value != null) {
			this.value = value;
		} else {
			this.value = "";
		}
		this.activateElement("value");
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	

}
