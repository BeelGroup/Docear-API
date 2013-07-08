package org.docear.parser.handler;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.docear.graphdb.relationship.StyleRelationship;
import org.docear.graphdb.relationship.Type;
import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.graphdb.threading.IGraphCreatorStep;
import org.docear.nanoxml.IElementHandler;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;


public class NodeElementHandler implements IElementHandler {
	private IGraphCreatorStep lastStep;
	private final GraphCreatorJob job;
//	private HashMap<String, Object> properties = new HashMap<String, Object>();
//	private HashMap<Object, Object> propertiesMap = new HashMap<Object, Object>();
	private String DCR_MAP_ID;
	private Map<String, Node> NODES;

	public NodeElementHandler(GraphCreatorJob job) {
		this.job = job;
	}	

	@Override
	public void addContent(char[] buffer) throws Exception {

	}

	@Override
	public void endElement(String name) throws Exception {

	}

	@Override
	public String forElement() {
		return "node";
	}

	@Override
	public Object startElement(final String name, final Map<String, String> attributes, final  Object parent) throws Exception {
		lastStep = new IGraphCreatorStep() {
			private Node node;
			
			public Node getNode() {
				return node;
			}

			public void run(GraphDatabaseService graphDB) throws Exception {
				if (parent != null && parent instanceof IGraphCreatorStep) {
					final Node parentNode = ((IGraphCreatorStep) parent).getNode();
					if (parentNode != null) {
						if (parentNode.hasProperty("dcr_id")) {							
							DCR_MAP_ID = parentNode.getProperty("dcr_id").toString();
							buildNodeIndex(getTraversalDescription().traverse(graphDB.getReferenceNode()));
						}

						lookupNode(graphDB, attributes);

						if (parentNode.hasProperty("dcr_id")) {
							lookupRelationship(parentNode, Type.ROOT);
						}
						else {
							lookupRelationship(parentNode, Type.CHILD);
						}
					}
				} else {
					job.setAborted(true);
				}
			}
			
			private void buildNodeIndex(Traverser traverser) {				
				NODES = new LinkedHashMap<String, Node>();
				long count = 0;
				long time = System.currentTimeMillis();
				for (Node node : traverser.nodes()) {
					String key = getNodeRevisionKey(node);
					if (key != null) {
						NODES.put(key, node);
						count++;
					}
				}
				System.out.println("build NodeIndex with "+count+" nodes ("+(System.currentTimeMillis()-time)+")");
			}
			
			private String getNodeRevisionKey(Node node) {
				String key = null;
				if (node.hasProperty("ID") && node.hasProperty("MODIFIED")) {
					String mod = node.getProperty("MODIFIED").toString();
					if(node.hasProperty("MOVED")) {
						if( mod.compareTo(node.getProperty("MOVED").toString()) < 0)					
							mod = node.getProperty("MOVED").toString();
					}
					key = node.getProperty("ID").toString() + mod; 
				}				
				return key;
			}
			
			private String getNodeRevisionKey(Map<String, String> props) {
				String key = null;
				if (props.containsKey("ID") && props.containsKey("MODIFIED")) {
					String mod = props.get("MODIFIED");
					if(props.containsKey("MOVED") && mod.compareTo(props.get("MOVED")) < 0) {
						mod = props.get("MOVED");
					}
					key = props.get("ID").toString() + mod; 
				}				
				return key;
			}

			private void lookupNode(GraphDatabaseService graphDB, Map<String, String> attributes) throws Exception {
				hashMapLookup(graphDB, attributes);
			}

			private void hashMapLookup(GraphDatabaseService graphDB, Map<String, String> attributes) throws Exception {
				String key = getNodeRevisionKey(attributes);
				
				if (NODES != null && key != null) {
					node = NODES.get(key);
				}
				
				

				if (node == null) {
					node = graphDB.createNode();
					addProperties(node, attributes);
				}
				
			}

			private TraversalDescription getTraversalDescription() {
				TraversalDescription td = Traversal.description()
						.relationships(Type.MAP, Direction.OUTGOING)
						.relationships(Type.REVISION, Direction.OUTGOING)
						.relationships(Type.CHILD, Direction.OUTGOING)
						.relationships(Type.ROOT, Direction.OUTGOING)
						.uniqueness(Uniqueness.NODE_GLOBAL).depthFirst().evaluator(new Evaluator() {
					String currentRevision = "___";		
					@Override
					public Evaluation evaluate(final Path path) {
						if (path.length() == 0) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
						else if (path.endNode().hasProperty("dcr_id") && path.endNode().getProperty("dcr_id").equals(DCR_MAP_ID)) {
							currentRevision = String.valueOf(path.endNode().getProperty("ID"));
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
						if (path.endNode().hasProperty("MAP_ID") && path.endNode().getProperty("MAP_ID").equals(DCR_MAP_ID)) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
						else if(path.lastRelationship().isType(Type.CHILD) || path.lastRelationship().isType(Type.ROOT)) {
							if(path.lastRelationship().hasProperty(currentRevision)) {
								return Evaluation.INCLUDE_AND_CONTINUE;
							}
						}
						return Evaluation.EXCLUDE_AND_PRUNE;
					}
				});

				return td;
			}

			private void lookupRelationship(final Node parentNode, Type type) {
				if (node.hasRelationship(Direction.INCOMING, type)) {
					Iterator<Relationship> iter = node.getRelationships(Direction.INCOMING, type).iterator();
					while (iter.hasNext()) {
						Relationship relation = iter.next();
						if (relation.getStartNode().equals(parentNode)) {
							// update Relationship properties
							updateRelationShip(relation);
							return;
						}
					}
					updateRelationShip(parentNode.createRelationshipTo(node, type));
				}
				else {
					updateRelationShip(parentNode.createRelationshipTo(node, type));
				}
			}

			private void updateRelationShip(Relationship relation) {
				relation.setProperty(job.getRevision(), "");
			}

			protected void addProperties(Node n, Map<String, String> props) throws Exception {				
				for (String key : props.keySet()) {
					if("STYLE_REF".equals(key) || "LOCALIZED_STYLE_REF".equals(key)) {
						Node styleNode = ((StylesManager)job.getFromContext(GraphCreatorJob.CONTEXT_STYLE_MAP)).getStyle(props.get(key));
						if(styleNode != null) {
							n.createRelationshipTo(styleNode, StyleRelationship.REFERENCE);
						}
						else {
							//job.setAborted(true);
							//throw new Exception("refenrenced style ("+props.get(key)+") node not found");
						}
					} 
					else {
						if(!"ENCRYPTED_CONTENT".equals(key)) {
							n.setProperty(key, props.get(key));
						}
					}
				}
			}

			@Override
			public void addProperty(String key, String value) {
				if(UnknownHandler.UNKNOWN_CONTENT_KEY.equals(key)) {
					return;
				}
				attributes.put(key, value);
			}
		};
		job.appendStep(lastStep);
		return lastStep;
	}

	public void initialize() throws Exception {
		lastStep = null;
		DCR_MAP_ID = null;
	}
	
	@SuppressWarnings("unused")
	private void linkNodeToDocument(final Object value) {
		job.appendStep(new IGraphCreatorStep() {
			private final IGraphCreatorStep parentStep = lastStep; 
			public Node getNode() {
				return null;
			}
			
			public void addProperty(String key, String value) {
			}
			
			@Override
			public void run(GraphDatabaseService graphDb) {
				Node docNode = getOrCreateDocumentNode(graphDb, value);
				
				connectDocumentFully(graphDb, docNode);
				
			}

			private void connectDocumentFully(GraphDatabaseService graphDb, Node docNode) {
				if(!docNode.hasRelationship(Direction.INCOMING)) {
					Node docLibraryNode;
					if(graphDb.getReferenceNode().hasRelationship(Direction.OUTGOING, Type.DOCUMENTS)) {
						Relationship rs = graphDb.getReferenceNode().getRelationships(Direction.OUTGOING, Type.DOCUMENTS).iterator().next();
						docLibraryNode = rs.getEndNode();
					}
					else {
						docLibraryNode = graphDb.createNode();
						graphDb.getReferenceNode().createRelationshipTo(docLibraryNode, Type.DOCUMENTS);
					}
					docLibraryNode.createRelationshipTo(docNode, Type.DOCUMENT);
				}
				
				Node nullAnnotationNode;
				if(!docNode.hasRelationship(Direction.OUTGOING, Type.DOCUMENT_NULL_ANNOTATION)) {
					nullAnnotationNode = graphDb.createNode();
					docNode.createRelationshipTo(nullAnnotationNode, Type.DOCUMENT_NULL_ANNOTATION);
				}
				else {
					Relationship rs = docNode.getRelationships(Direction.OUTGOING, Type.DOCUMENT_NULL_ANNOTATION).iterator().next();
					nullAnnotationNode = rs.getEndNode();
				}
				
				parentStep.getNode().createRelationshipTo(nullAnnotationNode, Type.DOCUMENT_LINK);

			}

			private Node getOrCreateDocumentNode(GraphDatabaseService graphDb, Object value) {
				UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "mapNodes") {
					protected void initialize(Node created, Map<String, Object> properties) {
						created.setProperty("DOC_ID", properties.get("DOC_ID"));
					}
				};
				
				return factory.getOrCreate("DOC_ID", getDocumentName(""+value));
			}

			private String getDocumentName(String link) {
				int pos = link.lastIndexOf("/");
				String name = link;
				String lowerLink = link.toLowerCase();
				// 
				if(pos == link.length()-1
					|| lowerLink.startsWith("http://") 
					|| lowerLink.startsWith("https://")
					|| lowerLink.startsWith("ftp://") ) {
					return name;		
				}
				if(pos > -1) {
					name = link.substring(pos+1);
				}
				return name;
			}
		});
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
