package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.DocumentXrefCategory;
import org.sciplore.resources.Institution;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentXrefCategoryCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		DocumentXrefCategory xrefCategory = new DocumentXrefCategory();
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("type") != null ) {
			xrefCategory.setType(XmlUtils.getXmlShort(attributes.getNamedItem("type").getTextContent()));
		}
		for(String key : children.keySet()) {
			if(key.equals("id")) {
				xrefCategory.setCategory(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("organization") && children.get(key,0) != null) {
				xrefCategory.setSource((Institution) children.get(key,0));
			}
		}
		return xrefCategory;
	}

}
