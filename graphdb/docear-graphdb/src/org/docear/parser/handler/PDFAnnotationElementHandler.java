package org.docear.parser.handler;

import java.util.HashMap;
import java.util.Map;

import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.graphdb.threading.IGraphModifierStep;
import org.docear.graphdb.threading.IGraphNodeCreator;
import org.docear.graphdb.threading.IGraphStep;
import org.docear.nanoxml.IElementHandler;
import org.docear.query.HashReferenceItem;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;



public class PDFAnnotationElementHandler implements IElementHandler {

	private final GraphCreatorJob job;
	private IGraphStep lastStep;
	private IElementHandler internalHandler;
	
	public PDFAnnotationElementHandler(GraphCreatorJob job) {
		this.job = job;
	}

	public void addContent(char[] buffer) throws Exception {
		
	}

	@Override
	public void endElement(String name) throws Exception {
	
	}

	public String forElement() {
		return "pdf_annotation";

	}

	public Object startElement(String name, final Map<String, String> attributes, final Object parent) throws Exception {
		lastStep = new IGraphModifierStep() {
			
			public void run(GraphDatabaseService graphDb) {
				if (parent != null && parent instanceof IGraphNodeCreator) {
					Node node = ((IGraphNodeCreator) parent).getNode();
					if (node != null) {
						addProperties(node, attributes);
					}
				}
			}
			
			protected void addProperties(Node n, Map<String, String> props) {
							
				@SuppressWarnings("unchecked")
				Map<String, HashReferenceItem> hashOccurences = getOrCreateHashOccurrenceMap();
				for (String key : props.keySet()) {					
					n.setProperty("annotation_"+key, props.get(key));
					if("document_hash".equals(key)) {
						increaseHashOccurence(props.get(key).toString(), hashOccurences);
					}
				}
			}

			/**
			 * @param hash 
			 * @param hashOccurences 
			 * 
			 */
			private void increaseHashOccurence(String hash, Map<String, HashReferenceItem> hashOccurences) {
				
				HashReferenceItem currentHashItem = hashOccurences.get(hash);
				
				if(currentHashItem == null) {
					currentHashItem = new HashReferenceItem(hash);
					hashOccurences.put(hash, currentHashItem);
				}
				currentHashItem.touch();
			}
			
			public void addProperty(String key, String value) {
				if("pdf_title".equals(key)) {
					attributes.put(key, value);
					//add title to hash-title-map for later upload to the web service 
					String hash = attributes.get("document_hash");
					if(hash != null) {
						Map<String, HashReferenceItem> hashOccurrences = getOrCreateHashOccurrenceMap();
						
						HashReferenceItem currentHashItem = hashOccurrences.get(hash);
						if(currentHashItem == null) {
							currentHashItem = new HashReferenceItem(hash);
							hashOccurrences.put(hash, currentHashItem);
						}
						currentHashItem.setTitle(value, true);
					}
					
				}				
			}

			/**
			 * @return
			 */
			private Map<String, HashReferenceItem> getOrCreateHashOccurrenceMap() {
				@SuppressWarnings("unchecked")
				Map<String, HashReferenceItem> hashOccurrences = (Map<String, HashReferenceItem>)job.getFromContext("hashOccurences");
				if(hashOccurrences == null) {
					hashOccurrences = new HashMap<String, HashReferenceItem>();
					job.setContext("hashOccurences", hashOccurrences);
				}
				return hashOccurrences;
			}
		};
		internalHandler = new PdfTitleContentHandler(lastStep);
		job.appendStep(lastStep);
		return parent;

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
	
	class PdfTitleContentHandler implements IElementHandler {
		private final IGraphStep targetStep;
		private String current_key = null;
		
		public PdfTitleContentHandler(IGraphStep step) {
			targetStep = step;
		}

		public Object startElement(final String name, final Map<String, String> attributes, final Object parent) throws Exception {
			current_key = name;
			return null;
		}

		public void endElement(final String name) throws Exception {
			
		}

		public void addContent(final char[] buffer) throws Exception {
			appendContent(new String(buffer));
		}

		private void appendContent(final String content) {
			if (targetStep != null) {
				targetStep.addProperty(current_key, content);
			}
		}

		@Override
		public void initialize() throws Exception {
			
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
