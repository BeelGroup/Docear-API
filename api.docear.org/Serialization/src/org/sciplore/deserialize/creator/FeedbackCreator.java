package org.sciplore.deserialize.creator;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.Feedback;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FeedbackCreator implements ObjectCreator {
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Feedback feedback = new Feedback();
		NamedNodeMap attributes = element.getAttributes();		
		for(int i=0; i < attributes.getLength(); i++) {
			Attr attribute = (Attr)attributes.item(i);
			if(attribute.getName().equals("created")) {
				try {
					feedback.setCreated(df.parse(attribute.getValue()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			feedback.setText(element.getTextContent());
		}
		return feedback;
	}

}
