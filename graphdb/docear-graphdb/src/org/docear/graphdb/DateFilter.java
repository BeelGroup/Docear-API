package org.docear.graphdb;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.docear.Logging.DocearLogger;
import org.docear.database.AlgorithmArguments;
import org.docear.xml.UserModel;
import org.neo4j.graphdb.Node;

/**
 * This class contains a number help methods for the parsing of the mindmaps in the graph DB.
 * @author gkapi
 */

public class DateFilter {
	
	public static void addNewSinceMaxDate(QuerySession session, AlgorithmArguments args, Node node, Boolean allUserMaps) {		;
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
	
	public static void clearNewSinceMaxDates(QuerySession session, boolean allUserMaps) {
		session.clearNewSinceMaxDates(allUserMaps);
	}
	
	public static void filterByDate(QuerySession session, UserModel userModel, Collection<Node> nodes, AlgorithmArguments args) {
		final Integer dataElement = (Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT);
		final Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
		
		if (dataElement == null || method == null || method == 0) {
			DocearLogger.info("NDSM: dataElement or dataElementType is null / 0");
			return;
		}
		
		switch (dataElement) {
		case 1:
			filterMapsByDate(session, nodes, method);
			break;
		case 2:
			filterNodesByDate(session, nodes, method);
			break;
		}
		
		userModel.addVariable("no_days_since_max", String.valueOf(session.getNoDaysSinceMax(true))); 
		userModel.addVariable("no_days_since_chosen", String.valueOf(session.getNoDaysSinceChosen())); 
	}
	
	private static Long getMapDate(Node map, Integer method) {
		Long date = null;
		
		switch (method) {
		case 1: // 1=edited 
			// date is given in the format yyyy-MM-dd HH:mm:ss
			if (map.hasProperty("CREATED")) {
				date = DateFilter.stringToMilliseconds(map.getProperty("CREATED").toString());
			}
			break;
		case 2: // 2=created 
			if (map.hasProperty("dcr_id")) {
				date = Long.valueOf(map.getProperty("dcr_id").toString().split("_")[0]);
			}
			break;
		default:
			if (map.hasProperty("CREATED")) {
				date = DateFilter.stringToMilliseconds(map.getProperty("CREATED").toString());
			}
			else {
				date = Long.MAX_VALUE;
			}
			
			if (map.hasProperty("dcr_id")) {
				date = Math.min(date, Long.valueOf(map.getProperty("dcr_id").toString().split("_")[0]));
			}
			break;
		}
		
		
		return date;
	}
	
	private static void addNewSinceMaxDateMap(QuerySession session, Node map, Integer method, boolean allUserMaps) {
		Long date = getMapDate(map, method);
		
		if (date != null) {
			session.addNewDate(date, allUserMaps);
		}
	}
	
	private static Long getNodeDate(Node node, Integer method) {
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
			return null;
		}
		
		date = Long.valueOf(node.getProperty(propertyName).toString());
		if (method == 0) {
			// get minimum of all strings
			String s1 = node.getProperty("MODIFIED").toString();
			String s2 = node.getProperty("CREATED").toString();
			if (s2.compareTo(s1) < 0) {
				s1 = s2;
			}
			s2 = node.getProperty("MOVED").toString();
			if (s2.compareTo(s1) < 0) {
				s1 = s2;
			}
			date = Long.valueOf(s1);
		}
		
		return date;
	}
	
	private static void addNewSinceMaxDateNode(QuerySession session, Node node, Integer method, boolean allUserMaps) {
		Long date = getNodeDate(node, method);
		
		if (date == null) {
			return;
		}
		
		session.addNewDate(date, allUserMaps);		
	}
	
	private static void filterNodesByDate(QuerySession session, Collection<Node> nodes, Integer method) {
		Date d = session.getFilterDate();
		if (d == null) {
			DocearLogger.info("no dates to filter!");
			return;
		}
		
		Long filterDate = d.getTime();
				
		for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
			Node node = it.next();
			if (getNodeDate(node, method) < filterDate) {
				it.remove();
			}
		}
	}

	private static void filterMapsByDate(QuerySession session, Collection<Node> nodes, Integer method) {	
		Date d = session.getFilterDate();
		if (d == null) {
			DocearLogger.error("NDSM no filter date available");
			return;
		}
		
		Long filterDate = d.getTime();
		
		DocearLogger.info("NDSM: filterDate: " + new Date(filterDate).toString());
		
		for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
			Node map = it.next();
			if (getMapDate(map, method) < filterDate) {
				it.remove();
			}
		}
	}
	
