package org.docear.parser.handler;

import java.util.Map;

import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.graphdb.threading.IGraphModifierStep;
import org.docear.graphdb.threading.IGraphStep;
import org.docear.nanoxml.IElementHandler;
import org.neo4j.graphdb.GraphDatabaseService;



public class StyleNodeElementHandler implements IElementHandler {

	private final GraphCreatorJob job;
	private IGraphStep lastStep;	
	
	public StyleNodeElementHandler(GraphCreatorJob job) {
		this.job = job;
	}

	public void addContent(char[] buffer) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void endElement(String name) throws Exception {
		// TODO Auto-generated method stub

	}

	public String forElement() {
		return "stylenode";

	}

	public Object startElement(String name, final Map<String, String> attributes, final Object parent) throws Exception {
		lastStep = new IGraphModifierStep() {
			
			public void run(GraphDatabaseService graphDb) {
				StylesManager sm = (StylesManager) job.getFromContext(GraphCreatorJob.CONTEXT_STYLE_MAP);
				sm.addAttributes(attributes);
			}
			
			public void addProperty(String key, String value) {
				if(UnknownHandler.UNKNOWN_CONTENT_KEY.equals(key)) {
					return;
				}
				attributes.put(key, value);
			}
		};
		job.prependStep(lastStep);
		return lastStep;

	}

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasContentHandler() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IElementHandler getContentHandler() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
