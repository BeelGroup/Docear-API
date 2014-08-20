	package util.recommendations;

import java.util.Random;

import org.hibernate.Session;
import org.sciplore.resources.Algorithm;

public class AlgorithmCommons {
	public static final int MAX_ELEMENT_AMOUNT = 1000;
	public static final int MAX_RESULT_AMOUNT = 1000;
	
	private final static Integer[] algorithmIds = new Integer[] {387672, 387673, 387675, 387676, 387677, 387679, 387681, 387682};
	private final static Random random = new Random();
	
	public static Algorithm getRandomAlgorithm(Session session, boolean firstTrial) {	
		int id = algorithmIds[random.nextInt(algorithmIds.length)];
		
		return (Algorithm) session.get(Algorithm.class, id);
	}

	public static Algorithm getDefault(Session session) {
		return (Algorithm) session.get(Algorithm.class, 387667);
	}
}
