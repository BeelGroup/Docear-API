package org.docear.nanoxml;

import java.util.Map;

public interface IElementHandler {
	
	/**
	 * @return Name of the XML element this handler is responsible for. <code>NULL</code> value is not allowed. 
	 */
	public String forElement();

	public Object startElement(String name, Map<String, String> attributes, Object parent) throws Exception;

	public void endElement(String name) throws Exception;
	
	public void initialize() throws Exception;
	
	public void addContent(char[] buffer) throws Exception;
	
	public boolean hasContentHandler();
	
	public IElementHandler getContentHandler() throws Exception;
}
