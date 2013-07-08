package org.docear.nanoxml;

import java.util.Map;

public class DefaultUnknownElementHandler implements IElementHandler {
	
	public String forElement() {
		return "";
	}

	public void addContent(char[] buffer) throws Exception {
	}
	
	public void endElement(String name) throws Exception {	
	}

	public void initialize() throws Exception {
	}

	public Object startElement(String name, Map<String, String> attributes, Object parent) throws Exception {
		return null;
	}

	public boolean allowsChildElements() {
		return true;
	}

	public boolean hasContentHandler() {
		return false;
	}

	public IElementHandler getContentHandler() throws Exception {
		return null;
	}

}
