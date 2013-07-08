package org.sciplore.deserialize.reader;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.sciplore.data.MultiValueMap;
import org.sciplore.deserialize.creator.ObjectCreator;
import org.sciplore.deserialize.traversing.TreePath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ObjectReader {
	private final ObjectCreatorMapper mapper; 
	
	protected abstract Document getDom(InputStream stream);
	
	public ObjectReader(ObjectCreatorMapper creatorMapper) {
		if(creatorMapper == null) {
			throw new NullPointerException("argument creatorMapper must not be NULL.");
		}
		this.mapper = creatorMapper;		
	}
	
	public Object parse(Reader reader) {
		Document doc = getDom(new ReaderWrapperInputStream(reader));
		if(doc != null) {
			return decideReturnType(doc);
		}
		return null;
	}
	
	public Object parse(InputStream istream) {
		Document doc = getDom(istream);
		if(doc != null) {
			return decideReturnType(doc);
		}
		return null;
	}
	
	protected Object decideReturnType(Document document) {
		
		if(document.getChildNodes().getLength() > 1 && document.getFirstChild().getNodeName().equals(document.getFirstChild().getNextSibling().getNodeName())) {
			return createObjectList(document);
		} 
		else {
			return createObject(document.getDocumentElement(), null);
		}
	}
	
	private Object createObject(Node node, TreePath parent) {
		TreePath path = new TreePath(node.getNodeName(), parent);
		MultiValueMap<String, Object> childObjects = new MultiValueMap<String, Object>(); 
		NodeList nodes = node.getChildNodes();
		for(int i=0; i < nodes.getLength(); i++) {
			if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				childObjects.put(nodes.item(i).getNodeName(), createObject(nodes.item(i), path));
			}
		}
		return handleElement(path, node, childObjects);
	}

	private List<Object> createObjectList(Node node) {
		List<Object> objects = new ArrayList<Object>();  
		NodeList nodes = node.getChildNodes();
		for(int i=0; i < nodes.getLength(); i++) {
			if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				objects.add(createObject(nodes.item(i), null));
			}
		}
		return objects;
	}
	
	private Object handleElement(TreePath path, Node node, MultiValueMap<String, Object> children) {
		ObjectCreator creator = mapper.getCreator(node.getNodeName());
		if(creator != null) {	
			return creator.createResource(path, node, children);
		}
		return null;
	}
}
