package org.docear.structs;

/**
 * This class holds information about each node including the node text and the depth in the mind map.
 * @author gkapi
 */

public class NodeInfo {
	
	private String text;
	private Integer depth; //node depth on the mind map
	private Integer noOfSiblings;
	private Integer noOfChildren;
	
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

	public Integer getNoOfSiblings() {
		return noOfSiblings;
	}

	public void setNoOfSiblings(Integer noOfsiblings) {
		this.noOfSiblings = noOfsiblings;
	}

	public Integer getNoOfChildren() {
		return noOfChildren;
	}

	public void setNoOfChildren(Integer noOfchildren) {
		this.noOfChildren = noOfchildren;
	}
	
}
