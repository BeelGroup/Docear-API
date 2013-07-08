package org.sciplore.deserialize.creator;


import org.sciplore.beans.Documents;
import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.formatter.Bean;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentsBeanCreator implements ObjectCreator {

	@Override
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Documents documents = new Documents();
		
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("href") != null) {
			documents.addActiveAttribute("href", attributes.getNamedItem("href").getTextContent());
		}
		if(attributes.getNamedItem("totalamount") != null) {
			documents.addActiveAttribute("totalamount", attributes.getNamedItem("totalamount").getTextContent());
		}
		
		for(String key : children.keySet()) {
			if(key.equals("document") && children.get(key) != null) {
				for(Object b : children.get(key)) {
					if(b instanceof Bean) {
						documents.add((Bean)b);
					}
				}
			}
		}
		return documents;
	}

}
