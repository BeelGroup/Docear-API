package org.sciplore.xtract.resultstructure;

public class TextContainer {
	boolean isParagraph;
	boolean isUndefined;
	boolean isTitle;
	String name;
	String textContent;
	public TextContainer() {
		isParagraph = false;
		isUndefined = false;
		isTitle = false;
		name = "";
		textContent = "";
	}
	public void setParagraph() {
		isParagraph = true;
		isUndefined = false;
		isTitle = false;
	}
	public void setUndefined() {
		isParagraph = false;
		isUndefined = true;
		isTitle = false;
	}
	public void setTitle() {
		isParagraph = false;
		isUndefined = false;
		isTitle = true;
	}
	public boolean isParagraph() {
		return ((isParagraph == true) && (isUndefined == false) && (isTitle == false));
	}
	public boolean isUndefined() {
		return ((isParagraph == false) && (isUndefined == true) && (isTitle == false));
	}
	public boolean isTitle() {
		return ((isParagraph == false) && (isUndefined == false) && (isTitle == true));
	}
	public void setName(String nameString) {
		name = nameString;
	}
	public String getName() {
		return name;
	}
	public void setTextContent(String textContentString) {
		textContent = textContentString;
	}
	public String getTextContent() {
		return textContent;
	}
	public boolean hasName() {
		if(name.equals("")) {
			return false;
		}
		return true;
	}
}
