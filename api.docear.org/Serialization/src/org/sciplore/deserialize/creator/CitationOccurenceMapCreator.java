package org.sciplore.deserialize.creator;

import java.util.HashMap;
import java.util.Map;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CitationOccurenceMapCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Map<String, Object> map = new HashMap<String, Object>();
		NamedNodeMap attributes = element.getAttributes();
		for(int i=0; i < attributes.getLength(); i++ ) {
			Attr attribute = (Attr) attributes.item(i);
			if(attribute.getName().equals("character") && !attribute.getValue().trim().isEmpty()) {
				map.put(attribute.getName(), Integer.parseInt(attribute.getValue()));
			}
			else if(attribute.getName().equals("word") && !attribute.getValue().trim().isEmpty()) {
				map.put(attribute.getName(), Integer.parseInt(attribute.getValue()));
			}
			else if(attribute.getName().equals("sentence") && !attribute.getValue().trim().isEmpty()) {
				map.put(attribute.getName(), Integer.parseInt(attribute.getValue()));
			}
			else if(attribute.getName().equals("paragraph") && !attribute.getValue().trim().isEmpty()) {
				map.put(attribute.getName(), Integer.parseInt(attribute.getValue()));
			}
			else if(attribute.getName().equals("chapter") && !attribute.getValue().trim().isEmpty()) {
				map.put(attribute.getName(), Integer.parseInt(attribute.getValue()));
			}
		}
		if(XmlUtils.getXmlString(element.getTextContent()) != null) {
			map.put("context", XmlUtils.getXmlString(element.getTextContent()));
		}
		return map;
	}

}
