package org.docear.structs;

/**
 * This class holds information about each node including the node text and the depth in the mind map.
 * @author gkapi
 */

public class NodeInfo {
	
	private String id; //the id of the node as stored in the graph database
	private String text; // the text of the node
	private Integer depth; //node depth on the mind map
	private Integer noOfSiblings;
	private Integer noOfChildren;
	private Integer wordCount; //number of words on the node (including stopwords)
	private String pdfTitle;
	private ReferenceInfo reference;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
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

	public Integer getWordCount() {
		return wordCount;
	}

	public void setWordCount(Integer wordCount) {
		this.wordCount = wordCount;
	}

	public ReferenceInfo getReference() {
		return reference;
	}

	public void setReference(ReferenceInfo reference) {
		this.reference = reference;
	}

	public String getPdfTitle() {
		return pdfTitle;
	}

	public void setPdfTitle(String pdfTitle) {
		this.pdfTitle = pdfTitle;
	}
	
}
