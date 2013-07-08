package org.docear.xml;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Keywords implements ISimpleXmlElement{
	
	private HashMap<String, String> variables = new HashMap<String, String>();	
	private ArrayList<Keyword> keywords = new ArrayList<Keyword>();
	
	public Keywords() {
	}
	
	public void addVariable(String name, String term) {
		this.variables.put(name, term);
	}
	
	public HashMap<String, String> getVariables() {
		return variables;
	}	
	
	public void addKeyword(String name, Double weight) {
		this.keywords.add(new Keyword(name, weight));
	}
	
	public ArrayList<Keyword> getKeywords() {
		return keywords;
	}
	
	public boolean isEmpty() {
		return keywords == null || keywords.size() == 0;
	}
	
	@Override
	public Element getElement(final  Document doc) {
		Element element = doc.createElement("keywords");
		
		for (String key : variables.keySet()) {
			element.setAttribute(key, variables.get(key));			
		}
		
		for (Keyword keyword : keywords) {
			element.appendChild(keyword.getElement(doc));
		}
		
		return element;
	}

}
