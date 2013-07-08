package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.Node;

public class DefaultStringCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		return element.getTextContent();
	}

}
