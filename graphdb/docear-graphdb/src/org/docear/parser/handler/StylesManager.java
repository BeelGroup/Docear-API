package org.docear.parser.handler;

import java.util.Map;
import java.util.TreeMap;

import org.docear.graphdb.relationship.StyleRelationship;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.UniqueFactory;


public class StylesManager {
	private final Map<String, Node> styles = new TreeMap<String, Node>();
	private final Map<String, Node> globalMap = new TreeMap<String, Node>();
	private final Map<String, Map<String, String>> styleAttr = new TreeMap<String, Map<String, String>>();
	private final GraphDatabaseService GRAPH_DB;
	
	
	public StylesManager(GraphDatabaseService graphDb) {
		GRAPH_DB = graphDb;
	}
	
	
	public void addAttributes(Map<String, String> attributes) {
		String styleName = getStyleName(attributes);
		styleAttr.put(styleName, attributes);
		getStyle(styleName);
	}

	private String getStyleName(Map<String, String> attributes) {
		String key = attributes.get("LOCALIZED_TEXT");
		if(key == null) {
			key = attributes.get("TEXT");
		}
		return key;
	}
	
	public Node getStyle(String styleName) {
		if(styleName == null) {
			throw new IllegalArgumentException("styleName must not be NULL");
		}
		Node node = styles.get(styleName);
		if(node == null) {
			node = getStyle(styleAttr.get(styleName));
			if(node != null) {
				styles.put(styleName, node);
			}
		}
		return node;
	}
	
	private Node getStyle(Map<String, String> attributes) {
		if(attributes == null) {
			return null;
		}
		return getOrCreateStyleRevisionNode(GRAPH_DB, attributes);
	}	
	
	private Node getOrCreateStyleRevisionNode(GraphDatabaseService graphDb, final Map<String, String> attributes) {
		UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "styleRevisions") {
			
			protected void initialize(Node created, Map<String, Object> properties) {
				created.setProperty("HASH", properties.get("HASH"));
				addProperties(created, attributes);
				interconnect(created);
			}
			
			private void interconnect(Node node) {				
				Node styleNode = getOrCreateStyleNodeFor(node);
				styleNode.createRelationshipTo(node, StyleRelationship.STYLE_REVISION);				 								
			}

			private Node getOrCreateStyleNodeFor(Node node) {				
				return getUsingIndex(node);
//				return getUsingCustom(node);
			}

//			private Node getUsingCustom(Node node) {
//				Node global = getOrCreateGlobalStylesMap(node.getGraphDatabase());
//				Iterator<Relationship> rsIter = global.getRelationships(Direction.OUTGOING, StyleRelationship.STYLE).iterator();
//				while(rsIter.hasNext()) {
//					Node n = rsIter.next().getEndNode();
//					if(node.getProperty("NAME").equals(n.getProperty("NAME"))) {
//						return n;
//					}
//				}
//				Node n = node.getGraphDatabase().createNode();
//				n.setProperty("NAME", node.getProperty("NAME"));
//				global.createRelationshipTo(n, StyleRelationship.STYLE);
//				return n;
//			}

			private Node getUsingIndex(final Node node) {
				UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(node.getGraphDatabase(), "styleNames") {
					protected void initialize(Node created, Map<String, Object> properties) {
						created.setProperty("NAME", properties.get("NAME"));
						Node global = getOrCreateGlobalStylesMap(created.getGraphDatabase());
						global.createRelationshipTo(created, StyleRelationship.STYLE);
					}
				};
				return factory.getOrCreate("NAME", node.getProperty("NAME"));
			}

			private Node getOrCreateGlobalStylesMap(GraphDatabaseService graphDb) {
				if(globalMap.containsKey(graphDb.toString())) {
					return globalMap.get(graphDb.toString());
				}
				Node globalMapStyles;
				if(!graphDb.getReferenceNode().hasRelationship(StyleRelationship.STYLES_MAP)) {
					globalMapStyles = graphDb.createNode();
					globalMapStyles.setProperty("TYPE", "GlobalStylesMap");
					graphDb.getReferenceNode().createRelationshipTo(globalMapStyles, StyleRelationship.STYLES_MAP);
				}
				else {
					globalMapStyles = graphDb.getReferenceNode().getRelationships(StyleRelationship.STYLES_MAP).iterator().next().getEndNode();
				}
				globalMap.put(graphDb.toString(), globalMapStyles);
				return globalMapStyles;
			}

			protected void addProperties(Node n, Map<String, String> props) {				
				for (String key : props.keySet()) {
					if("TEXT".equals(key) || "LOCALIZED_TEXT".equals(key)) {
						n.setProperty("NAME", props.get(key));
					}
					else {
						n.setProperty(key, props.get(key));
					}
				}
			}
		};
		
		return factory.getOrCreate("HASH", getStyleHash(attributes));
	}
	
	
	
	private Integer getStyleHash(Map<String, String> attributes) {				
		String fingerPrint = "";
		for(String key : attributes.keySet()) {
			fingerPrint += attributes.get(key);
		}
		return fingerPrint.hashCode();
	}

	
	
}
