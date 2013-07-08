package org.docear.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Keyword implements ISimpleXmlElement {	
	private String term;
	private Double weight;
		
	public Keyword(String value, Double weight) {
		this.term = value;
		this.weight = weight;
	}
		
		
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}


	@Override
	public Element getElement(final Document doc) {
		Element element = doc.createElement("keyword");		
		element.setTextContent(term);
		element.setAttribute("weight", weight.toString());
		
		return element;
	}

}
