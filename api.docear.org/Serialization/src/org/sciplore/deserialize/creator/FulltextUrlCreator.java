package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.FulltextUrl;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FulltextUrlCreator implements ObjectCreator {

	@Override
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		FulltextUrl fulltextUrl = new  FulltextUrl();
		NamedNodeMap attributes = element.getAttributes();
		for(int i=0; i < attributes.getLength(); i++) {
			Attr attribute = (Attr)attributes.item(i);
			if(attribute.getName().equals("licence")) {
				fulltextUrl.setLicence(Short.valueOf(attribute.getValue()));
			}
		}
		fulltextUrl.setFiletype((short)1);
		fulltextUrl.setUrl(element.getTextContent());
		fulltextUrl.setValid((short) 1);
		return fulltextUrl;
	}

}