//	/**
//	 * @param nodes
//	 * @param args
//	 * @param userModel
//	 */
//	 static void filterByDaysSinceLastForMaps(List<Node> maps, AlgorithmArguments args, UserModel userModel) {		 		 
//		 // do not filter the mindmap, if there is only one
//		 if (maps != null && maps.size() > 0) {
//				Map<String, Integer> daysSinceOldest = new HashMap<String, Integer>(); 
//				Set<Integer> daysSet = new HashSet<Integer>();
//				    
//				Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
//				
//				long firstNodeDate = 0;
//				int indexOfLastNode = maps.size() == 1 ? 0 : maps.size()-1;
//				long lastNodeDate = 0;
//				long nodeDate = 0;
//				Integer daysElapsedFromOldest = 0;
//				
//				switch (method) {
//					case 1: // 1=edited 
//						// date is given in the format yyyy-MM-dd HH:mm:ss
//						firstNodeDate = DateFilter.stringToMilliseconds(maps.get(0).getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
//						if (maps.get(indexOfLastNode).hasProperty("CREATED")) 
//							lastNodeDate = DateFilter.stringToMilliseconds(maps.get(indexOfLastNode).getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
//						break;
//					case 2: // 2=created 
//						firstNodeDate = Long.valueOf(maps.get(0).getProperty("dcr_id").toString().split("_")[0]);
//						if (maps.get(indexOfLastNode).hasProperty("dcr_id"))
//							// date is given in milliseconds along with a hash value
//							lastNodeDate = Long.valueOf(maps.get(indexOfLastNode).getProperty("dcr_id").toString().split("_")[0]); 
//				}
//				
//				for (Node node: maps) {
//					switch (method) {
//						case 1: // 1=edited 
//							if (node.hasProperty("CREATED"))
//								nodeDate = DateFilter.stringToMilliseconds(node.getProperty("CREATED").toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
//								break;
//						case 2: // 2=created (date is given in milliseconds)
//							if (node.hasProperty("dcr_id"))
//								nodeDate = Long.valueOf(node.getProperty("dcr_id").toString().split("_")[0]); 	
//					}
//					// the time difference from the oldest mindmap in the list
//					daysElapsedFromOldest = (int)TimeUnit.DAYS.convert(firstNodeDate - nodeDate, TimeUnit.MILLISECONDS);
//					daysSinceOldest.put(node.getProperty("ID").toString(), daysElapsedFromOldest);
//					daysSet.add(daysElapsedFromOldest);
//				}
//				
//				// get randomly the number of last days for which the mindmaps will be considered
//				Integer[] valueSetAsArray = daysSet.toArray(new Integer[daysSet.size()]);
//				int numberOfDaysSinceLast = valueSetAsArray[new Random().nextInt(valueSetAsArray.length)];
//				
//				int daysSinceMax = (int)TimeUnit.DAYS.convert(firstNodeDate - lastNodeDate, TimeUnit.MILLISECONDS);
//				// filter the nodes only if the returned value is lower than the maximum days since value
//				if (numberOfDaysSinceLast < daysSinceMax) 
//					for (Iterator<Node> it = maps.iterator(); it.hasNext();) {
//						Node node = it.next();
//						if ((Integer)daysSinceOldest.get(node.getProperty("ID").toString()) > numberOfDaysSinceLast)
//							it.remove();
//					}
//				
//				userModel.addVariable("no_days_since_max_maps", String.valueOf(daysSinceMax)); 
//				userModel.addVariable("no_days_since_chosen_maps", String.valueOf(numberOfDaysSinceLast)); 
//		 }
//	}
//	 
//	/**
//	 * @param nodes
//	 * @param args
//	 * @param userModelt
//	 */
//	static void filterByDaysSinceLastForNodes(Set<Node> nodes, AlgorithmArguments args, UserModel userModel) {		
//		Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
//		
//		 // do not filter the nodes, if there is only one
//		 if (nodes != null && nodes.size() > 0) {
//				String propertyName = "MODIFIED"; // to cover the case 1=edited	
//				Node[] nodesArray = nodes.toArray(new Node[nodes.size()]);
//				
//				switch (method) {
//					case 2: // 2=created 
//						propertyName = "CREATED";
//						break;
//					case 3: // 3=moved 
//						propertyName = "MOVED";
//				}
//				// get the date for teh newest (firstNodeDate) and the oldest (lastNodeDate) created node
//				long mostRecentNodeDate = getMaxDate(nodes, method);
//				long oldestNodeDate = getMinDate(nodes, method);
//				
//				Map<String, Integer> daysSinceOldest = new HashMap<String, Integer>(); 
//				Set<Integer> daysSet = new HashSet<Integer>();
//				
//				int daysSinceMax = (int)TimeUnit.DAYS.convert(mostRecentNodeDate - oldestNodeDate, TimeUnit.MILLISECONDS);
//				
//				Integer daysElapsedFromOldest = 0;
//				
//				for (Node node: nodesArray) {
//					if (node.hasProperty(propertyName)) {
//						// the time difference from the oldest mindmap in the list
//						daysElapsedFromOldest = (int)TimeUnit.DAYS.convert(mostRecentNodeDate - Long.valueOf(node.getProperty(propertyName).toString()), TimeUnit.MILLISECONDS);
//					    daysSinceOldest.put(node.getProperty("ID").toString(), daysElapsedFromOldest);
//						daysSet.add(daysElapsedFromOldest);
//					}
//				}
//				
//				// get randomly the number of last days for which the mindmaps will be considered
//				Integer[] valueSetAsArray = daysSet.toArray(new Integer[daysSet.size()]);
//				int numberOfDaysSinceLast = valueSetAsArray[new Random().nextInt(valueSetAsArray.length)];
//				
//				// filter the nodes only if the returned value is lower than the maximum days since value
//				if (numberOfDaysSinceLast < daysSinceMax) 
//					for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
//						Node node = it.next();
//						if ((Integer)daysSinceOldest.get(node.getProperty("ID").toString()) > numberOfDaysSinceLast)
//							it.remove();
//					}
//				
//				userModel.addVariable("no_days_since_max_nodes", String.valueOf(daysSinceMax)); 
//				userModel.addVariable("no_days_since_chosen_nodes", String.valueOf(numberOfDaysSinceLast));
//		 }
//	}
//	
//	static long getMaxDate(Set<Node> nodes, Integer method) {
//		long maxDate = -1;
//		String propertyName = "MODIFIED"; // to cover the case 1=edited	
//		
//		switch (method) {
//		case 2: // 2=created 
//			propertyName = "CREATED";
//			break;
//		case 3: // 3=moved 
//			propertyName = "MOVED";
//		}
//		
//		for (Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
//			Node node = it.next();			
//			if (node.hasProperty(propertyName)) {
//				Long nodeDate = Long.valueOf(node.getProperty(propertyName).toString());
//				if (nodeDate > maxDate) {
//					maxDate = nodeDate;
//				}
//			}			
//		}
//		return maxDate;
//	}
//	
//	static long getMinDate(Set<Node> nodes, Integer method) {
//		long minDate = Long.MAX_VALUE;
//		String propertyName = "MODIFIED"; // to cover the case 1=edited	
//		
//		switch (method) {
//		case 2: // 2=created 
//			propertyName = "CREATED";
//			break;
//		case 3: // 3=moved 
//			propertyName = "MOVED";
//		}
//		
//		for (Iterator<Node> it = nodes.iterator(); it.hasNext(); ) {
//			Node node = it.next();			
//			if (node.hasProperty(propertyName)) {
//    			Long nodeDate = Long.valueOf(node.getProperty(propertyName).toString());
//    			if (nodeDate < minDate) {
//    				minDate = nodeDate;
//    			}
//			}
//		}
//		return minDate;
//	}
	
	private static Long stringToMilliseconds(String dateString) {
		Long date = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = dateFormat.parse(dateString).getTime();
		}
		catch(Exception ignore) {			
		}
		
		if (date == null) {
			try {
				date = new Date(Long.parseLong(dateString)).getTime();		
			}
			catch(Exception ignore) {
			}
		}
		
		if (date == null) {
			System.out.println("org.docear.graphdb.DateFilter.stringToMilliseconds(String): \"" + dateString + "\" cannot be parsed!");
		}
	
		return date;
	}
	
}
