package util.recommendations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.queries.RecommendationsEvaluatorCacheQueries;
import org.sciplore.resources.Algorithm;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.RecommendationsEvaluatorCache;

import util.RecommendationCommons;
import util.Tools;

public class OfflineEvaluator implements Runnable {
	private volatile boolean stop = false;
	
	private Map<String, HashSet<Integer>> hashDocumentMap = new HashMap<String, HashSet<Integer>>();

	@Override
	public void run() {
		stop = false;

		while (!stop) {
			Session session = SessionProvider.sessionFactory.openSession();
			session.setFlushMode(FlushMode.MANUAL);
						
			List<Integer> itemIds = new ArrayList<Integer>();
			try {	
				List<RecommendationsEvaluatorCache> items = RecommendationsEvaluatorCacheQueries.getAllTuples(session);
					
				for (RecommendationsEvaluatorCache item : items) {
					itemIds.add(item.getId());
				}
			}
			catch (Exception e) {
				stop = true;
				e.printStackTrace();
				return;
			}
			finally {
				Tools.tolerantClose(session);
			}	

			for (Integer itemId : itemIds) {
				if (stop) {
					break;
				}
				evaluateAndSave(itemId);
			}
		}
	}
	
	public void stop() {
		stop = true;
	}
	
	public boolean isTargetDocument(Session session, String pdfHash, Integer documentId) {
		HashSet<Integer> documents = this.hashDocumentMap.get(pdfHash);
		
		if (documents == null) {
			documents = new HashSet<Integer>();
			for (DocumentsPdfHash item : DocumentsPdfHashQueries.getDocumentPdfHashes(session, pdfHash)) {
				documents.add(item.getDocument().getId());
			}
			
			this.hashDocumentMap.put(pdfHash, documents);
			System.out.println("hashDocumentMap.size(): "+this.hashDocumentMap.size());
		}
		
		return documents.contains(documentId);
	}
	
	
	private void evaluateAndSave(Integer recommendationsEvaluatorCacheId) {
		Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
    		Algorithm algorithm = AlgorithmCommons.getRandomAlgorithm(session, false);					
    		session.saveOrUpdate(algorithm);
    		session.flush();
    		
    		RecommendationsEvaluatorCache item = (RecommendationsEvaluatorCache) session.get(RecommendationsEvaluatorCache.class, recommendationsEvaluatorCacheId);		
    		
    		if (!RecommendationCommons.offlineEvaluate(session, item.getUser(), algorithm, item.getLatestNewMindmapsPdfHash().getPdfHash())) {
    			System.out.println("WARNING: no recommandations created for user: "+item.getUser().getId()+" and algorithm: "+algorithm.getId());
    		}
		}		
		finally {
			Tools.tolerantClose(session);
		}
	}


}
