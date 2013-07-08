package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.PersonXref;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PersonXrefCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		PersonXref xref = new PersonXref();
		NamedNodeMap attributes = element.getAttributes();
		for(int i=0; i < attributes.getLength(); i++ ) {
			Attr attribute = (Attr) attributes.item(i);
			if(attribute.getName().equals("source")) {
				xref.setSource(XmlUtils.getXmlString(attribute.getValue()));
			}
			else if(attribute.getName().equals("type")) {
				xref.setType(getTypeID(XmlUtils.getXmlString(attribute.getValue())));
			}
		}
		for(String key : children.keySet()) {
			if(key.equals("url")) {
				xref.setSourcesId(XmlUtils.getXmlString(children.get(key,0)));
			}			
		}
		
		return xref;
	}

	private Short getTypeID(String type) {
		
		if(type.equals("photo")) {
			return 1;
		}
		
		if(type.equals("profile")) {
			return 2;
		}

		if(type.equals("homepage")) {
			return 3;
		}

		if(type.equals("biography")) {
			return 4;
		}

		if(type.equals("article")) {
			return 5;
		}
		
		if(type.equals("publication")) {
			return 6;
		}
		
		return 0;
	}

}
