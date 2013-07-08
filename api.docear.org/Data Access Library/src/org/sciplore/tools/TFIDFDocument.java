package org.sciplore.tools;

import org.sciplore.resources.Document;

public class TFIDFDocument {
	private Document document;
	private Double weight;
	
	
	public TFIDFDocument() {		
	}
	
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}


	public Double getWeight() {
		return weight;
	}


	public void setWeight(Double weight) {
		this.weight = weight;
	}	
}
