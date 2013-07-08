package org.docear.parser.handler;

import java.util.Map;

import org.docear.graphdb.threading.IGraphStep;
import org.docear.nanoxml.IElementHandler;



public class IconElementHandler implements IElementHandler {

	public IconElementHandler() {
	}

	public void addContent(char[] buffer) throws Exception {
	}

	public void endElement(String name) throws Exception {
		// TODO Auto-generated method stub

	}

	public String forElement() {
		return "icon";

	}

	public Object startElement(String name, final Map<String, String> attributes, final Object parent) throws Exception {
		if(parent != null && parent instanceof IGraphStep) {
			addProperties((IGraphStep)parent, attributes);
		}
		return parent;
	}

	private void addProperties(IGraphStep parent, Map<String, String> attributes) {
		for(String key : attributes.keySet()) {
			parent.addProperty("icon_"+key, attributes.get(key));
		}		
	}

	
	public void initialize() throws Exception {
	}

	@Override
	public boolean hasContentHandler() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IElementHandler getContentHandler() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
