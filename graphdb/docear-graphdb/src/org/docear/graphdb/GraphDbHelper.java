package org.docear.graphdb;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.docear.Logging.DocearLogger;
import org.docear.database.AlgorithmArguments;
import org.docear.xml.UserModel;
import org.neo4j.graphdb.Node;

/**
 * This class contains a number help methods for the parsing of the mindmaps in the graph DB.
 * @author gkapi
 */

public class GraphDbHelper {
	
	public void addNewSinceMaxDate(QuerySession session, AlgorithmArguments args, Node node, boolean allUserMaps) {
		final Integer dataElement = (Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT);
		final Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
		
		if (dataElement == null || method == null || method == 0) {
			return;
		}
		
		switch (dataElement) {
		case 1:
			addNewSinceMaxDateMap(session, node, method, allUserMaps);
			break;
		case 2:
			addNewSinceMaxDateNode(session, node, method, allUserMaps);
			break;
		}
	}
	
	private void addNewSinceMaxDateMap(QuerySession session, Node node, Integer method, boolean allUserMaps) {
		Long date = null;
		
		switch (method) {
		case 1: // 1=edited 
			// date is given in the format yyyy-MM-dd HH:mm:ss
			date = GraphDbHelper.stringToMilliseconds(node.getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));			
			break;
		case 2: // 2=created 
			date = Long.valueOf(node.getProperty("dcr_id").toString().split("_")[0]);			 
		}
		
		if (date == null) {
			return;
		}
		
		if (allUserMaps) {
			session.addNewAllDate(date);
		}
		else {
			session.addNewAlgDate(date);
		}
	}
	
	private void addNewSinceMaxDateNode(QuerySession session, Node node, Integer method, boolean allUserMaps) {
		Long date = null;
		String propertyName = null;
		
		switch (method) {
			case 1: // 1=modified
				propertyName = "MODIFIED";
				break;
			case 2: // 2=created 
				propertyName = "CREATED";
				break;
			case 3: // 3=moved 
				propertyName = "MOVED";
				break;
		}
		
		if (propertyName == null) {
			return;
		}
		
		date = Long.valueOf(node.getProperty(propertyName).toString());
		
		if (allUserMaps) {
			session.addNewAllDate(date);
		}
		else {
			session.addNewAlgDate(date);
		}
	}
	
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
				int numberOfDaysSinceLast = valueSetAsArray[new Random().nextInt(valueSetAsArray.length)];
				
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
	 * @param nodes
	 * @param args
	 * @param userModelt
	 */
	static void filterByDaysSinceLastForNodes(Set<Node> nodes, AlgorithmArguments args, UserModel userModel) {		
		Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
		
		 // do not filter the nodes, if there is only one
		 if (nodes != null && nodes.size() > 0) {
				String propertyName = "MODIFIED"; // to cover the case 1=edited	
				Node[] nodesArray = nodes.toArray(new Node[nodes.size()]);
				
				switch (method) {
					case 2: // 2=created 
						propertyName = "CREATED";
						break;
					case 3: // 3=moved 
						propertyName = "MOVED";
				}
				// get the date for teh newest (firstNodeDate) and the oldest (lastNodeDate) created node
				long mostRecentNodeDate = getMaxDate(nodes, method);
				long oldestNodeDate = getMinDate(nodes, method);
				
				Map<String, Integer> daysSinceOldest = new HashMap<String, Integer>(); 
				Set<Integer> daysSet = new HashSet<Integer>();
				
				int daysSinceMax = (int)TimeUnit.DAYS.convert(mostRecentNodeDate - oldestNodeDate, TimeUnit.MILLISECONDS);
				
				Integer daysElapsedFromOldest = 0;
				
				for (Node node: nodesArray) {
					if (node.hasProperty(propertyName)) {
						// the time difference from the oldest mindmap in the list
						daysElapsedFromOldest = (int)TimeUnit.DAYS.convert(mostRecentNodeDate - Long.valueOf(node.getProperty(propertyName).toString()), TimeUnit.MILLISECONDS);
					    daysSinceOldest.put(node.getProperty("ID").toString(), daysElapsedFromOldest);
						daysSet.add(daysElapsedFromOldest);
					}
				}
				
				// get randomly the number of last days for which the mindmaps will be considered
				Integer[] valueSetAsArray = daysSet.toArray(new Integer[daysSet.size()]);
				int numberOfDaysSinceLast = valueSetAsArray[new Random().nextInt(valueSetAsArray.length)];
				
				// filter the nodes only if the returned value is lower than the maximum days since value
				if (numberOfDaysSinceLast < daysSinceMax) 
					for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
						Node node = it.next();
						if ((Integer)daysSinceOldest.get(node.getProperty("ID").toString()) > numberOfDaysSinceLast)
							it.remove();
					}
				
				userModel.addVariable("no_days_since_max_nodes", String.valueOf(daysSinceMax)); 
				userModel.addVariable("no_days_since_chosen_nodes", String.valueOf(numberOfDaysSinceLast));
		 }
	}
	
	static long getMaxDate(Set<Node> nodes, Integer method) {
		long maxDate = -1;
		String propertyName = "MODIFIED"; // to cover the case 1=edited	
		
		switch (method) {
		case 2: // 2=created 
			propertyName = "CREATED";
			break;
		case 3: // 3=moved 
			propertyName = "MOVED";
		}
		
		for (Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
			Node node = it.next();			
			if (node.hasProperty(propertyName)) {
				Long nodeDate = Long.valueOf(node.getProperty(propertyName).toString());
				if (nodeDate > maxDate) {
					maxDate = nodeDate;
				}
			}			
		}
		return maxDate;
	}
	
	static long getMinDate(Set<Node> nodes, Integer method) {
		long minDate = Long.MAX_VALUE;
		String propertyName = "MODIFIED"; // to cover the case 1=edited	
		
		switch (method) {
		case 2: // 2=created 
			propertyName = "CREATED";
			break;
		case 3: // 3=moved 
			propertyName = "MOVED";
		}
		
		for (Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
			Node node = it.next();			
			if (node.hasProperty(propertyName)) {
    			Long nodeDate = Long.valueOf(node.getProperty(propertyName).toString());
    			if (nodeDate < minDate) {
    				minDate = nodeDate;
    			}
			}
		}
		return minDate;
	}
	
	static long stringToMilliseconds(String dateString, DateFormat dateFormat) {
		try {
			return dateFormat.parse(dateString).getTime();
		} catch (ParseException e) {
			DocearLogger.error(e);
		}
		return -1;
	}
	
}
