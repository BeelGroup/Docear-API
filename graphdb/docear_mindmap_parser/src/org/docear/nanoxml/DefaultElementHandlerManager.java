package org.docear.nanoxml;

import java.util.ArrayList;
import java.util.List;

public class DefaultElementHandlerManager implements IElementHandlerManager {

	public final List<IElementHandler> handlers;
	
	public DefaultElementHandlerManager() {
		this(new ArrayList<IElementHandler>());
	}
	
	public DefaultElementHandlerManager(List<IElementHandler> handlers) {
		this.handlers = handlers;
	}
	
	public IElementHandler getHandlerForName(String name) {
		for(IElementHandler handler : handlers) {
			if(handler.forElement().equals(name)) {
				return handler;
			}
		}
		return null;
	}

	public void initializeAll() {
		for(IElementHandler handler : handlers) {
			try {
				handler.initialize();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
