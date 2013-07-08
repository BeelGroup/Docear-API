package org.sciplore.deserialize.creator;


import org.sciplore.beans.Author;
import org.sciplore.beans.Name_First;
import org.sciplore.beans.Name_Last;
import org.sciplore.beans.Name_Last_Prefix;
import org.sciplore.beans.Name_Last_Suffix;
import org.sciplore.beans.Name_Middle;
import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AuthorBeanCreator implements ObjectCreator {

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Author author = new Author();
		
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("href") != null) {
			author.addActiveAttribute("href", attributes.getNamedItem("href").getTextContent());
		}
		if(attributes.getNamedItem("id") != null) {
			author.addActiveAttribute("id", attributes.getNamedItem("id").getTextContent());
		}
		if(attributes.getNamedItem("rank") != null) {
			author.addActiveAttribute("rank", attributes.getNamedItem("rank").getTextContent());
		}
		Name_First nameFirst = new Name_First();
		Name_Middle nameMiddle = new Name_Middle();
		Name_Last nameLast = new Name_Last();
		Name_Last_Prefix nameLastPrefix = new Name_Last_Prefix();
		Name_Last_Suffix nameLastSuffix = new Name_Last_Suffix();
		for(String key : children.keySet()) {
			if(key.equals("name_first") && children.get(key) != null) {
				nameFirst.setValue(children.get(key,0).toString());
			}
			else
			if(key.equals("name_middle") && children.get(key) != null) {
				nameMiddle.setValue(children.get(key,0).toString());
			}
			else
			if(key.equals("name_last") && children.get(key) != null) {
				nameLast.setValue(children.get(key,0).toString());
			}
			else
			if(key.equals("name_last_prefix") && children.get(key) != null) {
				nameLastPrefix.setValue(children.get(key,0).toString());
			}
			else
			if(key.equals("name_last_suffix") && children.get(key) != null) {
				nameLastSuffix.setValue(children.get(key,0).toString());
			}
		}
		
		author.addActiveElement(nameFirst);
		author.addActiveElement(nameMiddle);
		author.addActiveElement(nameLastPrefix);
		author.addActiveElement(nameLast);		
		author.addActiveElement(nameLastSuffix);
		return author;
	}

}
