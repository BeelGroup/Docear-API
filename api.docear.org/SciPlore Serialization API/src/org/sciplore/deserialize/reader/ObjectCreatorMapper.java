package org.sciplore.deserialize.reader;

import java.util.LinkedHashMap;

import org.sciplore.deserialize.creator.ObjectCreator;

public class ObjectCreatorMapper {
	private final LinkedHashMap<String, ObjectCreator> mapper = new LinkedHashMap<String, ObjectCreator>();
	
	public void addCreator(String nodeName, ObjectCreator creator) {
		mapper.put(nodeName, creator);
	}
	
	public ObjectCreator getCreator(String nodeName) {
		return mapper.get(nodeName);
	}
	
}
