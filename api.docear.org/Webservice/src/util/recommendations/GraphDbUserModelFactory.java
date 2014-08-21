package util.recommendations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mrdlib.index.Searcher;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.resources.Algorithm;
import org.sciplore.resources.Document;
import org.sciplore.resources.GoogleDocumentQuery;
import org.sciplore.resources.SearchModel;
import org.sciplore.resources.User;
import org.sciplore.resources.UserModel;

import util.recommendations.xml.XmlUserModelParser;
import util.searchengine.SearchCommons;
import xml.XmlElement;

public class GraphDbUserModelFactory {

	List<UserModelItem> userModelItems = null;

	private final UserModel userModel;
	private SearchModel searchModel = null;
	private String xml;
	private final User user;

	private Double ratioKeywordsInModel = null;
	private Double ratioReferencesInModel = null;

	public GraphDbUserModelFactory(Session session, User user) throws Exception {
		this(session, user, null);
	}

	public GraphDbUserModelFactory(Session session, User user, Algorithm algorithm) throws Exception {
		userModel = new UserModel(session);
		this.user = user;

		try {
			start(session, algorithm);
		}
		catch (Exception e) {
			start(session, AlgorithmCommons.getDefault(session));
		}
	}

	private void start(Session session, Algorithm algorithm) throws Exception {
		xml = createModel(session, user, algorithm);

		if (xml == null || xml.trim().length() == 0) {
			Integer algId = null;
			if (algorithm != null) {
				algId = algorithm.getId();
			}
			RecommendationCommons.logger.log("xml empty for user [" + user.getId() + "] and algorithm [" + algId + "]");
		}
		processXmlModel(session);

	}

	private void processXmlModel(Session session) throws Exception {
		if (userModel.getAlgorithm().getApproach() == Algorithm.APPROACH_STEREOTYPE) {
			userModel.setModel("@@STEREOTYPE@@");
			userModel.setFeatureCountExpanded(null);
			userModel.setFeatureCountExpandedUnique(null);
			userModel.setFeatureCountReduced(null);
			userModel.setFeatureCountReducedUnique(null);
			userModel.setUmFeatureWeightAvg(null);
			userModel.setUmFeatureWeightLast3Avg(null);
			userModel.setUmFeatureWeightLast5Avg(null);
			userModel.setUmFeatureWeightLast10Avg(null);
			userModel.setUmFeatureWeightMax(null);
			userModel.setUmFeatureWeightMin(null);
			userModel.setUmFeatureWeightTop3Avg(null);
			userModel.setUmFeatureWeightTop5Avg(null);
			userModel.setUmFeatureWeightTop10Avg(null);
			return;
		}

		XmlUserModelParser parser = new XmlUserModelParser(xml);

		Searcher searcher = new Searcher();
		userModelItems = extractKeywordsFromResponse(searcher, parser.getKeywords(), userModel.getAlgorithm());
		userModelItems.addAll(extractReferencesFromResponse(session, searcher, parser.getReferences(), userModel.getAlgorithm()));
		if (userModelItems == null || userModelItems.size() == 0) {
			RecommendationCommons.logger.log("userModelItems empty for algorithm[" + userModel.getAlgorithm().getId() + "] and xml:\n" + xml);
			return;
		}

		Collections.sort(userModelItems, new Comparator<UserModelItem>() {
			public int compare(UserModelItem o1, UserModelItem o2) {
				if (o1.getWeight() == null) {
					System.out.println("o1 changed");
					o1.setWeight(0d);
				}
				if (o2.getWeight() == null) {
					System.out.println("o2 changed");
					o2.setWeight(0d);
				}

				if (o1.getWeight().doubleValue() < o2.getWeight().doubleValue()) {
					return 1;
				}
				else if (o1.getWeight().doubleValue() == o2.getWeight().doubleValue()) {
					return 0;
				}
				else {
					return -1;
				}
			}
		});

		storeGoogleDocumentQueries(session, userModelItems);

		if (userModel.getAlgorithm().getResultAmount() > userModelItems.size()) {
			throw new Exception("userModelItems smaller than algorithm's resultAmount!");
		}
		// int resultAmount = new
		// Random().nextInt(Math.min(userModelItems.size(),
		// AlgorithmCommons.MAX_RESULT_AMOUNT)) + 1;
		// userModel.getAlgorithm().setResultAmount(resultAmount);

		if (userModel.getAlgorithm().getDataElement() == Algorithm.DATA_ELEMENT_MAPS) {
			userModel.getAlgorithm().setElementAmount(parser.getMeta("element_amount_maps"));
			userModel.getAlgorithm().setNoDaysSinceMax(parser.getMeta("no_days_since_max_maps"));
			userModel.getAlgorithm().setNoDaysSinceChosen(parser.getMeta("no_days_since_chosen_maps"));
		}
		else if (userModel.getAlgorithm().getDataElement() == Algorithm.DATA_ELEMENT_NODES) {
			userModel.getAlgorithm().setElementAmount(parser.getMeta("element_amount_nodes"));
			userModel.getAlgorithm().setNoDaysSinceMax(parser.getMeta("no_days_since_max_nodes"));
			userModel.getAlgorithm().setNoDaysSinceChosen(parser.getMeta("no_days_since_chosen_nodes"));
		}

		userModelItems = userModelItems.subList(0, userModel.getAlgorithm().getResultAmount());
		userModel.setModel(getModelString());

		try {
			searchModel = SearchCommons.createSearchModel(session, user, userModel, userModelItems);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		addUserModelVariables(parser);
	}

	private void storeGoogleDocumentQueries(Session session, List<UserModelItem> userModelItems) {
		Transaction transaction = session.beginTransaction();
		try {
			int counter = 0;
			StringBuilder googleModel = new StringBuilder();
			for (UserModelItem item : userModelItems) {
				if (item instanceof UserModelKeywordItem) {
					counter++;
					if (counter == 10) {
						break;
					}
					googleModel.append(item.getItem()).append(" ");
				}
			}

			GoogleDocumentQuery query = new GoogleDocumentQuery(session);
			query.setCreated_date(new java.util.Date());
			query.setModel(googleModel.toString().trim());
			query.setPriority(GoogleDocumentQuery.CREATED_BY_RECOMMENDATION);

			session.saveOrUpdate(query);
			session.flush();
			transaction.commit();
		}
		catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
		}
	}

