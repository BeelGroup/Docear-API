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
			alg.setNodeWeightingScheme(null);
			alg.setNodeDepth(null);
			alg.setNoSiblings(null);
			alg.setNoChildren(null);
			
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
		
		//0=none; 1=node_depth; 2=no_siblings; 3=no_children; 4=combination of the above node weighting schemes
		//i = r.nextInt(5);
		i = 4;
		switch(i) {
		case 1: //only node depth considered
			alg.setNodeWeightingScheme(i);
			// choose a random value for node depth among: 1=divide by node depth; 2=multiply with node depth
			alg.setNodeDepth(r.nextInt(2)==1 ? 1 : 2);
			break;
		case 2: //only no siblings considered
			alg.setNodeWeightingScheme(i);
			// choose a random value for no_sliblings among: 1=divide by no of siblings; 2=multiply with no of siblings
			alg.setNoSiblings(r.nextInt(2)==1 ? 1 : 2);
			break;
		case 3: //only no children considered
			alg.setNodeWeightingScheme(i);
			// choose a random value for no_children among: 1=divide by no of children; 2=multiply with no of children
			alg.setNoChildren(r.nextInt(2)==1 ? 1 : 2);
			break;
		case 4: //combined case
			alg.setNodeWeightingScheme(i);
			alg.setNodeDepth(r.nextInt(2)==1 ? 1 : 2);
			alg.setNoSiblings(r.nextInt(2)==1 ? 1 : 2);
			alg.setNoChildren(r.nextInt(2)==1 ? 1 : 2);
		}
		
		return alg;
	}

	public static Algorithm getDefault(Session session) {
		return new Algorithm(session);
	}
}
