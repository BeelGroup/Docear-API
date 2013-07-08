package org.sciplore.deserialize.creator;

import java.util.ArrayList;
import java.util.List;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.Node;

public class DefaultListCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		List<Object> list = new ArrayList<Object>();
		for(String key : children.keySet()) {
			if(children.get(key) != null) {
				for(Object o : children.get(key)) {
					list.add(o);
				}
			}
		}
		return list;
	}

}
