package xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Traverser {
	public enum TraversalMethod {
		BREADTH_FIRST, DEPTH_FIRST
	}

	private final TraversalMethod method;
	
	public Traverser(TraversalMethod method) {
		this.method = method;
	}
	
	abstract public boolean acceptElement(XmlElement element, XmlPath path);
	
	public Collection<XmlElement> traverse(XmlElement element) {
		List<XmlElement> acceptedElements = new ArrayList<XmlElement>();		
		traverse(element, null, acceptedElements);
		return acceptedElements;
	}
		
	private void traverse(XmlElement element, XmlPath path, Collection<XmlElement> acceptedElements) {
		int count = 0;
		if(this.method.equals(TraversalMethod.BREADTH_FIRST)) {
			for(XmlElement child : element.getChildren()) {
				if(acceptElement(child, new XmlPath(path, child.getName()+"["+count+"]"))) {
					acceptedElements.add(child);
				}
				count++;
			}
			count = 0;
			for(XmlElement child : element.getChildren()) {
				traverse(child, new XmlPath(path, child.getName()+"["+count+"]"), acceptedElements);
				count++;
			}			
		}
		else {
			for(XmlElement child : element.getChildren()) {
				XmlPath nPath = new XmlPath(path, child.getName()+"["+count+"]");
				if(acceptElement(child, nPath)) {
					acceptedElements.add(child);
				}
				traverse(child, nPath, acceptedElements);
				count++;
			}
		}
	}

}
