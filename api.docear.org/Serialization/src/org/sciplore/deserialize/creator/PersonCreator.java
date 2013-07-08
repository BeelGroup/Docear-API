package org.sciplore.deserialize.creator;

import java.util.List;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.Person;
import org.sciplore.resources.PersonHomonym;
import org.sciplore.resources.PersonXref;
import org.sciplore.resources.Resource;
import org.w3c.dom.Node;

public class PersonCreator implements ObjectCreator {

	public Resource createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Person person = new Person(); 
		for(String key : children.keySet()) {
			if(key.equals("name_first")) {
				person.setNameFirst(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_middle")) {
				person.setNameMiddle(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_last")) {
				person.setNameLast(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_last_prefix")) {
				person.setNameLastPrefix(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("name_last_suffix")) {
				person.setNameLastSuffix(XmlUtils.getXmlString(children.get(key,0)));
			}
			else
			if(key.equals("authorxrefs") && children.get(key,0) != null) {
				if(children.get(key,0) instanceof List) {
					List<?> list = (List<?>) children.get(key,0);
					for(Object ref : list) {
						if(ref != null && ref instanceof PersonXref) {
							PersonXref xref = (PersonXref)ref;
							xref.setPerson(person);
							person.addXref(xref);
						}
					}
				}
			}
			else
			if(key.equals("homonyms") && children.get(key,0) != null) {
				if(children.get(key,0) instanceof List) {
					List<?> list = (List<?>) children.get(key,0);
					for(Object ref : list) {
						if(ref != null && ref instanceof PersonHomonym) {
							person.addHomonym((PersonHomonym) ref);
						}
					}
				}
			}
		}
		person.setValid((short)1);
		return person;
	}

}
