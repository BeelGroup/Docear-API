package org.sciplore.deserialize.creator;


import org.sciplore.beans.Document;
import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.formatter.Bean;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentBeanCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Document document = new Document();
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("href") != null) {
			document.addActiveAttribute("href", attributes.getNamedItem("href").getTextContent());
		}
		if(attributes.getNamedItem("id") != null) {
			document.addActiveAttribute("id", attributes.getNamedItem("id").getTextContent());
		}
		if(attributes.getNamedItem("hash") != null) {
			document.addActiveAttribute("hash", attributes.getNamedItem("hash").getTextContent());
		}
		if(attributes.getNamedItem("type") != null) {
			document.addActiveAttribute("type", attributes.getNamedItem("type").getTextContent());
		}
		
		
		for(String key : children.keySet()) {
			if(key.equals("year") && children.get(key) != null && children.get(key, 0) instanceof Bean) {
				document.addActiveElement((Bean)children.get(key,0));
			} 
			else
			if(key.equals("title") && children.get(key) != null && children.get(key, 0) instanceof Bean) {
				document.addActiveElement((Bean)children.get(key,0));
			}			 
			else
			if(key.equals("authors") && children.get(key) != null) {
				document.addActiveElement((Bean)children.get(key,0));
			}
		}
		return document;
	}

}
