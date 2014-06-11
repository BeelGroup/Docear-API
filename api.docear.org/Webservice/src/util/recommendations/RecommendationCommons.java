package util.recommendations;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mrdlib.index.DocumentHashItem;
import org.mrdlib.index.Searcher;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.queries.RecommendationsDocumentsQueries;
import org.sciplore.queries.RecommendationsDocumentsSetQueries;
import org.sciplore.queries.UserQueries;
import org.sciplore.resources.Algorithm;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.RecommendationsDocuments;
import org.sciplore.resources.RecommendationsDocumentsSet;
import org.sciplore.resources.SearchModel;
import org.sciplore.resources.User;
import org.sciplore.resources.UserModel;

import rest.UserRessource;
import util.Tools;

public class RecommendationCommons {	
	public static final OfflineEvaluator offlineEvaluator = new OfflineEvaluator();
	public static final List<Document> stereotypeDocuments = new ArrayList<Document>();
	public static RecommendationLogger logger;	
	static {		
		try {
			logger = new RecommendationLogger("/tmp/recommendationsLogger.log");
		}
		catch (IOException e) {
			logger = null;
			e.printStackTrace();
		}
		
		if (!UserRessource.PARSER_WORKING_PATH.exists()) {
			UserRessource.PARSER_WORKING_PATH.mkdirs();
		}
		if (!UserRessource.PARSER_CACHE.exists()) {
			UserRessource.PARSER_CACHE.mkdirs();
		}

		// TODO: nicht funktioniert id 1541486
		int[] stereotypDocIds = { 6253, 1124055, 65550, 1204882, 1280653, 1360, 356429, 864449, 12140, 1541262, 1541801, 1541913, 1542177, 1542234 };
		for (int id : stereotypDocIds) {
			final Session session = SessionProvider.sessionFactory.openSession();
			stereotypeDocuments.add((Document) session.get(Document.class, id));
		}
	}

	private static boolean locked = false;
	private static boolean stop = false;

	/**
	 * This method does not save any recommendations. Instead it tries to compute them and logs if anýthing goes wrong
	 * @param session
	 * @param logFile
	 */
	public static void stopOfflineRecommendations() {
		if (locked) {
			stop = true;
		}
	}
	
	/**
	 * This method does not save any recommendations. Instead it tries to compute them and logs if anýthing goes wrong
	 * @param session
	 * @param logFile
	 */
	public static void dryRun(Session session) {
		if (locked) {
			throw new RuntimeException("computing of recommendations already in progress!");
		}
		
		locked = true;
		
		try {			
			List<BigInteger> users = UserQueries.getUsers(session);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}

			for (BigInteger userId : users) {				
				session = SessionProvider.sessionFactory.openSession();
				try {
    				if (stop) {
    					stop = false;
    					locked = false;
    					System.out.println("stopping RecommendationRunner");
    					return;
    				}
    				User user = (User) session.get(User.class, userId.intValue());
    				dryRunForSingleUser(session, user);
				}
				catch(Exception e) {
					logger.log("exception in main loop for user ["+userId.intValue()+"]");
				}
				finally {
					session.close();
				}
			}
		}
		
