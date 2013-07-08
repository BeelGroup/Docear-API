package org.docear.parser.handler;

import java.util.Map;

import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.graphdb.threading.IGraphModifierStep;
import org.docear.graphdb.threading.IGraphNodeCreator;
import org.docear.graphdb.threading.IGraphStep;
import org.docear.nanoxml.IElementHandler;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;


public class RichcontentElementHandler implements IElementHandler {

	private final GraphCreatorJob job;
	private IGraphStep lastStep;
	private IElementHandler internalHandler;

	public RichcontentElementHandler(GraphCreatorJob job) {
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
		return "richcontent";

	}

	@Override
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
				if (props.get("RichContent") != null) {
					node.setProperty(props.get("TYPE"), props.get("RichContent"));
				}
			}

			public void addProperty(String key, String value) {
				if (attributes.containsKey("RichContent")) {
					attributes.put("RichContent", attributes.get("RichContent") + value);
				} else {
					attributes.put("RichContent", value);
				}
			}

		};
		internalHandler = new InternalContentHandler(lastStep);
		job.appendStep(lastStep);
		return lastStep;
	}

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasContentHandler() {
		return true;
	}

	@Override
	public IElementHandler getContentHandler() throws Exception {
		return internalHandler;
	}

	class InternalContentHandler implements IElementHandler {
		private final IGraphStep targetStep;

		public InternalContentHandler(IGraphStep step) {
			targetStep = step;
		}

		public Object startElement(final String name, final Map<String, String> attributes, final Object parent) throws Exception {
			appendContent("<" + name + " ");
			for (String key : attributes.keySet()) {
				addAttribute(key, attributes.get(key));
			}
			appendContent(">");
			return null;
		}

		public void endElement(final String name) throws Exception {
			appendContent("</" + name + ">");
		}

		private void addAttribute(final String key, final Object value) throws Exception {
			appendContent(key + "=\"" + value + "\" ");
		}

		public void addContent(final char[] buffer) throws Exception {
			appendContent(new String(buffer));
		}

		private void appendContent(final String content) {
			if (targetStep != null) {
				targetStep.addProperty("", content);
			}
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

		@Override
		public String forElement() {
			return "";
		}
	}

}
