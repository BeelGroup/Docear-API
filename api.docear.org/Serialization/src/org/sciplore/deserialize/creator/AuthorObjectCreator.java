package org.sciplore.deserialize.creator;


import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.Person;
import org.sciplore.resources.PersonHomonym;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AuthorObjectCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		if(path.hasAncestor("document")) {
			DocumentPerson docPerson = new  DocumentPerson();
			docPerson.setPersonHomonym(getPersonHomonym(element, children));
			if(docPerson.getPersonHomonym() != null) {
				docPerson.setPersonMain(docPerson.getPersonHomonym().getPerson());
				NamedNodeMap attributes = element.getAttributes();
				Attr rank = (Attr) attributes.getNamedItem("rank");
				if(rank != null) {
					docPerson.setRank(XmlUtils.getXmlShort(rank.getValue().trim()));
				}
				return docPerson;
			}
			return null;
		} else {
			return getPersonHomonym(element, children);
		}
	}

	private PersonHomonym getPersonHomonym(Node element, MultiValueMap<String, Object> children) {
		PersonHomonym homonym = new PersonHomonym();
		NamedNodeMap attributes = element.getAttributes();
		for(int i=0; i < attributes.getLength(); i++ ) {
			Attr attribute = (Attr) attributes.item(i);
			if(attribute.getName().equals("id") && !attribute.getValue().trim().isEmpty()) {
				homonym.setId(XmlUtils.getXmlInteger(attribute.getValue().trim()));
			}
		}
		for(String key : children.keySet()) {
			if(key.equals("name_first")) {
				homonym.setNameFirst(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_middle")) {
				homonym.setNameMiddle(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_last")) {
				homonym.setNameLast(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_last_prefix")) {
				homonym.setNameLastPrefix(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_last_suffix")) {
				homonym.setNameLastSuffix(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("person") && children.get(key,0) != null && children.get(key,0) instanceof Person) {
				homonym.setPerson((Person) children.get(key,0));
			}
		}
		if(homonym.getPerson() == null) {
			homonym.setPerson((Person) new PersonCreator().createResource(null, element, children));
		}
		
		homonym.setValid((short)1);
		return homonym;
	}
	

}
