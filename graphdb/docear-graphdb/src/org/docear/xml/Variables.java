package org.docear.xml;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Variables implements ISimpleXmlElement {
	
	final private HashMap<String, String> variables = new HashMap<String, String>();
	
	public void addVariable(String name, String value) {
		variables.put(name, value);
	}
	
	public HashMap<String, String> getVariables() {
		return variables;
	}

	@Override
	public Element getElement(Document doc) {
		Element element = doc.createElement("meta");
				
		for (String key : variables.keySet()) {			
			element.setAttribute(key, variables.get(key));			
		}
		
		return element;
	}

}
