	package util.recommendations;

import java.util.ArrayList;
import java.util.Random;

// and fix the following parameters: element_amount; results_amount; no_days_since_chosen;


import org.hibernate.Session;
import org.sciplore.resources.Algorithm;

public class AlgorithmCommons {
	public static final int MAX_ELEMENT_AMOUNT = 1000;
	public static final int MAX_RESULT_AMOUNT = 1000;
	
	private final static ArrayList<Integer> algorithmIds = new ArrayList<Integer>();
	static {
		for (int i=387701; i<=387755; i++) {
			algorithmIds.add(i);
		}
		for (int i=387757; i<=387758; i++) {
			algorithmIds.add(i);
		}
	}
			
	private final static Random random = new Random();
	
	public static Algorithm getRandomAlgorithm(Session session, boolean firstTrial) {	
		int id = algorithmIds.get(random.nextInt(algorithmIds.size()));
		
		return (Algorithm) session.get(Algorithm.class, id);
	}

	public static Algorithm getDefault(Session session) {
		return (Algorithm) session.get(Algorithm.class, 387756);
	}
}
