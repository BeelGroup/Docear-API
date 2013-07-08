package org.sciplore.deserialize.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.Citation;
import org.sciplore.resources.Document;
import org.w3c.dom.Node;

public class CitationCreator implements ObjectCreator {

	@SuppressWarnings("unchecked")
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		List<Citation> citations = new ArrayList<Citation>();
//		NamedNodeMap attributes = element.getAttributes();
//		for(int i=0; i < attributes.getLength(); i++ ) {
//			Attr attribute = (Attr) attributes.item(i);
//			if(attribute.getName().equals("id") && !attribute.getValue().trim().isEmpty()) {
//				citation.setId(Integer.parseInt(attribute.getValue()));
//			}
//		}
		Document citedDocument = (Document) children.get("document", 0);
		if(citedDocument == null) {
			return null;
		}
		if(children.get("occurences",0) != null) {			
			for(Object occurence : (List<Object>)children.get("occurences",0)) {
				if(occurence != null && occurence instanceof Map) {
					Map<String, Object> map = (Map<String, Object>) occurence;
					Citation citation = new Citation();
					for(String occurenceKey : map.keySet()) {
						if(occurenceKey.equals("character")) {
							citation.setCountCharacter((Integer) map.get(occurenceKey));
						}
						else if(occurenceKey.equals("word")) {
							citation.setCountWord((Integer) map.get(occurenceKey));
						}
						else if(occurenceKey.equals("sentence")) {
							citation.setCountSentence((Integer) map.get(occurenceKey));
						}
						else if(occurenceKey.equals("paragraph")) {
							citation.setCountParagraph((Integer) map.get(occurenceKey));
						}
						else if(occurenceKey.equals("chapter")) {
							citation.setCountChapter((Integer) map.get(occurenceKey));
						}
						else if(occurenceKey.equals("context")) {
							citation.setContext((String) map.get(occurenceKey));
						}
					}
					citation.setCitedDocument(citedDocument);
					citations.add(citation);
				}
			}
		}	
		return citations;
	}

}
