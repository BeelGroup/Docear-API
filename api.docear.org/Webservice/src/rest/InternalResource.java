package rest;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.docear.googleparser.GoogleScholarParser;
import org.docear.googleparser.WebSearchResult;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mrdlib.index.Searcher;
import org.sciplore.data.NameSeparator;
import org.sciplore.data.NameSeparator.NameComponents;
import org.sciplore.database.AtomicOperation;
import org.sciplore.database.AtomicOperationHandle;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.queries.DocumentsBibtexPdfHashQueries;
import org.sciplore.queries.DocumentsBibtexQueries;
import org.sciplore.queries.DocumentsBibtexUsersQueries;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.queries.InternalQueries;
import org.sciplore.queries.MindmapsPdfHashQueries;
import org.sciplore.resources.Algorithm;
import org.sciplore.resources.Contact;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentsBibtex;
import org.sciplore.resources.DocumentsBibtexPdfHash;
import org.sciplore.resources.DocumentsBibtexUsers;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.GoogleDocumentQuery;
import org.sciplore.resources.MindmapsPdfHash;
import org.sciplore.resources.Person;
import org.sciplore.resources.User;

import util.BibtexCommons;
import util.DocumentCommons;
import util.InternalCommons;
import util.RecommendationCommons;
import util.ResourceCommons;
import util.Tools;
import util.UserCommons;
import util.UserSessionProvider;
import util.UserSessionProvider.UserSession;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/internal")
public class InternalResource {
	private static final int MAX_GOOGLE_REQUESTS_PER_IP = 20;
	private static Boolean MUTEX = true;
	private static Boolean RANDOM_MODEL_CREATION_IN_PROGRESS = false;
	public static Integer HMA_GOOGLE_REQUESTS = 0;
	private static String ipChani = "";
	
