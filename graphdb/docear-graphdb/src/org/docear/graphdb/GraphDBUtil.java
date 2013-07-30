package org.docear.graphdb;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.docear.database.AlgorithmArguments;
import org.docear.xml.UserModel;
import org.neo4j.graphdb.Node;

/**
 * This class contains a number help methods for the parsing of the mindmaps in the graph DB.
 * @author gkapi
 */

public class GraphDBUtil {
	
	/**
	 * @param nodes
	 * @param args
	 * @return a map with each entry containing the mindmap node id mapped to the number of days since the mindmap was edited for the last time or created
	 */
	 static List<Node> filterByDaysSinceLastForMaps(List<Node> maps, AlgorithmArguments args, UserModel userModel) {		 
		 // do not filter the mindmap, if there is only one
		 if (maps != null && maps.size() > 0) {
				Map<String, Integer> daysSinceOldest = new HashMap<String, Integer>(); 
				Set<Integer> daysSet = new HashSet<Integer>();
				    
				Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
				
				int indexOfLastNode = maps.size() == 1 ? 0 : maps.size()-1;
				long lastNodeDate = 0;
				long nodeDate = 0;
				Integer daysElapsedFromOldest = 0;
				
				switch (method) {
					case 1: // 1=edited 
						// date is given in the format yyyy-MM-dd HH:mm:ss
						if (maps.get(indexOfLastNode).hasProperty("CREATED"))
							lastNodeDate = GraphDBUtil.stringToMilliseconds(maps.get(indexOfLastNode).getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
							break;
					case 2: // 2=created 
						if (maps.get(indexOfLastNode).hasProperty("dcr_id"))
							// date is given in milliseconds along with a hash value
							lastNodeDate = Long.valueOf(maps.get(indexOfLastNode).getProperty("dcr_id").toString().split("_")[0]); 
				}
				
				for (Node node: maps) {
					switch (method) {
						case 1: // 1=edited 
							if (node.hasProperty("CREATED"))
								nodeDate = GraphDBUtil.stringToMilliseconds(node.getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
								break;
						case 2: // 2=created (date is given in milliseconds)
							if (node.hasProperty("dcr_id"))
								nodeDate = Long.valueOf(node.getProperty("dcr_id").toString().split("_")[0]); 	
					}
					// the time difference from the oldest mindmap in the list
					daysElapsedFromOldest = (int)TimeUnit.DAYS.convert(nodeDate - lastNodeDate, TimeUnit.MILLISECONDS);
					daysSinceOldest.put(node.getProperty("ID").toString(), daysElapsedFromOldest);
					daysSet.add(daysElapsedFromOldest);
				}
				
				// get randomly the number of last days for which the mindmaps will be considered
				Integer[] valueSetAsArray = daysSet.toArray(new Integer[daysSet.size()]);
				int indexOfnumberOfDaysSinceLast = new Random().nextInt(valueSetAsArray.length);
				int numberOfDaysSinceLast = valueSetAsArray.length == 0 ? 0 : valueSetAsArray[indexOfnumberOfDaysSinceLast];
				
				// filter the nodes only if the restuned value is lower than the size of the distinct days set
				if (indexOfnumberOfDaysSinceLast < valueSetAsArray.length - 1) 
					for (Iterator<Node> it = maps.iterator(); it.hasNext();) {
						Node node = it.next();
						if ((Integer)daysSinceOldest.get(node.getProperty("ID").toString()) > numberOfDaysSinceLast)
							it.remove();
					}
				
				userModel.addVariable("no_days_since_maps", String.valueOf(numberOfDaysSinceLast)); 
		 }
	
		return maps;
	}
	 
	/**
	 * 
	 * @param nodes
	 * @param args
	 * @return a map with each entry containing the node id mapped to  the number of days since the node was edited for the last time or created or moved
	 */	 
	static Set<Node> filterByDaysSinceLastForNodes(Set<Node> nodes, AlgorithmArguments args, UserModel userModel) {	
		 // do not filter the nodes, if there is only one
		 if (nodes != null && nodes.size() > 0) {
				Map<String, Integer> daysSinceOldest = new HashMap<String, Integer>(); 
				Set<Integer> daysSet = new HashSet<Integer>();
				
				Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
				
				int indexOfLastNode = nodes.size() == 1 ? 0 : nodes.size()-1;
				long lastNodeDate;
				String propertyName = "MODIFIED"; // to cover the case 1=edited
				Integer daysElapsedFromOldest = 0;
				
				Node[] nodesArray = nodes.toArray(new Node[nodes.size()]);
				
				switch (method) {
					case 2: // 2=created 
						propertyName = "CREATED";
						break;
					case 3: // 3=moved 
						propertyName = "MOVED";
				}
				
				lastNodeDate = Long.valueOf(nodesArray[indexOfLastNode].getProperty(propertyName).toString());
				
				for (Node node: nodesArray) {
					if (node.hasProperty(propertyName)) {
						// the time difference from the oldest mindmap in the list
						daysElapsedFromOldest = (int)TimeUnit.DAYS.convert(Long.valueOf(node.getProperty(propertyName).toString()) - lastNodeDate, TimeUnit.MILLISECONDS);
					    daysSinceOldest.put(node.getProperty("ID").toString(), daysElapsedFromOldest);
						daysSet.add(daysElapsedFromOldest);
					}
				}
				
				// get randomly the number of last days for which the mindmaps will be considered
				Integer[] valueSetAsArray = daysSet.toArray(new Integer[daysSet.size()]);
				int indexOfnumberOfDaysSinceLast = new Random().nextInt(valueSetAsArray.length);
				int numberOfDaysSinceLast = valueSetAsArray.length == 0 ? 0 : valueSetAsArray[indexOfnumberOfDaysSinceLast];
				
				// filter the nodes only if the restuned value is lower than the size of the distinct days set
				if (indexOfnumberOfDaysSinceLast < valueSetAsArray.length - 1) 
					for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
						Node node = it.next();
						if ((Integer)daysSinceOldest.get(node.getProperty("ID").toString()) > numberOfDaysSinceLast)
							it.remove();
					}
				
				userModel.addVariable("no_days_since_nodes", String.valueOf(numberOfDaysSinceLast));
		 }
		
		return nodes;
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