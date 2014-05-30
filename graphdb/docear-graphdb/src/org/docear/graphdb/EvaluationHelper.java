package org.docear.graphdb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.docear.database.AlgorithmArguments;
import org.docear.graphdb.relationship.Type;
import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.xml.UserModel;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Traverser;

public class EvaluationHelper {
	public void addTotalCountVariables(QuerySession session, int userId, AlgorithmArguments args, UserModel userModel) {
		try {
    		Collection<Node> allMaps = getMapsForUser(session, userId, ALGORITHM_FOR_ALL_USER_ELEMENTS, userModel, true, null);    		
    		userModel.addVariable("mind-map_count_total", ""+allMaps.size());
    				
    		int nodeCountTotal = 0;
    		int linkCountTotal = 0;
    		
    		HashSet<String> hashes = new HashSet<String>();
    		for (Node startNode : allMaps) {
    			try {				
    				final String mapType = startNode.getSingleRelationship(Type.REVISION, Direction.INCOMING).getStartNode().getProperty(GraphCreatorJob.PROPERTY_MAP_TYP).toString();
    				Iterator<Path> it = getUserLatestRevisionsTraversal(startNode, mapType, ALGORITHM_FOR_ALL_USER_ELEMENTS, null).iterator();				
    				while (it.hasNext()) {
    					Path path = it.next();
    					if (!path.endNode().hasProperty("ID") || !DEMO_NODES.contains(path.endNode().getProperty("ID").toString())) {
    						nodeCountTotal++;
    					}
    				}
    				
    				Traverser traverser = getUserLatestRevisionsReferencesTraversal(startNode, mapType, ALGORITHM_FOR_ALL_USER_ELEMENTS, null);
    				for (Node n : traverser.nodes()) {					
    					if(!n.hasProperty("DCR_PRIVACY_LEVEL") || "PUBLIC".equals(n.getProperty("DCR_PRIVACY_LEVEL"))) {
    						if (n.hasProperty("annotation_document_hash")) {
    							if (!n.hasProperty("ID") || !DEMO_NODES.contains(n.getProperty("ID").toString())) {
    								hashes.add((String) n.getProperty("annotation_document_hash"));
    								linkCountTotal++;
    							}
    						}
    					}
    					
    				}
    			}
    			catch(Throwable t) {
    				t.printStackTrace();
    			}
    		}
    		userModel.addVariable("node_count_total", ""+nodeCountTotal);
    		userModel.addVariable("paper_count_total", ""+hashes.size());
    		userModel.addVariable("link_count_total", ""+linkCountTotal);
    		
    		if (new Integer(1).equals(args.getArgument(AlgorithmArguments.DATA_ELEMENT))) {
    			userModel.addVariable("entity_total_count", ""+allMaps.size());
    		}
    		else if (new Integer(2).equals(args.getArgument(AlgorithmArguments.DATA_ELEMENT))) {
    			userModel.addVariable("entity_total_count", ""+nodeCountTotal);
    		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
