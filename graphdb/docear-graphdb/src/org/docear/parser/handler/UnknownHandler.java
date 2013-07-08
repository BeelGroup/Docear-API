package org.docear.parser.handler;

import java.util.Map;

import org.docear.graphdb.threading.IGraphStep;
import org.docear.nanoxml.IElementHandler;



public class UnknownHandler implements IElementHandler {

    public static final String UNKNOWN_CONTENT_KEY = "UNKNOWN_CONTENT";
    
    private IGraphStep parent;

    public UnknownHandler() {
    }

    public String forElement() {
    	return "";
    }

    public Object startElement(final String name, final Map<String, String> attributes, final Object parent) throws Exception {	
		if (parent != null && parent instanceof IGraphStep) {
		   this.parent = ((IGraphStep) parent);
		  
		   appendContent("<" + name + " ");
		   for(String key : attributes.keySet()) {
				addAttribute(key, attributes.get(key));
		   }
		   appendContent(">");
		}
		
		
		return null;
    }

    public void endElement(final String name) throws Exception {
		appendContent("</" + name + ">");
    }

	private void addAttribute(final String key, final Object value) throws Exception {
		appendContent(key + "=\"" + value + "\" ");			
	}

    public void addContent(final char[] buffer) throws Exception {
		appendContent( new String(buffer));
    }

    private void appendContent(final String content) {
		if (parent != null) {
		     parent.addProperty(UNKNOWN_CONTENT_KEY, content);    
		}
    }

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasContentHandler() {
		return false;
	}

	@Override
	public IElementHandler getContentHandler() throws Exception {
		return null;
	}
	
}