		finally {
			locked = false;		
		}
	}
	
	public static void enqueueRecommendionsGeneratorTaskAfterNewMap(final User user) {
    	if (user.isAllowRecommendations()) {
    		AsynchronousRecommendationsGeneratorAfterNewMap.executeAsynch(new Runnable() {				
    			@Override
    			public void run() {
    				final Session session = SessionProvider.sessionFactory.openSession();
    				RecommendationsDocumentsSet recDocSet = null;
    				try {
    					session.setFlushMode(FlushMode.MANUAL);
    					User u = (User) session.get(User.class, user.getId());
    					recDocSet = RecommendationsDocumentsSetQueries.getLatestRecommendationsSet(session, u, RecommendationsDocumentsSet.TRIGGER_TYPE_MINDMAP_UPLOAD);
    				}
    				catch(Exception e) {
    					e.printStackTrace();
    				}
    				finally {
    					Tools.tolerantClose(session);
    				}
    				
    				//only generate new recommendations if the last time is more than 1 hour past
    				long now = System.currentTimeMillis();
    				if (recDocSet == null || recDocSet.getCreated().getTime() < (now - 3600000L)) {
    					RecommendationCommons.forceComputeForSingleUser(user.getId(), RecommendationsDocumentsSet.TRIGGER_TYPE_MINDMAP_UPLOAD);
    				}
    				else {
    					System.out.println("do not compute recommendations for user "+user.getId()+", last attempt has been "+ (now-recDocSet.getCreated().getTime()) + "ms ago.");
    				}
    			}
    		});
    		System.out.println("AsynchronousRecommendationsGeneratorAfterNewMap --> running recommendation generator tasks: "+AsynchronousRecommendationsGeneratorAfterNewMap.getSingleExecTaskCount());
    	}
	}
	
	private static boolean dryRunForSingleUser(Session session, User user) {
		return dryRunForSingleUser(session, user, null);
	}
	
	public static boolean dryRunForSingleUser(Session session, User user, Algorithm algorithm) {
		try {	
			System.out.println("compute recommendations for user: " + user.getId() + " (" + user.getUsername() + ")");

			Integer days = UserQueries.getDaysUsed(session, user);
			if (days == null || days < 2) {
				logger.log("user ["+user.getId()+"] not not using Docear long enough");				
				return false;
			}

			GraphDbUserModelFactory response = new GraphDbUserModelFactory(session, user, algorithm);
			if (response.getUserModel().getModel() == null) {
				logger.log("user model empty for user ["+user.getId()+"]");
				return false;
			}

			RecommendationsDocumentsSet recDocSet = null;			
			if (response.getUserModel() != null && response.getUserModel().getModel() != null) {
//				if (response.getUserModel().getModel().equals(GraphDbUserModelFactory.GRAPHDB_TYPE_SEPARATOR)) {
//					// model is empty
//					return false;
//				}

				/*****************************************
				 * stereotype recommendations
				 *****************************************/
				if (response.getUserModel().getAlgorithm().getApproach() == Algorithm.APPROACH_STEREOTYPE) {
					recDocSet = getStereotypes(session, user, response.getUserModel());
					recDocSet.setRecAmountCurrent(10);
					recDocSet.setRecAmountPotential(stereotypeDocuments.size());
					recDocSet.setRecAmountShould(10);
				}
				/*****************************************
				 * use our own database for CBF and bibocoupling
				 *****************************************/
				else {
					try {
						recDocSet = searchLucene(session, user, response, "text");
						if (recDocSet == null || recDocSet.getRecommendationsDocuments() == null || recDocSet.getRecommendationsDocuments().size()==0) {
							logger.log("lucene search returned no documents for user ["+user.getId()+"]");
						}
						else {
							logger.log("lucene search returned "+ recDocSet.getRecommendationsDocuments().size()+" documents for user ["+user.getId()+"]");
						}
						logger.log("no_days_since_max for user ["+user.getId()+"] and algorithm ["+recDocSet.getUserModel().getAlgorithm().getId()+"]: "+recDocSet.getUserModel().getAlgorithm().getNoDaysSinceMax());
					}
					catch(Exception e) {
						logger.log("exception when searching lucene for user ["+user.getId()+"]: "+response.getLuceneQuery().toString());
					}
				}
			}

			if (recDocSet == null) {
				return false;
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		logger.log("recommendation task finished for user ["+user.getId()+"]");

		return true;
	}

	public static void computeForAllUsers(Session session) {
		if (locked) {
			throw new RuntimeException("computing of recommendations already in progress!");
		}

		locked = true;
		try {
			List<BigInteger> users = UserQueries.getUsers(session);
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}

			for (BigInteger userId : users) {
				if (stop) {
					stop = false;
					locked = false;
					System.out.println("stopping RecommendationRunner");
					return;
				}
				computeForSingleUser(userId.intValue(), 0);
			}
		}
		finally {
			locked = false;
		}
	}
	
	// try up to 5 times to generate correct recommendations
	public static void forceComputeForSingleUser(User user, int triggerType) {
		for (int i=0; i<5; i++) {
			if (computeForSingleUser(user, triggerType)) {
				return;
			}
		}		
	}

	public static boolean computeForSingleUser(User user, int triggerType) {
		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			return computeForSingleUser(session, user, triggerType);
		}
		finally {
			try {
				Tools.tolerantClose(session);
			}
			catch (Exception ex) {
				// do nth.
			}
		}
	}
	
	// try up to 5 times to generate correct recommendations
	public static void forceComputeForSingleUser(int userId, int triggerType) {
		for (int i=0; i<5; i++) {
			if (computeForSingleUser(userId, triggerType)) {
				return;
			}
		}	
	}

	public static boolean computeForSingleUser(int userId, int triggerType) {		
		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			User user = (User) session.get(User.class, userId);
			return computeForSingleUser(session, user, triggerType);
		}
		finally {
			try {
				Tools.tolerantClose(session);
			}
			catch (Exception ex) {
				// do nth.
			}
		}
	}
	
	public static boolean offlineEvaluate(Session session, User user, Algorithm algorithm, String offlineEvaluatorPaperHash){
		try {
    		Integer days = UserQueries.getDaysUsed(session, user);
    		if (days == null || days < 2) {
    			return false;
    		}
    
    		GraphDbUserModelFactory response = new GraphDbUserModelFactory(session, user, algorithm);
    		if (response.getUserModel().getModel() == null) {
    			return false;
    		}
    		
    		RecommendationsDocumentsSet recDocSet = null;
    		if (response.getUserModel() != null && response.getUserModel().getModel() != null) {
    			recDocSet = searchLucene(session, user, response, "text", 10, false, offlineEvaluatorPaperHash);
    		}
    		
    		if (recDocSet == null) {
    			return false;
    		}
    		
    		recDocSet.setTriggerType(null);
    		recDocSet.setOfflineEvaluator(true);
    		recDocSet.setOld(false);
//    		recDocSet.setRecommendationsDocuments(new HashSet<RecommendationsDocuments>());
    		recDocSet.setRecAmountCurrent(0);
    		
    		Transaction transaction = session.beginTransaction();
    		try {
    			//STEFAN remove when TF-IDF on full texts is validated
    			if (recDocSet.getUserModel().getEntityTotalCount() == null) {
    				System.out.println("erroneous user model: " + response.getXml());
    			}
    			
    			session.saveOrUpdate(recDocSet);
    			session.flush();
    			
    			transaction.commit();
    		}
    		catch (Exception e) {    			
    			transaction.rollback();
    			e.printStackTrace();
    			System.out.println("error for user model: "+response.getUserModel().getModel());
    		}
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
		
	}
	
	private static boolean computeForSingleUser(Session session, User user, int triggerType) {
		try {	
			System.out.println("compute recommendations for user: " + user.getId() + " (" + user.getUsername() + ")");

			Integer days = UserQueries.getDaysUsed(session, user);
			if (days == null || days < 2) {
				return false;
			}

			GraphDbUserModelFactory response = new GraphDbUserModelFactory(session, user);
			if (response.getUserModel().getModel() == null) {
				return false;
			}
			
			RecommendationsDocumentsSet recDocSet = null;			
			if (response.getUserModel() != null && response.getUserModel().getModel() != null) {
//				if (response.getUserModel().getModel().equals(GraphDbUserModelFactory.GRAPHDB_TYPE_SEPARATOR)) {
//					// model is empty
//					return false;
//				}

				/*****************************************
				 * stereotype recommendations
				 *****************************************/
				if (response.getUserModel().getAlgorithm().getApproach() == Algorithm.APPROACH_STEREOTYPE) {
					recDocSet = getStereotypes(session, user, response.getUserModel());
					recDocSet.setRecAmountCurrent(10);
					recDocSet.setRecAmountPotential(stereotypeDocuments.size());
					recDocSet.setRecAmountShould(10);
				}
				/*****************************************
				 * use our own database for CBF and bibocoupling
				 *****************************************/
				else {
					recDocSet = searchLucene(session, user, response, "text");
				}
			}

			if (recDocSet == null) {
				return false;
			}			

			recDocSet.setTriggerType(triggerType);
			recDocSet.setOfflineEvaluator(false);
			
			Transaction transaction = session.beginTransaction();
			try {
				//STEFAN remove when TF-IDF on full texts is validated
				if (recDocSet.getUserModel().getEntityTotalCount() == null) {
					System.out.println("erroneous user model: " + response.getXml());
				}
				
				session.saveOrUpdate(recDocSet);
				
				
				SearchModel searchModel = response.getSearchModel();
				if (searchModel != null) {
					session.saveOrUpdate(searchModel);
				}
				
				session.flush();
				RecommendationCommons.logger.log("recommendations saved for user["+recDocSet.getUser().getId()+"] with algorithm["+recDocSet.getUserModel().getAlgorithm().getId()+"] having stopWordRemoval set to: "+recDocSet.getUserModel().getAlgorithm().getStopWordRemoval()+"\n#####\n");
				
				transaction.commit();
			}
			catch (Exception e) {
				transaction.rollback();
				e.printStackTrace();				
				System.out.println("error when saving the model for user ["+recDocSet.getUser().getId()+"] with algorithm["+recDocSet.getUserModel().getAlgorithm().getId()+"] having no_days_since_max: "+recDocSet.getUserModel().getAlgorithm().getNoDaysSinceMax());
				System.out.println("error when saving the model for user ["+recDocSet.getUser().getId()+"] with algorithm["+recDocSet.getUserModel().getAlgorithm().getId()+"] having node_count_before_expanded: "+recDocSet.getUserModel().getNodeCountBeforeExpanded());
			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		System.out.println("recommendation_task finished for user: "+user.getId());

		return true;
	}

	private static RecommendationsDocumentsSet getStereotypes(Session session, User user, UserModel userModel) {
		long time = System.currentTimeMillis();
		Set<RecommendationsDocuments> recommendationDocuments = new HashSet<RecommendationsDocuments>();

		synchronized (stereotypeDocuments) {
			Collections.shuffle(stereotypeDocuments);
		}

		Collections.shuffle(stereotypeDocuments);
		int recommendationsCount = Math.min(stereotypeDocuments.size(), 10);

		RecommendationsDocumentsSet recDocSet = createRecommendationSet(session, user, userModel, System.currentTimeMillis() - time);
		for (int i = 0; i < recommendationsCount; i++) {
			Document document = stereotypeDocuments.get(i);
			try {
				// create and save hibernate objects
				RecommendationsDocuments recdoc = createDocumentRecommendation(session, recDocSet, document.getFulltextUrls().iterator().next(), null, i+1, null);
				if (recdoc != null) {
					recommendationDocuments.add(recdoc);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		recDocSet.setRecommendationsDocuments(recommendationDocuments);

		return recDocSet;
	}
	
	private static RecommendationsDocumentsSet searchLucene(Session session, User user, GraphDbUserModelFactory factory, String field) throws Exception {
		return searchLucene(session, user, factory, field, 50, true, null);		
	}

	private static RecommendationsDocumentsSet searchLucene(Session session, User user, GraphDbUserModelFactory factory, String field, int maxRecSelectedFromTopX, boolean shuffle, String offlineEvaluatorPaperHash) throws Exception {
		long time = System.currentTimeMillis();
		if (factory == null || factory.getUserModel() == null) {
			return null;
		}

		Set<RecommendationsDocuments> recommendationDocuments = new HashSet<RecommendationsDocuments>();

		Searcher searcher = new Searcher();		
		List<DocumentHashItem> items = searcher.search(factory.getLuceneQuery(), 0, 1000);

		if (items == null || items.size() == 0) {
			return null;
		}

		RecommendationsDocumentsSet recDocSet = createRecommendationSet(session, user, factory.getUserModel(), System.currentTimeMillis() - time);
		System.out.println("lucene returned "+items.size()+" items.");
		int recSelectedFromTopX = Math.min(maxRecSelectedFromTopX, items.size());
		recDocSet.setRecAmountPotential(items.size());
				
		Integer offlineEvaluatorPaperPosition = null;
		if (offlineEvaluatorPaperHash != null) {
			offlineEvaluatorPaperPosition = 0;
			
			for (int i = 0; i<items.size(); i++) {
				Integer documentId = items.get(i).documentId;
				if (documentId != null) {
					if (offlineEvaluator.isTargetDocument(session, offlineEvaluatorPaperHash, documentId)) {
						offlineEvaluatorPaperPosition = i+1;
					}
				}				
			}
		}
		
		recDocSet.setRecSelectedFromTopX(recSelectedFromTopX);
		items = items.subList(0, recSelectedFromTopX);
		if (shuffle) {
			Collections.shuffle(items);
		}
				
		int i=0;
		int recAmountShould = 10;
		int recOriginalRankMax = items.get(0).rank;
		int recOriginalRankMin = recOriginalRankMax;
		
		int rankSum = 0;
		for (DocumentHashItem item : items) {
			if (item.rank < recOriginalRankMin) {
				recOriginalRankMin = item.rank;
			}
			if (item.rank > recOriginalRankMax) {
				recOriginalRankMax = item.rank;
			}
			
			rankSum += item.rank;
			
			Document doc = null;
			FulltextUrl fulltextUrl = null;
			if (item.pdfHash != null) {
				DocumentsPdfHash dph = DocumentsPdfHashQueries.getPdfHash(session, item.pdfHash);
				if (dph != null) {
					doc = dph.getDocument();
					try {
						fulltextUrl = dph.getFulltextUrls().get(0);
					}
					catch (Exception e) {
						doc = null;
						System.out.println("UserResource.getLiteratureRecommendations: " + e.getMessage());
					}
				}
			}

			// fallback if hash unknown or null
			if (doc == null) {
				doc = DocumentQueries.getDocument(session, item.documentId);
				try {
					fulltextUrl = doc.getFulltextUrls().iterator().next();
				}
				catch (Exception e) {
					continue;
				}
			}
			if (fulltextUrl == null) {
				continue;
			}

			RecommendationsDocuments recdoc = createDocumentRecommendation(session, recDocSet, fulltextUrl, item.rank, ++i, item.relevance);
			if (recdoc != null) {
				recommendationDocuments.add(recdoc);
			}

			// only return 10 recommmendations
			if (recommendationDocuments.size() >= recAmountShould) {
				break;
			}
		}
		
		recDocSet.setOfflineEvaluatorPaperPosition(offlineEvaluatorPaperPosition);	
		recDocSet.setRecOriginalRankMax(recOriginalRankMax);
		recDocSet.setRecOriginalRankMin(recOriginalRankMin);
		recDocSet.setRecOriginalRankAvg(((double) rankSum) / ((double) items.size())); 
		recDocSet.setRecAmountShould(recAmountShould);
		recDocSet.setRecAmountCurrent(i);
		
		recDocSet.setRecommendationsDocuments(recommendationDocuments);

		return recDocSet;

	}

	private static RecommendationsDocumentsSet createRecommendationSet(Session session, User user, UserModel model, long computationTime) {
		RecommendationsDocumentsSet recDocSet = new RecommendationsDocumentsSet(session);

		recDocSet.setUser(user);
		recDocSet.setUserModel(model);
		recDocSet.setCreated(new Date());
		recDocSet.setComputationTime(computationTime);

		return recDocSet;
	}

	private static RecommendationsDocuments createDocumentRecommendation(Session session, RecommendationsDocumentsSet recDocSet, FulltextUrl fulltextUrl,
			Integer originalRank, Integer presentationRank, Float relevance) {
		RecommendationsDocuments recDoc = new RecommendationsDocuments(session);
		recDoc.setRecommentationsDocumentsSet(recDocSet);
		recDoc.setFulltextUrl(fulltextUrl);
		recDoc.setOriginalRank(originalRank);
		recDoc.setPresentationRank(presentationRank);
		recDoc.setRelevance(relevance);

		return recDoc;
	}

	public static void click(Session session, RecommendationsDocuments recDoc) {
		recDoc.setClicked(new Date(System.currentTimeMillis()));
		
		RecommendationsDocumentsSet recDocSet = recDoc.getRecommentationsDocumentsSet();
		int clicked = 0;
		for (RecommendationsDocuments rd : recDocSet.getRecommendationsDocuments()) {
			if (rd.getClicked() != null) {
				clicked += 1;
			}
		}
		
		recDocSet.setRecClickedCount(clicked);
		recDocSet.setRecClickedCtr(((double) clicked) / ((double) recDocSet.getRecommendationsDocuments().size()));
		
		session.saveOrUpdate(recDocSet);
		session.flush();
	}

	public static void computeDeliveryVariables(Integer id) {
		Session session = SessionProvider.getNewSession();
		session.setFlushMode(FlushMode.MANUAL);
		
		RecommendationsDocumentsSet recDocSet = new RecommendationsDocumentsSet(session);
		recDocSet.setId(id);
		recDocSet = (RecommendationsDocumentsSet) recDocSet.getPersistentIdentity();
		
		recDocSet.setUserDaysStarted(UserQueries.getDaysStarted(session, recDocSet.getUser()));
		
		long timeSinceRegistration = System.currentTimeMillis() - recDocSet.getUser().getRegistrationdate().getTime();
		recDocSet.setUserDaysSinceRegistered((int) (timeSinceRegistration / 1000 / 3600 / 24));
		
		recDocSet.setUserSetsDelivered(RecommendationsDocumentsSetQueries.getCountDeliveredBefore(session, recDocSet.getUser()));
		
		for (RecommendationsDocuments recDoc : recDocSet.getRecommendationsDocuments()) {
			recDoc.setShownBefore(RecommendationsDocumentsQueries.getShownBefore(session, recDoc).intValue());
		}
		
		Transaction t = session.beginTransaction();
		try {
			session.saveOrUpdate(recDocSet);
			session.flush();
			t.commit();
		}
		catch(Exception e) {
			t.rollback();
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	

}
