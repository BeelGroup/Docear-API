package org.sciplore.deserialize.creator;

import java.util.Map;

import org.w3c.dom.Node;

public interface ResourceCreator {
	public Object createResource(Node element, Map<String, Object> children);
}
