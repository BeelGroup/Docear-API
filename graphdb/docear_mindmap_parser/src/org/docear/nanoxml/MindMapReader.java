package org.docear.nanoxml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.n3.nanoxml.IXMLBuilder;

public class MindMapReader implements IXMLBuilder {

	private static final int CONTENTLEVEL_NOT_SET = -999999999;

	private IElementHandler unknownHandler = new DefaultUnknownElementHandler();

	private final IElementHandlerManager handlerManager;
	private IElementHandler currentHandler = null;
	private Object currentObject = null;
	private Stack<IElementHandler> handlerStack = new Stack<IElementHandler>();
	private Stack<Object> objectStack = new Stack<Object>();
	private Map<String, String> attributes = new HashMap<String, String>();

	private int startContentLevel;
	private int elementLevel;

	public MindMapReader() {
		this(new ArrayList<IElementHandler>());
	}

	public MindMapReader(List<IElementHandler> handlers) {
		this(new DefaultElementHandlerManager());
	}

	public MindMapReader(IElementHandlerManager manager) {
		this.handlerManager = manager;
	}

	public void startBuilding(String systemID, int lineNr) throws Exception {
		currentObject = null;
		currentHandler = null;
		handlerStack.removeAllElements();
		objectStack.removeAllElements();
		this.handlerManager.initializeAll();
		startContentLevel = CONTENTLEVEL_NOT_SET;
		elementLevel = 0;
	}

	public void newProcessingInstruction(String target, Reader reader) throws Exception {
		// System.out.println("New PI with target " + target);
	}

	public void startElement(String name, String nsPrefix, String nsSystemID, String systemID, int lineNr) throws Exception {
		attributes = new HashMap<String, String>();
	}

	public void addAttribute(String key, String nsPrefix, String nsSystemID, String value, String type) throws Exception {
		attributes.put(key, value);
	}

	public void elementAttributesProcessed(String name, String nsPrefix, String nsSystemID) throws Exception {
		elementLevel++;
		IElementHandler handler = this.handlerManager.getHandlerForName(name);
		
		if (handler == null) {
			handler = getUnknownHandler();
		}

		if(currentHandler != null && currentHandler.hasContentHandler()) {
			if(startContentLevel == CONTENTLEVEL_NOT_SET) {
				startContentLevel = elementLevel;
			}
			currentHandler.getContentHandler().startElement(name, attributes, currentObject);
		} 
		else {
			startContentLevel = CONTENTLEVEL_NOT_SET;
			if (currentObject != null) {
				objectStack.push(currentObject);
			}
			currentObject = handler.startElement(name, attributes, currentObject);
	
			if (currentHandler != null) {
				handlerStack.push(currentHandler);
			}
			currentHandler = handler;
		}
	}
	
	public void endElement(String name, String nsPrefix, String nsSystemID) throws Exception {
		if(currentHandler != null && currentHandler.hasContentHandler() && elementLevel >= startContentLevel && startContentLevel!=CONTENTLEVEL_NOT_SET) {
			currentHandler.getContentHandler().endElement(name);
		} 
		else {
			try {
				if (currentHandler != null) {
					currentHandler.endElement(name);
				}
				currentHandler = handlerStack.pop();
				if (getUnknownHandler().equals(currentHandler)) {
					return;
				}
			} catch (EmptyStackException e) {
				currentHandler = null;
			}
			try {
				currentObject = objectStack.pop();
			} catch (EmptyStackException e) {
				// currentObject = null;
			}
		}
		elementLevel--;
	}
	
	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
		if (currentHandler != null) {
			int ch = -1;
			char[] buffer = new char[4096];
			Integer i = 0;
			while ((ch = reader.read()) != -1) {
				buffer[i] = (char) ch;
				buffer = incArrayIndex(buffer, i);
				i++;
				
			}
			if(currentHandler.hasContentHandler()) {
				currentHandler.getContentHandler().addContent(Arrays.copyOf(buffer, i));
			}
			else {
				currentHandler.addContent(Arrays.copyOf(buffer, i));
			} 
		}
	}


	public Object getResult() throws Exception {
		return null;
	}

	public IElementHandler getUnknownHandler() {
		return unknownHandler;
	}

	public void setUnknownHandler(IElementHandler handler) {
		this.unknownHandler = handler;
	}
	
	private char[] incArrayIndex(char[] array, Integer i) {
		i++;
		if (i >= array.length) {
			return extendArray(array, array.length*2);
		}
		return array;
		
	}

	private char[] extendArray(char[] array, int newLength) {
		char[] nArray = new char[newLength];
		for(int a=0; a < array.length; a++) {
			nArray[a] = array[a];
		}
		return nArray;
	}


}
