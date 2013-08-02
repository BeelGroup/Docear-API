package org.docear.graphdb;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.docear.database.AlgorithmArguments;
import org.docear.graphdb.relationship.Type;
import org.docear.graphdb.relationship.UserRelationship;
import org.docear.graphdb.threading.GraphCreatorJob;
import org.docear.query.HashReferenceItem;
import org.docear.structs.NodeInfo;
import org.docear.structs.ReferenceInfo;
import org.docear.xml.UserModel;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

/**
* This class is responsible for extracting the required nodes and relevant information from the graph DB.
* @author beel, stlanger, marcel, gkapi
* @version 1.x
*/

public class GraphDbWorker {
	
	private final static AlgorithmArguments ALGORITHM_FOR_ALL_USER_ELEMENTS;
	static {
		ALGORITHM_FOR_ALL_USER_ELEMENTS = new AlgorithmArguments();
		ALGORITHM_FOR_ALL_USER_ELEMENTS.setAlgorithmForAllElements();
	}
	
	private final static Set<String> DEMO_NODES = new HashSet<String>(Arrays.asList(new String[] {"ID_1723255651", "ID_1201323859", "ID_949131562", "ID_1136372076", "ID_396380598", 
		"ID_1443707089", "ID_1314196426", "ID_684647090", "ID_958748268", "ID_669246343", "ID_671957684", "ID_633568829", "ID_88393893", 
		"ID_834603878", "ID_635125153", "ID_637808618", "ID_1492445323", "ID_1866470331", "ID_268562794", "ID_1000594438", "ID_1913544196", 
		"ID_492177429", "ID_1650532587", "ID_1475491030", "ID_920592203", "ID_359495161", "ID_1818652651", "ID_1286991743", "ID_1563401899", 
		"ID_775383505", "ID_1145413517", "ID_695547925", "ID_1706806178", "ID_251122884", "ID_1625173874", "ID_298059862", "ID_1373336333", 
		"ID_1850957007", "ID_897735871", "ID_455171254", "ID_1947531230", "ID_1916608265", "ID_414996855", "ID_1141289532", "ID_1880424274", 
		"ID_1085988171", "ID_1896466450", "ID_25606850", "ID_1316465405", "ID_210411846", "ID_1039608161", "ID_734818819", "ID_182288749", 
		"ID_1866937594", "ID_549788626", "ID_166372850", "ID_1643291026", "ID_658844051", "ID_1046976431", "ID_402486912", "ID_647468086", 
		"ID_1698803512", "ID_1791784277", "ID_418884785", "ID_850664765", "ID_1137752462", "ID_703448322", "ID_1297465718", "ID_1886246636", 
		"ID_566654208", "ID_460451549", "ID_349142527", "ID_691117196", "ID_1282796891", "ID_1030916019", "ID_282700923", "ID_1422384115", 
		"ID_1845462789", "ID_1616796999", "ID_1891755637", "ID_871612900", "ID_1546013773", "ID_1792512538", "ID_452528799", "ID_1279466317", 
		"ID_1178635212", "ID_1513162719", "ID_701321157", "ID_1117394712", "ID_1864216888", "ID_136570750", "ID_792588810", "ID_1304861695", 
		"ID_308113122", "ID_481316229", "ID_154100943", "ID_130342491", "ID_1287672899", "ID_304901584", "ID_1649686681", "ID_1380349306", 
		"ID_853514210", "ID_236562698", "ID_600717826", "ID_1398434521", "ID_778625557", "ID_491553564", "ID_1226802166", "ID_649461033", 
		"ID_971039626", "ID_525837283", "ID_1915898063", "ID_1318535933", "ID_1957467098", "ID_1434886095", "ID_1580203669", "ID_1850169993", 
		"ID_269140470", "ID_634699852", "ID_204794864", "ID_1921629847", "ID_1119495334", "ID_932870811", "ID_33520894", "ID_401086023", 
		"ID_1227135545", "ID_577805129", "ID_708188547", "ID_64906310", "ID_570232913", "ID_275479838", "ID_316295013", "ID_365252525", 
		"ID_731165111", "ID_770978894", "ID_1787203578", "ID_1346798590", "ID_837270340", "ID_623185460", "ID_1986629300", "ID_740548951", 
		"ID_1241321237", "ID_744937414", "ID_1640385861", "ID_1887373749", "ID_1467097879", "ID_1591431726", "ID_1452058670", "ID_1721266520", 
		"ID_1080334653", "ID_1153374301", "ID_559653108", "ID_729680291", "ID_784567414", "ID_942602768", "ID_195571125", "ID_1899637700", 
		"ID_493071469", "ID_200987983", "ID_1576241944", "ID_150087640", "ID_836153271", "ID_1641903022", "ID_1533218274", "ID_184652233", 
		"ID_527031514", "ID_484280163", "ID_1934165708", "ID_115851811", "ID_694591499", "ID_73557042", "ID_1862662878", "ID_1622116177", 
		"ID_775745861", "ID_1871601649", "ID_386687607", "ID_156487929", "ID_1710120997", "ID_1620693421", "ID_1932019998", "ID_448465592", 
		"ID_43166963", "ID_1247812247", "ID_76148427", "ID_213818615", "ID_1504972219", "ID_822106860", "ID_1954347648", "ID_657573964", 
		"ID_1723255651", "ID_989309610", "ID_1723255651", "ID_989309610", "ID_1723255651", "ID_989309610", "ID_1842977434", "ID_1133327934", 
		"ID_1088249400", "ID_1644946250", "ID_1677163355", "ID_1343576812", "ID_1384418042", "ID_1530515638", "ID_878641558", "ID_639641377", 
		"ID_900629802", "ID_1859668322", "ID_1723255651", "ID_1626968962", "ID_1272394717", "ID_37380019", "ID_34223233", "ID_110842003", 
		"ID_61635320", "ID_492113293", "ID_692315417", "ID_508046962", "ID_1449904402", "ID_1346712303", "ID_1559932570", "ID_1840009540", 
		"ID_859276746", "ID_896226000", "ID_1949034820", "ID_1324525201", "ID_243626125", "ID_86954645", "ID_1961778618", "ID_511389851", 
		"ID_1102927270", "ID_989309610", "ID_1723255651", "ID_1626968962", "ID_1702652111", "ID_1064987695", "ID_1388901423", "ID_745579181", 
		"ID_1574325399", "ID_1682353811", "ID_1085246510", "ID_1220638992", "ID_338700635", "ID_1507770469", "ID_1123671529", "ID_722288763", 
		"ID_1423006439", "ID_1269985682", "ID_726353834", "ID_1031563798", "ID_1236947106", "ID_1106797781", "ID_1773051357", "ID_801698581",
		"ID_1126175486", "ID_194005043", "ID_489028120", "ID_741046291", "ID_829680537", "ID_784381737", "ID_1778320028", "ID_1910886753", 
		"ID_1174558250", "ID_889354645", "ID_1396983859", "ID_1238582164", "ID_1174386984", "ID_1400984546", "ID_912535957", "ID_1930051329", 
		"ID_1392476056", "ID_236427717", "ID_1085792533", "ID_123563531", "ID_1940856338", "ID_721576223", "ID_1417884650", "ID_1850076380", 
		"ID_373642256", "ID_1289497345", "ID_331289321", "ID_1616130138", "ID_142245057", "ID_1454640722", "ID_831327175", "ID_1759697893", 
		"ID_625521591", "ID_591529648", "ID_1671464522", "ID_1568709921", "ID_206518827", "ID_626563939", "ID_1784808313", "ID_824695411", 
		"ID_1422394808", "ID_1706012736", "ID_603428882", "ID_1908326084", "ID_1841216507", "ID_996296940", "ID_1460019399", "ID_1158812201", 
		"ID_308497549", "ID_716859086", "ID_1346712303", "ID_507843905", "ID_1175633579", "ID_508046962", "ID_1449904402", "ID_1720860152", 
		"ID_989309610", "ID_1723255651", "ID_1693751078", "ID_1380533534", "ID_199412342", "ID_1123671529", "ID_1314800443", "ID_1013913135", 
		"ID_577115243", "ID_797443631", "ID_1481327252", "ID_422948839", "ID_219875762", "ID_1309584788", "ID_1098134891", "ID_512165507", 
		"ID_1200714877", "ID_972169939", "ID_1392476056", "ID_414399340", "ID_366012179", "ID_389971830", "ID_1940856338", "ID_56949802", 
		"ID_40512105", "ID_1773171427", "ID_591738915", "ID_227074519", "ID_837868971", "ID_1595771395", "ID_220604319", "ID_1262024868", 
		"ID_335508516", "ID_183681093", "ID_989309610", "ID_916121753", "ID_1266382435", "ID_196579720", "ID_156430450", "ID_1086499639", 
		"ID_624499378", "ID_158830913"}));

