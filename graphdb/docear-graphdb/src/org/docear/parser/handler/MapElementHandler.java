package org.docear.parser.handler;

import java.util.Iterator;
import java.util.Map;

import org.docear.graphdb.relationship.Type;
import org.docear.graphdb.relationship.UserRelationship;
import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.graphdb.threading.IGraphCreatorStep;
import org.docear.nanoxml.IElementHandler;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.UniqueFactory;


public class MapElementHandler implements IElementHandler {

	private IGraphCreatorStep lastStep;
	private final GraphCreatorJob job;

	public MapElementHandler(GraphCreatorJob job) {
		this.job = job;
	}

	@Override
	public void addContent(char[] arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void endElement(String name) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String forElement() {
		return "map";
	}

	@Override
	public Object startElement(String name, final Map<String, String> attributes, Object parent) throws Exception {
		if(!attributes.containsKey("dcr_id")) {
			job.setAborted(true);
			return null;
		}
		//final Map<String, String> props = new HashMap<String, String>(attributes);
		lastStep = new IGraphCreatorStep() {
			private Node node;
			
			public Node getNode() {
				return node;
			}

			public void addProperty(String key, String value) {
				if(UnknownHandler.UNKNOWN_CONTENT_KEY.equals(key)) {
					return;
				}
				attributes.put(key, value);
			}
			
			public void run(GraphDatabaseService graphDB) throws Exception {
				
				Node parent = getOrCreateMapNode(graphDB, attributes.get("dcr_id"));
				if (!parent.hasProperty("map_type")) {
					Object mapType = job.getFromContext(GraphCreatorJob.PROPERTY_MAP_TYP);
					parent.setProperty(GraphCreatorJob.PROPERTY_MAP_TYP, (mapType == null ? "" : mapType));
				}

				String revision_id = job.getRevision();
				job.setContext("mapNode", parent);
				
				if(hasRevision(parent, revision_id)) {
					throw new DuplicateRevisionException();
				}
				
				node = graphDB.createNode();
				
				Node revisionNode = getNode();
				job.setContext("revisionNode", revisionNode);
				
				revisionNode.setProperty("dcr_id", attributes.get("dcr_id"));
				revisionNode.setProperty("ID", revision_id);				
				revisionNode.setProperty("CREATED", job.getRevisionTimestamp());
				revisionNode.setProperty("allow_content_research", job.getFromContext(GraphCreatorJob.PROPERTY_ALLOW_CONTENT_RESEARCH));
				revisionNode.setProperty("allow_information_retrieval", job.getFromContext(GraphCreatorJob.PROPERTY_ALLOW_INFORMATION_RETRIEVAL));
				revisionNode.setProperty("allow_usage_research", job.getFromContext(GraphCreatorJob.PROPERTY_ALLOW_USAGE_RESEARCH));
				revisionNode.setProperty("allow_recommendations", job.getFromContext(GraphCreatorJob.PROPERTY_ALLOW_RECOMMENDATIONS));
				String affiliation = (String) job.getFromContext(GraphCreatorJob.PROPERTY_AFFILIATION);
				if (affiliation != null) {
					revisionNode.setProperty("affiliation", affiliation);
				}
				revisionNode.setProperty("app_build", job.getFromContext(GraphCreatorJob.PROPERTY_APPLICATION_BUILD));
								
				parent.createRelationshipTo(revisionNode, Type.REVISION);
				
				Node user = getOrCreateUniqueUser(graphDB, job.getUserID());
				user.createRelationshipTo(revisionNode, UserRelationship.OWNS);
				
				job.setContext("userNode", user);
			}

			private boolean hasRevision(Node parent, String revision_id) throws DuplicateRevisionException {
				Iterator<Relationship> iter = parent.getRelationships(Direction.OUTGOING, Type.REVISION).iterator();
				while(iter.hasNext()) {
					Node rev = iter.next().getEndNode();
					if(rev.hasProperty("ID") && revision_id.equals(rev.getProperty("ID"))) {
						return true;
					}
				}
				return false;
			}
			
			private Node getOrCreateMapNode(GraphDatabaseService graphDb, final String mapID) {
				UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "maps") {
					protected void initialize(Node created, Map<String, Object> properties) {
						created.setProperty("MAP_ID", properties.get("MAP_ID"));
						created.setProperty("TEXT", job.getFile().getName());
						Object mapType = job.getFromContext(GraphCreatorJob.PROPERTY_MAP_TYP);
						created.setProperty(GraphCreatorJob.PROPERTY_MAP_TYP, (mapType == null ? "" : mapType));
						created.getGraphDatabase().getReferenceNode().createRelationshipTo(created, Type.MAP);
					}
				};

				return factory.getOrCreate("MAP_ID", mapID);
			}

			private Node getOrCreateUniqueUser(GraphDatabaseService graphDb, final int userID) {
				UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "users") {
					protected void initialize(Node created, Map<String, Object> properties) {
						created.setProperty("USER_ID", properties.get("USER_ID"));
						Node userSet = getOrCreateUserSet(created.getGraphDatabase());
						userSet.createRelationshipTo(created, UserRelationship.USER);
					}

					private Node getOrCreateUserSet(GraphDatabaseService graphDb) {
						Node globalUserSet;
						if(!graphDb.getReferenceNode().hasRelationship(UserRelationship.USER_SET)) {
							globalUserSet = graphDb.createNode();
							globalUserSet.setProperty("TYPE", "GlobalUserSet");
							graphDb.getReferenceNode().createRelationshipTo(globalUserSet, UserRelationship.USER_SET);
						}
						else {
							globalUserSet = graphDb.getReferenceNode().getRelationships(UserRelationship.USER_SET).iterator().next().getEndNode();
						}
						return globalUserSet;
					}
				};

				return factory.getOrCreate("USER_ID", userID);
			}
		};
		job.appendStep(lastStep);
		return lastStep;
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

}
