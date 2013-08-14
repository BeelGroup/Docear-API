package org.sciplore.xtract.textstructure;

import java.util.Vector;

/*
 * this class represent an xml documet which includes a vector pages 
 */
public class XMLDocument {

	Vector<Page> pages;

	public XMLDocument() {
		pages = new Vector<Page>();
	}

	public void addPage(Page pageVar) {
		pages.add(pageVar);
	}

	public Vector<Page> getPages() {
		return pages;
	}

	public FontSpec getFontSpecification(int fontId) {
		for (Page pa : pages) {
			FontSpec temp = pa.getFontSpecification(fontId);
			if (temp != null)
				return temp;
		}
		return null;
	}

	public String toString() {
		String Temp = "";
		for (Page pa : pages) {
			Temp += pa.toString();
		}
		return Temp;
	}

}
