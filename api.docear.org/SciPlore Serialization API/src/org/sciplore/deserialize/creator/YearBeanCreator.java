package org.sciplore.deserialize.creator;


import org.sciplore.beans.Year;
import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class YearBeanCreator implements ObjectCreator {

	
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Year year = new Year();
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("href") != null) {
			year.addActiveAttribute("href", attributes.getNamedItem("href").getTextContent());
		}
		
		year.setValue(element.getTextContent());
		return year;
	}

}
