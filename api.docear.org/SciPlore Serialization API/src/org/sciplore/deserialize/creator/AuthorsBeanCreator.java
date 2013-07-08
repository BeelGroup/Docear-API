package org.sciplore.deserialize.creator;


import org.sciplore.beans.Author;
import org.sciplore.beans.Authors;
import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.formatter.Bean;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AuthorsBeanCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Authors authors = new Authors();
		
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("href") != null) {
			authors.addActiveAttribute("href", attributes.getNamedItem("href").getTextContent());
		}
		
		
		for(String key : children.keySet()) {
			if(key.equals("author") && children.get(key) != null) {
				for(Object b : children.get(key)) {
					if(b instanceof Author) {
						authors.add((Bean)b);
					}
				}
			}
		}
		return authors;
	}

}
