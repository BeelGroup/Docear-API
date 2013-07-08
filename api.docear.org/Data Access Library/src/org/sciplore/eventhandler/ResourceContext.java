package org.sciplore.eventhandler;

import java.util.Hashtable;
import java.util.Stack;

public class ResourceContext {
	private final Stack<Object> lifoStack = new Stack<Object>();
	private final Hashtable<Object, Object> persistenceTable = new Hashtable<Object, Object>();
	private int recDepth = 0;
	
	public void pushToCascadeStack(Object o) {
		lifoStack.push(o);
	}
	
	public Object popFromCascadeStack() {
		return lifoStack.pop();
	}
	
	public boolean isOnCascadeStack(Object o) {
		return lifoStack.contains(o);
	}
	
	public boolean isPersistent(Object resource) {
		return persistenceTable.containsKey(resource);
	}
	
	public void addPersistentInstance(Object resource, Object persistent) {
		if(isPersistent(resource)) {
			return;
		}
		persistenceTable.put(resource, persistent);
	}
	
	public Object getPersistentResource(Object resource) {
		return persistenceTable.get(resource);
	}
	
	public void clearPeristenceTable() {
		this.persistenceTable.clear();
		recDepth = 0;
	}
	
	public void incRecDepth() {
		this.recDepth++;
	}
	
	public void decRecDepth() {
		this.recDepth--;
	}
	
	public boolean isTopLevel() {
		return (this.recDepth == 0);
	}
}
