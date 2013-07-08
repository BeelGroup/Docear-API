package org.sciplore.deserialize.traversing;

public class TreePath {
	private final TreePath parent;
	private final String name;
	
	public TreePath(String name, TreePath parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public boolean hasAncestor(String name) {
		if(parent != null) {
			if(parent.getName().equals(name)) {
				return true;
			} 
			else {
				return parent.hasAncestor(name);
			}
		}
		return false;
	}
	
	public TreePath getParent() {
		return this.parent;
	}
	
	private String getName() {
		return this.name;
	}

	public String toString() {
		return (parent == null ? "" : parent.toString()) + "/"+this.name;
	}
}
