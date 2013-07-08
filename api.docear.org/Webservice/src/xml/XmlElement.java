package xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xml.Traverser.TraversalMethod;

public class XmlElement {
	private final String name;
	private String content;
	
	private final Map<String, String> attributes = new HashMap<String, String>();
	private final List<XmlElement> children = new ArrayList<XmlElement>();
	private XmlElement parent;
	
	public XmlElement(String name) {
		this.name = name;
	}
	
	public void addChild(XmlElement child) {
		if (child != null) {
			children.add(child);
			child.setParent(this);
		}
	}
	
	private void setParent(XmlElement element) {
		this.parent = element;
	}
	
	public XmlElement getParent() {
		return parent;
	}

	public void removeChild(XmlElement child) {
		if(child != null) {
			children.remove(child);
		}
	}
	
	public List<XmlElement> getChildren() {
		return children;
	}
	
	public boolean hasParent(String elementName) {
		if(elementName == null || getParent() == null) {
			return false;
		}
		if(elementName.equals(getParent().getName())) {
			return true;
		}
		return getParent().hasParent(elementName);
	}
	
	public boolean hasChildren() {
		return childCount() > 0;
	}
	
	public int childCount() {
		return children.size();
	}
	
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}
	
	public String getAttributeValue(String name) {
		return attributes.get(name);
	}
	
	public boolean hasAttributes() {
		return attributes.size() > 0;
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
		
	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String toString() {
		return "["+name+":children="+childCount()+";attr=]";
	}
	
	public Collection<XmlElement> findAll(final String elementName) {
		return new  Traverser(TraversalMethod.DEPTH_FIRST) {
			public boolean acceptElement(XmlElement element, XmlPath path) {
				if(element.getName().equals(elementName)) {
					return true;
				}
				return false;
			}
		}.traverse(this);
		
	}

	public XmlElement find(final String elementName) {
		Collection<XmlElement> results = new  Traverser(TraversalMethod.DEPTH_FIRST) {
			public boolean acceptElement(XmlElement element, XmlPath path) {
				if(element.getName().equals(elementName)) {
					return true;
				}
				return false;
			}
		}.traverse(this);
		
		if(results.size() == 0) {
			return null;
		}
		return results.iterator().next();
	}

}