	private void addUserModelVariables(XmlUserModelParser parser) throws Exception {
		int countKeyWords = 0;
		int countReferences = 0;
		for (UserModelItem item : userModelItems) {
			if (item instanceof UserModelKeywordItem) {
				countKeyWords++;
			}
			else if (item instanceof UserModelReferenceItem) {
				countReferences++;
			}
		}

		// variables directly returned from the graphdb
		userModel.setEntityTotalCount(parser.getMeta("entity_total_count"));
		userModel.setFeatureCountExpanded(parser.getMeta("feature_count_expanded"));
		userModel.setFeatureCountExpandedUnique(parser.getMeta("feature_count_expanded_unique"));
		userModel.setFeatureCountReduced(parser.getMeta("feature_count_reduced"));
		userModel.setFeatureCountReducedUnique(parser.getMeta("feature_count_reduced_unique"));
		userModel.setMindmapCountTotal(parser.getMeta("mind-map_count_total"));
		userModel.setNodeCountBeforeExpanded(parser.getMeta("node_count_before_expanded"));
		userModel.setNodeCountExpanded(parser.getMeta("node_count_expanded"));
		userModel.setNodeCountTotal(parser.getMeta("node_count_total"));
		userModel.setPaperCountTotal(parser.getMeta("paper_count_total"));
		userModel.setLinkCountTotal(parser.getMeta("link_count_total"));

		// compute variables
		if (userModelItems.size() > 0 && userModel.getNodeCountTotal() > 0) {
			userModel.setRatioKeywords((double) countKeyWords / userModelItems.size());
			userModel.setRatioReferences((double) countReferences / userModelItems.size());

			userModel.setUmSizeRelative(((double) userModel.getNodeCountExpanded()) / ((double) userModel.getNodeCountTotal()));
		}
		else {
			throw new RuntimeException("damaged userModel (\"0\" values): " + userModel.getModel() + " size: " + userModelItems.size() + " / nodeCountTotal: "
					+ userModel.getNodeCountTotal());
		}

		userModel.setUmFeatureWeightMax(userModelItems.get(0).getWeight());
		userModel.setUmFeatureWeightMin(userModelItems.get(userModelItems.size() - 1).getWeight());

		// initialize variables with first item
		userModel.setUmFeatureWeightTop3Avg(userModelItems.get(0).getWeight());
		userModel.setUmFeatureWeightLast3Avg(userModelItems.get(userModelItems.size() - 1).getWeight());
		userModel.setUmFeatureWeightTop5Avg(userModelItems.get(0).getWeight());
		userModel.setUmFeatureWeightLast5Avg(userModelItems.get(userModelItems.size() - 1).getWeight());
		userModel.setUmFeatureWeightTop10Avg(userModelItems.get(0).getWeight());
		userModel.setUmFeatureWeightLast10Avg(userModelItems.get(userModelItems.size() - 1).getWeight());

		userModel.setUmFeatureWeightAvg(userModelItems.get(0).getWeight());
		for (int i = 1; i < userModelItems.size(); i++) {
			userModel.setUmFeatureWeightAvg(userModel.getUmFeatureWeightAvg() + userModelItems.get(i).getWeight());
			if (i < 3) {
				userModel.setUmFeatureWeightTop3Avg(userModel.getUmFeatureWeightTop3Avg() + userModelItems.get(i).getWeight());
				userModel.setUmFeatureWeightLast3Avg(userModel.getUmFeatureWeightLast3Avg() + userModelItems.get(userModelItems.size() - i - 1).getWeight());
			}
			if (i < 5) {
				userModel.setUmFeatureWeightTop5Avg(userModel.getUmFeatureWeightTop5Avg() + userModelItems.get(i).getWeight());
				userModel.setUmFeatureWeightLast5Avg(userModel.getUmFeatureWeightLast5Avg() + userModelItems.get(userModelItems.size() - i - 1).getWeight());
			}
			if (i < 10) {
				userModel.setUmFeatureWeightTop10Avg(userModel.getUmFeatureWeightTop10Avg() + userModelItems.get(i).getWeight());
				userModel.setUmFeatureWeightLast10Avg(userModel.getUmFeatureWeightLast10Avg() + userModelItems.get(userModelItems.size() - i - 1).getWeight());
			}
		}

		userModel.setUmFeatureWeightAvg(userModel.getUmFeatureWeightAvg() / userModelItems.size());
		if (userModelItems.size() >= 3) {
			userModel.setUmFeatureWeightTop3Avg(userModel.getUmFeatureWeightTop3Avg() / 3);
			userModel.setUmFeatureWeightLast3Avg(userModel.getUmFeatureWeightLast3Avg() / 3);
		}
		else {
			userModel.setUmFeatureWeightTop3Avg(null);
			userModel.setUmFeatureWeightLast3Avg(null);
		}
		if (userModelItems.size() >= 5) {
			userModel.setUmFeatureWeightTop5Avg(userModel.getUmFeatureWeightTop5Avg() / 5);
			userModel.setUmFeatureWeightLast5Avg(userModel.getUmFeatureWeightLast5Avg() / 5);
		}
		else {
			userModel.setUmFeatureWeightTop5Avg(null);
			userModel.setUmFeatureWeightLast5Avg(null);
		}
		if (userModelItems.size() >= 10) {
			userModel.setUmFeatureWeightTop10Avg(userModel.getUmFeatureWeightTop10Avg() / 10);
			userModel.setUmFeatureWeightLast10Avg(userModel.getUmFeatureWeightLast10Avg() / 10);
		}
		else {
			userModel.setUmFeatureWeightTop10Avg(null);
			userModel.setUmFeatureWeightLast10Avg(null);
		}

	}

