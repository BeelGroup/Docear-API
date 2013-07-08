package org.docear.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Reference implements ISimpleXmlElement {
	
	private String title;
	private String hash;
	private Double weight;
	
	public Reference(String title, String hash, Double weight) {
		this.title = title;
		this.hash = hash;
		this.weight = weight;
	}	
		
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	@Override
	public Element getElement(final Document doc) {
		Element element = doc.createElement("reference");
		element.setTextContent(title);
		element.setAttribute("hash", hash);
		element.setAttribute("weight", weight.toString());
				
		return element;
	}

	
}
