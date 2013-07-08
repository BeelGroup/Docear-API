package org.sciplore.deserialize.creator;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.Node;

public class ShortCreator implements ObjectCreator {

	@Override
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		try {
			return new Short(element.getTextContent());
		} 
		catch (Exception e) {
			return null;
		}
		
	}

}