	public static void main(String[] args) {
		BooleanQuery booleanQuery = new BooleanQuery();

		// StringBuilder sbKeywords = new StringBuilder();
		// StringBuilder sbReferences = new StringBuilder();
		//
		for (String item : "bci test cli docear".split(" ")) {
			if (item.trim().length() == 0) {
				continue;
			}

			Query query = new TermQuery(new Term("text", item));
			booleanQuery.add(query, BooleanClause.Occur.SHOULD);

		}

		System.out.println(booleanQuery.toString());
	}

	public Query getLuceneQuery() {
		BooleanQuery booleanQuery = new BooleanQuery();

		// StringBuilder sbKeywords = new StringBuilder();
		// StringBuilder sbReferences = new StringBuilder();
		//
		for (UserModelItem item : userModelItems) {
			if (item.getItem().trim().length() == 0) {
				continue;
			}

			if (item instanceof UserModelKeywordItem) {
				Query query = new TermQuery(new Term("text", item.getItem()));
				if (userModel.getAlgorithm().getFeatureWeightSubmission() != null && userModel.getAlgorithm().getFeatureWeightSubmission()) {
					query.setBoost((float) item.getWeight().doubleValue());
				}
				booleanQuery.add(query, BooleanClause.Occur.SHOULD);
				// sbKeywords.append(item).append(" ");
			}
			else if (item instanceof UserModelReferenceItem) {
				Query query = new TermQuery(new Term("references", item.getItem()));
				if (userModel.getAlgorithm().getFeatureWeightSubmission() != null && userModel.getAlgorithm().getFeatureWeightSubmission()) {
					query.setBoost((float) item.getWeight().doubleValue());
				}
				booleanQuery.add(query, BooleanClause.Occur.SHOULD);
				// sbReferences.append(item).append(" ");
			}
		}

		return booleanQuery;
	}

	public UserModel getUserModel() {
		return userModel;
	}

	public SearchModel getSearchModel() {
		return searchModel;
	}

	public Double getRatioKeywordsInModel() {
		return ratioKeywordsInModel;
	}

	public void setRatioKeywordsInModel(Double ratioKeywordsInModel) {
		this.ratioKeywordsInModel = ratioKeywordsInModel;
	}

	public Double getRatioReferencesInModel() {
		return ratioReferencesInModel;
	}

	public void setRatioReferencesInModel(Double ratioReferencesInModel) {
		this.ratioReferencesInModel = ratioReferencesInModel;
	}

