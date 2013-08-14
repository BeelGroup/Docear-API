package org.sciplore.xtract.resultstructure;

import java.util.Vector;

public class ResultXMLDocument {
	private String id;
	private String title = "";
	private Vector<TextContainer> textContents;
	public ResultXMLDocument() {
		textContents = new Vector<TextContainer>();
	}
	public void setID(String ident) {
		id = ident;
	}
	public String getID() {
		return id;
	}
	public void setTitle(String titleString) {
		title = titleString;
	}
	public String getTitle() {
		return title;
	}
	public void addTextContainer(TextContainer textContainer) {
		textContents.add(textContainer);
	}
	public Vector<TextContainer> getTextContainers() {
		return textContents;
	}
}