	@GET	
	@Path("/test")
	public Response test(@Context UriInfo ui, @Context HttpServletRequest request) {		
		final Session session = SessionProvider.sessionFactory.openSession();
		try {			
			SQLQuery query = (SQLQuery) session.createSQLQuery("select model from user_models U where id > 170000 order by rand() limit 10");
			long time = System.currentTimeMillis();
			for (Object o : query.list()) {
				IndexReader ir = SessionProvider.getLuceneIndexer().getIndexReader(); //true=apply deletes (slower/costly); false=ignore deletes(if deleted docs are no problem)			
				IndexSearcher is = new IndexSearcher(ir);	
				System.out.println("\n---\n"+(String)o+"\n---\n");
				Query q = new QueryParser(Version.LUCENE_34, "title", new StandardAnalyzer(Version.LUCENE_34)).parse((String) o);
				TopDocs td = is.search(q, 1000);
			}
			System.out.println("TEST --> used 10 query in "+(System.currentTimeMillis()-time)+"ms.");
			
			String s  = "text:joeran text:finanzierung text:fremdkapital text:sichern text:optionen text:übersicht text:aktuell text:marketing text:recommender text:infrastruktur text:docear text:parser text:recs text:server text:recommendation text:sachsen text:pdf text:naher text:anhalt text:venture text:adjustments text:mäßig text:diss text:offline text:misc text:schreiben text:retrieval text:verlage text:1st text:zukunft text:ventures.com text:indexer text:forschung text:interessant text:stiftungen text:sponsored text:flood text:evaluator text:email text:scholar text:capital text:langfristig text:todos text:google text:nutzer text:www.hp text:labels text:captcha text:hackfwd.com text:outsorcing text:workflow text:www.bvp.com text:publish text:gewinnen text:ctrs text:möglichst text:fix text:tweaks text:journals text:recommendations text:decent text:notfallplan text:breaker text:neue text:surfstick text:angels text:versicherungen text:nutzern text:bmp text:alt text:evaluating text:werbung text:www.holtzbrinck.com text:finden text:www.holtzbrinck text:webservice text:bekommen text:todo text:laufen text:bugs text:umsatz text:future text:plattformen text:system text:3rd text:user text:companyinfo text:vielen text:viele text:brandenburg text:database text:logging text:ziel text:stefan text:paper text:timeout text:eigener text:help text:cbf text:store text:lassen text:user's text:mind text:bei text:jöran text:januar text:infrastructure text:dumont text:titles text:users text:http text:pdfs text:bibtex text:last_notification text:von text:www.intel.com text:maps text:posters text:papers text:bund text:frequencies text:beta8 text:stars text:blacklisted text:website text:business text:index text:person text:implement text:adding text:happens text:ctr text:much text:results text:timestamp text:hash text:clicked text:wir text:too text:table text:systems text:meta text:index.htm text:parsing text:xref text:document_title text:folded text:artikel text:unified text:clicks text:displaying text:add text:index.php text:für text:literature text:usability text:map text:get text:ignore text:index.html text:holding text:user_applications text:unregistered text:nicht text:verlinkte text:die text:dfg text:freeplane text:stop_word text:creator text:illustration text:presentation text:keyword text:parscit text:trigger text:registration text:hartz4 text:ich text:hashs text:optimize text:analyse text:irrelvant text:haben text:dateien text:accuracy text:concept text:citations text:berlin text:improve text:extraction text:nodes text:recommend text:instad text:passwort text:calculate text:moved text:monitoring text:wievielen text:nur text:entries text:types text:auch text:fulltextid text:prototypen.html text:how text:proxy text:title text:pappers text:wants text:2nd text:modelling text:opened text:detailid text:invalid text:sync text:machtes text:bug text:habe text:weilsie text:auf text:schließ text:paper.was text:empfehlungen text:graphdb text:tabelle text:einzahlen text:dlib text:sind text:müssen.und text:diplomiertem text:what text:sessions text:dbtotal text:beyond text:literaturordner text:titel text:der text:sindwir text:organic text:davon text:idf text:wieviele text:highlight text:document_id text:produktdesigner text:annotations text:intervall text:zuschuss text:analyze text:hochgeschoben text:gender text:kannst text:muss text:document text:ribbons text:internal text:oder text:node text:service text:und text:euro text:dsl text:nutzerein text:vertrag text:zypern text:reingucken text:null text:extrahiertem text:ist text:papern text:aber text:algorithm text:registered text:read text:setzen text:enhalten text:dass text:eine text:fachrichtung text:downloader text:header text:merkwürdigkeiten text:documentid text:edited text:update text:vielleicht text:runterladen text:parsed text:sein text:sollten text:annotation text:workspace text:never text:vermögen text:flag text:produktdesign text:startup text:machen text:process text:bookmarks text:webserver text:gehts text:influence text:method text:different text:search text:assume text:löschen text:falsches text:privater text:mit text:daad text:loged text:ego text:gleichen text:checker text:info text:shown text:bloß text:data text:mindmaps text:grüße text:benutzt text:referenzierte text:e.g text:text text:tech text:neu text:deleting text:sense text:verlinken text:das text:holistic text:anzahlen text:fixing text:wieder text:eingetragen text:dann text:model text:monat text:irgendwo text:lösung text:marcel text:volltext text:neuen text:conference text:seltsam text:abgeschaltet text:speichern text:maximum text:insgesamt text:filtering text:nochmal text:wundern text:decides text:wenn text:metadaten text:mal text:temporäre text:prototypen text:send text:referenzen text:dokumente text:hallo text:already text:sie text:keine text:classifying text:bisherige text:empfohlen text:aktiviert text:aufgefallen text:public text:when text:hin text:4th text:zuordnung text:created text:ein text:drin text:mehrfach text:hashes text:observation text:wieso text:lücken text:additional text:age text:prozentuale text:performance text:leichter text:structure text:zwischen text:easier text:übersehen text:eigentlich text:zusätzlich text:adjustment text:parse text:storing text:cancel text:check text:existiert text:immer text:duplicates text:connects text:mache text:absolutely text:bag text:gmbh text:use text:abend text:query text:inhalt text:förderung text:bibliographic text:algorithmus text:als text:classify text:hast text:collaborative text:turned text:downloading text:seite text:seitdem text:opens text:ziemlich text:vermutlich text:work text:entweder text:deliver text:wichtig text:finished text:ids text:sehen text:sammeln text:revisions text:sollen text:user’s text:incoming text:datenbank text:basierend text:convert text:gern text:etwas text:möglich text:überhaupt text:otherwise text:minuten text:mitglieder text:paar text:statistik text:still text:date text:real text:wurden text:ask text:meinen text:teste text:uns text:erzeugt text:counting text:mehr text:fehler text:letzte text:duplicate text:ranks text:erstellt text:sonst text:darüber text:falls text:einen text:unsere text:eher text:eigenen text:falle text:erhalten text:unserer text:einem text:werden text:sql text:wie text:jetzt text:wenige text:während text:make text:removal text:haut text:collect text:save text:zum text:url text:maybe text:unterschiede text:werte text:gesamten text:mich text:anhand text:kaum text:gemacht text:unterschiedliche text:join text:number text:displayed text:unterschiedlichen text:gerade text:wenig text:link text:den text:heute text:über";
			
			time = System.currentTimeMillis();
			IndexReader ir = SessionProvider.getLuceneIndexer().getIndexReader(); //true=apply deletes (slower/costly); false=ignore deletes(if deleted docs are no problem)			
			IndexSearcher is = new IndexSearcher(ir);			
			Query q = new QueryParser(Version.LUCENE_34, "title", new StandardAnalyzer(Version.LUCENE_34)).parse(s);
			TopDocs td = is.search(q, 1000);
			System.out.println("TEST --> used special query in "+(System.currentTimeMillis()-time)+"ms.");
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
    		Tools.tolerantClose(session);
		}
		
		return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, "ok");
	}
	
	
	
	@GET
	@Path("/testAlg")
	public Response testAlg(@Context UriInfo ui, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			SQLQuery query = session.createSQLQuery("SELECT DISTINCT user_id FROM recommendations_documents_set WHERE created IS NOT NULL AND created >='2013-09-01' ORDER BY created DESC");
			List<BigInteger> result = query.list();		
			
			int counter = 0;
			for (BigInteger userId : result) {
				RecommendationCommons.logger.log("computing test recommendation for user ["+userId+"] ("+counter+" of "+result.size()+")");
				RecommendationCommons.computeForSingleUser(userId.intValue(), 0);
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				counter++;
			}
		}
		finally {
			Tools.tolerantClose(session);
		}
		
		return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, "ok");
	}
	
	@GET
	@Path("/recommendations/compute")
	public Response computeRecommendations(@Context UriInfo ui, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}
			
			RecommendationCommons.computeForAllUsers(session);
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, "ok");
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.CONFLICT, e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/recommendations/dryRun")
	public Response dryRunOfflineRecommendations(@Context UriInfo ui, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}
			
			RecommendationCommons.dryRun(session);
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, "ok");
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.CONFLICT, e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/recommendations/stop")
	public Response stop(@Context UriInfo ui, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}
			
			RecommendationCommons.stopOfflineRecommendations();
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, "ok");
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.CONFLICT, e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/recommendations/testcomputation")
	public Response testcomputation(@Context UriInfo ui, @Context HttpServletRequest request, @QueryParam("userId") Integer userId, 
			@QueryParam("algorithmId") Integer algorithmId) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}
			
			System.out.println("computing recommendations for user["+userId+"] and algorithm ["+algorithmId+"]");
			
			user = (User) session.get(User.class, userId);
			Algorithm algorithm = (Algorithm) session.get(Algorithm.class, algorithmId);
			
			RecommendationCommons.dryRunForSingleUser(session, user, algorithm);
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, "ok");
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.CONFLICT, e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@POST
	@Path("/chani")
	public Response search(@Context UriInfo ui, @Context HttpServletRequest request, @FormParam("ip") String ip) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("stlanger");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}

			ipChani = ip;

			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, "ok");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/chani")
	public Response search(@Context UriInfo ui, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("stlanger");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}

			return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, ipChani);
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@POST
	@Path("/idf")
	public Response getMetadata(@Context UriInfo ui, @Context HttpServletRequest request, @FormParam("terms") String csvTerms) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}

			String[] terms = csvTerms.split(" ");

			Searcher searcher;
			try {
				searcher = new Searcher();
				Double[] idf = searcher.getIDF(terms, "text");

				StringBuilder sb = new StringBuilder();
				sb.append(idf[0]);
				for (int i = 1; i < idf.length; i++) {
					sb.append(" ").append(idf[i]);
				}

				return Tools.getHTTPStatusResponse(Status.OK, sb.toString());
			}
			catch (Exception e) {
				e.printStackTrace();
				return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
		finally {
			Tools.tolerantClose(session);
		}

	}

	
	@GET
	@Path("/index/search")
	public Response search(@Context UriInfo ui, @Context HttpServletRequest request, @QueryParam("query") String query,
			@QueryParam("number") int maxResults) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			session.setFlushMode(FlushMode.MANUAL);

			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}

			StringBuilder sb = new StringBuilder();
			try {
				Query q = new QueryParser(Version.LUCENE_34, "title", new StandardAnalyzer(Version.LUCENE_34)).parse(query);
				IndexReader ir = SessionProvider.getLuceneIndexer().getIndexReader();
				IndexSearcher is = new IndexSearcher(ir);
			
				Term term = new Term(query.split(":")[0], query.split(":")[1]);
				TermDocs termDocs = ir.termDocs();
				termDocs.seek(term);
				while (termDocs.next()) {
					org.apache.lucene.document.Document doc = is.doc(termDocs.doc());
					sb.append("freq: ").append(termDocs.freq()).append("\n").append(InternalCommons.getLuceneDocumentContent(doc)).append("\n\n");
				}

				return UserCommons.getHTTPStatusResponse(ClientResponse.Status.OK, sb.toString());
			}
			catch (Exception e) {
				e.printStackTrace();
				return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@POST
	@Path("/mindmaps/{id}/pdf_hashes")
	// send hashes as csv: "hash:count,hash:count,..."
	public Response postPdfHashesInMindmap(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") Long mindmapId,
			@FormParam("username") String userName, @FormParam("pdfHashes") String pdfHashes) {

		List<Document> documentsForSpider = new ArrayList<Document>();
		
		Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			if (pdfHashes == null || pdfHashes.trim().length() <= 0) {
				return Tools.getHTTPStatusResponse(Status.NO_CONTENT, "OK");
			}

			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!("pdfdownloader".equals(userName)) | !ResourceCommons.authenticate(request, user)) {
				System.out.println("(GoogleQueryWorker) Rejected unauthorised attempt to update database");
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}

			if (MindmapsPdfHashQueries.isRevisionAlreadyStored(session, mindmapId)) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.NOT_MODIFIED,
						"hashes for this mindmap revision already in database");
			}			
			
			Transaction transaction = session.beginTransaction();
			try {
				for (String s : pdfHashes.split("@@\\|\\.\\|@@")) {
					// item[0] --> pdfHash; item[1] --> title (optional);
					// item[1] --> count in mindmap
					String[] item = s.split("@@\\|-\\|@@");
					if (item.length >= 3) {
						MindmapsPdfHash mph = new MindmapsPdfHash();
						mph.setMindmapId(mindmapId);
						mph.setPdfHash(item[0]);
						mph.setCount(Integer.parseInt(item[2]));
						session.saveOrUpdate(mph);

						// insert document (title) if not exists and title has
						// more than 2 words
						if (item[1].trim().length() > 0 && item[1].trim().split(" ").length > 2) {
							Document doc = new Document(session);
							String title = item[1].trim();
							doc.setTitle(title);
							if (DocumentQueries.getValidCleanTitle(title) == null) {								
								continue;
							}

							session.saveOrUpdate(doc);
							session.flush();
							
							doc = (Document) doc.getPersistentIdentity();
							
							DocumentsPdfHash pdfhash = new DocumentsPdfHash();
							pdfhash.setSession(session);
							pdfhash.setDocument(doc);
							pdfhash.setHash(item[0]);
							
							DocumentsPdfHash tmp = (DocumentsPdfHash) pdfhash.getPersistentIdentity();
							if(tmp == null) {    
    							session.saveOrUpdate(pdfhash);
    							session.flush();
    							
    							documentsForSpider.add(doc);
							}							
						}
					}
				}
				session.flush();
				transaction.commit();	
				
			}
			catch (Throwable e) {
				e.printStackTrace();
				transaction.rollback();
				return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
			} 
			
			transaction = session.beginTransaction();
			try {
    			for (Document doc : documentsForSpider) {
    				Document d = (Document) doc.getPersistentIdentity();
    				if (d==null) {    					
    					System.out.println("rest.InternalResource.postPdfHashesInMindmap(ui, request, mindmapId, userName, pdfHashes) --> NO PERSISTENT DOCUMENT FOR TITLE: \""+doc.getTitle()+"\"");    					
    					continue;
    				}
        			if (d.getXrefs() == null || d.getXrefs().size() == 0) {
        				InternalCommons.addUserDocumentToSpiderList(session, d);
        			}
    			}
    			session.flush();
    			transaction.commit();
			}
			catch (Throwable e) {
				e.printStackTrace();
				transaction.rollback();
				return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
			}
			

			return Tools.getHTTPStatusResponse(Status.OK, "OK");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/documents/{hash}/metadata")
	public Response getMetadata(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "hash") String hash,
			@QueryParam("username") String userName, @QueryParam("title") String title,
			@DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("stream") boolean stream) {

		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		User user = new User(session).getUserByEmailOrUsername(userName);
		if (user == null || !ResourceCommons.authenticate(request, user)) {
			return Tools.getHTTPStatusResponse(Status.UNAUTHORIZED,
					"This method is currently only available for users who have enabled at least one of Docear's online features.");
		}
		if (!DocumentsBibtexUsersQueries.hasAvailableRequests(session, user.getId())) {
			return Tools.getHTTPStatusResponse(Status.FORBIDDEN, "You can only request metadata for " + DocumentsBibtexUsersQueries.MAX_USER_REQUESTS_PER_DAY
					+ " documents a day.");
		}

		Set<Integer> possibleBibIDs = new HashSet<Integer>();
		try {
			// Hash und Titel für Nutzer speichern um die notwendigen
			// Datensätze
			// nach dem PUT "putAndReturnMetadata(...)" erzeugen zu können
			UserSession userSession = UserSessionProvider.getUserSessionProvider().getUserSession(user.getId());
			userSession.remove("lastHash");
			userSession.remove("possibleBibIDs");
			userSession.put("possibleBibIDs", possibleBibIDs);

			Set<DocumentsBibtex> bibResults = new TreeSet<DocumentsBibtex>(new Comparator<DocumentsBibtex>() {

				public int compare(DocumentsBibtex bib1, DocumentsBibtex bib2) {
					return bib1.getId() - bib2.getId();
				}
			});

			Collection<DocumentsBibtex> documentsbibtex = DocumentsBibtexQueries.getDocumentsBibtexByHash(session, hash);
			// wenn hash schon zu einem Dokument in der Datenbank vorhanden
			// ist
			if (documentsbibtex != null && documentsbibtex.size() > 0) {
				for (DocumentsBibtex bib : documentsbibtex) {
					if (bib.getBibtex() != null && !bib.getBibtex().isEmpty()) {
						bibResults.add(bib);
					}
				}
				// --> if no title has been extracted (OCR) --> use titel of the
				// first document which matches the hash
				if (title == null || title.isEmpty()) {
					title = documentsbibtex.iterator().next().getDocument().getTitle();
				}
			}

			// no bibtex-hash combination found:
			// use Google-Parser to find similar titles
			if (bibResults.size() == 0) {
				System.out.println("searching metadata using google scholar");
				if (title == null) {
					return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "no metadata found");
				}
				GoogleScholarParser parser = GoogleScholarParser.createParser("en", null);
				parser.createCookiesEnabled(false);

				if (HMA_GOOGLE_REQUESTS >= MAX_GOOGLE_REQUESTS_PER_IP) {
					InternalCommons.hmaReconnectThread(false);
					return Tools.getHTTPStatusResponse(Status.CONFLICT, "Docear meta-data service temporarily not available. Please try again later.");
				}

				Collection<WebSearchResult> webSearchResults = parser.getMatchingTitles(title, null);
				if (parser.getLastReponseCode() == 503) {
					InternalCommons.hmaReconnectThread(true);
				}
				System.out.println("metadata extraction found results: "+webSearchResults.size());
				
				// requests to google: 1 for the page + 1 for each bibtex entry
				Transaction transaction = session.beginTransaction();
				synchronized (HMA_GOOGLE_REQUESTS) {
					HMA_GOOGLE_REQUESTS += webSearchResults.size() + 1;
				}
				
				try {
					DocumentsBibtexUsers dbu = DocumentsBibtexUsersQueries.getDocumentsBibtexUser(session, user.getId());
					dbu.setCounter(dbu.getCounter() + 1);
					session.saveOrUpdate(dbu);
					session.flush();

//					dbp.setCounter(dbp.getCounter() + googleRequests);
//					session.saveOrUpdate(dbp);
					session.flush();
					transaction.commit();
				}
				catch (Exception e) {
					e.printStackTrace();
					transaction.rollback();
				}

				for (Iterator<WebSearchResult> iterator = webSearchResults.iterator(); iterator.hasNext();) {
					transaction = session.beginTransaction();
					try {
						WebSearchResult webSearchResult = iterator.next();
						if (webSearchResult.getBibTexLink() == null) {
							continue;
						}
						Document document = new Document(session);
						document.setTitle(webSearchResult.getTitle());
						if (DocumentQueries.getValidCleanTitle(title) == null) {
							return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.BAD_REQUEST, "The title you have entered is too short to process your query. Please use a title with at least 7 letters.");
						}
						String year = null;
						if (webSearchResult.getYear() != null) {
							year = webSearchResult.getYear().toString();
							document.setPublishedYear(Short.valueOf(year));
						}

						URL link = webSearchResult.getLink();
						if (link != null) {
							DocumentXref xref = new DocumentXref(session);
							xref.setDocument(document);
							xref.setSource("scholar.google.com");

							xref.setSourcesId(link.toExternalForm());
							xref.setCiteCount(webSearchResult.getCiteCount());
							xref.setDlAttempts(0);
							xref.setDocument(document);
							document.addXref(xref);

							session.saveOrUpdate(xref);
						}
						session.saveOrUpdate(document);
						session.flush();
						session.clear();

						Document d = DocumentQueries.getDocument(session, document);
						DocumentsPdfHash dph = new DocumentsPdfHash();
						dph.setDocument(d);
						dph.setHash(hash);
						session.saveOrUpdate(dph);

						String bibtex = parser.dlBibTeXData(webSearchResult.getBibTexLink(), null);						
						if (bibtex != null) {
							System.out.println("metadata extraction retrieved: "+bibtex);
							bibtex = BibtexCommons.prepareForSave(bibtex);
							DocumentsBibtex db = DocumentsBibtexQueries.getDocumentsBibtex(session, d, bibtex);
							// if no documentsbibtex entry found, create new
							// entry
							db = new DocumentsBibtex(session);
							db.setBibtex(bibtex);
							db.setDocument(d);

							session.saveOrUpdate(db);
							session.flush();
							session.clear();
						}
						else {
							System.out.println("metadata extraction: no bibtex found");
						}
						transaction.commit();

						bibResults.addAll(DocumentsBibtexQueries.getDocumentsBibtex(session, d));
					}
					catch (Throwable ex) {
						ex.printStackTrace();
						transaction.rollback();
					}
				}
			}			
			
			if (bibResults != null && bibResults.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (DocumentsBibtex bib : bibResults) {
					sb.append(BibtexCommons.prepareBibEntry(bib.getBibtex(), bib.getId()));
					sb.append("\n\n");
					possibleBibIDs.add(bib.getId());
				}
				userSession.put("lastHash", hash);
				System.out.println(sb.toString());
				return Tools.getHTTPStatusResponse(Status.OK, sb.toString());
			}

			return Tools.getHTTPStatusResponse(Status.NO_CONTENT, "no BibTeX data found.");

		}
		catch (Exception e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		finally {
			if (HMA_GOOGLE_REQUESTS >= MAX_GOOGLE_REQUESTS_PER_IP) {
				InternalCommons.hmaReconnectThread(false);
			}			
			Tools.tolerantClose(session);
		}
	}

	@PUT
	@Path("/documents/{hash}/metadata")
	public Response putAndReturnMetadata(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam("hash") String hash,
			@QueryParam(value = "id") Integer bibID, @QueryParam("username") String userName,
			@DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("commit") boolean commit, @QueryParam("stream") boolean stream) {
		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		Transaction transaction = session.beginTransaction();
		try {
			User user = new User(session).getUserByEmailOrUsername(userName);
			if (!ResourceCommons.authenticate(request, user)) {
				return Tools.getHTTPStatusResponse(Status.UNAUTHORIZED, "no valid access token.");
			}

			// do not just accept the given document_id and hash, but check if
			// it really was in the UserSession
			// prevent from malicious attempts to set the wrong document_id to a
			// hash
			UserSession userSession = UserSessionProvider.getUserSessionProvider().getUserSession(user.getId());
			if (hash == null || userSession == null || !hash.equals(userSession.get("lastHash"))) {
				return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "invalid hash");
			}

			@SuppressWarnings("unchecked")
			Set<DocumentsBibtex> possibleBibIDs = (Set<DocumentsBibtex>) userSession.get("possibleBibIDs");
			if (possibleBibIDs == null || (bibID != null && !possibleBibIDs.contains(bibID))) {
				return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "denying write-access to this document");
			}

			Collection<DocumentsBibtex> dbByHash = DocumentsBibtexQueries.getDocumentsBibtexByHash(session, hash);

			DocumentsPdfHash dph = DocumentsPdfHashQueries.getPdfHash(session, hash);

			for (DocumentsBibtex db : dbByHash) {
				DocumentsBibtexPdfHash dbp = DocumentsBibtexPdfHashQueries.getDocumentsBibtexPdfHash(session, db.getId(), dph.getId());
				if (dbp == null) {
					dbp = new DocumentsBibtexPdfHash();
					dbp.setDocumentsBibtex(db);
					dbp.setDocumentsPdfHash(dph);
				}
				// commitCounter++
				if (commit && dbp.getDocumentsBibtex().getId().equals(bibID)) {
					dbp.setCommitCounter(dbp.getCommitCounter() + 1);
				}
				// set rejectCounter++ for all other DocumentsBibtex
				else {
					dbp.setRejectCounter(dbp.getRejectCounter() + 1);
				}

				session.saveOrUpdate(dbp);
			}
			session.flush();
			session.clear();

			transaction.commit();

			return Tools.getHTTPStatusResponse(Status.OK, "OK");
		}
		catch (Exception e) {
			e.printStackTrace();
			transaction.rollback();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}

	}

	@POST
	// @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/recommendations/finish_keywords")
	public Response finishKeywords(@FormParam("clientId") String clientId, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			InternalQueries.finishKeywords(session, clientId, true);
		}
		catch (Exception e) {
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}

		return Tools.getHTTPStatusResponse(Status.OK, "ok");
	}

	@POST
	// @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/recommendations/reset_keywords")
	public Response resetKeywords(@FormParam("clientId") String clientId, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			InternalQueries.finishKeywords(session, clientId, false);
		}
		catch (Exception e) {
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}

		return Tools.getHTTPStatusResponse(Status.OK, "ok");
	}

	@POST
	// @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/recommendations/document")
	public Response newDocument(@FormParam("title") final String title, @FormParam("link") final String link, @FormParam("username") String userName,
			@FormParam("source") final String source, @FormParam("modelId") final Integer modelId, @FormParam("year") final Short year,
			@FormParam("citeCount") final Integer citeCount, @FormParam("rank") final Integer rank, @Context HttpServletRequest request) {

		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!("pdfdownloader".equals(userName)) | !ResourceCommons.authenticate(request, user)) {
				System.out.println("(GoogleQueryWorker) Rejected unauthorised attempt to update database");
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}

			
			AtomicOperation<Response> ao = new AtomicOperation<Response>() {
				
				@Override
				public Response exec(Session session) {
					Transaction transaction = session.beginTransaction();
					try {
    					// save to database
    					Document document = new Document(session, title);
    					if (DocumentQueries.getValidCleanTitle(title) == null) {
    						return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.BAD_REQUEST, "title is not valid");
    					}
    					document.setPublishedYear(year);
    					
    					Document doc = (Document) document.getPersistentIdentity();
    					if (doc == null) {
    						session.save(document);    						
    						session.refresh(document);
    					}
    					else {
    						document = doc;
    					}
    					
    					document.setSession(session);
    					
    					DocumentXref xref = new DocumentXref();
    					xref.setSession(session);
    					xref.setCiteCount(citeCount == null ? 0 : citeCount);
    					xref.setRank(rank);
    					xref.setSource(source.toLowerCase());
    					xref.setSourcesId(link);
    
    					xref.setDlAttempts(0);
    					xref.setDocument(document);
    					document.addXref(xref);
    
    					session.setFlushMode(FlushMode.MANUAL);
    					DocumentXref persistent = (DocumentXref) xref.getPersistentIdentity();
    					if (persistent != null) {
    						if (persistent.getRank() == null || persistent.getRank() > rank) {
    							persistent.setRank(rank);
    							session.update(persistent);
    						}
    					}
    					else {    					
    						session.save(xref);
    					}
    
    					GoogleDocumentQuery model = (new GoogleDocumentQuery(session)).getGoogleDocumentQuery(modelId);
    					model.setQuery_date(Calendar.getInstance().getTime());
    					session.update(model);
    					session.flush();
    					transaction.commit();
    
    					return Response.status(Status.ACCEPTED).build();
    				}
    				catch (Exception e) {
    					e.printStackTrace();
    					transaction.rollback();
    					System.out.println(e);
    					System.out.println("(GoogleQueryWorker) failed to update in database - this should not happen!");
    					return Response.status(Status.BAD_REQUEST).build();
    				}
				}
			};
			
			AtomicOperationHandle<Response> handle = SessionProvider.atomicManager.addOperation(ao);

			return handle.getResult();
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/recommendations/retrieve_keywords")
	public Response retrieveKeywordsRobot(@Context UriInfo uriInfo, @Context HttpServletRequest request, @QueryParam("clientId") String clientId, @QueryParam("count") Integer count) {
		// prevent from dirty read
		synchronized (MUTEX) {
			final Session session = SessionProvider.sessionFactory.openSession();
			try {
				User pdfdownloaderUser = new User(session).getUserByEmailOrUsername("pdfdownloader");
				if (!ResourceCommons.authenticate(request, pdfdownloaderUser)) {
					return UserCommons.getHTTPStatusResponse(ClientResponse.Status.UNAUTHORIZED, "(GoogleQueryWorker) no valid access token.");
				}

				List<GoogleDocumentQuery> models = InternalQueries.retrieveKeywords(session, count);
				String result = "";
				for (GoogleDocumentQuery model : models) {
					model.setModel(model.getModel().replaceAll("\n", " "));
					model.setLockId(clientId);
					result = result.concat(model.getId() + ":");
					result = result.concat(model.getModel());
					result = result.concat("\n");
					Transaction transaction = session.beginTransaction();
					session.saveOrUpdate(model);
					session.flush();
					transaction.commit();
				}

				if (models.size() == 0) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							synchronized (RANDOM_MODEL_CREATION_IN_PROGRESS) {
								if (RANDOM_MODEL_CREATION_IN_PROGRESS) {
									return;
								}
							}

							RANDOM_MODEL_CREATION_IN_PROGRESS = true;
							try {
								InternalCommons.createRandomUserModels();
							}
							finally {
								RANDOM_MODEL_CREATION_IN_PROGRESS = false;
							}
						}
					}).start();
				}

				return Response.ok(result, MediaType.TEXT_PLAIN).build();
			}
			catch (Throwable t) {
				System.out.println(t.getMessage());
				return Response.status(Status.NO_CONTENT).build();
			}
			finally {
				Tools.tolerantClose(session);
			}

		}
	}

	@POST
	@Path("/document/{hash}/references")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postReferences(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "hash") String hash,
			@FormDataParam("referencesData") InputStream xtractStream, @FormDataParam("xtract") FormDataContentDisposition xtractDetail,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("stream") boolean stream) {

		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
		if (!ResourceCommons.authenticate(request, user)) {
			return Tools.getHTTPStatusResponse(Status.UNAUTHORIZED, "not authorized.");
		}

		Document doc = DocumentQueries.getDocumentByHashOrTitle(session, hash, null);
		if (doc == null) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "request document with hash='" + hash + "' does not exist.");
		}

		try {
			if ("xml".equalsIgnoreCase(format)) {
				Transaction transaction = session.beginTransaction();
				try {
					String xtrString = IOUtils.toString(xtractStream, "UTF-8");
					DocumentCommons.updateDocumentData(session, doc, xtrString);

					transaction.commit();
					Tools.getLuceneIndexer().updateDocument(doc, hash).commit();
					return Tools.getHTTPStatusResponse(Status.OK, "OK");
				}
				catch (Exception e) {
					e.printStackTrace();
					return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could not add references: " + e.getMessage());
				}
				finally {
					if (transaction.isActive()) {
						transaction.rollback();
					}
				}
			}
		}
		finally {
			Tools.tolerantClose(session);
		}
		return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "invalid format=\"" + format + "\" for POST request");
	}

	// //DISABLE THIS METHOD AS SOON AS IT HAS BEEN USED
	@GET
	@Path("/document/cleantitles")
	public Response regenerateCleanTitles(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
		InternalCommons.regenerateCleantitles();
		return Tools.getHTTPStatusResponse(Status.OK, "OK");
	}

	@GET
	@Path("/recommendations/offline_evaluator/generate_cache/{maxMphId}")
	public Response generateEvaluatorCache(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "maxMphId") int maxMphId) {
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		
		User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
		if (!ResourceCommons.authenticate(request, user)) {
			System.out.println("(GoogleQueryWorker) Rejected unauthorised e to update database");
			return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
		}
				
		//maxMphId is for now: 1943258
		org.hibernate.Query query = session.createSQLQuery("INSERT INTO recommendations_evaluator_chache(user_id, latest_mindmaps_pdfhash_id) " +
				"SELECT A.user, MAX(min_id) AS id FROM (SELECT MIN(MP.id) AS min_id, M.user FROM mindmaps_pdfhash MP " + 
				"JOIN documents_pdfhash DP ON (MP.pdfhash = DP.hash) JOIN document_xref DX ON (DX.document_id = DP.document_id) " +
				"JOIN mindmaps M ON (M.id = MP.mindmap_id) " +
				"WHERE MP.id <= :maxMphId " +
				"AND DX.indexed = 1 AND M.user NOT IN (1,2,27)" +
				"GROUP BY M.user, MP.pdfhash) A " +
				"JOIN (SELECT M.user FROM mindmaps_pdfhash MP JOIN mindmaps M ON (M.id = MP.mindmap_id) GROUP BY M.user HAVING COUNT(DISTINCT MP.pdfhash) >= 10) B " +
				"ON (A.user = B.user) " +
				"GROUP BY user").setParameter("maxMphId", maxMphId);		
		query.executeUpdate();
		
		return Tools.getHTTPStatusResponse(Status.OK, "OK");
	}
	
	@GET
	@Path("/recommendations/offline_evaluator/start")
	public Response startEvaluator(@Context UriInfo ui, @Context HttpServletRequest request) {
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		
		User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
		if (!ResourceCommons.authenticate(request, user)) {
			System.out.println("(GoogleQueryWorker) Rejected unauthorised attempt to update database");
			return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
		}
		
		RecommendationCommons.offlineEvaluator.run();
		
		return Tools.getHTTPStatusResponse(Status.OK, "OK");
	}
	
	@GET
	@Path("/recommendations/offline_evaluator/stop")	
	public Response stopEvaluator(@Context UriInfo ui, @Context HttpServletRequest request) {
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		
		User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
		if (!ResourceCommons.authenticate(request, user)) {
			System.out.println("(GoogleQueryWorker) Rejected unauthorised attempt to update database");
			return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
		}
		
		RecommendationCommons.offlineEvaluator.stop();
		
		return Tools.getHTTPStatusResponse(Status.OK, "OK");
	}
	
	@POST
	@Path("/documents/{hash}/emails/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postDocumentsEmails(@Context UriInfo ui, @Context HttpServletRequest request,
			@PathParam("hash") String hash,	FormDataMultiPart f) {
		
		List<String> emails = new ArrayList<String>();
		List<FormDataBodyPart> parts = f.getFields("email");
		if(parts != null) {
			for (FormDataBodyPart part : parts) {
				emails.add(part.getValue());
			}
		}
		if(!emails.isEmpty()) {
			Session session = Tools.getSession();
			session.setFlushMode(FlushMode.MANUAL);
			
			try {
				User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
				if (!ResourceCommons.authenticate(request, user)) {
					return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
				}
				
				Document document = DocumentQueries.getDocumentByHashOrTitle(session, hash, null);
				if (document == null) {
					return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "request document with hash='" + hash + "' does not exist.");
				}
				Transaction transaction = session.beginTransaction();
				try {
					DocumentCommons.updateDocumentPersons(session, document, emails);
					transaction.commit();
				}
				catch (Exception e) {
					transaction.rollback();
					return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could update documentsPersons: "+e.getMessage());
				}
			}
			finally {
				Tools.tolerantClose(session);
			}
		}
		
		return Tools.getHTTPStatusResponse(Status.OK, "OK");
	}
	
	@GET
	@Path("/docidx/{author_mail}")	
	public Response getDocIdxList(@PathParam("author_mail") String mail, @QueryParam("token") String token, @Context UriInfo ui, @Context HttpServletRequest request) {
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			Contact contact = Contact.getContact(session, mail);
			Person person = contact.getPerson();
			
			if (token == null || !token.equals(person.getDocidxIdToken())) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid token.");
			}
			
			person.setSession(session);			
			List<DocumentPerson> documentList = person.getDocumentsIndexed();
			
			person.setDocidxLastDisplayed(new Date());
			
			session.update(person);
			session.flush();
			return Tools.getHTTPStatusResponse(Status.OK, InternalCommons.buildDocumentIndexListXML(documentList, person, contact));
		}
		catch (Exception e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "Error: "+ e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/mailer/persons/chunk")	
	public Response getNextMailerChunk(@Context UriInfo ui, @Context HttpServletRequest request, @QueryParam("chunksize") Integer chunkSize) {
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				System.out.println("Rejected unauthorized attempt to retrieve data");
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}
			
			Collection<Object[]> results = InternalCommons.getNextMailerChunk(session, (chunkSize == null ? 100 : chunkSize)); 
			
			return Tools.getHTTPStatusResponse(Status.OK, InternalCommons.buildMailerChunkXML(results));
		}
		catch (Exception e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "Error: "+ e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/mailer/persons/{person_id: \\d+}/update")	
	public Response getMailerUpdatePerson(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam("person_id") Integer personId, @QueryParam("notified") Boolean notified, @QueryParam("reset") Boolean reset) {
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				System.out.println("Rejected unauthorized attempt to retrieve data");
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}
			
			Person person = (Person) session.load(Person.class, personId); 
			if(notified != null && notified) {
				person.setDocidxLastNotified(new Date());
				Integer counter = person.getDocidxNotificationCount();
				if(counter == null) {
					counter = 0;
				}
				counter++;
				person.setDocidxNotificationCount(counter);
			}
			if(reset != null && reset) {
				person.setDocidxNewDocuments(false);
			}
			session.update(person);
			session.flush();
			
			return Tools.getHTTPStatusResponse(Status.OK, "OK");
		}
		catch (Exception e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "Error: "+ e.getMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@POST
	@Path("/docidx/{author_mail}")	
	public Response postDocIdxListMail(@PathParam("author_mail") String mail, @FormParam("token") String token
			, @FormParam("first_name") String firstName
			, @FormParam("middle_name") String middleName
			, @FormParam("last_name") String lastName
//			, @FormParam("forbidden_doc") List<String> forbiddenDocumentIds
			, @FormParam("ignore_all") Boolean ignoreAll
			, @FormParam("allowNotification") Boolean allowNotification
			, @FormParam("notification_option") String notification_option
			, @FormParam("doc_option") List<String> doc_options
			, @FormParam("wrong_title") List<String> wrong_titles
			, @Context UriInfo ui, @Context HttpServletRequest request) {
		Session session = Tools.getSession();
		Transaction transaction = session.beginTransaction();
		session.setFlushMode(FlushMode.MANUAL);
		
		try {
			Contact contact = Contact.getContact(session, mail);
			Person person = contact.getPerson();
			person.setSession(session);
			
			if (token == null || !token.equals(person.getDocidxIdToken())) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid token.");
			}
			//TODO: adjust to the new form field!!!!!
			if((notification_option != null && "3".equals(notification_option)) || (ignoreAll != null && ignoreAll)) {
				Collection<DocumentPerson> documents = person.getDocumentsIndexed();
				for (DocumentPerson docPerson : documents) {
					InternalCommons.removeFulltextFromIndex(session, docPerson);
				}
			}
			else {
				
				for (String id : wrong_titles) {
					DocumentPerson docPerson = (DocumentPerson) session.load(DocumentPerson.class, Integer.parseInt(id));
					docPerson.setDocidxWrongTitle(true);
					session.update(docPerson);
					session.flush();
				}
				
				for (String option : doc_options) {
					if(option.startsWith("dontIndex_")) {
						String id = option.substring("dontIndex_".length());
						DocumentPerson docPerson = (DocumentPerson) session.load(DocumentPerson.class, Integer.parseInt(id));
						docPerson.setDocidxAllow(false);
						session.update(docPerson);
						session.flush();
						InternalCommons.removeFulltextFromIndex(session, docPerson);
					}
					else if(option.startsWith("notAuthor_")) {
						String id = (option.substring("notAuthor_".length()));
						DocumentPerson docPerson = (DocumentPerson) session.load(DocumentPerson.class, Integer.parseInt(id));
						session.delete(docPerson);
						session.flush();
						
					}
					else if(option.startsWith("isCollection_")) {
						String id = option.substring("isCollection_".length());
						DocumentPerson docPerson = (DocumentPerson) session.load(DocumentPerson.class, Integer.parseInt(id));
						docPerson.setDocidxIsCollection(true);
						session.update(docPerson);
						session.flush();
					}
				}
			}
			
			//TODO update person data
			boolean dirty = false;
			if((notification_option != null && "3".equals(notification_option))) {				
				person.setDocidxAllow(false);
				dirty = true;
			}
			if(person.getDocidxNotify() && (notification_option != null && "2".equals(notification_option))) {
				person.setDocidxNotify(false);
				dirty = true;
			}
			if(!person.getDocidxNotify() && (notification_option != null && "1".equals(notification_option))) {
				person.setDocidxNotify(true);				
				dirty = true;
			}
			NameSeparator nameSeparator = new NameSeparator();
			String name = InternalCommons.normalizeStr(firstName) +" ";
			name += InternalCommons.normalizeStr(middleName) + " ";
			name += InternalCommons.normalizeStr(lastName);
			NameComponents nameParts = nameSeparator.seperateName(InternalCommons.normalizeStr(name));
			
			if(!InternalCommons.normalizeStr(person.getNameFirst()).equals(InternalCommons.normalizeStr(nameParts.getFirstName()))) {
				person.setNameFirst(nameParts.getFirstName());
				dirty = true;
			}
			if(!InternalCommons.normalizeStr(nameParts.getMiddleName()).equals(InternalCommons.normalizeStr(person.getNameMiddle()))) {
				person.setNameMiddle(nameParts.getMiddleName());
				dirty = true;
			}
			if(!InternalCommons.normalizeStr(nameParts.getLastNamePrefix()).equals(InternalCommons.normalizeStr(person.getNameLastPrefix()))) {
				person.setNameLastPrefix(nameParts.getLastNamePrefix());
				dirty = true;
			}
			if(!InternalCommons.normalizeStr(nameParts.getLastName()).equals(InternalCommons.normalizeStr(person.getNameLast()))) {
				person.setNameLast(nameParts.getLastName());
				dirty = true;
			}
			if(!InternalCommons.normalizeStr(nameParts.getLastNameSuffix()).equals(InternalCommons.normalizeStr(person.getNameLastSuffix()))) {
				person.setNameLastSuffix(nameParts.getLastNameSuffix());
				dirty = true;
			}
			if(dirty) {
				session.update(person);
				session.flush();
			}
			
			return Tools.getHTTPStatusResponse(Status.OK, "OK");
		}
		catch (Exception e) {
			e.printStackTrace();
			try {
				transaction.rollback();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "Error: "+ e.getMessage());
		}
		finally {
			if(transaction.isActive()) {
				transaction.commit();
			}
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/xrefs/pdf_urls")
	public Response getDocuments(@Context UriInfo ui, @Context HttpServletRequest request
			, @QueryParam("number") Integer number
			, @QueryParam("max_rank") Integer maxRank
			, @QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		
		try {
			List<Object[]> xrefList = DocumentQueries.getDocumentNotIndexedDownloadUrls(session, number, maxRank);
			return Tools.getHTTPStatusResponse(Status.OK, InternalCommons.buildDownloadListXML(xrefList));
		}
		catch (NullPointerException e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "no documents found.");

		}
		catch(Exception e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "unknown error");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
}
