package org.docear.nanoxml;

public interface IElementHandlerManager {
	public IElementHandler getHandlerForName(String name);
	public void initializeAll();
}
