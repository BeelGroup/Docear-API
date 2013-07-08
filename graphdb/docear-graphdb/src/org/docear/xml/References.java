package org.docear.xml;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class References implements ISimpleXmlElement {
	final private HashMap<String, String> variables = new HashMap<String, String>();	
	private ArrayList<Reference> references = new ArrayList<Reference>();

	public References() {
	}
	
	public void addVariable(String name, String value) {
		variables.put(name, value);
	}
	
	public HashMap<String, String> getVariables() {
		return variables;
	}
	
	public void addReference(String title, String hash, Double weight) {
		references.add(new Reference(title, hash, weight));
	}
	
	public ArrayList<Reference> getReferences() {
		return references;
	}
	
	public boolean isEmpty() {
		return references == null || references.size() == 0;
	}
		
	@Override
	public Element getElement(Document doc) {
		Element element = doc.createElement("references");
		
		for (String key : variables.keySet()) {
			element.setAttribute(key, variables.get(key));			
		}
		
		for (Reference reference : references) {
			element.appendChild(reference.getElement(doc));
		}
				
		return element;
	}

	
}
