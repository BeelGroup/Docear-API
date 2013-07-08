package org.sciplore.deserialize.creator;


import java.util.List;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.Citation;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.FulltextUrl;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentCreator implements ObjectCreator {

	@Override
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		Document document = new Document();
		NamedNodeMap attributes = element.getAttributes();
		for(int i=0; i < attributes.getLength(); i++ ) {
			Attr attribute = (Attr) attributes.item(i);
			if(attribute.getName().equals("id") && !attribute.getValue().trim().isEmpty()) {
				document.setId(XmlUtils.getXmlInteger(attribute.getValue()));
			}
		}	
		
		for(String key : children.keySet()) {
			if(key.equals("title") && children.get(key,0) != null) {
				document.setTitle(children.get(key,0).toString());
			}
			else
			if(key.equals("year") && children.get(key,0) != null) {
				document.setPublishedYear((Short) children.get(key,0));
			}
			else
			if(key.equals("doi") && children.get(key,0) != null) {
				document.setDoi(children.get(key,0).toString());
			}
			else
			if(key.equals("abstract") && children.get(key,0) != null) {
				document.setAbstract(children.get(key,0).toString());
			}
			else
			if(key.equals("authors") && children.get(key,0) != null) {
				if(children.get(key,0) instanceof List) {
					List<?> list = (List<?>) children.get(key,0);
					for(Object ref : list) {
						if(ref != null && ref instanceof DocumentPerson) {
							DocumentPerson docPerson = (DocumentPerson)ref;
							docPerson.setDocument(document);
							document.addPerson(docPerson);
						}
					}
				}
			}
			else
			if(key.equals("references") && children.get(key,0) != null) {
				if(children.get(key,0) instanceof List) {
					List<?> list = (List<?>) children.get(key,0);
					for(Object ref : list) {
						if(ref != null && ref instanceof Citation) {							
							Citation citation = (Citation)ref;
							citation.setCitingDocument(document);
							document.addCitation(citation);
						}
					}
				}
			}
			else
			if(key.equals("xrefs") && children.get(key,0) != null) {
				if(children.get(key,0) instanceof List) {
					List<?> list = (List<?>) children.get(key,0);
					for(Object ref : list) {
						if(ref != null && ref instanceof DocumentXref) {
							((DocumentXref) ref).setDocument(document);
							document.addXref((DocumentXref) ref);
						}
					}
				}
			}
			else
			if(key.equals("fulltexts") && children.get(key,0) != null) {
				if(children.get(key,0) instanceof List) {
					List<?> list = (List<?>) children.get(key,0);
					for(Object ref : list) {
						if(ref != null && ref instanceof FulltextUrl) {
							FulltextUrl ftUrl = (FulltextUrl)ref;
							ftUrl.setDocument(document);
							document.addFulltextUrl(ftUrl);
						}
					}
				}
			}
//			
//			else
			
//			if(key.equals("comments") && children.get(key,0) != null) {
//				if(children.get(key,0) instanceof List) {
//					List<?> list = (List<?>) children.get(key,0);
//					for(Object ref : list) {
//						if(ref != null && ref instanceof Feedback) {
//							document.addFeedback((Feedback)ref);
//						}
//					}
//				}
//			}

//			else
//			if(key.equals("documents") && children.get(key,0) != null) {
//				if(children.get(key,0) instanceof List) {
//					List<?> list = (List<?>) children.get(key,0);
//					for(Object ref : list) {
//						if(ref != null && ref instanceof PersonXref) {
////							person.addDocument(ref);
//						}
//					}
//				}
//			}
			
			
			
		}
		if(document.getTitle() == null || document.getTitle().trim().isEmpty()) {
			return null;			
		}
		return document;
	}

}