	private String createModel(Session session, User user, Algorithm algorithm) {
		String response = null;

		// only try for 5 times, if random algorithm is created in this method
		// and not already given
		int maxTrials = 1;
		if (algorithm == null) {
			maxTrials = 5;
		}
		boolean valid = false;
		long time = 0;
		try {
			while (maxTrials > 0 && !valid) {
				time = System.currentTimeMillis();
				System.out.println("debug: entering if algorithm is null: " + (algorithm == null));
				if (algorithm == null) {
					algorithm = AlgorithmCommons.getRandomAlgorithm(session, maxTrials == 5);					
					//session.saveOrUpdate(algorithm);
					
					RecommendationCommons.logger.log("created algorithm [" + algorithm.getId() + "] for user [" + user.getId() + "]");
				}
				userModel.setAlgorithm(algorithm);
				valid = true;
				// TESTCASE
				// model.setModel("1:2:20@@|#|@@mind map based recommender@@|#|@@3AAE9EA8ED40A3D6825DC6EEDAE41A8F5CBB46D75B8D8251787E3AA5363BA@@|-|@@Plagiarism in natural and programming languages: an overview of current tools and technologies@@|-|@@18");
				if (userModel.getAlgorithm().getApproach() == Algorithm.APPROACH_STEREOTYPE) {
					return null;
				}
				try {
					response = requestUserModel(user, userModel.getAlgorithm(), null);
					if (response == null) {
						RecommendationCommons.logger.log("empty userModel for user [" + user.getId() + "] and algorithm [" + userModel.getAlgorithm().getId()
								+ "]");
						maxTrials--;
						algorithm = null;
						valid = false;
					}
					else {
						valid = true;
					}
				}
				catch (Exception e) {
					valid = false;
					RecommendationCommons.logger.log("exception for algorithm [" + userModel.getAlgorithm().getId() + "] and user [" + user.getId() + "]: "
							+ e.getMessage());
					e.printStackTrace();
				}
			}
			if (!valid) {
				time = System.currentTimeMillis();
				algorithm = AlgorithmCommons.getDefault(session);
				userModel.setAlgorithm(algorithm);
				RecommendationCommons.logger.log("using default algorithm [" + userModel.getAlgorithm().getPersistentIdentity().getId() + "] for user ["
						+ user.getId() + "]");
				response = requestUserModel(user, userModel.getAlgorithm(), null);
			}
		}
		finally {
			userModel.setExecutionTime((int) (System.currentTimeMillis() - time));
		}

		return response;
	}

	private String requestUserModel(User user, Algorithm algorithm, String excludeHash) {
		MultivaluedMap<String, String> params = new MultivaluedStringMap();
		params.add("userId", "" + user.getId());
		params.add("algorithmArguments", algorithm.toString());

		// used for offline evaluator: exclude this hash and all later created
		// or modified nodes from usermodel
		if (excludeHash != null) {
			params.add("excludeHash", excludeHash);
		}

		return GraphDbUtils.getXmlFromRequest("/db/data/ext/DocearPlugin/graphdb/keywords/", params);
	}

	private String getModelString() {
		StringBuilder sb = new StringBuilder();

		if (userModelItems == null) {
			return null;
		}
		for (UserModelItem item : userModelItems) {
			sb.append(item.getItem()).append(" ");
		}

		return sb.toString().trim();
	}

	private List<UserModelItem> extractKeywordsFromResponse(Searcher searcher, Collection<XmlElement> collection, Algorithm alg) throws Exception {
		List<UserModelItem> keywordItems = new ArrayList<UserModelItem>();
		if (collection == null || collection.size() == 0 || alg == null) {
			return keywordItems;
		}

		double factor = 1d;
		if (alg.getDataElementTypeWeighting() != null) {
			String[] factors = alg.getDataElementTypeWeighting().split(",");
			if (factors.length >= 2) {
				factor = Double.valueOf(factors[1]);
			}
		}

		for (XmlElement element : collection) {
			String term = element.getContent();
			Double weight = Double.valueOf(element.getAttributeValue("weight"));

			if (alg.getWeightingScheme() == Algorithm.WEIGHTING_SCHEME_TFIDF && alg.getWeightIDF() == Algorithm.WEIGHT_IDF_DOCUMENTS) {
				weight *= searcher.getIDF(term, "text");
			}
			weight *= factor;
			if (weight.isInfinite()) {
				weight = 0.000001;
			}

			keywordItems.add(new UserModelKeywordItem(term, weight));
		}

		return keywordItems;
	}

