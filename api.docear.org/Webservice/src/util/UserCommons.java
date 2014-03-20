package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sciplore.queries.ApplicationQueries;
import org.sciplore.queries.RecommendationsLabelQueries;
import org.sciplore.queries.RecommendationsRatingsLabelQueries;
import org.sciplore.queries.RecommendationsUsersSettingsQueries;
import org.sciplore.queries.UserQueries;
import org.sciplore.resources.Algorithm;
import org.sciplore.resources.Application;
import org.sciplore.resources.GoogleDocumentQuery;
import org.sciplore.resources.RecommendationsLabel;
import org.sciplore.resources.RecommendationsRatingsLabel;
import org.sciplore.resources.RecommendationsUsersSettings;
import org.sciplore.resources.User;
import org.sciplore.resources.UserPasswordRequest;
import org.sciplore.resources.UsersApplications;
import org.sciplore.utilities.config.Config;

public class UserCommons {
	public static Properties docearEMailConfig;
	
	static {
		docearEMailConfig = Config.getProperties("org.mrdlib");		
	}
	
	public static final String MINDMAPS_PATH = "/srv/docear/mindmaps/";
	

	public static Response getRedirectResponse(URL url, String title) {
		String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n";
		html += "<html>\n";
		html += "<head>\n";
		html += "<title>" + title + "</title>\n";
		html += "<meta http-equiv=\"REFRESH\" content=\"0;url=" + url.toExternalForm() + "\">\n";
		html += "</HEAD>\n";
		html += "<BODY>\n";
		html += "</BODY>\n";
		html += "</HTML>\n";

		ResponseBuilder builder = Response.status(Status.OK);
		builder.type(MediaType.TEXT_HTML_TYPE);
		builder.entity(html);

		return builder.build();
	}

	public static Response getRedirectedResponse(URI uri) {
		ResponseBuilder builder = Response.status(Status.FOUND);
		builder.location(uri);

		return builder.build();
	}

	public static Response getHTTPStatusResponse(int responseStatus, String msg) {
		if (msg == null || msg.length() == 0) {
			responseStatus = Status.NO_CONTENT.getStatusCode();
		}
		ResponseBuilder builder = Response.status(responseStatus);
		if (responseStatus != Status.NO_CONTENT.getStatusCode()) {
			builder.type("text/plain; charset=utf-8");
			builder.entity(msg);
		}

		Response resp = builder.build();
		return resp;
	}