	private final GraphDatabaseService database;
		
	private final static Map<String, List<String>> excludeNodeTextOnType = new TreeMap<String, List<String>>();
	static {
		excludeNodeTextOnType.put("incoming", Arrays.asList("Incoming"));
		excludeNodeTextOnType.put("literature_annotations", Arrays.asList("Literature &amp; Annotations"));
		excludeNodeTextOnType.put("my_publications", Arrays.asList("My Publications"));
		excludeNodeTextOnType.put("trash", Arrays.asList("Trash"));
		excludeNodeTextOnType.put("temp", Arrays.asList("Temp"));
		excludeNodeTextOnType.put("", Arrays.asList("New Mindmap", "Neue Mindmap"));
	}

	public GraphDbWorker(GraphDatabaseService graphDb) {
		this.database = graphDb;
	}
	
	public Set<Node> getRelevantNodes(int userId, AlgorithmArguments args, UserModel userModel, String excludePdfHash) {
		Collection<Node> allUserMaps = getMapsForUser(userId, ALGORITHM_FOR_ALL_USER_ELEMENTS, userModel);
		Long minExcludeDate = null;
		// needed for offline evaluator: skip this paper and all nodes that have been created after the user has used this paper
		//FIXME: !!! this only means that we get the minimum date of the node holding the pdfhash - not the date, when the pdf was added to the node
		if (excludePdfHash != null) {
			minExcludeDate = getMinCreatedDateByPdfHash(allUserMaps, excludePdfHash);
		}
		
		addTotalCountVariables(allUserMaps, args, userModel, minExcludeDate);		
		Collection<NodeRevision> nodeSet = getNodeCollection(userId, args, userModel, minExcludeDate);

		if(nodeSet == null) {
			return null;
		}
		
		Set<Node> nodes = new HashSet<Node>();
		Iterator<NodeRevision> iter = nodeSet.iterator();		
			
		// get random number from size+1 --> amount==0 means take all, everything else means the size itself
		int amount = new Random().nextInt(Math.min(nodeSet.size(), AlgorithmArguments.MAX_ELEMENT_AMOUNT)+1);
				
		if (amount > 0) {
			if (amount > nodeSet.size()) {
				// not enough nodes
				return null;
			}
			// for (NodeRevision n : nodes) {
			for (int i=0; iter.hasNext() && i<amount; i++) {
				NodeRevision revNode = iter.next();				
				if (new Integer(1).equals(args.getArgument(AlgorithmArguments.ROOTPATH))) {
					appendUpToRoot(revNode, nodes, ((Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT_TYPE) == 2), minExcludeDate);
				}
				if (new Integer(1).equals(args.getArgument(AlgorithmArguments.SIBLINGS))) {
					appendSiblings(revNode, nodes, ((Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT_TYPE) == 2), minExcludeDate);
				}
								
				nodes.add(revNode.getNode());				
				
				// only depth 1 for now
				if (new Integer(1).equals(args.getArgument(AlgorithmArguments.CHILDREN))) {
					appendChildren(revNode, nodes, 1, 0, ((Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT_TYPE) == 2), minExcludeDate);
				}
			}
		} else {	
			// no amount is set, just take every node			
			while (iter.hasNext()) {
				amount++;
				NodeRevision revNode = iter.next();
				nodes.add(revNode.getNode());
			}
		}
		
		//remove demo nodes
		Set<Node> foundDemoNodes = new HashSet<Node>();		
		for (Node node : nodes) {
			if (node.hasProperty("ID") && DEMO_NODES.contains(node.getProperty("ID").toString())) {
				foundDemoNodes.add(node);
				amount--;
			}
		}
		for (Node node : foundDemoNodes) {
			nodes.remove(node);
		}
		System.out.println("demo-nodes: "+foundDemoNodes.size()+" of "+nodes.size());
		
		userModel.addVariable("element_amount_nodes", String.valueOf(amount));
		userModel.addVariable("node_count_before_expanded", String.valueOf(amount));
		userModel.addVariable("node_count_expanded", String.valueOf(nodes.size()));
		
		Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD); 		
		if (new Integer(2).equals(args.getArgument(AlgorithmArguments.DATA_ELEMENT)) && method != null && method > 0) 
			// get randomly the number of last days for which the nodes will be considered
			GraphDbHelper.filterByDaysSinceLastForNodes(nodes, args, userModel);
		
