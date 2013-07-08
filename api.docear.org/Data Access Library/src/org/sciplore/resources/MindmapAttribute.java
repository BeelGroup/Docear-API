package org.sciplore.resources;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * This class represents an attribute of a mind map node
 */
@Entity
@Table(name = "mindmap_attributes")
public class MindmapAttribute extends Resource{
	
	
	@ManyToOne
	@JoinColumn(name = "node_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private MindmapNode mindmapNode;
	
	private String attributeName;
	private String attributeValue;	
	private String elementName;	
	private String elementContent;
	
	public MindmapAttribute(){}
	
	public MindmapAttribute(Session s){
		this.setSession(s);
	}
	
	public void create(MindmapNode node,
					  String attributeName,
					  String attributeValue,
					  String elementName,
					  String elementContent){
		
		this.setMindmapNode(node);
		this.setAttributeName(attributeName);
		this.setAttributeValue(attributeValue);
		this.setElementName(elementName);
		this.setElementContent(elementContent);
		this.save();	
	}
	
	public Resource getPersistentIdentity() {
		//TODO check for identity
		return null;
	}
	
	public MindmapNode getMindmapNode() {
		return mindmapNode;
	}
	public void setMindmapNode(MindmapNode mindmapNode) {
		this.mindmapNode = mindmapNode;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	public String getElementName() {
		return elementName;
	}
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	public String getElementContent() {
		return elementContent;
	}
	public void setElementContent(String elementContent) {
		this.elementContent = elementContent;
	}
}
