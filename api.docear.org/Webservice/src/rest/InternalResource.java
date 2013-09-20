package rest;

import java.io.InputStream;
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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.docear.googleparser.GoogleScholarParser;
import org.docear.googleparser.WebSearchResult;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mrdlib.index.Searcher;
import org.sciplore.data.NameSeparator;
import org.sciplore.data.NameSeparator.NameComponents;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.queries.DocumentsBibtexPdfHashQueries;
import org.sciplore.queries.DocumentsBibtexQueries;
import org.sciplore.queries.DocumentsBibtexUsersQueries;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.queries.InternalQueries;
import org.sciplore.queries.MindmapsPdfHashQueries;
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
import org.sciplore.utilities.concurrent.AsynchUtilities;

import util.BibtexCommons;
import util.DocidxNotificationCommons;
import util.DocumentCommons;
import util.FulltextCommons;
import util.InternalCommons;
import util.RecommendationCommons;
import util.ResourceCommons;
import util.Tools;
import util.UserCommons;
import util.UserSessionProvider;
import util.UserSessionProvider.UserSession;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.FormDataContentDisposition;
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
		DocidxNotificationCommons commons = new DocidxNotificationCommons();
		commons.getNotificationReceiverXMl();
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
	public Response newDocument(@FormParam("title") String title, @FormParam("link") String link, @FormParam("username") String userName,
			@FormParam("source") String source, @FormParam("modelId") Integer modelId, @FormParam("year") Short year,
			@FormParam("citeCount") Integer citeCount, @FormParam("rank") Integer rank, @Context HttpServletRequest request) {

		final Session session = SessionProvider.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!("pdfdownloader".equals(userName)) | !ResourceCommons.authenticate(request, user)) {
				System.out.println("(GoogleQueryWorker) Rejected unauthorised attempt to update database");
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid access token.");
			}

			// save to database
			Document document = new Document(session, title);
			if (DocumentQueries.getValidCleanTitle(title) == null) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.BAD_REQUEST, "title is not valid");
			}
			document.setPublishedYear(year);

			DocumentXref xref = new DocumentXref();
			xref.setCiteCount(citeCount == null ? 0 : citeCount);
			xref.setRank(rank);
			xref.setSource(source.toLowerCase());
			xref.setSourcesId(link);

			xref.setDlAttempts(0);
			xref.setDocument(document);
			document.addXref(xref);

			DocumentXref persistent = (DocumentXref) xref.getPersistentIdentity();
			if (persistent != null) {
				if (persistent.getRank() == null || persistent.getRank() > rank) {
					persistent.setRank(rank);
					session.update(persistent);
				}
			}
			else {
				session.setFlushMode(FlushMode.MANUAL);
				session.saveOrUpdate(xref);
			}

			GoogleDocumentQuery model = (new GoogleDocumentQuery(session)).getGoogleDocumentQuery(modelId);
			model.setQuery_date(Calendar.getInstance().getTime());
			session.saveOrUpdate(model);
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
					DocumentCommons.updateDocumentData(session, doc, xtractStream);

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
	
	@GET
	@Path("/docidx/{author_mail}")	
	public Response getDocIdxList(@PathParam("author_mail") String mail, @QueryParam("token") String token, @Context UriInfo ui, @Context HttpServletRequest request) {
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			Contact contact = Contact.getContact(session, mail);
			Person person = contact.getPerson();
			
			if (token == null || token.equals(person.getDocidxIdToken())) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid token.");
			}
			
			List<DocumentPerson> documentList = person.getDocumentsIndexed();
			
			person.setDocidxLastDisplayed(new Date());
			
			session.flush();
			return Tools.getHTTPStatusResponse(Status.OK, InternalCommons.buildDocumentIndexListXML(documentList, person, contact));
		}
		catch (Exception e) {
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
			, @FormParam("forbidden_docs") List<String> forbiddenDocumentIds
			, @FormParam("ignore_all") Boolean ignoreAll
			, @FormParam("allowNotification") Boolean allowNotification
			, @Context UriInfo ui, @Context HttpServletRequest request) {
		Session session = Tools.getSession();
		Transaction transaction = session.beginTransaction();
		session.setFlushMode(FlushMode.MANUAL);
		
		try {
			Contact contact = Contact.getContact(session, mail);
			Person person = contact.getPerson();
			
			if (token == null || token.equals(person.getDocidxIdToken())) {
				return UserCommons.getHTTPStatusResponse(com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED, "no valid token.");
			}
			
			if(ignoreAll != null && ignoreAll) {
				Collection<DocumentPerson> documents = person.getDocumentsIndexed();
				for (DocumentPerson docPerson : documents) {
					deleteFulltextFromIndex(session, docPerson);
				}
			}
			else {
				//docs
				for (String docId : forbiddenDocumentIds) {
					DocumentPerson docPerson = (DocumentPerson) session.load(DocumentPerson.class, Integer.parseInt(docId));
					docPerson.setDocidxAllow(false);
					session.update(docPerson);
					session.flush();
					deleteFulltextFromIndex(session, docPerson);					
				}
			}
			
			//TODO update person data
			boolean dirty = false;
			if(ignoreAll != null) {
				person.setDocidxAllow(!ignoreAll);
				dirty = true;
			}
			if(allowNotification != null) {
				person.setDocidxNotify(allowNotification);
				dirty = true;
			}
			NameSeparator nameSeparator = new NameSeparator();
			String name = InternalCommons.normalizeStr(firstName) +" ";
			name += InternalCommons.normalizeStr(middleName) + " ";
			name += InternalCommons.normalizeStr(lastName);
			NameComponents nameParts = nameSeparator.seperateName(InternalCommons.normalizeStr(name));
			if(nameParts.getFirstName() != null && !nameParts.getFirstName().trim().isEmpty() && !nameParts.getFirstName().equals(person.getNameFirst())) {
				person.setNameFirst(nameParts.getFirstName());
				dirty = true;
			}
			if(nameParts.getMiddleName() != null && !nameParts.getMiddleName().trim().isEmpty() && !nameParts.getMiddleName().equals(person.getNameMiddle())) {
				person.setNameMiddle(nameParts.getMiddleName());
				dirty = true;
			}
			if(nameParts.getLastNamePrefix() != null && !nameParts.getLastNamePrefix().trim().isEmpty() && !nameParts.getLastNamePrefix().equals(person.getNameLastPrefix())) {
				person.setNameLastPrefix(nameParts.getLastNamePrefix());
				dirty = true;
			}
			if(nameParts.getLastName() != null && !nameParts.getLastName().trim().isEmpty() && !nameParts.getLastName().equals(person.getNameLast())) {
				person.setNameLast(nameParts.getLastName());
				dirty = true;
			}
			if(nameParts.getLastNameSuffix() != null && !nameParts.getLastNameSuffix().trim().isEmpty() && !nameParts.getLastNameSuffix().equals(person.getNameLastSuffix())) {
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

	/**
	 * @param session
	 * @param docPerson
	 */
	private void deleteFulltextFromIndex(Session session, DocumentPerson docPerson) {
		List<DocumentsPdfHash> pdfHashes = DocumentsPdfHashQueries.getPdfHashes(session, docPerson.getDocument());
		for (DocumentsPdfHash documentsPdfHash : pdfHashes) {
			//TODO
			//FulltextCommons.requestPlainTextUpdate(docPerson.getDocument(), documentsPdfHash.getHash());
		}
	}
}
