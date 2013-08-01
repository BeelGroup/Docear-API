package org.docear.graphdb;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.docear.database.AlgorithmArguments;
import org.docear.graphdb.GraphDbWorker.NodeRevision;
import org.docear.xml.UserModel;
import org.neo4j.graphdb.Node;

/**
 * This class contains a number help methods for the parsing of the mindmaps in the graph DB.
 * @author gkapi
 */

public class GraphDbHelper {
	
	/**
	 * @param nodes
	 * @param args
	 * @param userModel
	 */
	 static void filterByDaysSinceLastForMaps(List<Node> maps, AlgorithmArguments args, UserModel userModel) {		 		 
		 // do not filter the mindmap, if there is only one
		 if (maps != null && maps.size() > 0) {
				Map<String, Integer> daysSinceOldest = new HashMap<String, Integer>(); 
				Set<Integer> daysSet = new HashSet<Integer>();
				    
				Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
				
				long firstNodeDate = 0;
				int indexOfLastNode = maps.size() == 1 ? 0 : maps.size()-1;
				long lastNodeDate = 0;
				long nodeDate = 0;
				Integer daysElapsedFromOldest = 0;
				
				switch (method) {
					case 1: // 1=edited 
						// date is given in the format yyyy-MM-dd HH:mm:ss
						firstNodeDate = GraphDbHelper.stringToMilliseconds(maps.get(0).getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
						if (maps.get(indexOfLastNode).hasProperty("CREATED")) 
							lastNodeDate = GraphDbHelper.stringToMilliseconds(maps.get(indexOfLastNode).getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
						break;
					case 2: // 2=created 
						firstNodeDate = Long.valueOf(maps.get(0).getProperty("dcr_id").toString().split("_")[0]);
						if (maps.get(indexOfLastNode).hasProperty("dcr_id"))
							// date is given in milliseconds along with a hash value
							lastNodeDate = Long.valueOf(maps.get(indexOfLastNode).getProperty("dcr_id").toString().split("_")[0]); 
				}
				
				for (Node node: maps) {
					switch (method) {
						case 1: // 1=edited 
							if (node.hasProperty("CREATED"))
								nodeDate = GraphDbHelper.stringToMilliseconds(node.getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
								break;
						case 2: // 2=created (date is given in milliseconds)
							if (node.hasProperty("dcr_id"))
								nodeDate = Long.valueOf(node.getProperty("dcr_id").toString().split("_")[0]); 	
					}
					// the time difference from the oldest mindmap in the list
					daysElapsedFromOldest = (int)TimeUnit.DAYS.convert(firstNodeDate - nodeDate, TimeUnit.MILLISECONDS);
					daysSinceOldest.put(node.getProperty("ID").toString(), daysElapsedFromOldest);
					daysSet.add(daysElapsedFromOldest);
				}
				
				// get randomly the number of last days for which the mindmaps will be considered
				Integer[] valueSetAsArray = daysSet.toArray(new Integer[daysSet.size()]);
				int indexOfnumberOfDaysSinceLast = new Random().nextInt(valueSetAsArray.length);
				int numberOfDaysSinceLast = valueSetAsArray.length == 0 ? 0 : valueSetAsArray[indexOfnumberOfDaysSinceLast];
				
				int daysSinceMax = (int)TimeUnit.DAYS.convert(firstNodeDate - lastNodeDate, TimeUnit.MILLISECONDS);
				// filter the nodes only if the returned value is lower than the maximum days since value
				if (numberOfDaysSinceLast < daysSinceMax) 
					for (Iterator<Node> it = maps.iterator(); it.hasNext();) {
						Node node = it.next();
						if ((Integer)daysSinceOldest.get(node.getProperty("ID").toString()) > numberOfDaysSinceLast)
							it.remove();
					}
				
				userModel.addVariable("no_days_since_max_maps", String.valueOf(daysSinceMax)); 
				userModel.addVariable("no_days_since_chosen_maps", String.valueOf(numberOfDaysSinceLast)); 
		 }
	}
	 
	/**
	 * 
	 * @param nodes
	 * @param args
	 * @param userModel
	 */	 
	static void filterByDaysSinceLastForNodes(Collection<NodeRevision> nodes, AlgorithmArguments args, UserModel userModel) {		
		 // do not filter the nodes, if there is only one
		 if (nodes != null && nodes.size() > 0) {
				Map<String, Integer> daysSinceOldest = new HashMap<String, Integer>(); 
				Set<Integer> daysSet = new HashSet<Integer>();
				
				Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
				
				long firstNodeDate = 0;
				int indexOfLastNode = nodes.size() == 1 ? 0 : nodes.size()-1;
				long lastNodeDate;
				String propertyName = "MODIFIED"; // to cover the case 1=edited
				Integer daysElapsedFromOldest = 0;
				
				NodeRevision[] nodesArray = nodes.toArray(new NodeRevision[nodes.size()]);
				
				switch (method) {
					case 2: // 2=created 
						propertyName = "CREATED";
						break;
					case 3: // 3=moved 
						propertyName = "MOVED";
				}
				
				firstNodeDate = Long.valueOf(nodesArray[0].getNode().getProperty(propertyName).toString());
				lastNodeDate = Long.valueOf(nodesArray[indexOfLastNode].getNode().getProperty(propertyName).toString());
				
				for (NodeRevision node: nodesArray) {
					if (node.getNode().hasProperty(propertyName)) {
						// the time difference from the oldest mindmap in the list
						daysElapsedFromOldest = (int)TimeUnit.DAYS.convert(firstNodeDate - Long.valueOf(node.getNode().getProperty(propertyName).toString()), TimeUnit.MILLISECONDS);
					    daysSinceOldest.put(node.getNode().getProperty("ID").toString(), daysElapsedFromOldest);
						daysSet.add(daysElapsedFromOldest);
					}
				}
				
				// get randomly the number of last days for which the mindmaps will be considered
				Integer[] valueSetAsArray = daysSet.toArray(new Integer[daysSet.size()]);
				int indexOfnumberOfDaysSinceLast = new Random().nextInt(valueSetAsArray.length);
				int numberOfDaysSinceLast = valueSetAsArray.length == 0 ? 0 : valueSetAsArray[indexOfnumberOfDaysSinceLast];
				
				int daysSinceMax = (int)TimeUnit.DAYS.convert(firstNodeDate - lastNodeDate, TimeUnit.MILLISECONDS);
				// filter the nodes only if the returned value is lower than the maximum days since value
				if (numberOfDaysSinceLast < daysSinceMax) 
					for (Iterator<NodeRevision> it = nodes.iterator(); it.hasNext();) {
						NodeRevision nodeRev = it.next();
						if ((Integer)daysSinceOldest.get(nodeRev.getNode().getProperty("ID").toString()) > numberOfDaysSinceLast)
							it.remove();
					}
				
				userModel.addVariable("no_days_since_max_nodes", String.valueOf(daysSinceMax)); 
				userModel.addVariable("no_days_since_chosen_nodes", String.valueOf(numberOfDaysSinceLast));
		 }
	}
	
	static long stringToMilliseconds(String dateString, DateFormat dateFormat) {
		try {
			return dateFormat.parse(dateString).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
}
