package org.sciplore.deserialize.creator;


import java.util.List;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.reader.XmlUtils;
import org.sciplore.deserialize.traversing.TreePath;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentXrefCategory;
import org.sciplore.resources.Institution;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentXrefCreator implements ObjectCreator {

	@Override
	public Object createResource(TreePath path, Node element, MultiValueMap<String, Object> children) {
		DocumentXref xref = new DocumentXref();
		NamedNodeMap attributes = element.getAttributes();
		for(int i=0; i < attributes.getLength(); i++ ) {
			Attr attribute = (Attr) attributes.item(i);
			if(attribute.getName().equals("source")) {
				xref.setSource(XmlUtils.getXmlString(attribute.getValue()));
			}
		}
		
		for(int i=0; i < attributes.getLength(); i++ ) {
			Attr attribute = (Attr) attributes.item(i);
			if(attribute.getName().equals("id") && !attribute.getValue().trim().isEmpty()) {
				xref.setId(XmlUtils.getXmlInteger(attribute.getValue()));
			}
		}	
		
		for(String key : children.keySet()) {
			if(key.equals("organization") && children.get(key,0) != null && children.get(key,0) instanceof Institution) {
				xref.setInstitution((Institution) children.get(key,0));
			}
			else
				if(key.equals("sourceid") && children.get(key,0) != null) {
					xref.setSourcesId(children.get(key,0).toString());
				}
				else
					if(key.equals("url") && children.get(key,0) != null) {
						String url = XmlUtils.getXmlString(children.get(key,0));
						if(xref.getSourcesId() != null && url != null) {
							System.out.println(xref.getSourcesId());
							int lastIndex = url.lastIndexOf(xref.getSourcesId());
							if(lastIndex > -1) {
								xref.getInstitution().setXrefBaseUrl(url.substring(0, lastIndex));
							}
						}
						//xref.setSource(children.get(key,0).toString());
					}
					else
						if(key.equals("releasedate")) {
							xref.setReleaseDate(XmlUtils.getXmlDate(children.get(key,0).toString(), "yyyy-MM-dd HH:mm:ss"));
						}
						else
							if(key.equals("categories") && children.get(key,0) != null) {
								if(children.get(key,0) instanceof List) {
									List<?> list = (List<?>) children.get(key,0);
									for(Object ref : list) {
										if(ref != null && ref instanceof DocumentXrefCategory) {
											((DocumentXrefCategory) ref).setXref(xref);
											xref.getDocumentXrefCategories().add((DocumentXrefCategory) ref);
										}
									}
								}
							}

		}

		return xref;
	}

}
