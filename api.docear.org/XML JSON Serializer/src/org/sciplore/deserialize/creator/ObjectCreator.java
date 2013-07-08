package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.w3c.dom.Node;

public interface ObjectCreator {
	public Object createResource(Node element, MultiValueMap<String, Object> children);
}