	public static Response getFileStream(int responseStatus, final String path, String fileName) {
		StreamingOutput output = new StreamingOutput() {
			@Override
			public void write(OutputStream stream) throws IOException, WebApplicationException {
				File f = new File(MINDMAPS_PATH + File.separator + path);
				FileInputStream in = new FileInputStream(f);

				while (true) {
					int data = in.read();
					if (data == -1) {
						break;
					}
					stream.write(data);
				}
				
				in.close();
			}
		};
		try {
			fileName = URLEncoder.encode(fileName + ".zip", "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ResponseBuilder builder = Response.status(responseStatus);
		builder.header("Content-disposition", "filename=" + fileName);
		builder.type(MediaType.APPLICATION_OCTET_STREAM_TYPE);
		builder.entity(output);

		return builder.build();

	}

	public static Response getHTTPStatusResponse(Status responseStatus, String msg) {
		return getHTTPStatusResponse(responseStatus.getStatusCode(), msg);
	}

//	public static GraphDbUserModelResponse getUserModelResponse(final Session session, User user) {
//		UserModel model = new UserModel(session);
//		WebResource res = client.resource(NEO4JREST_URI).path("/db/data/ext/DocearPlugin/graphdb/keywords/");
//		ClientResponse response;
//		int maxTrials = 5;
//		Algorithm algo;
//		boolean valid = true;
//		long time = 0;
//		try {
//			do {
//				time = System.currentTimeMillis();
//				algo = AlgorithmCommons.getRandomAlgorithm(session, maxTrials==5);
//				valid = true;
//				if (algo.getApproach() == Algorithm.APPROACH_STEREOTYPE) {
//					model.setModel("@@STEREOTYPE@@");
//					model.setAlgorithm(algo);
//					return model;
//				}
//				response = requestKeywords(user, algo, res);
//				if (response == null || !response.getClientResponseStatus().equals(Status.OK)) {
//					maxTrials--;
//					valid = false;
//				}
//			} while (maxTrials > 0 && !valid);
//			if (!valid) {
//				time = System.currentTimeMillis();
//				algo = AlgorithmCommons.getDefault(session);
//				response = requestKeywords(user, algo, res);
//				if (response == null || !response.getClientResponseStatus().equals(Status.OK)) {
//					return null;
//				}
//			}
//			model.setAlgorithm(algo);			
//			model.setModel(stripMetadata(model, response.getEntity(String.class).replace("\"", "")));
//			return model;
//		}
//		finally {
//			model.setExecutionTime((int) (System.currentTimeMillis()-time));
//		}
//	}


//	private static String stripMetadata(UserModel model, String strModel) {		
////		strModel = "1:2:20@@|.|@@3AAE9EA8ED40A3D6825DC6EEDAE41A8F5CBB46D75B8D8251787E3AA5363BA@@|-|@@Plagiarism in natural and programming languages: an overview of current tools and technologies@@|-|@@18";
////		strModel = "20:20:20@@|.|@@64947824AE9B96EF149240379AC16A2D37B331469212D2CACDC0DFB2302BAC@@|-|@@Improving search via personalized query expansion using social media@@|-|@@1@@|.|@@FBCE52861EA4FAE5F78B846C0DA438F742CFF658BBC984C7167AC9E7BAD737B@@|-|@@Machine Learning in User Modeling@@|-|@@1@@|.|@@CBE7CBE6A956B0FA599B644FB1BC775C327AC095E23122942DDA8E5536B6ADA@@|-|@@Collaborative Recommendation: A Robustness Analysis@@|-|@@1@@|.|@@6726C1E1B2F7D2612AEE2D81B186F97B3D68F6D99FC8A62CDFA53BED4981D@@|-|@@Configurable Indexing and Ranking for XML Information Retrieval@@|-|@@1@@|.|@@71BDBC4269F40E57AE1BFED2AD63362876A7B057982AF8DFF2304813347CF@@|-|@@Forum@@|-|@@1@@|.|@@66A14751BD461FBFA5FEDE1252135448872A361D262FA8C3C286B9D51F0ACE9@@|-|@@study on Personalized Recommendation Model of Internet Advertisement@@|-|@@1@@|.|@@71CE47564CCCD9D9BA46CC2A1D7F692A4C2C3D66661B73E9ACBFDD48F1A3B7@@|-|@@L a t e n t C l a s s M o d e l s f o r C o l l a b o r a t i v e F i l t e r i n g@@|-|@@1@@|.|@@BFF021C1EC674AE4CE46C8F1ACB58666D82D9925DEC7FA7B95CA61388A860@@|-|@@I n d e x S t r u c t u r e s f o r S e l e c t i v e D i s s e m i n a t i o n o f I n f o r m a t i o n U n d e r t h e B o o l e a n M o d e l ? T a k W Y a n a n d H e c t o r G a r c i a M o l i n a D e p a r t m e n t o f C o m p u t e r S c i e n c e S t a n f o r d U n i v e r s i t y S t a n f o r d C A D e c e m b e r A b s t r a c t T h e n u m b e r s i z e a n d u s e r p o p u l a t i o n o f b i b l i o g r a p h i c a n d f u l l t e x t d o c u m e n t d a t a b a s e s a r e r a p i d l y g r o w i n g W i t h a h i g h d o c u m e n t a r r i v a l r a t e i t b e c o m e s e s s e n t i a l f o r u s e r s o f s u c h d a t a b a s e s t o h a v e a c c e s s t o t h e v e r y l a t e s t d o c u m e n t s y e t t h e h i g h d o c u m e n t a r r i v a l r a t e a l s o m a k e s i t d i c u l t f o r t h e u s e r s t o k e e p t h e m s e l v e s u p d a t e d I t i s d e s i r a b l e t o a l l o w u s e r s t o s u b s c r i b e p r o l e s i e q u e r i e s t h a t a r e c o n s t a n t l y e v a l u a t e d s o t h a t t h e y w i l l b e a u t o m a t i c a l l y i n f o r m e d o f n e w a d d i t i o n s t h a t m a y b e o f i n t e r e s t S u c h s e r v i c e i s t r a d i t i o n a l l y c a l l e d S e l e c t i v e D i s s e m i n a t i o n o f I n f o r m a t i o n S D I T h e h i g h d o c u m e n t a r r i v a l r a t e t h e h u g e n u m b e r o f u s e r s a n d t h e t i m e l i n e s s r e q u i r e m e n t o f t h e s e r v i c e p o s e a c h a l l e n g e i n a c h i e v i n g e c i e n t S D I I n t h i s p a p e r w e p r o p o s e s e v e r a l i n d e x s t r u c t u r e s f o r i n d e x i n g p r o l e s a n d a l g o r i t h m s t h a t e c i e n t l y m a t c h d o c u m e n t s a g a i n s t l a r g e n u m b e r o f p r o l e s W e a l s o p r e s e n t a n a l y s i s a n d s i m u l a t i o n s r e s u l t s t o c o m p a r e t h e i r p e r f o r m a n c e u n d e r d i e r e n t s c e n a r i o s I n t r o d u c t i o n W i t h t h e i m p r o v i n g c o s t e e c t i v e n e s s o f s e c o n d a r y s t o r a g e a n d t h e e x p a n d i n g v o l u m e o f d i g i t i z e d t e x t u a l d a t a t h e n u m b e r a n d s i z e o f b i b l i o g r a p h i c a n d f u l l t e x t d o c u m e n t d a t a b a s e s a r e r a p i d l y ? T h i s r e s e a r c h w a s s p o n s o r e d b y t h e A d v a n c e d R e s e a r c h P r o j e c t s A g e n c y A R P A o f t h e D e p a r t m e n t o f D e f e n s e u n d e r G r a n t N o M D A J w i t h t h e C o r p o r a t i o n f o r N a t i o n a l R e s e a r c h I n i t i a t i v e s C N R I T h e v i e w s a n d c o n c l u s i o n s c o n t a i n e d i n t h i s d o c u m e n t a r e t h o s e o f t h e a u t h o r s a n d s h o u l d n o t b e i n t e r p r e t e d a s n e c e s s a r i l y r e p r e s e n t i n g t h e o c i a l p o l i c i e s o r e n d o r s e m e n t e i t h e r e x p r e s s e d o r i m p l i e d o f A R P A t h e U S G o v e r n m e n t o r C N R I@@|-|@@1@@|.|@@AF2AB2105D5311E405DF23A515A86B275F2C61A38CB48545C1D16183A5797@@|-|@@Copycats roam in era of the net@@|-|@@1@@|.|@@54137FDF21EBC2C5A1B27D23C55B7E521167E55BA32DEF2EBDCC2AEEE6F321@@|-|@@Less is More@@|-|@@1@@|.|@@DC96F9A02B1D1149573872B8383FDB6A935BFB8C09F9A6A6555AE1B5C64527@@|-|@@Social Tagging Recommender Systems@@|-|@@1@@|.|@@3AAE9EA8ED40A3D6825DC6EEDAE41A8F5CBB46D75B8D8251787E3AA5363BA@@|-|@@Plagiarism in natural and programming languages: an overview of current tools and technologies@@|-|@@1@@|.|@@4627E1EC9216C771DCF79E4D2A3012F53B4679C7802D13BE2E96B2E874C5FA@@|-|@@User Modelling for News Web Sites with Word Sense Based Techniques@@|-|@@1@@|.|@@2D17388D6F7F98BC75C6959D67EF3C649934582D8D847D6DBA97A1FA356429@@|-|@@Towards a Tag-Based User Model: How Can User Model Benefit from Tags?@@|-|@@1@@|.|@@E33B333EFB1E59B9FE0E238CD17ABC7BD9BC25FD3ED398492BB24F6E5CEB9F@@|-|@@Efficient Bayesian Hierarchical User Modeling for Recommendation Systems@@|-|@@1@@|.|@@5C96B6CAE69ABC808B6AD54FE635B0107E36C2DF785C1EC7C882789DB14F0@@|-|@@Ontology-Based Recommender Systems@@|-|@@1@@|.|@@332A9AE935847E75E4662D346A5231C6E6E3858869FB68F81B3E2B5B7AB5637@@|-|@@A User-and Item-Aware Weighting Scheme for Combining Predictive User Models@@|-|@@1@@|.|@@B06430338AD6B5F78B222D1F4E886E2F03E9B389D6778ADD081D71E6CAA189@@|-|@@Early Detection of Potential Experts in Question Answering Communities@@|-|@@1@@|.|@@D7A5CDC855CB15885C1A73CB730E284166DD155ED5455224BCA795AB1705BA7@@|-|@@Mining Software Usage Data@@|-|@@1@@|.|@@A5C3B7EE15386A2FC53A08BBFCC42C1CB95CA9FD4AB4BBBF8FBB06BD37AAFAF@@|-|@@Interest-Based Personalized Search@@|-|@@1";
//		if(strModel == null) {
//			return strModel;
//		}
//		int idx = strModel.indexOf(GRAPHDB_TYPE_SEPARATOR);
//		if(idx > -1) {
//			String strMetadata = strModel.substring(0, idx);
//			String[] metaTokens = strMetadata.split(":");
//			if(model != null && metaTokens.length == 3) {
//				try {
//					model.setCount(Integer.parseInt(metaTokens[0]));
//				}
//				catch (Exception e) {
//				}
//				try {
//					model.setTotalCount(Integer.parseInt(metaTokens[1]));
//				}
//				catch (Exception e) {
//				}
//				try {
//					model.setNodeCount(Integer.parseInt(metaTokens[2]));
//				}
//				catch (Exception e) {
//				}
//			}
//			idx += GRAPHDB_TYPE_SEPARATOR.length();
//		}
//		else {
//			idx = 0;
//		}
//		return strModel.substring(idx);
//	}
		
	private static List<Algorithm> googleDocAlgorithms = new ArrayList<Algorithm>();
	static {
		// 1. mind map basiert (stopword removal immer an)
		// a. 10 häufigsten wörter aller mind maps
		Algorithm alg = new Algorithm(null);
		alg.setStopWordRemoval(Algorithm.STOP_WORD_REMOVAL_YES);
		alg.setDataSource(Algorithm.DATA_SOURCE_MAP);
		alg.setDataElement(Algorithm.DATA_ELEMENT_MAPS);
		alg.setResultAmount(10);
		googleDocAlgorithms.add(alg);

		// b. häufigsten wörter der letzten editierten mind map
		alg = new Algorithm(null);
		alg.setStopWordRemoval(Algorithm.STOP_WORD_REMOVAL_YES);
		alg.setDataSource(Algorithm.DATA_SOURCE_MAP);
		alg.setDataElement(Algorithm.DATA_ELEMENT_MAPS);
		alg.setElementSelectionMethod(Algorithm.ELEMENT_SELECTION_METHOD_EDITED);
		alg.setElementAmount(1);
		alg.setResultAmount(10);
		googleDocAlgorithms.add(alg);

		// c 10 häufigsten wörter der letzten zwei editierten mind maps
		alg = new Algorithm(null);
		alg.setStopWordRemoval(Algorithm.STOP_WORD_REMOVAL_YES);
		alg.setDataSource(Algorithm.DATA_SOURCE_MAP);
		alg.setDataElement(Algorithm.DATA_ELEMENT_MAPS);
		alg.setElementSelectionMethod(Algorithm.ELEMENT_SELECTION_METHOD_EDITED);
		alg.setElementAmount(2);
		alg.setResultAmount(10);
		googleDocAlgorithms.add(alg);

		// 2. knoten basiert (Stopword:ja; siblings:ja; children:ja; root: nein)
		// a. 10 häufigsten wörter der letzten 25 editierten knoten
		alg = new Algorithm(null);
		alg.setStopWordRemoval(Algorithm.STOP_WORD_REMOVAL_YES);
		alg.setSiblingNodes(Algorithm.SIBLING_NODES_YES);
		alg.setChildNodes(Algorithm.CHILD_NODES_YES);
		alg.setDataSource(Algorithm.DATA_SOURCE_MAP);
		alg.setDataElement(Algorithm.DATA_ELEMENT_NODES);
		alg.setElementSelectionMethod(Algorithm.ELEMENT_SELECTION_METHOD_EDITED);
		alg.setElementAmount(25);
		alg.setResultAmount(10);
		googleDocAlgorithms.add(alg);

		// b. 10 häufigsten wörter der letzten 100 editierten knoten
		alg = new Algorithm(null);
		alg.setStopWordRemoval(Algorithm.STOP_WORD_REMOVAL_YES);
		alg.setSiblingNodes(Algorithm.SIBLING_NODES_YES);
		alg.setChildNodes(Algorithm.CHILD_NODES_YES);
		alg.setDataSource(Algorithm.DATA_SOURCE_MAP);
		alg.setDataElement(Algorithm.DATA_ELEMENT_NODES);
		alg.setElementSelectionMethod(Algorithm.ELEMENT_SELECTION_METHOD_EDITED);
		alg.setElementAmount(100);
		alg.setResultAmount(10);
		googleDocAlgorithms.add(alg);

		// c. 10 häufigsten wörter der letzten 500 editierten knoten
		alg = new Algorithm(null);
		alg.setStopWordRemoval(Algorithm.STOP_WORD_REMOVAL_YES);
		alg.setSiblingNodes(Algorithm.SIBLING_NODES_YES);
		alg.setChildNodes(Algorithm.CHILD_NODES_YES);
		alg.setDataSource(Algorithm.DATA_SOURCE_MAP);
		alg.setDataElement(Algorithm.DATA_ELEMENT_NODES);
		alg.setElementSelectionMethod(Algorithm.ELEMENT_SELECTION_METHOD_EDITED);
		alg.setElementAmount(500);
		alg.setResultAmount(10);
		googleDocAlgorithms.add(alg);
	}

//	public static List<GoogleDocumentQuery> getGoogleDocumentQueryModels(final Session session, User user) {
//		List<GoogleDocumentQuery> models = new ArrayList<GoogleDocumentQuery>();
//
//		WebResource res = GraphDbUserModelFactory.getClient().resource(GraphDbUserModelFactory.NEO4JREST_URI).path("/db/data/ext/DocearPlugin/graphdb/keywords/");
//		ClientResponse response;
//
//		for (Algorithm alg : googleDocAlgorithms) {
//			alg.setSession(session);
//			response = requestKeywords(user, alg, res);
//			if (response != null && response.getClientResponseStatus().equals(Status.OK)) {
//				addKeywordCombinations(session, response.getEntity(String.class).replace("\"", ""), models);
//			}
//		}
//
//		return models;
//	}

	public static void addKeywordCombinations(final Session session, String model, List<GoogleDocumentQuery> models) {
		if (model == null || model.isEmpty()) {
			return;
		}
		//RESET implement it
//		model = stripMetadata(null, model);
		String[] tokens = model.split(" ");

		
		addSubString(session, models, tokens, 2);
		addSubString(session, models, tokens, 4);
		addSubString(session, models, tokens, 7);
		addSubString(session, models, tokens, 10);
	}

	private static void addSubString(final Session session, List<GoogleDocumentQuery> models, String[] tokens, int max) {
		if(max > (tokens.length+1)) {
			return;
		}
		GoogleDocumentQuery m = new GoogleDocumentQuery(session);
		String s = tokens[0];
		for (int i = 1; i < max && i < tokens.length; i++) {
			s += " " + tokens[i];
		}
		m.setModel(s);
		models.add(m);
	}
	
	public static boolean parseBoolean(String string) {
		if (string == null || string.equalsIgnoreCase("")) {
			return false;
		}
		return string.equals("1") || string.equalsIgnoreCase("true");
	}

	public static File saveMindMap(User user, InputStream inStream) throws IOException {
		File ret = new File("" + user.getId());
		File directory = new File(MINDMAPS_PATH, "" + user.getId());
		if (!directory.exists()) {
			directory.mkdirs();
		}

		ret = new File(ret, System.currentTimeMillis() + ".zip");
		File file = new File(MINDMAPS_PATH, ret.getPath());

		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		int readChar = -1;
		while ((readChar = inStream.read()) != -1) {
			fileOutputStream.write(readChar);
		}
		fileOutputStream.flush();
		fileOutputStream.close();
		inStream.close();
		return ret;
	}

	public static void storeApplicationNumber(Session session, HttpServletRequest request) {
		try {
			User user = UserQueries.getUserByAccessToken(session, request.getHeader("accessToken"));
    		Integer version = Integer.parseInt(request.getHeader("version"));
    		if (version != null) {
    			Transaction transaction = session.beginTransaction();
    			try {
    				Application application = ApplicationQueries.getApplicationByBuildNumber(session, version);				
    
    				UsersApplications userApplications = new UsersApplications();
    				userApplications.setApplication(application);
    				userApplications.setIp(request.getRemoteAddr());
    				userApplications.setTime(new Date());
    				userApplications.setUser(user);
    
    				session.saveOrUpdate(userApplications);
    				session.flush();
    				transaction.commit();
    			}
    			catch (Exception e) {
    				transaction.rollback();
    			}
    		}
		}
		catch(Exception e) {
			return;
		}
	}
	
	//older versions of Docear don't send version with every request --> return null
	public static Integer getClientVersionFromRequest(HttpServletRequest request) {
		try {			
			return Integer.parseInt(request.getHeader("version"));
		}
		catch(Exception e) {
			return null;
		}
	}
	
//	public static String getUserModelFromGraphDbReferencesResponse(Session session, UserModel model) throws Exception {		
//		LinkedList<TFIDFDocument> userModel = new LinkedList<TFIDFDocument>();		
//		System.out.println(model.getModel());
//		
//		String[] lines = model.getModel().split("@@\\|\\.\\|@@");
//		System.out.println(lines.length);
//		
//		Searcher searcher = new Searcher();
//		
//		for (String line : lines) {
//			String[] s = line.split("@@\\|-\\|@@");
//			try {
//				Document d = DocumentQueries.getDocumentByHashOrTitle(session, s[0], s[1]);				
//				if (d!=null) {					
//					TFIDFDocument tfd = new TFIDFDocument();
//					tfd.setDocument(d);
//					Double tf = Double.parseDouble(s[2]);
//					Double idf = 1d;
//					if (model.getAlgorithm().getWeightingScheme() == Algorithm.WEIGHTING_SCHEME_TFIDF) {						
//						idf = searcher.getIDF("references", "dcr_doc_id_"+d.getId());
//					}
//					tfd.setWeight(tf * idf);					
//					
//					boolean inserted = false;
//					//insertion sort based on tfd.count
//					for (int i=0; i<userModel.size(); i++) {
//						if(userModel.get(i).getWeight()<tfd.getWeight()) {
//							userModel.add(i, tfd);
//							inserted = true;
//						}						
//					}
//					if (!inserted) {
//						userModel.add(tfd);
//					}					
//				}
//			}
//			catch(Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return convertBibliographicCouplingUserModel(userModel);
//		
//	}
//	
//	private static String convertBibliographicCouplingUserModel(final List<TFIDFDocument> documents) {
//		StringBuilder sb = new StringBuilder();
//		for (TFIDFDocument doc : documents) {
//			sb.append("dcr_doc_id_").append(doc.getDocument().getId()).append(" ");
//		}
//		
//		return sb.toString();
//	}
	
//	@Deprecated
//	//this method delivers wrong results as TF should be based on the model, not the results from DocumentQueries
//	public static List<TFDocument> getDocumentsFromBibliographicCoupling(Session session, UserModel model, int count) {		
//		List<Document> userModel = new ArrayList<Document>();
//		System.out.println(model.getModel());
//		
//		for (String line : model.getModel().split("@@\\|\\.\\|@@")) {
//			String[] s = line.split("@@\\|-\\|@@");
//			
//			try {
//				Document d = DocumentQueries.getDocumentByHashOrTitle(session, s[0], s[1]);				
//				if (d!=null) {
//					userModel.add(d);
//				}
//			}
//			catch(Exception e) {
//				e.printStackTrace();
//			}
//			
//			convertBibliographicCouplingUserModel(model, userModel);
//		}
//		
//		List<TFDocument> documents = CitationsQueries.getBiboCouplingDocuments(session, userModel, 50);
//		return documents;
//		
////		Document testdoc = DocumentQueries.getDocument(session, 673353);
////		List<CitationsAbsoluteSimilarity> absoluteSimilarities = CitationsQueries.getAbsoluteSimilarities(session, testdoc);
////		System.out.println(absoluteSimilarities.size());
//	}
	
	
	
	
	public static synchronized RecommendationsUsersSettings getRecommendationsUsersSettings(Session session, User user) {
		RecommendationsUsersSettings settings = RecommendationsUsersSettingsQueries.getRecommendationsUsersSettings(session, user);
		if (settings == null) {
			settings = new RecommendationsUsersSettings();
			
			RecommendationsLabel label = RecommendationsLabelQueries.getRandomLabel(session);
			RecommendationsRatingsLabel ratingLabel = RecommendationsRatingsLabelQueries.getRandomLabel(session);
			label.setSession(session);
			ratingLabel.setSession(session);
			settings.setRecommendationLabel(label);
			settings.setRecommendationRatingLabel(ratingLabel);
			settings.setUser(user);	
			user.setSession(session);
			settings.setSession(session);
			if (label.getType() == RecommendationsLabel.TYPE_ORGANIC) {
				Random random = new Random();
				settings.setUsePrefix(random.nextBoolean());
				if (settings.getUsePrefix()) {
					settings.setHighlight(random.nextBoolean());
				}
				else {
					settings.setHighlight(false);
				}
			}
			else {
				settings.setUsePrefix(false);
				settings.setHighlight(false);
			}
			
			Transaction transaction = session.beginTransaction();
			try {
				settings.save();
				transaction.commit();
			}
			catch (Exception e) {
				e.printStackTrace();
				transaction.rollback();
				return null;
			}
		}
		
		return settings;
		
	}

	public static String getPasswordRequestMailText(UserPasswordRequest pwRequest, String email) {
		StringBuilder message = new StringBuilder();

		try {
			email = URLEncoder.encode(email, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
		message.append("Hello,\n");
		message.append("\n");
		message.append("you receive this email because you requested to change your password. Please use the link below and follow the instructions on the site.\n");
		message.append("\n");
		message.append("Please use the link below and follow the instructions on the site.\n");
		message.append("\n");		
		message.append(docearEMailConfig.getProperty("docear.pwd.reset.url", "https://www.docear.org/my-docear/change-password")+"?token="+pwRequest.getToken()+"&mail="+email+"\n");
		message.append("\n");
		message.append("If you did not request a password change, please ignore this email."); 
		message.append("\n");
		message.append("\n");		
		message.append("Yours,\n");
		message.append("The Docear Team.\n");
		return message.toString();
	}

	public static String getUserRegistrationMail(String username, String password, String email) {
		StringBuilder message = new StringBuilder();

		message.append("Hello,\n");
		message.append("\n");
		message.append("you successfully signed up at Docear. You can now use Docear's free online features such as PDF metadata retrieval, Backup, and Recommendations.\n");
		message.append("\n");
		message.append("Your information are as follows:\n");
		message.append("\n");
		message.append("Email: "+email+"\n");
		message.append("User: "+username+"\n");
		message.append("Password: "+maskedPassword(password, false)+"\n");
		message.append("\n");
		message.append("If you experience any problems, or have questions on how to use Docear, please do not hesitate to contact us https://www.docear.org/docear/contact/. You may also find the following links useful.\n");
		message.append("\n");
		message.append("Manual: https://www.docear.org/support/user-manual/\n");
		message.append("Videos: https://www.docear.org/software/screenshots/\n");
		message.append("Jobs & Internships: https://www.docear.org/docear/jobs/\n");
		message.append("Blog: https://www.docear.org/docear/blog/\n");
		message.append("\n");
		message.append("\n");		
		message.append("Yours,\n");
		message.append("The Docear Team.\n");
		return message.toString();
	}
	
	public static String getUserResetConfirmationMail(String username, String password, String email) {
		StringBuilder message = new StringBuilder();

		message.append("Hello,\n");
		message.append("\n");
		message.append("you successfully changed your password for Docear. Your account information is as follows:\n");
		message.append("\n");
		message.append("Email: "+email+"\n");
		message.append("User: "+username+"\n");
		message.append("Password: "+maskedPassword(password, false)+"\n");
		message.append("\n");
		message.append("\n");
		//remove as soon as Docear-Desktop handles unauthorized exceptions properly
		message.append("Please note that all stored sessions with your old docear account credentials are invalid from now on.\n");
		message.append("\n");
		message.append("\n");		
		message.append("Yours,\n");
		message.append("The Docear Team.\n");
		return message.toString();
	}

	public static String maskedPassword(String password, boolean showFirstLetters) {
		int len = password.length();
		StringBuilder maskedString = new StringBuilder();
		if(showFirstLetters) {
			maskedString.append(password.substring(0,3));
		}
		for(int i = 3; i < len; i++) {
			maskedString.append("*");
		}
		if(!showFirstLetters) {
			maskedString.append(password.substring(len-3));
		}
		return maskedString.toString();	
	}

	
}
