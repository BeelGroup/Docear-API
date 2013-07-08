package org.sciplore.resources;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * This class represents a mind map node
 */
@Entity
@Table(name = "mindmap_nodes")
public class MindmapNode extends Resource{
	
	
	
	@ManyToOne
	@JoinColumn(name = "mindmaprevision_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Mindmap mindmap;
	
	@Column(name = "node_id")
	private String nodeID;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	private MindmapNode parent;
	
	@OneToMany(mappedBy = "parent")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Feedback> children = new HashSet<Feedback>();
	
	private Date created;
	private Date modified;
	private String text;
	
	public MindmapNode(){}
	
	public MindmapNode(Session s){
		this.setSession(s);
	}
	
	public void create(Mindmap mindmap,
					  String nodeID,
					  MindmapNode parent,
					  Date created,
					  Date modified,
					  String text){
		
		this.setMindmap(mindmap);
		this.setNodeID(nodeID);
		this.setParent(parent);
		this.setCreated(created);
		this.setModified(modified);
		this.setText(text);
		this.save();
	}
	
	
	public String getNodeID() {
		return nodeID;
	}
	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	public MindmapNode getParent() {
		return parent;
	}
	public void setParent(MindmapNode parent) {
		this.parent = parent;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Mindmap getMindmap() {
		return mindmap;
	}
	public void setMindmap(Mindmap mindmap) {
		this.mindmap = mindmap;
	}
	public Set<Feedback> getChildren() {
		return children;
	}
	public void setChildren(Set<Feedback> children) {
		this.children = children;
	}
	
	public Resource getPersistentIdentity() {
		//TODO check for identity
		return null;
	}

}
