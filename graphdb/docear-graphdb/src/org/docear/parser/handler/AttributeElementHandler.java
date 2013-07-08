package org.docear.parser.handler;

import java.util.Map;

import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.graphdb.threading.IGraphModifierStep;
import org.docear.graphdb.threading.IGraphNodeCreator;
import org.docear.graphdb.threading.IGraphStep;
import org.docear.nanoxml.IElementHandler;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;



public class AttributeElementHandler implements IElementHandler {

	private final GraphCreatorJob job;
	private IGraphStep lastStep;

	public AttributeElementHandler(GraphCreatorJob job) {
		this.job = job;
	}

	public void addContent(char[] buffer) throws Exception {
	}

	@Override
	public void endElement(String name) throws Exception {
	}

	public String forElement() {
		return "attribute";

	}

	public Object startElement(String name, final Map<String, String> attributes, final Object parent) throws Exception {
		lastStep = new IGraphModifierStep() {
			private final Object parentStep = parent;

			public void run(GraphDatabaseService graphDb) {
				if (parentStep != null && parentStep instanceof IGraphNodeCreator) {
					Node node = ((IGraphNodeCreator) parentStep).getNode();
					if (node != null) {
						addProperties(node, attributes);
					}
				}
			}

			protected void addProperties(Node node, Map<String, String> props) {
				node.setProperty("attribute_" + props.get("NAME"), props.get("VALUE"));				
			}
			
			public void addProperty(String key, String value) {				
			}

		};
		job.appendStep(lastStep);
		return lastStep;
	}

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasContentHandler() {
		return false;
	}

	@Override
	public IElementHandler getContentHandler() throws Exception {
		return null;
	}

}
