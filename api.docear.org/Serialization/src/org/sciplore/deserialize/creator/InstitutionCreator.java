package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.Institution;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class InstitutionCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Institution institution = new Institution();
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("id") != null ) {
			institution.setId(XmlUtils.getXmlInteger(attributes.getNamedItem("id").getTextContent()));
		}
		
		for(String key : children.keySet()) {
			if(key.equals("name")) {
				institution.setName(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("url")) {
				institution.setUrl(XmlUtils.getXmlString(children.get(key,0)));
			}
		}
		return institution;
	}

}
