package org.docear.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Variable implements ISimpleXmlElement {
	private String elementName;
	private String name;
	private String value;
	
	public Variable(String elementName, String name, String value) {
		this.elementName = elementName;
		this.name = name;
		this.value = value;
	}

	@Override
	public Element getElement(final Document doc) {
		Element element = doc.createElement(elementName);
		element.setAttribute("name", name);
		element.setAttribute("value", value);
		
		return element;
	}

}
