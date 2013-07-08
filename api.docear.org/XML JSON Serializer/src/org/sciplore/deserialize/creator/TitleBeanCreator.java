package org.sciplore.deserialize.creator;


import org.sciplore.beans.Title;
import org.sciplore.data.MultiValueMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class TitleBeanCreator implements ObjectCreator {

	@Override
	public Object createResource(Node element, MultiValueMap<String, Object> children) {
		Title title = new Title();
		NamedNodeMap attributes = element.getAttributes();
		if(attributes.getNamedItem("href") != null) {
			title.addActiveAttribute("href", attributes.getNamedItem("href").getTextContent());
		}
		System.out.println(element.getTextContent());
		title.setValue(element.getTextContent());
		
		return title;
	}

}