	// example user model:
	// 7B7C27B68C35D2CA2362BA22D2AA53BFABF65152C785671A622382E5332174A@@|-|@@@@|-|@@8@@|.|@@455179C42677191A9C3F49C41CD83366FFD88D6C21B87D841284935DC09A21@@|-|@@Variations
	// on language modeling for information
	// retrieval.@@|-|@@6@@|.|@@3D1243B2E35191686518F809E22F7F3E77853801D676117EE644B23906037@@|-|@@@@|-|@@4@@|.|@@B3EED843615C183EE61DBC63EBC8CCA75E0A08A3F503FA94D4D7454761CE75F@@|-|@@@@|-|@@4@@|.|@@326558C4845EFBC4EF7A6A84872384FE3BFEFCFC115BD9D24FE75C4B5CB93@@|-|@@Rate
	// it Again: Increasing Recommendation Accuracy by User
	// re-Rating@@|-|@@3@@|.|@@A732EC905C8EA8872DE4C0215B13DC79EC153DB667BCB3D0F4AD81147E106B@@|-|@@@@|-|@@2@@|.|@@472EF86B9C4B90352AB745136999BA1E291DC3287F9F24F1C948316F3EC@@|-|@@@@|-|@@2@@|.|@@4D14C2EA151FD9319DA641AED4F1AC61FF6EA4815B2734042B9A484D5ED5367@@|-|@@@@|-|@@2@@|.|@@7DA67FA6C64FB8A0DAFEAA16ABA82FB290ABC2DBAFCC5044683568FF5A84D@@|-|@@\"{I}
	// would like to thank my
	// supervisor\". {A}cknowledgements in graduate dissertations@@|-|@@2@@|.|@@4AC31D2A210DBE094C8287C0F0456D4D9AF741C81AA1DE60C2D2FC5E1D5CEF@@|-|@@@@|-|@@2@@|.|@@1E97A26C96C7FAB894CEF46EECFDD204C778F2B74F3689DC29E8DE3285FDD8@@|-|@@@@|-|@@2@@|.|@@8BFD2595702653CD6866298BB76AC764BAE23D343FB956B7DCB413937757@@|-|@@@@|-|@@2@@|.|@@5CFAEDBE5734690EE4B9EE1294F78ADDF2B9FF267E8E27756C562BB2BE5373F@@|-|@@{C}omputers and i{P}hones and {M}obile {P}hones, oh my! {A} logs-based comparison of search users on different devices.@@|-|@@2@@|.|@@9B73AEFF9EE53D139FFE0A1BC9EAC3B768F7D6092D2E8B7E82C9557FAFBCA@@|-|@@@@|-|@@2@@|.|@@71AB248E661822AB6F78689360FB314E398E281204DD1C69326AED6A59C55D@@|-|@@@@|-|@@2@@|.|@@416044E63930A7831128776AA016513451062FEEB53796C783F45F8F8B517C1@@|-|@@@@|-|@@2@@|.|@@67B9819B016DC3713F6F2C5416289AB28EA655403932324A1959A190A48CCF@@|-|@@@@|-|@@2@@|.|@@B72B8DC1ECB7B2A9C2525D7269E946C5112B7FB7265914954703ACBABFA25C@@|-|@@@@|-|@@2@@|.|@@9BC248F4B8256926EEBA27846180CC8CFB7BD646A3076B94BD79E2718681@@|-|@@{R}ecommending scientific articles using citeulike@@|-|@@2@@|.|@@E2354E939586A5C412EDC1F4C35A59DFDBDD6834B4C1606BD610A3DAD4C3BC@@|-|@@@@|-|@@2@@|.|@@AFBCA0E29B652A8AC58C1699A7C0B41245777D5032D0C76FB9C345D1AB1E0AC@@|-|@@@@|-|@@1@@|.|@@BA9915991F94E19D7B98C7DE7A9599EAADE65CEBD957BF18C5715A635C472C0@@|-|@@@@|-|@@1@@|.|@@13E239485624AF34212DF5E1FDE2246F722598EE2FD943DC241F529E663374A1@@|-|@@@@|-|@@1@@|.|@@52EE2657691C631F13510B5812A98A2A1BE49629905D16EDAC54F2535726E7@@|-|@@@@|-|@@1@@|.|@@F2E1BE2F468E91EEEC8AB8C4D1525C519F859E114EA57DEC4AD5F84FAA224@@|-|@@@@|-|@@1@@|.|@@1494C4A1427B345C31FA8818703F12F9756AD7361A4139E5AF0B2E2862AE331@@|-|@@@@|-|@@1@@|.|@@CA413026401523D474C55C9FE2843A1CE58330D1B5F91C2C19738063F2E89646@@|-|@@@@|-|@@1@@|.|@@9AC7196D9B37B3082807CEC69997989A764D7C273D6EFA1DD9DE1F3538157@@|-|@@@@|-|@@1@@|.|@@1EFC663F634B076916DFD19324A3ED1F89D5BDAFDDCEC412D3C177A66B3A58@@|-|@@@@|-|@@1@@|.|@@97F7466666D526254139DEA4A688CD476A872AAAB171EBEB68CBBDBAB8A0@@|-|@@@@|-|@@1@@|.|@@F61271DBDB7FC93223E74FCE5334B7B1FF11F32104A452AF9030975CF5D680@@|-|@@@@|-|@@1@@|.|@@9CF7C72276EDCE481FA6D49DE29680861FD2CD3615933C80D1D369274120E3CF@@|-|@@@@|-|@@1@@|.|@@A445DB8B4453BA9DABDB3B427E7E645E5B24AE18486A37986CBD5CCA518C47E@@|-|@@@@|-|@@1@@|.|@@5168DFBC45F61DFACBF41E435646EB97E7EDF1C12C3F66D41FB0D445A978A59B@@|-|@@@@|-|@@1@@|.|@@665CD6FF9679DED429D660446EB6F655A19856A56F85F32FD96EAD68F4C8@@|-|@@@@|-|@@1@@|.|@@7EDD8C2C368E36CD18FD93C238E8E36F2B17D4FCD2F7A03E4A91BC3EC0527A1@@|-|@@@@|-|@@1@@|.|@@32ACAF4E3170E2624148C258BFAD148A464141AA4489B5CD9717E2BE3AC19FE@@|-|@@{A}utomatic document metadata extraction using support vector machines@@|-|@@1@@|.|@@29D1582E602826DC7F99D851F119642F561139FCAA669A879A99408D3BF27@@|-|@@{R}esearch paper recommender systems: {A} random-walk based approach@@|-|@@1@@|.|@@7FDA5BE0BFBE4F5C93E64457E6E95F5129DED6765A9D0444C49AA434F4DD6@@|-|@@@@|-|@@1@@|.|@@46F581E99E916432135AA96854E231D558FCF6E22E10806CEDBB31A091178337@@|-|@@@@|-|@@1@@|.|@@47C844D0F783702C81E5518AADC845E8A1D29F83C71211484DA845567180BB@@|-|@@{T}he evolution of library and information science 1965–1985: {A} content analysis of journal articles@@|-|@@1@@|.|@@EA29F6DE45542E8F1D80D1FF397948B1EB9D44756A7E18BE046B1BA29FDE5@@|-|@@@@|-|@@1@@|.|@@155C51B9E2861A3BCAB326555874F8ADD98ACFAFC8507C59FF8FBE892C8517E@@|-|@@@@|-|@@1@@|.|@@B49232DB4B05A6F6EE056E0934045E643E94FC156A8BCAE857C686E04EA4@@|-|@@@@|-|@@1@@|.|@@4EFFF7C11A7D924EB3F114B99BBE9D160637E69CB1A08553B0A93DF48D6@@|-|@@@@|-|@@1@@|.|@@1EA168F64C14955AA0419C96FEE93AE9AB094ED7EB9F43BA775C4D71ED5@@|-|@@@@|-|@@1@@|.|@@684BED3C248B87263798E49A9EC3EC974DF4E8C5D7A2F4BAC181D40EA755456@@|-|@@{R}esearch {P}aper {R}ecommender {S}ystems: {A} {S}ubspace {C}lustering {A}pproach@@|-|@@1@@|.|@@1CCD81E21EA51724AB7CBEF9CD710AFB8A2B13FBB05D9EBCEF5CCF1764FF@@|-|@@@@|-|@@1@@|.|@@AD669451B5CF8D138285FCC7E8D22A8EB830AD45AA296CDA178EEBAED6FAC2B@@|-|@@@@|-|@@1@@|.|@@C367612E3036AF1BD6A96764A2F252860F1FB9952D2CF43C701632AD4B237@@|-|@@@@|-|@@1@@|.|@@CE1CD1F28A2DCC82C14763294D10408618B1D7E55F207B8887EC9E5339E519@@|-|@@@@|-|@@1@@|.|@@3CCAEA43E61B773E95DB883B89E43AB68829CF571B487E9ACD163E9FA7F271B@@|-|@@@@|-|@@1@@|.|@@416AF5C747DABDD58FCA53AF8962D86D71ECA57901ED3C09444622F622572CF@@|-|@@@@|-|@@1@@|.|@@98A4CE9CAE14FBCC2291E195DEFD2B0379C2A8EA08A1187C3064E42E87@@|-|@@@@|-|@@1@@|.|@@A5968566053C37329EBA595723C7EEEC6AA9BF93C5FA2461D632B33156BA3@@|-|@@@@|-|@@1"
	private List<UserModelItem> extractReferencesFromResponse(Session session, Searcher searcher, Collection<XmlElement> collection, Algorithm alg)
			throws Exception {
		ArrayList<UserModelItem> referenceItems = new ArrayList<UserModelItem>();
		if (collection == null || collection.size() == 0) {
			return referenceItems;
		}

		double factor = 1d;
		if (alg.getDataElementTypeWeighting() != null) {
			String[] factors = alg.getDataElementTypeWeighting().split(",");
			if (factors.length >= 2) {
				factor = Double.valueOf(factors[1]);
			}
		}

		for (XmlElement element : collection) {
			String hash = element.getAttributeValue("hash");
			String title = element.getContent();
			Double weight = Double.valueOf(element.getAttributeValue("weight"));

			try {
				Document d = DocumentQueries.getDocumentByHashOrTitle(session, hash, title);
				if (d != null) {
					String term = "dcr_doc_id_" + d.getId();
					if (alg.getWeightingScheme() == Algorithm.WEIGHTING_SCHEME_TFIDF) {
						weight *= searcher.getIDF(term, "references");
					}
					weight *= factor;
					if (weight.isInfinite()) {
						weight = 0.000001;
					}

					referenceItems.add(new UserModelReferenceItem(term, weight));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return referenceItems;
	}

	public String getXml() {
		return xml;
	}

	// private String stripMetadata(String graphDbResponse) {
	// // strModel =
	// //
	// "1:2:20@@|.|@@3AAE9EA8ED40A3D6825DC6EEDAE41A8F5CBB46D75B8D8251787E3AA5363BA@@|-|@@Plagiarism in natural and programming languages: an overview of current tools and technologies@@|-|@@18";
	// // strModel =
	// //
	// "20:20:20@@|.|@@64947824AE9B96EF149240379AC16A2D37B331469212D2CACDC0DFB2302BAC@@|-|@@Improving search via personalized query expansion using social media@@|-|@@1@@|.|@@FBCE52861EA4FAE5F78B846C0DA438F742CFF658BBC984C7167AC9E7BAD737B@@|-|@@Machine Learning in User Modeling@@|-|@@1@@|.|@@CBE7CBE6A956B0FA599B644FB1BC775C327AC095E23122942DDA8E5536B6ADA@@|-|@@Collaborative Recommendation: A Robustness Analysis@@|-|@@1@@|.|@@6726C1E1B2F7D2612AEE2D81B186F97B3D68F6D99FC8A62CDFA53BED4981D@@|-|@@Configurable Indexing and Ranking for XML Information Retrieval@@|-|@@1@@|.|@@71BDBC4269F40E57AE1BFED2AD63362876A7B057982AF8DFF2304813347CF@@|-|@@Forum@@|-|@@1@@|.|@@66A14751BD461FBFA5FEDE1252135448872A361D262FA8C3C286B9D51F0ACE9@@|-|@@study on Personalized Recommendation Model of Internet Advertisement@@|-|@@1@@|.|@@71CE47564CCCD9D9BA46CC2A1D7F692A4C2C3D66661B73E9ACBFDD48F1A3B7@@|-|@@L a t e n t C l a s s M o d e l s f o r C o l l a b o r a t i v e F i l t e r i n g@@|-|@@1@@|.|@@BFF021C1EC674AE4CE46C8F1ACB58666D82D9925DEC7FA7B95CA61388A860@@|-|@@I n d e x S t r u c t u r e s f o r S e l e c t i v e D i s s e m i n a t i o n o f I n f o r m a t i o n U n d e r t h e B o o l e a n M o d e l ? T a k W Y a n a n d H e c t o r G a r c i a M o l i n a D e p a r t m e n t o f C o m p u t e r S c i e n c e S t a n f o r d U n i v e r s i t y S t a n f o r d C A D e c e m b e r A b s t r a c t T h e n u m b e r s i z e a n d u s e r p o p u l a t i o n o f b i b l i o g r a p h i c a n d f u l l t e x t d o c u m e n t d a t a b a s e s a r e r a p i d l y g r o w i n g W i t h a h i g h d o c u m e n t a r r i v a l r a t e i t b e c o m e s e s s e n t i a l f o r u s e r s o f s u c h d a t a b a s e s t o h a v e a c c e s s t o t h e v e r y l a t e s t d o c u m e n t s y e t t h e h i g h d o c u m e n t a r r i v a l r a t e a l s o m a k e s i t d i c u l t f o r t h e u s e r s t o k e e p t h e m s e l v e s u p d a t e d I t i s d e s i r a b l e t o a l l o w u s e r s t o s u b s c r i b e p r o l e s i e q u e r i e s t h a t a r e c o n s t a n t l y e v a l u a t e d s o t h a t t h e y w i l l b e a u t o m a t i c a l l y i n f o r m e d o f n e w a d d i t i o n s t h a t m a y b e o f i n t e r e s t S u c h s e r v i c e i s t r a d i t i o n a l l y c a l l e d S e l e c t i v e D i s s e m i n a t i o n o f I n f o r m a t i o n S D I T h e h i g h d o c u m e n t a r r i v a l r a t e t h e h u g e n u m b e r o f u s e r s a n d t h e t i m e l i n e s s r e q u i r e m e n t o f t h e s e r v i c e p o s e a c h a l l e n g e i n a c h i e v i n g e c i e n t S D I I n t h i s p a p e r w e p r o p o s e s e v e r a l i n d e x s t r u c t u r e s f o r i n d e x i n g p r o l e s a n d a l g o r i t h m s t h a t e c i e n t l y m a t c h d o c u m e n t s a g a i n s t l a r g e n u m b e r o f p r o l e s W e a l s o p r e s e n t a n a l y s i s a n d s i m u l a t i o n s r e s u l t s t o c o m p a r e t h e i r p e r f o r m a n c e u n d e r d i e r e n t s c e n a r i o s I n t r o d u c t i o n W i t h t h e i m p r o v i n g c o s t e e c t i v e n e s s o f s e c o n d a r y s t o r a g e a n d t h e e x p a n d i n g v o l u m e o f d i g i t i z e d t e x t u a l d a t a t h e n u m b e r a n d s i z e o f b i b l i o g r a p h i c a n d f u l l t e x t d o c u m e n t d a t a b a s e s a r e r a p i d l y ? T h i s r e s e a r c h w a s s p o n s o r e d b y t h e A d v a n c e d R e s e a r c h P r o j e c t s A g e n c y A R P A o f t h e D e p a r t m e n t o f D e f e n s e u n d e r G r a n t N o M D A J w i t h t h e C o r p o r a t i o n f o r N a t i o n a l R e s e a r c h I n i t i a t i v e s C N R I T h e v i e w s a n d c o n c l u s i o n s c o n t a i n e d i n t h i s d o c u m e n t a r e t h o s e o f t h e a u t h o r s a n d s h o u l d n o t b e i n t e r p r e t e d a s n e c e s s a r i l y r e p r e s e n t i n g t h e o c i a l p o l i c i e s o r e n d o r s e m e n t e i t h e r e x p r e s s e d o r i m p l i e d o f A R P A t h e U S G o v e r n m e n t o r C N R I@@|-|@@1@@|.|@@AF2AB2105D5311E405DF23A515A86B275F2C61A38CB48545C1D16183A5797@@|-|@@Copycats roam in era of the net@@|-|@@1@@|.|@@54137FDF21EBC2C5A1B27D23C55B7E521167E55BA32DEF2EBDCC2AEEE6F321@@|-|@@Less is More@@|-|@@1@@|.|@@DC96F9A02B1D1149573872B8383FDB6A935BFB8C09F9A6A6555AE1B5C64527@@|-|@@Social Tagging Recommender Systems@@|-|@@1@@|.|@@3AAE9EA8ED40A3D6825DC6EEDAE41A8F5CBB46D75B8D8251787E3AA5363BA@@|-|@@Plagiarism in natural and programming languages: an overview of current tools and technologies@@|-|@@1@@|.|@@4627E1EC9216C771DCF79E4D2A3012F53B4679C7802D13BE2E96B2E874C5FA@@|-|@@User Modelling for News Web Sites with Word Sense Based Techniques@@|-|@@1@@|.|@@2D17388D6F7F98BC75C6959D67EF3C649934582D8D847D6DBA97A1FA356429@@|-|@@Towards a Tag-Based User Model: How Can User Model Benefit from Tags?@@|-|@@1@@|.|@@E33B333EFB1E59B9FE0E238CD17ABC7BD9BC25FD3ED398492BB24F6E5CEB9F@@|-|@@Efficient Bayesian Hierarchical User Modeling for Recommendation Systems@@|-|@@1@@|.|@@5C96B6CAE69ABC808B6AD54FE635B0107E36C2DF785C1EC7C882789DB14F0@@|-|@@Ontology-Based Recommender Systems@@|-|@@1@@|.|@@332A9AE935847E75E4662D346A5231C6E6E3858869FB68F81B3E2B5B7AB5637@@|-|@@A User-and Item-Aware Weighting Scheme for Combining Predictive User Models@@|-|@@1@@|.|@@B06430338AD6B5F78B222D1F4E886E2F03E9B389D6778ADD081D71E6CAA189@@|-|@@Early Detection of Potential Experts in Question Answering Communities@@|-|@@1@@|.|@@D7A5CDC855CB15885C1A73CB730E284166DD155ED5455224BCA795AB1705BA7@@|-|@@Mining Software Usage Data@@|-|@@1@@|.|@@A5C3B7EE15386A2FC53A08BBFCC42C1CB95CA9FD4AB4BBBF8FBB06BD37AAFAF@@|-|@@Interest-Based Personalized Search@@|-|@@1";
	// if (graphDbResponse == null) {
	// return graphDbResponse;
	// }
	// int idx = graphDbResponse.indexOf(GRAPHDB_TYPE_SEPARATOR);
	// if (idx > -1) {
	// String strMetadata = graphDbResponse.substring(0, idx);
	// String[] metaTokens = strMetadata.split(":");
	// if (metaTokens.length == 3) {
	// try {
	// count = Integer.parseInt(metaTokens[0]);
	// }
	// catch (Exception e) {
	// }
	// try {
	// totalCount = Integer.parseInt(metaTokens[1]);
	// }
	// catch (Exception e) {
	// }
	// try {
	// nodeCount = Integer.parseInt(metaTokens[2]);
	// }
	// catch (Exception e) {
	// }
	// }
	// idx += GRAPHDB_TYPE_SEPARATOR.length();
	// }
	// else {
	// idx = 0;
	// }
	// return graphDbResponse.substring(idx);
	// }

}
