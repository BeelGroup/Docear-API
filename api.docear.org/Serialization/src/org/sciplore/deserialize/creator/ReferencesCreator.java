package org.sciplore.deserialize.creator;

import java.util.ArrayList;
import java.util.List;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.Citation;
import org.w3c.dom.Node;

public class ReferencesCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		List<Object> list = new ArrayList<Object>();
		for(String key : children.keySet()) {
			if((children.get(key) != null)) {
				for(Object obj : children.get(key)) {
					if(obj != null && obj instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<Citation> citations = (List<Citation>)obj;
						for(Citation citation : citations) {
							list.add(citation);							
						}
					}					
				}
			}
		}
		return list;
	}

}
