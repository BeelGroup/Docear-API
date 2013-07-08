package org.sciplore.deserialize.reader;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.creator.ObjectCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ObjectReader {
	private final ObjectCreatorMapper mapper; 
	
	protected abstract Document getDOM(Reader reader);
	
	public ObjectReader(ObjectCreatorMapper creatorMapper) {
		if(creatorMapper == null) {
			throw new NullPointerException("argument creatorMapper must not be NULL.");
		}
		this.mapper = creatorMapper;		
	}
	
	public Object parse(Reader reader) {
		Document doc = getDOM(reader);
		if(doc != null) {
			return decideReturnType(doc);
		}
		return null;
	}
	
	private Object decideReturnType(Document document) {
		
		if(document.getChildNodes().getLength() > 1 && document.getFirstChild().getNodeName().equals(document.getFirstChild().getNextSibling().getNodeName())) {
			return createObjectList(document);
		} 
		else {
			return createObject(document.getDocumentElement());
		}
	}
	
	private Object createObject(Node node) {
		MultiValueMap<String, Object> childObjects = new MultiValueMap<String, Object>(); 
		NodeList nodes = node.getChildNodes();
		for(int i=0; i < nodes.getLength(); i++) {
			if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				childObjects.put(nodes.item(i).getNodeName(), createObject(nodes.item(i)));
			}
		}
		return handleElement(node, childObjects);
	}

	private List<Object> createObjectList(Node node) {
		List<Object> objects = new ArrayList<Object>();  
		NodeList nodes = node.getChildNodes();
		for(int i=0; i < nodes.getLength(); i++) {
			if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				objects.add(createObject(nodes.item(i)));
			}
		}
		return objects;
	}
	
	private Object handleElement(Node node, MultiValueMap<String, Object> children) {
		ObjectCreator creator = mapper.getCreator(node.getNodeName());
		if(creator != null) {
			return creator.createResource(node, children);
		}
		return null;
	}
}
