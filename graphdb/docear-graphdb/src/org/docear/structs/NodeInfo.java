package org.docear.structs;

/**
 * This class holds information about each node including the node text and the depth in the mind map.
 * @author gkapi
 */

public class NodeInfo {
	
	private String text;
	private Integer depth;
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Integer getDepth() {
		return depth;
	}
	
	public void setDepth(Integer depth) {
		this.depth = depth;
	}
	
}