		return nodes;
	}
	
	private void addTotalCountVariables(Collection<Node> allMaps, AlgorithmArguments args, UserModel userModel, final Long minExcludeDate) {		
		userModel.addVariable("mind-map_count_total", ""+allMaps.size());
				
		int nodeCountTotal = 0;
		int linkCountTotal = 0;
		
		HashSet<String> hashes = new HashSet<String>();
		for (Node startNode : allMaps) {
			try {				
				final String mapType = startNode.getSingleRelationship(Type.REVISION, Direction.INCOMING).getStartNode().getProperty(GraphCreatorJob.PROPERTY_MAP_TYP).toString();
				Iterator<Path> it = getUserLatestRevisionsTraversal(startNode, mapType, ALGORITHM_FOR_ALL_USER_ELEMENTS, minExcludeDate).iterator();				
				while (it.hasNext()) {
					Path path = it.next();
					if (!path.endNode().hasProperty("ID") || !DEMO_NODES.contains(path.endNode().getProperty("ID").toString())) {
						nodeCountTotal++;
					}
				}
				
				Traverser traverser = getUserLatestRevisionsReferencesTraversal(startNode, mapType, ALGORITHM_FOR_ALL_USER_ELEMENTS, minExcludeDate);
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

	public List<NodeInfo> getUserNodesInfo(int userId, AlgorithmArguments args, UserModel userModel, String pdfHash) {
		return getUserNodesInfo(getRelevantNodes(userId, args, userModel, pdfHash), args);
	}
	
	public String getUserText(int userId, AlgorithmArguments args, UserModel userModel, String pdfHash) {
		return getUserText(getRelevantNodes(userId, args, userModel, pdfHash));
	}
	
	public void fillUserReferences(int userId, AlgorithmArguments args, UserModel userModel, String pdfHash) {		
		extractReferences(getRelevantNodes(userId, args, userModel, pdfHash), userModel);
	}
	
	private void extractReferences(Collection<Node> nodes, UserModel userModel) {		
		Map<String, HashReferenceItem> occurenceMap = new HashMap<String, HashReferenceItem>();
		
		int featureCountReferences = 0;
		Iterator<Node> iter = nodes.iterator();
		while (iter.hasNext()) {
			Node n = iter.next();
			try {				
				if (n.hasProperty("annotation_document_hash")) {
					String hash = String.valueOf(n.getProperty("annotation_document_hash"));
					
    				//retrieve counter item
    				HashReferenceItem item = occurenceMap.get(hash);
    				
    				
    				//create item if necessary 
    				if(item == null) {
    					item = new HashReferenceItem(hash);
    					occurenceMap.put(hash, item);
    				}
    				
    				featureCountReferences++;
    				
    				// increase the count
    				item.touch();
    				
    				// update title
    				if(n.hasProperty("attribute_title")) {					
    					item.setTitle(n.getProperty("attribute_title").toString(), false);
    
    				}				
    				else if(n.hasProperty("annotation_pdf_title")) {					
    					item.setTitle(n.getProperty("annotation_pdf_title").toString(), true);
    
    				}
				}
			} catch (Exception e) {
				e.printStackTrace();			
			}			
		}
		
		// expanded and reduced is the same for references, since stopwords can't be removed on references
		userModel.getReferences().addVariable("feature_count_expanded", ""+featureCountReferences);
		userModel.getReferences().addVariable("feature_count_reduced", ""+featureCountReferences);
		userModel.getReferences().addVariable("feature_count_expanded_unique", ""+occurenceMap.size());
		userModel.getReferences().addVariable("feature_count_reduced_unique", ""+occurenceMap.size());
		
		for (HashReferenceItem item : occurenceMap.values()) {
			userModel.getReferences().addReference(item.getDocumentTitle(), item.getDocumentHash(), (double) item.getCount());
		}
	}

	private Collection<NodeRevision> getNodeCollection(int userId, AlgorithmArguments args, UserModel userModel, final Long minExcludeDate) {
		Collection<Node> allMaps = getMapsForUser(userId, ALGORITHM_FOR_ALL_USER_ELEMENTS, userModel);		
		userModel.addVariable("mind-map_count_total", ""+allMaps.size());
		
		Collection<Node> maps = getMapsForUser(userId, args, userModel);
		
		if (maps == null || maps.size() == 0) {
			return null;
		}
		
		Collection<NodeRevision> nodeSet = null;
		
		// 2 = mind map nodes are considered
		if (new Integer(2).equals(args.getArgument(AlgorithmArguments.DATA_ELEMENT))) {
			final Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD); 			
						
			// 0=all, 1=edited, 2=created, 3=moved
			if (method != null) {
				nodeSet = new TreeSet<NodeRevision>(new Comparator<NodeRevision>() {
					public int compare(NodeRevision rev1, NodeRevision rev2) {
						switch (method) {
							case 1: {
								return rev2.getNode().getProperty("MODIFIED").toString().compareTo(rev1.getNode().getProperty("MODIFIED").toString());
							}
							case 2: {
								String str1 = rev1.getNode().getProperty("CREATED").toString();
								String str2 = rev2.getNode().getProperty("CREATED").toString();
								// DOCEAR - workaround for "freeplane root node created time" bug
								try {
									if (rev1.getNode().hasRelationship(Direction.INCOMING, Type.ROOT)) {
										str1 = rev1.getNode().getRelationships(Type.ROOT, Direction.INCOMING).iterator().next().getStartNode().getProperty("dcr_id").toString().split("_")[0];
									}
									if (rev2.getNode().hasRelationship(Direction.INCOMING, Type.ROOT)) {
										str2 = rev2.getNode().getRelationships(Type.ROOT, Direction.INCOMING).iterator().next().getStartNode().getProperty("dcr_id").toString().split("_")[0];
										//str2 = rev2.getNode().getSingleRelationship(Type.ROOT, Direction.INCOMING).getStartNode().getProperty("dcr_id").toString();
									}
								} catch (Exception e) {
									e.printStackTrace();
									return 1;
								}
								
								int comp2 = str2.compareTo(str1);
								// return (o1.getProperty("CREATED").toString().compareTo(o2.getProperty("CREATED").toString()));
								return (comp2);
							}
							case 3: {
								int comp = 0;
								try {
									long rev1Mod = Long.parseLong(rev1.getNode().getProperty("MOVED").toString());
									long rev2Mod = Long.parseLong(rev2.getNode().getProperty("MOVED").toString());
									comp = (int) (rev2Mod-rev1Mod);
								}
								catch (Exception e) {
									comp = 1;
								}
								return comp;
							}
							default: {
								int comp = 0;
								//modified times
								long rev1Mod = Long.parseLong(rev1.getNode().getProperty("MODIFIED").toString());
								long rev2Mod = Long.parseLong(rev2.getNode().getProperty("MODIFIED").toString());
								
								//created times
								String str1 = rev1.getNode().getProperty("CREATED").toString();
								String str2 = rev2.getNode().getProperty("CREATED").toString();
								// DOCEAR - workaround for "freeplane root node created time" bug
								try {
									if (rev1.getNode().hasRelationship(Direction.INCOMING, Type.ROOT)) {
										str1 = rev1.getNode().getRelationships(Type.ROOT, Direction.INCOMING).iterator().next().getStartNode().getProperty("dcr_id").toString().split("_")[0];
									}									
								} 
								catch (Exception e) {
								}								
								rev1Mod = Math.max(rev1Mod, Long.parseLong(str1));
								
								try {
									if (rev2.getNode().hasRelationship(Direction.INCOMING, Type.ROOT)) {
										str2 = rev2.getNode().getRelationships(Type.ROOT, Direction.INCOMING).iterator().next().getStartNode().getProperty("dcr_id").toString().split("_")[0];
									}
								} 
								catch (Exception e) {
								}
								rev2Mod = Math.max(rev2Mod, Long.parseLong(str2));
								
								//moved times
								try {								
									if(rev1.getNode().hasProperty("MOVED")) {
										rev1Mod = Math.max(rev1Mod, Long.parseLong(rev1.getNode().getProperty("MOVED").toString()));
									}
								}
								catch (Exception e) {									
								}
								try {
									if(rev2.getNode().hasProperty("MOVED")) {
										rev2Mod = Math.max(rev2Mod, Long.parseLong(rev2.getNode().getProperty("MOVED").toString()));
									}									
								}
								catch (Exception e) {									
								}
								comp = (int) (rev2Mod-rev1Mod);
								return comp;
							}
						}
					}
				});
			}
		}
		if (nodeSet == null) {
			nodeSet = new HashSet<NodeRevision>();
		}
		
		for (Node startNode : maps) {
			try {
				collectNodes(nodeSet, startNode, args, minExcludeDate);
				System.out.println("user("+userId+"): use map "+startNode.getProperty("ID").toString());
			}
			catch(Throwable t) {
				t.printStackTrace();
			}
		}	
		
		return nodeSet;
	}

	private void appendUpToRoot(NodeRevision revNode, Collection<Node> nodes, boolean referencesOnly, Long minExcludeDate) {
		Iterable<Relationship> parents = revNode.getNode().getRelationships(Type.CHILD, Direction.INCOMING);
		for (Relationship parent : parents) {
			if (parent.hasProperty(revNode.getRevisionKey())) {
				//take nodes with reference information only
				if(referencesOnly) {
					appendUpToRoot(new NodeRevision(parent.getStartNode(), revNode.getRevisionKey(), revNode.getMapType()), nodes, referencesOnly, minExcludeDate);
					if(parent.getStartNode().hasProperty("annotation_document_hash") || parent.getStartNode().hasProperty("annotation_pdf_title")) {
						//take 'public' nodes only into account
						if (minExcludeDate == null || !parent.hasProperty("MODIFIED") || Long.valueOf(parent.getProperty("MODIFIED").toString()) < minExcludeDate) {
							if(!parent.getStartNode().hasProperty("DCR_PRIVACY_LEVEL") || "PUBLIC".equals(parent.getStartNode().getProperty("DCR_PRIVACY_LEVEL"))) {
								nodes.add(parent.getStartNode());
							}							
						}
					}
				} else {
					if (parent.getStartNode().hasRelationship(Type.ROOT, Direction.INCOMING)) {
						if (parent.getStartNode().hasProperty("LOCALIZED_TEXT")) {
							continue;
						}
						String text = extractText(parent.getStartNode());
						List<String> excludeList = excludeNodeTextOnType.get(revNode.getMapType());
						if (excludeList != null && excludeList.contains(text)) {
							continue;
						}
					}
					appendUpToRoot(new NodeRevision(parent.getStartNode(), revNode.getRevisionKey(), revNode.getMapType()), nodes, referencesOnly, minExcludeDate);
					//take 'public' nodes only into account
					if(!parent.getStartNode().hasProperty("DCR_PRIVACY_LEVEL") || "PUBLIC".equals(parent.getStartNode().getProperty("DCR_PRIVACY_LEVEL"))) {
						nodes.add(parent.getStartNode());
					}
				}
			}
		}
	}

	private void appendSiblings(NodeRevision revNode, Collection<Node> nodes, boolean referencesOnly, Long minExcludeDate) {
		Iterable<Relationship> parents = revNode.getNode().getRelationships(Type.CHILD, Direction.INCOMING);
		for (Relationship parent : parents) {
			if (parent.hasProperty(revNode.getRevisionKey())) {
				if (minExcludeDate == null || !parent.hasProperty("MODIFIED") || Long.valueOf(parent.getProperty("MODIFIED").toString()) < minExcludeDate) {
					appendChildren(new NodeRevision(parent.getStartNode(), revNode.getRevisionKey(), revNode.getMapType()), nodes, 1, 0, referencesOnly, minExcludeDate);
				}
			}
		}

	}

	private void appendChildren(NodeRevision revNode, Collection<Node> nodes, final int maxDepth, int currentDepth, boolean referencesOnly, Long minExcludeDate) {
		if(maxDepth <= currentDepth) {
			return;
		}
		currentDepth++;
		Iterable<Relationship> children = revNode.getNode().getRelationships(Type.CHILD, Direction.OUTGOING);
		for (Relationship child : children) {
			if (child.hasProperty(revNode.getRevisionKey())) {
				if (minExcludeDate == null || !child.getEndNode().hasProperty("MODIFIED") || Long.valueOf(child.getEndNode().getProperty("MODIFIED").toString()) < minExcludeDate) {
    				if(!referencesOnly || child.getEndNode().hasProperty("annotation_document_hash") || child.getEndNode().hasProperty("annotation_pdf_title")) {
    					//take 'public' nodes only into account
    					if(!child.getEndNode().hasProperty("DCR_PRIVACY_LEVEL") || "PUBLIC".equals(child.getEndNode().getProperty("DCR_PRIVACY_LEVEL"))) {
    						nodes.add(child.getEndNode());
    					}
    				}
				}
				//nodes.add(child.getEndNode());
				appendChildren(new NodeRevision(child.getEndNode(), revNode.getRevisionKey(), revNode.getMapType()), nodes, maxDepth, currentDepth, referencesOnly, minExcludeDate);
			}
		}
	}

	private void collectNodes(Collection<NodeRevision> nodes, Node mapRoot, AlgorithmArguments args, Long minExludeDate) {
		final String mapType = mapRoot.getSingleRelationship(Type.REVISION, Direction.INCOMING).getStartNode().getProperty(GraphCreatorJob.PROPERTY_MAP_TYP).toString();
		Traverser traverser = null;
		if (new Integer(2).equals((Integer) args.getArgument(AlgorithmArguments.DATA_ELEMENT_TYPE))) {
			traverser = getUserLatestRevisionsReferencesTraversal(mapRoot, mapType, args, minExludeDate);
		}
		else {
			//for DATA_ELEMENT_TYPE 0 and 1 use normal traverser
			traverser = getUserLatestRevisionsTraversal(mapRoot, mapType, args, minExludeDate);
		}
		final String revisionKey = mapRoot.getProperty("ID").toString();
		
		for (Node n : traverser.nodes()) {
			//take 'public' nodes only into account
			if(!n.hasProperty("DCR_PRIVACY_LEVEL") || "PUBLIC".equals(n.getProperty("DCR_PRIVACY_LEVEL"))) {
				// ignore folded or unfolded nodes based on the algorithm parameter value
				switch((Integer)args.getArgument(AlgorithmArguments.NODE_VISIBILITY)) {
					case 1:
						if (!n.hasProperty("FOLDED") || (n.hasProperty("FOLDED") && !"true".equals(n.getProperty("FOLDED").toString())))
							nodes.add(new NodeRevision(n, revisionKey, mapType));		
						break;
					case 2:
						if (n.hasProperty("FOLDED") && "true".equals(n.getProperty("FOLDED").toString()))
							nodes.add(new NodeRevision(n, revisionKey, mapType));	
						break;
					default: //use all, case 0 or NULL
						nodes.add(new NodeRevision(n, revisionKey, mapType));
				}
			}
		}
	}

	/**
	 * @param collection of nodes on the user mind map
	 * @return arraylist of node info with information on each node
	 */
	private List<NodeInfo> getUserNodesInfo(Collection<Node> nodes, AlgorithmArguments args) {		
		if (nodes == null || nodes.size() == 0) {
			return null;
		}
		
		List<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();		
		try {			
			for (Node node: nodes)
				nodeInfos.add(extraxtNodeInfo(node, args));
			return nodeInfos;
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return null;
	}
	
	private String getUserText(Collection<Node> nodes) {		
		if (nodes == null) {
			return null;
		}
		
		StringBuffer buffer = new StringBuffer();		
		try {			
			Iterator<Node> iter = nodes.iterator();
			while (iter.hasNext()) {
				appendToBuffer(extractText(iter.next()), buffer);
			}
			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();			
		}
		return null;
	}

	/**
	 * @param node
	 * @return node info object
	 */
	private NodeInfo extraxtNodeInfo(Node node, AlgorithmArguments args) {
		NodeInfo nodeInfo = new NodeInfo();
		nodeInfo.setId(node.getProperty("ID").toString());
		
		// make only the necessary node-related calculations
		if (args.getArgument(AlgorithmArguments.NODE_DEPTH) != null 
				&& !new Integer(0).equals(args.getArgument(AlgorithmArguments.NODE_DEPTH)))
			nodeInfo.setDepth(calculateDepth(node));
		
		if (args.getArgument(AlgorithmArguments.NO_SIBLINGS) != null 
				&& !new Integer(0).equals(args.getArgument(AlgorithmArguments.NO_SIBLINGS))) 
			nodeInfo.setNoOfSiblings(calculateNoOfSiblings(node));
		
		if (args.getArgument(AlgorithmArguments.NO_CHILDREN) != null 
				&& !new Integer(0).equals(args.getArgument(AlgorithmArguments.NO_CHILDREN))) {
			switch ((Integer)args.getArgument(AlgorithmArguments.NO_CHILDREN_LEVEL)) {
				case 1:
					nodeInfo.setNoOfChildren((calculateNoOfChildren(node)));;
					break;
				case 2:
					nodeInfo.setNoOfChildren((calculateNoOfSubtreeNodes(node)));;
			}
		}

		if (args.getArgument(AlgorithmArguments.WORD_COUNT) != null 
				&& !new Integer(0).equals(args.getArgument(AlgorithmArguments.WORD_COUNT))) 
			nodeInfo.setWordCount((calculateWordCount(node)));;

		if (args.getArgument(AlgorithmArguments.NODE_INFO_SOURCE) != null) {
			nodeInfo.setText(extractText(node));
			nodeInfo.setReference(extractReference(node));
			nodeInfo.setPdfTitle(extractPdfTitle(node));
		}		
		return nodeInfo;
	}
	
	private String extractText(Node node) {
		StringBuffer text = new StringBuffer();
		if (node.hasProperty("TEXT")) {
			appendToBuffer(node.getProperty("TEXT").toString(), text);
		}
		if (node.hasProperty("LOCALIZED_TEXT")) {
			appendToBuffer(node.getProperty("LOCALIZED_TEXT").toString(), text);
		}
		if (node.hasProperty("NODE")) {
			appendToBuffer(extractText(node.getProperty("NODE").toString()), text);
		}
		if (node.hasProperty("NOTE")) {
			appendToBuffer(extractText(node.getProperty("NOTE").toString()), text);
		}

		return filterText(text.toString());
	}
	
	private ReferenceInfo extractReference(Node node) {			
		if (node.hasProperty("LINK") && node.hasProperty("attribute_title")) {
			ReferenceInfo refInfo = new ReferenceInfo();
			refInfo.setTitle(node.getProperty("attribute_title").toString());
			if (node.hasProperty("attribute_journal")) 
				refInfo.setJournal(node.getProperty("attribute_journal").toString());
			if (node.hasProperty("attribute_authors")) 
				refInfo.setAuthors(node.getProperty("attribute_authors").toString());
			if (node.hasProperty("attribute_year")) 
				refInfo.setYear(Integer.valueOf(node.getProperty("attribute_year").toString()));
			return refInfo;
		}
		return null;
	}
	
	private String extractPdfTitle(Node node) {	
		// add the field only if it has not already been added as a reference
		if (node.hasProperty("LINK") && !node.hasProperty("attribute_title")) {
			try {
				String[] linkParts = java.net.URLDecoder.decode(node.getProperty("LINK").toString(), "UTF-8").trim().split(File.separator);
				return linkParts[linkParts.length-1].split(".pdf")[0];
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * @param node
	 * @return the depth of a node on the mind map (distance from root mind map node)
	 */
	private Integer calculateDepth(Node node) {
		Iterable<Relationship> parents = node.getRelationships(Type.CHILD, Direction.INCOMING);
		if (parents != null) {
			for (Relationship parent : parents) {
				return new Integer(1) + calculateDepth(parent.getStartNode());
			}
		}
		return new Integer(0);
	}
	
	/**
	 * @param node
	 * @return the number of siblings of the node (on the mind map)
	 */
	private Integer calculateNoOfSiblings(Node node) {
		Integer noOfSiblings = 0;
		Iterable<Relationship> parents = node.getRelationships(Type.CHILD, Direction.INCOMING);
		
		// each mind map node has only one parent 
		for (Relationship parent : parents) {
			noOfSiblings += calculateNoOfChildren(parent.getStartNode());
		}
		// minus 1 since the node itself is also considered
		return noOfSiblings - 1;
	}
	
	/**
	 * @param node
	 * @return the number of children of the node (on the mind map)
	 */
	private Integer calculateNoOfChildren(Node node) {
		Integer noOfChildren = 0;
		Iterator<Relationship> it = node.getRelationships(Type.CHILD, Direction.OUTGOING).iterator();
		
		while(it.hasNext()) {
			it.next(); 
			noOfChildren++;
		}
		return noOfChildren;
	}
	
	/**
	 * @param node
	 * @return the number of nodes for the tree that has as root the node used as parameter
	 */
	private Integer calculateNoOfSubtreeNodes(Node node) {
		Integer noOfChildren = 0;
		Iterator<Relationship> it = node.getRelationships(Type.CHILD, Direction.OUTGOING).iterator();
		
		while(it.hasNext()) {
			Relationship relation = it.next(); 
			noOfChildren++;
			noOfChildren += calculateNoOfSubtreeNodes(relation.getEndNode());
		}
		return noOfChildren;
	}
	
	/**
	 * @param node
	 * @return the number of words on that node
	 */
	private Integer calculateWordCount(Node node) {
		String text = extractText(node).trim();
	   if (text.isEmpty()) 
		   return 0;
	   return text.split("\\s+").length; //separate string around spaces
	}
	
	private String filterText(String text) {
		return text.toLowerCase()
				// image extensions
				.replace(".tiff", "")
				.replace(".png", "")
				.replace(".gif", "")
				.replace(".bmp", "")
				.replace(".jpg", "")
				.replace(".jpeg", "")
				// ms office extensions
				.replace(".docx", "")
				.replace(".doc", "")
				.replace(".xls", "")
				.replace(".xlsx", "")
				.replace(".ppt", "")
				//openoffice extensions
				.replace(".odt", "")
				.replace(".ods", "")
				.replace(".odp", "")
				.replace(".odg", "")
				.replace(".odf", "")
				.replace(".odi", "")
				.replace(".odm", "")
				.replace(".ott", "")
				.replace(".ots", "")
				.replace(".otp", "")
				.replace(".otg", "")
				//web
				.replace(".css", "")
				.replace(".js", "")
				.replace(".xml", "")
				.replace(".html", "")
				.replace(".htm", "")
				.replace(".xhtml", "")
				.replace(".xsd", "")
				.replace(".xsl", "")
				.replace(".php", "")				
				.replace(".cgi", "")
				// others
				.replace(".pdf", "")
				.replace(".mm", "")
				.replace(".dcr", "")
				.replace(".zip", "")
				.replace(".ps", "")
				.replace(".avi", "")
				.replace(".mp3", "")
				.replace(".mp2", "")
				.replace(".mpg", "")
				.replace(".mpeg", "")
				
				
				;
	}

	private String extractText(String html) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("");

		ParserDelegator parserDelegator = new ParserDelegator();

		ParserCallback parserCallback = new ParserCallback() {

			public void handleText(final char[] data, final int pos) {
				stringBuilder.append(data);
			}

			public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) {
			}

			public void handleEndTag(Tag t, final int pos) {
			}

			public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) {
			}

			public void handleComment(final char[] data, final int pos) {
			}

			public void handleError(final java.lang.String errMsg, final int pos) {
			}
		};

		try {
			parserDelegator.parse(new StringReader(html), parserCallback, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stringBuilder.toString();
	}

	private void appendToBuffer(String text, StringBuffer buffer) {
		buffer.append(text);
		buffer.append(" ");
	}
	
	public Long getMinCreatedDateByPdfHash(Collection<Node> allUserMaps, final String pdfHash) {
		Long minDate = null;
		for (Node map: allUserMaps) {
			Traverser traverser = getUserNodesByPdfHashTraversal(map, pdfHash);
			for (Node node : traverser.nodes()) {				
				Long created = Long.valueOf(node.getProperty("CREATED").toString());
				if (minDate == null || created < minDate) {
					minDate = created;
				}				
			}
		}
		
		return minDate;
	}
	
	private Traverser getUserNodesByPdfHashTraversal(final Node parent, final String pdfHash) {
		TraversalDescription td = Traversal.description().relationships(Type.CHILD, Direction.OUTGOING).relationships(Type.ROOT, Direction.OUTGOING).uniqueness(Uniqueness.NODE_PATH).depthFirst().evaluator(new Evaluator() {
			private final String revisionKey = parent.getProperty("ID").toString();
			
			public Evaluation evaluate(final Path path) {
				if (path.length() == 0) {
					return Evaluation.EXCLUDE_AND_CONTINUE;
				} 
				else if (path.lastRelationship().hasProperty(revisionKey)) {					
					if(path.endNode().hasProperty("annotation_document_hash") && path.endNode().getProperty("annotation_document_hash").equals(pdfHash)) {
						return Evaluation.INCLUDE_AND_CONTINUE;
					}
					return Evaluation.EXCLUDE_AND_CONTINUE;
				}
				return Evaluation.EXCLUDE_AND_PRUNE;			
			}
		});
		
		return td.traverse(parent);
	}

	private Traverser getUserLatestRevisionsReferencesTraversal(final Node parent, final String mapType, final AlgorithmArguments args, final Long minExludeDate) {
		TraversalDescription td = Traversal.description().relationships(Type.CHILD, Direction.OUTGOING).relationships(Type.ROOT, Direction.OUTGOING).uniqueness(Uniqueness.NODE_PATH).depthFirst().evaluator(new Evaluator() {
			private final String revisionKey = parent.getProperty("ID").toString();
			private final Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
			
			public Evaluation evaluate(final Path path) {
				if (path.length() == 0) {
					return Evaluation.EXCLUDE_AND_CONTINUE;
				} 
				else if (path.lastRelationship().hasProperty(revisionKey)) {
					//if only nodes with moved property (method=3)
					if(method == 3) {
						if(!path.endNode().hasProperty("MOVED")) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}
					if(minExludeDate != null) {
						if (path.endNode().hasProperty("MODIFIED") && minExludeDate <= Long.valueOf(path.endNode().getProperty("MODIFIED").toString())) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}
					if(path.endNode().hasProperty("annotation_document_hash") || path.endNode().hasProperty("annotation_pdf_title")) {
						return Evaluation.INCLUDE_AND_CONTINUE;
					}
					return Evaluation.EXCLUDE_AND_CONTINUE;
				}
				return Evaluation.EXCLUDE_AND_PRUNE;
			}
		});

		return td.traverse(parent);
	}
	
	private Traverser getUserLatestRevisionsTraversal(final Node parent, final String mapType, final AlgorithmArguments args, final Long minExludeDate) {
		TraversalDescription td = Traversal.description().relationships(Type.CHILD, Direction.OUTGOING).relationships(Type.ROOT, Direction.OUTGOING).uniqueness(Uniqueness.NODE_PATH).depthFirst().evaluator(new Evaluator() {
			private final String revisionKey = parent.getProperty("ID").toString();
			private final Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD);
			
			public Evaluation evaluate(final Path path) {
				if (path.length() == 0) {
					return Evaluation.EXCLUDE_AND_CONTINUE;
				} else if (path.length() == 1) {
					// root node
					if (path.endNode().hasProperty("LOCALIZED_TEXT")) {
						return Evaluation.EXCLUDE_AND_CONTINUE;
					}
					if(minExludeDate != null) {
						if (path.endNode().hasProperty("MODIFIED") && minExludeDate <= Long.valueOf(path.endNode().getProperty("MODIFIED").toString())) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}
					String text = extractText(path.endNode()).trim();
					List<String> excludeList = excludeNodeTextOnType.get(mapType);
					if (excludeList != null && excludeList.contains(text)) {
						return Evaluation.EXCLUDE_AND_CONTINUE;
					} else if(method == 3) {
						if(!path.endNode().hasProperty("MOVED")) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}
					return Evaluation.INCLUDE_AND_CONTINUE;
					
				} else if (path.lastRelationship().hasProperty(revisionKey)) {
					//if only nodes with moved property (method=3)
					if(method == 3) {
						if(!path.endNode().hasProperty("MOVED")) {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}
					if(minExludeDate != null) {
						if (path.endNode().hasProperty("MODIFIED") && minExludeDate <= Long.valueOf(path.endNode().getProperty("MODIFIED").toString())) {
							System.out.println("excluding node with modified date: "+path.endNode().getProperty("MODIFIED"));
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
						else {
							System.out.println("including node with modified date: "+path.endNode().getProperty("MODIFIED"));
						}
					}
					return Evaluation.INCLUDE_AND_CONTINUE;
				}
				return Evaluation.EXCLUDE_AND_PRUNE;
			}
		});

		return td.traverse(parent);
	}

	private List<Node> getMapsForUser(int userId, AlgorithmArguments args, UserModel userModel) {
		List<Node> allMaps = getLatestMapsForUser(userId, args);
		if(allMaps == null) {
			return null;
		}
		
		// if data_element is mind maps: order the mind maps
		if (new Integer(1).equals(args.getArgument(AlgorithmArguments.DATA_ELEMENT))) {			
			final Integer method = (Integer) args.getArgument(AlgorithmArguments.ELEMENT_SELECTION_METHOD); 

			if (method != null && method > 0) {
				Collections.sort(allMaps, new Comparator<Node>() {
					@Override
					public int compare(Node o1, Node o2) {
						switch (method) {
						case 1: // 1=edited
							String created1 = o1.getProperty("CREATED").toString(); //'created' on a revision node indicates the time when it was saved
							String created2 = o2.getProperty("CREATED").toString();
							int comp1 = created2.compareTo(created1);
							return (comp1);
						case 2: // 2=created
							String str1 = o1.getProperty("dcr_id").toString();
							String str2 = o2.getProperty("dcr_id").toString();
							int comp2 = str2.compareTo(str1);
							return (comp2);
						}

						return 0;
					}
				});
			}
			
			if (method != null && method > 0) 
				// get randomly the number of last days for which the mindmaps will be considered
				GraphDbHelper.filterByDaysSinceLastForMaps(allMaps, args, userModel);

			// get random number from size+1 --> amount==0 means take all, everything else means the size itself
			int amount = new Random().nextInt(Math.min(allMaps.size(), AlgorithmArguments.MAX_ELEMENT_AMOUNT)+1);
			if (amount > 0) {
				allMaps = allMaps.subList(0, amount);
			}
			
			userModel.addVariable("element_amount_maps", String.valueOf(amount));
		}

		return allMaps;
	}
	
	private List<Node> getLatestMapsForUser(int userId, AlgorithmArguments args) {
		Map<String, Node> latestRev = getLatestMapsForUser(userId);

		if(latestRev == null) {
			return null;
		}
		
		if (new Integer(1).equals(args.getArgument(AlgorithmArguments.LIMITATION))) {
			Iterator<Entry<String, Node>> maps = latestRev.entrySet().iterator();
			while (maps.hasNext()) {
				Entry<String, Node> entry = maps.next();
				Node mapNode = entry.getValue();
				if (!mapNode.hasProperty(GraphCreatorJob.PROPERTY_AFFILIATION) || !"library".equals(mapNode.getProperty(GraphCreatorJob.PROPERTY_AFFILIATION))) {
					maps.remove();
				}
			}
		}

		return new ArrayList<Node>(latestRev.values());
	}

	/**
	 * @param userId
	 * @return
	 */
	public Map<String, Node> getLatestMapsForUser(int userId) {
		return getLatestMapsForUser(userId, true);
	}
	
	/**
	 * @param userId
	 * @param applyFilter
	 * @return
	 */
	public Map<String, Node> getLatestMapsForUser(int userId, boolean applyFilter) {
		Map<String, Node> latestRev = new TreeMap<String, Node>();
		Node user = database.index().forNodes("users").get("USER_ID", userId).getSingle();
		if (user == null) {
			return null;
		}
		Iterator<Relationship> iter = user.getRelationships(UserRelationship.OWNS).iterator();
		while (iter.hasNext()) {
			Relationship rs = iter.next();
			Node revisionNode = rs.getEndNode();

			if(applyFilter) {
				Node mapNode = revisionNode.getRelationships(Direction.INCOMING, Type.REVISION).iterator().next().getStartNode();
				if (mapNode.hasProperty("map_type") && "trash".equals(mapNode.getProperty("map_type"))) {
					continue;
				}
				// check if recommendation use is allowed
				if ("1".equals((String) revisionNode.getProperty("allow_recommendations", "0"))) {
					addOrReplaceRevision(latestRev, revisionNode);
				}
			}
			else {
				addOrReplaceRevision(latestRev, revisionNode);
			}
		}
		//System.out.println("user("+userId+"): iterated through "+count+" map revisions");
		return latestRev;
	}

	/**
	 * @param latestRev
	 * @param revisionNode
	 */
	private void addOrReplaceRevision(Map<String, Node> latestRev, Node revisionNode) {
		String revisionCreated = revisionNode.getProperty("CREATED").toString();
		int revisionID = Integer.parseInt(revisionNode.getProperty("ID").toString());
		String mapId = (String) revisionNode.getProperty("dcr_id");
		if (latestRev.containsKey(mapId)) {
			Node n = latestRev.get(mapId);
			String latestRevisionCreated = n.getProperty("CREATED").toString();
			int latestRevisionID = Integer.parseInt(n.getProperty("ID").toString());
			if (revisionCreated.compareTo(latestRevisionCreated) > 0) {
				latestRev.put(mapId, revisionNode);
			}
			else if(revisionCreated.compareTo(latestRevisionCreated) == 0 && (revisionID > latestRevisionID)) {
				latestRev.put(mapId, revisionNode);
			}
		} else {
			latestRev.put(mapId, revisionNode);
		}
	}
	
//	public Collection<String> getUserNodeText(Integer userId, AlgorithmArguments args) {
//		Collection<NodeRevision> nodeSet = getNodeCollection(userId, args);
//		
//		if(nodeSet == null) {
//			return Collections.emptyList();
//		}
//		
//		Iterator<NodeRevision> iter = nodeSet.iterator();
//		Integer amount = args.getArgument(AlgorithmArguments.ELEMENT_AMOUNT);
//		Map<String, List<Node>> mapNodes = new HashMap<String, List<Node>>();
//		if (amount != null && amount > 0) {
//			if (amount > nodeSet.size()) {
//				// not enough nodes
//				return null;
//			}
//			// for (NodeRevision n : nodes) {
//			while (iter.hasNext() && amount > 0) {
//				
//				NodeRevision revNode = iter.next();
//				amount--;
//				List<Node> nodes = mapNodes.get(revNode.revisionKey);
//				if(nodes == null) {
//					nodes = new UniqueArrayList();
//					mapNodes.put(revNode.revisionKey, nodes);
//				}
//				if (new Integer(1).equals(args.getArgument(AlgorithmArguments.ROOTPATH))) {
//					appendUpToRoot(revNode, nodes);
//				}
//				if (new Integer(1).equals(args.getArgument(AlgorithmArguments.SIBLINGS))) {
//					appendSiblings(revNode, nodes);
//				}
//				nodes.add(revNode.getNode());
//				// only depth 1 for now
//				if (new Integer(1).equals(args.getArgument(AlgorithmArguments.CHILDREN))) {
//					appendChildren(revNode, nodes, 1, 0);
//				}
//			}
//		} else {
//			// no amount is set: just take every node
//			while (iter.hasNext()) {
//				NodeRevision revNode = iter.next();
//				List<Node> nodes = mapNodes.get(revNode.revisionKey);
//				if(nodes == null) {
//					nodes = new UniqueArrayList();
//					mapNodes.put(revNode.revisionKey, nodes);
//				}				
//				nodes.add(revNode.getNode());
//			}
//		}
//		StringBuffer buffer = new StringBuffer();
//		for(Entry<String, List<Node>> entry : mapNodes.entrySet()) { 
//			List<Node> nodes = entry.getValue();
//			Iterator<Node> nodesIter = nodes.iterator();
//			while (nodesIter.hasNext()) {
//				appendToBuffer(extractText(nodesIter.next()), buffer);
//			}
//		}
//		List<String> termList = new ArrayList<String>();
//		Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_35, new StringReader(buffer.toString()));
//		TokenStream stream = new DocearAnalyzer(Version.LUCENE_35, args).getTokenStream(tokenizer);
//		try {
//			while(stream.incrementToken()) {
//				String term = stream.getAttribute(CharTermAttribute.class).toString().trim();
//				if(term != null && !term.isEmpty()) {
//					termList.add(term);
//				}
//			}
//		} catch (IOException e) {
//		}
//		return termList;
//	}
//
//	private final class UniqueArrayList extends ArrayList<Node> {
//		private static final long serialVersionUID = 4787061214194129012L;
//
//		public boolean add(Node node) {
//			if(this.contains(node)) {
//				return false;
//			}
//			return super.add(node);
//		}
//	}

	class NodeRevision {
		private final Node node;
		private final String revisionKey;
		private final String mapType;

		public NodeRevision(Node node, String revisionKey, String mapType) {
			this.node = node;
			this.revisionKey = revisionKey;
			this.mapType = mapType;
		}

		public Node getNode() {
			return this.node;
		}

		public String getRevisionKey() {
			return this.revisionKey;
		}

		public String getMapType() {
			return mapType;
		}
	}

	public List<Map<String, String>> getUserMapDataList(Integer userId, boolean applyFilter) {
		return getUserMapDataList(userId, null, applyFilter);
	}
	
	public List<Map<String, String>> getUserMapDataList(Integer userId, List<Node> selectedMaps, boolean applyFilter) {
		List<Map<String, String>> mapDataList = new ArrayList<Map<String,String>>();
		Map<String, Node> maps = getLatestMapsForUser(userId, applyFilter);
		if(maps.size() == 0) {
			System.out.println();
		}
		if(selectedMaps != null) {
			filterMaps(selectedMaps, maps);
		}
		if(maps != null) {
			for (Entry<String, Node> map : maps.entrySet()) {
				StringBuffer text = new StringBuffer();
				Node startNode = map.getValue();
				if(!startNode.hasRelationship(Direction.OUTGOING, Type.ROOT)) {
					continue;
				}
				try {					
					final String mapType = startNode.getSingleRelationship(Type.REVISION, Direction.INCOMING).getStartNode().getProperty(GraphCreatorJob.PROPERTY_MAP_TYP).toString();
					Traverser traverser = getUserLatestRevisionsTraversal(startNode, mapType, new AlgorithmArguments(""), null);
					//final String revisionKey = startNode.getProperty("ID").toString();
					for (Node n : traverser.nodes()) {
						text.append(extractText(n));
					}
					Map<String, String> data = new HashMap<String, String>();
					data.put("id", String.valueOf(userId)+"_"+map.getKey());
					data.put("dcr_id", map.getKey());
					data.put("text", text.toString());
					mapDataList.add(data);
				}
				catch(Throwable t) {
					t.printStackTrace();
				}
			}
		}
		return mapDataList;
	}

	private void filterMaps(List<Node> allowedMaps, Map<String, Node> maps) {
		Iterator<Entry<String, Node>> iter = maps.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Node> entry = iter.next();
			if(!allowedMaps.contains(entry.getValue())) {
				iter.remove();
			}
		}
		
	}
}
