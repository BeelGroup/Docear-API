	package util.recommendations;

import java.util.Random;

import org.hibernate.Session;
import org.sciplore.resources.Algorithm;

public class AlgorithmCommons {
	public static final int MAX_ELEMENT_AMOUNT = 1000;
	public static final int MAX_RESULT_AMOUNT = 1000;
	
	//0=all; 1= 1hrs; 2hrs; 5 hrs; 12 hrs; 24hrs; 48; 120hrs;336hrs  
	private static int[] timeFrames = {0,1,2,5,12,24,48,120,336};
	
	public static Algorithm getRandomAlgorithm(Session session, boolean firstTrial) {	
		Random r = new Random();
		Algorithm alg = new Algorithm(session);
		
		int i = r.nextInt(50); //p=0.02 for stereotype recommendations
		if (i == 0 && firstTrial) {		
			alg.setChildNodes(null);
			alg.setDataElement(null);
			alg.setDataElementType(null);
			alg.setDataElementTypeWeighting(null);
			alg.setDataSource(null);
			alg.setDataSourceLimitation(null);			
			alg.setElementSelectionMethod(null);			
			alg.setRootPath(null);
			alg.setSiblingNodes(null);
			alg.setStemming(null);
			alg.setStopWordRemoval(null);
			alg.setTimeFrame(null);
			alg.setWeightIDF(null);
			alg.setWeightingScheme(null);
			alg.setApproach(Algorithm.APPROACH_STEREOTYPE);
			alg.setWeightTF(null);
			alg.setNodeDepth(null);
			alg.setNodeDepthMetric(null);
			alg.setNoSiblings(null);
			alg.setNoSiblingsMetric(null);
			alg.setNoChildren(null);
			alg.setNoChildrenMetric(null);
			alg.setWordCount(null);
			alg.setWordCountMetric(null);
			alg.setNodeWeightComboScheme(null);
			alg.setNodeWeightNormalization(null);
			
			return alg;
		}		
		
		alg.setDataSource(1); // 1=mind maps
		alg.setDataSourceLimitation(r.nextInt(2)); // 0 = all; (1=library; 2= project folder; ...) <- erstmal nur '0' und '1' nehmen
		
		//STEFAN reset to 10%?
		int element_temp = r.nextInt(2);
		int element;
		if (element_temp == 0) {
			element = 0; //1 = entire mind maps; used in 10% of cases
		} else {
			element = 1; //2 = mind map nodes; used in 90% of cases
		}
		alg.setDataElement(element+1);
		
		if (alg.getDataElement() == 2) { // only use silblings or children when using nodes as input element with amount restrictions
			alg.setChildNodes(r.nextInt(2)); //0=no; 1=level 1; 2=level 2; 3 = level 3; ... (for now, only level 1)
			alg.setSiblingNodes(r.nextInt(2)); //0=no; 1=yes (consider sibling nodes)
		}
		
		alg.setRootPath(r.nextInt(2)); //0=no; 1=yes (consider nodes up to the root)
		if (alg.getDataElement() == 1) { //do not consider root_path if using the entire mindmap
			alg.setRootPath(null);
		}
		
		alg.setDataElementType(r.nextInt(3)); //0=both, 1=text, 2=citations (bibocoupling)		
		alg.setElementSelectionMethod(r.nextInt(4)); //0=all; 1=edited; 2=created; 3=moved; 4=opened; 5=selected; 6=unfolded (e.g. the last X edited nodes)						
		
		int timeFrame = r.nextInt(timeFrames.length); //<----- we ignore this for now 
		timeFrame = 0;
		alg.setTimeFrame(timeFrames[timeFrame]);
		
		//STEFAN reset to 10%?
		i = r.nextInt(2);
		alg.setStopWordRemoval(i==0 ? 0:1);
	
		alg.setWeightTF(1); //TF uses only mindmaps for now
		alg.setWeightIDF(r.nextInt(2)+1); //1=IDF based on mind maps; 2=IDF based on full texts of papers --> a few line below it will be reset to 0 if weighting_scheme == 1
		
		//1=TF (p=1/3); 2=TFIDF (p=(2/3)
		i = r.nextInt(3);
		if (i==0) {
			alg.setWeightingScheme(1);
			alg.setWeightIDF(null);
		}
		else {
			alg.setWeightingScheme(2);
		}
		
		//keywords use "1" as factor for their weight
		String s = "1";
		//citations are used
		if (alg.getDataElementType() != 1) {
			s += "," + (r.nextInt(1000)+1);
			
			// IDF for citations needs to be based on fulltexts
			if (alg.getWeightingScheme() == 2) {
				alg.setWeightIDF(2);
			}
			
		}
		alg.setDataElementTypeWeighting(s);
		
		if (alg.getDataElementType() == 3) {
			alg.setStopWordRemoval(null);
		}
		// if pure citations are used as user model, there is not stop word removal
		if (alg.getDataElementType()==2) {
			alg.setStopWordRemoval(null);
		}
		
		if (r.nextBoolean()) {
			alg.setFeatureWeightSubmission(true);
		}
		else {
			alg.setFeatureWeightSubmission(false);
		}

//		i = r.nextInt(2);
//		//p=0.5 for bibliographic coupling; only use TF-weighting for now
//		if (i==0) {			
//			alg.setDataElementType(Algorithm.DATA_ELEMENT_TYPE_CITATIONS);			
//			alg.setWeightIDF(1);
//		}
//		else {
//			alg.setApproach(Algorithm.APPROACH_CONTENT_BASED);
//		}
		
        // 0=all; 1=mind maps; 2=references; 3=pdfs; 4=mind maps and references; 5=mind maps and pdfs; 6=references and pdfs
		alg.setNodeInfoSource(r.nextInt(7));
		
		//0=all; 1=consider only unfolded nodes; 2=consider only folded nodes
		alg.setNodeVisibility(r.nextInt(3));
		
		// choose a random value for node depth among: 0=not considered; 1=consider node depth; 2=consider reverse node depth (1/node depth)
		i = r.nextInt(3);
		alg.setNodeDepth(i);
		if (i != 0)
			// choose a random value for node depth metric among: 0=absolute value; 1=natural logarithm; 2=logarithm with base 10; 3=square root
			alg.setNodeDepthMetric(r.nextInt(4));
	
		// choose a random value for no_siblings among: 0=not considered; 1=consider no siblings; 2=consider reverse no siblings (1/no siblings)
		int j = r.nextInt(3);
		alg.setNoSiblings(j);
		if (j != 0)
			// choose a random value for no_siblings metric among: 0=absolute value; 1=natural logarithm; 2=logarithm with base 10; 3=square root
			alg.setNoSiblingsMetric(r.nextInt(4));
		
		// choose a random value for no_children among: 0=not considered; 1=consider no children; 2=consider reverse no children (1/no children)
		int k = r.nextInt(3);
		alg.setNoChildren(k);
		if (k != 0) {
			// choose a random value for no_children level between: 1=consider only direct children; 2=consider all subtree nodes (wit the node as root); 
			alg.setNoChildrenLevel(r.nextInt(2) + 1);
			// choose a random value for no_children metric among: 0=absolute value; 1=natural logarithm; 2=logarithm with base 10; 3=square root
			alg.setNoChildrenMetric(r.nextInt(4));
		}
		
		// choose a random value for word count among: 0=not considered; 1=consider word count; 2=consider reverse word count (1/word count)
		int l = r.nextInt(3);
		alg.setWordCount(l);
		if (l != 0)
			// choose a random value for word count metric among: 0=absolute value; 1=natural logarithm; 2=logarithm with base 10; 3=square root
			alg.setWordCountMetric(r.nextInt(4));
	
		// if at least one parameter/factor is set
		if (i !=0 || j !=0 || k !=0 || l !=0) 
			// 0=none; 1=normalize the total node weight (relative to the max value); 2=normalize the node weight for each parameter
			alg.setNodeWeightNormalization(r.nextInt(3));

		// if at least two parameters/factors are set
		if ((i!=0 && (j!=0 || k!=0 || l!=0)) || (j!=0 && (i!=0 || k!=0 || l!=0)) || (k!=0 && (j!=0 || i!=0 || l!=0)) ||
				(l!=0 && (j!=0 || k!=0 || i!=0)))
			// 0=add all; 1=multiply all; 2=keep max; 3=keep avg
			alg.setNodeWeightComboScheme(r.nextInt(4));
		
		return alg;
	}

	public static Algorithm getDefault(Session session) {
		Algorithm alg = new Algorithm(session);
		return alg;
	}
}
