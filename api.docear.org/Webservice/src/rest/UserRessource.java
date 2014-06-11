package rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sciplore.database.AtomicOperation;
import org.sciplore.database.AtomicOperationHandle;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.ApplicationQueries;
import org.sciplore.queries.MindmapQueries;
import org.sciplore.queries.RecommendationsDocumentsQueries;
import org.sciplore.queries.RecommendationsDocumentsSetQueries;
import org.sciplore.resources.Application;
import org.sciplore.resources.Contact;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.Mindmap;
import org.sciplore.resources.RecommendationsDocuments;
import org.sciplore.resources.RecommendationsDocumentsSet;
import org.sciplore.resources.RecommendationsUsersSettings;
import org.sciplore.resources.User;
import org.sciplore.resources.UserPasswordRequest;
import org.sciplore.tools.SciploreResponseCode;
import org.sciplore.utilities.DocearLogger;

import util.ResourceCommons;
import util.Tools;
import util.UserCommons;
import util.recommendations.AsynchronousRecommendationsGeneratorAfterAutoRec;
import util.recommendations.AsynchronousRecommendationsGeneratorAfterRecRequest;
import util.recommendations.RecommendationCommons;
import util.recommendations.xml.XMLBuilder;

@Path("/user")
public class UserRessource {

	public static final File PARSER_BASE = new File("/home/stefan/work/mindmap-parser");
	public static final File PARSER_CACHE = new File(PARSER_BASE, "cache");
	public static final File PARSER_WORKING_PATH = new File(PARSER_BASE, "new");

	private final static String SEP = "|#|";
	private static final long EXPIRATION_TIME_MILLIS = 3600*24;

	@GET
	@Path("/{username}/recommendations/")
	public Response getAllRecommendations(@PathParam("username") String userName, @Context UriInfo uriInfo, @Context HttpServletRequest request,
			@DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("stream") boolean stream, @QueryParam("auto") boolean auto) {
		return getLiteratureRecommendations(userName, uriInfo, request, format, stream, auto);
	}

	@GET
	@Path("/{username}/recommendations/fulltext/{hash}")
	public Response getRecommendationFulltext(@PathParam("hash") String hashId, @PathParam("username") String userName, @Context UriInfo uriInfo,
			@Context HttpServletRequest request, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("stream") boolean stream) {
		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			User user = new User(session).getUserByEmailOrUsername(userName);
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "no valid access token.");
			}

			RecommendationsDocuments recDoc = RecommendationsDocumentsQueries.getRecommendationsDocument(session, hashId, user);
			// only react if not clicked already && recDoc has deliveredDate
			if (recDoc.getClicked() == null && recDoc.getRecommentationsDocumentsSet().getDelivered() != null) {
				RecommendationCommons.click(session, recDoc);				
			}

			URL url = null;
			FulltextUrl fulltextUrl = null;
			try {
				fulltextUrl = recDoc.getFulltextUrl();
				if (fulltextUrl != null) {
					url = new URL(fulltextUrl.getUrl());
					return UserCommons.getRedirectedResponse(url.toURI());
				}
				// return UserCommons.getRedirectResponse(url,
				// "Redirecting to Resource");
			}
			catch(MalformedURLException e) {
				System.out.println("rest.UserRessource.getRecommendationFulltext(hashId, userName, uriInfo, request, format, stream):\n"
						+ "url: "+url+"\nfulltextUrl: "+fulltextUrl+" throws Exception: \n"
								+ e.getMessage());
			}
			catch (Exception e) {				
				e.printStackTrace();
				return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}			
		finally {
			Tools.tolerantClose(session);
		}

		return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "unexpected exception when trying to fetch recommendation's url");
	}	

	@PUT
	@Path("/{username}/recommendations/{recommendationsSetId}")
	public Response putLiteratureRecommendationsUserStatus(@Context UriInfo ui, @Context HttpServletRequest request, 
			@PathParam("username") String userName, @PathParam("recommendationsSetId") final Integer recommendationsDocumentsSetId,
			@QueryParam("rating") final Integer rating) {
		
		final Session session = SessionProvider.getNewSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
    		final User user = new User(session).getUserByEmailOrUsername(userName);		
    		if (!ResourceCommons.authenticate(request, user)) {
    			return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED, "no valid access token.");
    		}
		}
		finally {
			Tools.tolerantClose(session);
		}
    		
		AtomicOperation<Response> op = new AtomicOperation<Response>() {
			@Override
			public Response exec(Session session) {
				try {    				
    				RecommendationsDocumentsSet recDocSet = (RecommendationsDocumentsSet) session.get(RecommendationsDocumentsSet.class, recommendationsDocumentsSetId);
    				if (recDocSet == null) {
    					return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "");
    				}			
    				
    				if (rating == null && recDocSet.getReceived() == null) {
    					recDocSet.setReceived(new Date());
    				}
    				
    				if (rating != null) {
    					recDocSet.setUserRating(rating);
    				}
    				
    				session.update(recDocSet);
    				session.flush();
    			}
    			catch(Exception e) {
    				DocearLogger.error(e);
    				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "");
    			}
				return UserCommons.getHTTPStatusResponse(Status.OK, "OK");
				
			}
		};
		AtomicOperationHandle<Response> handle = SessionProvider.atomicManager.addOperation(op);
		try {
			return handle.getResult();
		}
		catch (IOException e) {
			DocearLogger.error(e);
			return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}		
	}
	
	@GET
	@Path("/{username}/recommendations/documents/")
	public Response getLiteratureRecommendations(@PathParam("username") String userName, @Context UriInfo uriInfo, @Context HttpServletRequest request,
			@DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("stream") boolean stream, @QueryParam("auto") boolean auto) {
		long overAll = System.currentTimeMillis();

		final Session session = SessionProvider.getNewSession();
		session.setFlushMode(FlushMode.MANUAL);

		final User user = new User(session).getUserByEmailOrUsername(userName);
				
		if (!ResourceCommons.authenticate(request, user)) {
			return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED, "no valid access token.");
		}

		try {
			RecommendationsUsersSettings settings = UserCommons.getRecommendationsUsersSettings(session, user);

			final RecommendationsDocumentsSet recDocSet = RecommendationsDocumentsSetQueries.getLatestUnusedRecommendationsSet(session, user);
			if (recDocSet.getRecommendationsDocuments().size() == 0) {
				return UserCommons.getHTTPStatusResponse(Status.NO_CONTENT, "");
			}

			// update time of execution for each recommendation item
			Transaction transaction = session.beginTransaction();
			// STEFAN retrieve applicationnumber only with buildnumber and application name

			try {
				String xml = XMLBuilder.buildRecommendationsXML(recDocSet, settings, uriInfo, userName);
				Integer build = UserCommons.getClientVersionFromRequest(request);
				recDocSet.setApplication(ApplicationQueries.getApplicationByBuildNumber(session, build));
				recDocSet.setDelivered(new Date());
				Long execution_time = System.currentTimeMillis() - overAll;
				recDocSet.setDeliveryTime(execution_time);
				recDocSet.setAuto(auto);
								
				session.update(recDocSet);
				session.flush();
				transaction.commit();
				
				final int recId = recDocSet.getId();
				AtomicOperation<Response> op = new AtomicOperation<Response>() {
					@Override
					public Response exec(Session session) {
						try {    				
							RecommendationCommons.computeDeliveryVariables(recId);
		    			}
		    			catch(Exception e) {
		    				DocearLogger.error(e);
		    				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "");
		    			}
						return UserCommons.getHTTPStatusResponse(Status.OK, "OK");
						
					}
				};
				SessionProvider.atomicManager.addOperation(op);
								
				return UserCommons.getHTTPStatusResponse(Status.OK, xml);
			}
			catch (Exception e) {
				e.printStackTrace();
				transaction.rollback();
				return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "unexpected error");
			}
			
		}
		finally {
			if (System.getProperty("docear_debug") == null || System.getProperty("docear_debug").equals("false")) {			
    			if (auto) {
        			AsynchronousRecommendationsGeneratorAfterAutoRec.executeAsynch(new Runnable() {
        				@Override
        				public void run() {
        					RecommendationCommons.forceComputeForSingleUser(user.getId(), RecommendationsDocumentsSet.TRIGGER_TYPE_AUTO_RECOMMENDATION);
        				}
        			});
        			System.out.println("AsynchronousRecommendationsGeneratorAfterAutoRec --> running recommendation generator tasks: "+AsynchronousRecommendationsGeneratorAfterAutoRec.getSingleExecTaskCount());
    			}
    			else {
    				AsynchronousRecommendationsGeneratorAfterRecRequest.executeAsynch(new Runnable() {
        				@Override
        				public void run() {
        					RecommendationCommons.forceComputeForSingleUser(user.getId(), RecommendationsDocumentsSet.TRIGGER_TYPE_RECOMMENDATION_REQUEST);
        				}
        			});
    				System.out.println("AsynchronousRecommendationsGeneratorAfterRecRequest --> running recommendation generator tasks: "+AsynchronousRecommendationsGeneratorAfterRecRequest.getSingleExecTaskCount());
    			}			
			}
			
			Tools.tolerantClose(session);
		}
		
	}

	private static int getTimeout(Integer build) {
		// if generation of documents takes more than 4 seconds,
		// they probably never arrive at the user with a timeout
		// of 5 seconds

		int timeout = 6000;
		// Docear's connection timeout increased from 5s to 7s
		// on build 135
		if (build == null || build < 135) {
			timeout = 4000;
		}

		return timeout;
	}

	// @GET
	// @Path("/{username}/recommendations/{id}")
	// public Response getLiteratureRecommendations(@PathParam("id") int id,
	// @PathParam("username") String userName, @Context UriInfo uriInfo,
	// @Context HttpServletRequest request, @DefaultValue(Tools.DEFAULT_FORMAT)
	// @QueryParam("format") String format, @QueryParam("stream") boolean
	// stream) {
	// final Session session = SessionProvider.sessionFactory.openSession();
	// try {
	// User user = new User(session).getUserByEmailOrUsername(userName);
	// if (!ResourceCommons.authenticate(request, user)) {
	// return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED,
	// "no valid access token.");
	// }
	//
	// RecommendationsUsersSettings settings =
	// UserCommons.getRecommendationsUsersSettings(session, user);
	//
	// RecommendationsDocuments recDoc =
	// RecommendationsDocumentsQueries.getRecommendationsDocuments(session, id,
	// user);
	// if (recDoc == null) {
	// return UserCommons.getHTTPStatusResponse(Status.NO_CONTENT, "");
	// }
	//
	// Bean bean = new BeanFactory(uriInfo,
	// request).getRecommendationBean(recDoc, "user/" + user.getUsername() +
	// "/recommendations/", settings);
	// return Tools.getSerializedResponse(format, bean, stream);
	// }
	// finally {
	// Tools.tolerantClose(session);
	// }
	//
	// }

	@POST
	@Path("/{pathUsername}")
	public Response create(@PathParam("pathUsername") String pathUsername, @FormParam("userName") String userName, @FormParam("password") String password,
			@FormParam("retypedPassword") String retypedPassword, @FormParam("userType") int userType, @FormParam("eMail") String eMail,
			@FormParam("firstName") String firstName, @FormParam("middleName") String middleName, @FormParam("lastName") String lastName,
			@FormParam("birthYear") int birthYear, @FormParam("generalNewsLetter") String generalNewsLetter,
			@FormParam("recommenderNewsLetter") String searchNewsLetter, @FormParam("mindmappingNewsLetter") String splmmNewsLetter,
			@FormParam("isMale") String male, @FormParam("remoteAddr") String remoteAddr, @Context HttpServletRequest request) {
		// if(!request.isSecure()) {
		// return UserCommons.getHTTPStatusResponse(Status.FORBIDDEN,
		// "Only secured communication is allowed.");
		// }
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			String charset = request.getCharacterEncoding();
			if (charset == null) {
				charset = "UTF-8";
			}
			try {
				pathUsername = URLDecoder.decode(pathUsername, charset);
			}
			catch (UnsupportedEncodingException e1) {
				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "Couldn't Decode Input Data.");
			}
			boolean generalNewsLetterBool = UserCommons.parseBoolean(generalNewsLetter);
			boolean searchNewsLetterBool = UserCommons.parseBoolean(searchNewsLetter);
			boolean splmmNewsLetterBool = UserCommons.parseBoolean(splmmNewsLetter);
			if (!pathUsername.equalsIgnoreCase(userName)) {
				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "Wrong Input Data.");
			}

			Boolean isMale = null;
			if (male != null && male.length() > 0) {
				isMale = Boolean.parseBoolean(male);
			}

			if (remoteAddr == null) {
				remoteAddr = request.getRemoteAddr();
			}
			if (userType==User.USER_TYPE_ANONYMOUS) {
				System.out.println("debug user creation spammer with IP: "+remoteAddr);
			}

			SciploreResponseCode response = new User(session).createUser(userName, password, retypedPassword, eMail, firstName, middleName, lastName,
					birthYear, generalNewsLetterBool, searchNewsLetterBool, splmmNewsLetterBool, (short) userType, isMale, remoteAddr);
			if(response.getResponseCode() == 200) {
				rest.Tools.mail(eMail, "Docear Account Signup Confirmation", UserCommons.getUserRegistrationMail(userName, password, eMail));
			}
			return UserCommons.getHTTPStatusResponse(response.getResponseCode(), response.getResponseMessage());
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/{pathUsername}/mindmaps/{id}")
	public Response getMindMap(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam("pathUsername") String userName, @PathParam("id") Integer id) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername(userName);
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED, "no valid access token.");
			}

			String accessToken = request.getHeader("accessToken");
			Mindmap map = MindmapQueries.getMindmap(session, user, accessToken, id, 1);
//					.getMindmap(session, user, accessToken, id, new Integer(1));

			if (map == null) {
				return UserCommons.getHTTPStatusResponse(Status.FORBIDDEN, "no mindmap found.");
			}

			return UserCommons.getFileStream(SciploreResponseCode.OK, map.getStoragePath(), map.getFilename().replace(";", "_").replace("=", "_"));

		}
		catch (Exception e) {
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(Status.FORBIDDEN, "no mindmap found.");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@SuppressWarnings("deprecation")
	@GET
	@Path("/{pathUsername}/mindmaps")
	public Response getMindMap(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam("pathUsername") String userName) {
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername(userName);
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED, "no valid access token.");
			}

			String output = "";
			String accessToken = request.getHeader("accessToken");
			for (Mindmap map : MindmapQueries.getMindmaps(session, user, accessToken, 1)) {
				output += map.getId() + SEP + map.getMindmapId() + SEP + map.getRevision().toLocaleString() + SEP + map.getFilepath() + SEP + map.getFilename()
						+ SEP + map.getFilesize() + "\n";
			}

			return UserCommons.getHTTPStatusResponse(Status.OK, output);

		}
		finally {
			Tools.tolerantClose(session);
		}

	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/{pathUsername}/mindmaps")
	public Response addMindMap(@PathParam("pathUsername") String userName, FormDataMultiPart multiPart, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();

		final User user = new User(session).getUserByEmailOrUsername(userName);
		try {
			if (!ResourceCommons.authenticate(request, user)) {
				return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED, "no valid access token.");
			}

			if (multiPart.getField("file") == null) {
				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "No File was uploaded.");
			}

			BodyPartEntity bodyPartEntity = (BodyPartEntity) multiPart.getField("file").getEntity();

			File file, absoluteFile;
			try {
				file = UserCommons.saveMindMap(user, bodyPartEntity.getInputStream());
				absoluteFile = new File(UserCommons.MINDMAPS_PATH + File.separator + file.getPath());
			}
			catch (Exception ex) {
				return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could not save the mindmap.");
			}

			ZipFile zipFile;
			try {
				zipFile = new ZipFile(absoluteFile);
			}
			catch (Exception ex) {
				absoluteFile.delete();
				// use status.OK so that Docear deletes the broken ZIP file from the upload queue
				return UserCommons.getHTTPStatusResponse(Status.OK, "could not open the uploaded .zip file.");
			}

			ZipEntry metaDataEntry = zipFile.getEntry("metadata.inf");
			if (metaDataEntry == null) {
				absoluteFile.delete();
				return UserCommons.getHTTPStatusResponse(Status.NOT_ACCEPTABLE, "not supported data format. meta information are missing.");
			}

			Properties metaProperties;
			try {
				metaProperties = new Properties();
				metaProperties.load(zipFile.getInputStream(metaDataEntry));
			}
			catch (Exception ex) {
				absoluteFile.delete();
				return UserCommons.getHTTPStatusResponse(Status.OK, "error while loading meta information.");
			}
			finally {
				try {
					zipFile.close();
				}
				catch (IOException ioex) {
					// do nth.
				}
			}

			boolean allowBackup;
			boolean allowContentResearch;
			boolean allowInformationRetrieval;
			boolean allowUsageResearch;
			boolean allowRecommendations;

			try {
				allowBackup = UserCommons.parseBoolean(metaProperties.getProperty("backup"));
			}
			catch (Exception e) {
				allowBackup = false;
			}
			try {
				allowContentResearch = UserCommons.parseBoolean(metaProperties.getProperty("allow_content_research"));

			}
			catch (Exception e) {
				allowContentResearch = false;
			}
			try {
				allowInformationRetrieval = UserCommons.parseBoolean(metaProperties.getProperty("allow_information_retrieval"));
			}
			catch (Exception e) {
				allowInformationRetrieval = false;
			}
			try {
				allowUsageResearch = UserCommons.parseBoolean(metaProperties.getProperty("allow_usage_research"));
			}
			catch (Exception e) {
				allowUsageResearch = false;
			}
			try {
				allowRecommendations = UserCommons.parseBoolean(metaProperties.getProperty("allow_recommendations"));
			}
			catch (Exception e) {
				allowRecommendations = false;
			}

			user.setAllowBackup(allowBackup);
			user.setAllowContentResearch(allowContentResearch);
			user.setAllowInformationRetrieval(allowInformationRetrieval);
			user.setAllowUsageResearch(allowUsageResearch);
			user.setAllowRecommendations(allowRecommendations);

			int filesize = Integer.parseInt(metaProperties.getProperty("filesize"));
			String filename = metaProperties.getProperty("filename");
			String filepath = metaProperties.getProperty("filepath");
			String mindmapID = metaProperties.getProperty("mindmap_id");
			String timestamp = metaProperties.getProperty("timestamp");
			Date revision = new Date(Long.parseLong(timestamp));

			Application app = new Application(session);
			String app_name = metaProperties.getProperty("application_name");
			String app_version = metaProperties.getProperty("application_version");
			String app_status = metaProperties.getProperty("application_status");
			String app_status_version = metaProperties.getProperty("application_status_version");
			String app_build = metaProperties.getProperty("application_build");
			String app_date = metaProperties.getProperty("application_date");

			boolean isLibraryMap = UserCommons.parseBoolean(metaProperties.getProperty("is_library_map", "false"));
			String mapType = metaProperties.getProperty("map_type");

			app.setName(app_name);
			app.setVersion(app_version);
			app.setVersionStatus(app_status);
			app.setKey(app_name + "_" + app_version + "_" + app_status + "_" + app_status_version + "_build" + app_build);
			app.setApplicationId(evalApplicationId(app_name));
			app.setValid((short) 1);
			try {
				app.setBuildNumber(Integer.parseInt(app_build));
			}
			catch (Exception e) {
				// TODO: sth useful
			}
			try {
				app.setVersionStatusNumber(Integer.parseInt(app_status_version));
			}
			catch (Exception e) {
				// TODO: sth useful
			}
			try {
				String[] version = app_version.split("\\.");
				app.setVersionMajor(Integer.parseInt(version[0]));
				app.setVersionMid(Integer.parseInt(version[1]));
				app.setVersionMinor(Integer.parseInt(version[2]));
			}
			catch (Exception e) {
				// TODO: sth useful
			}

			try {
				app.setReleaseDate(DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse(app_date));
			}
			catch (Exception e) {
				// TODO: sth useful
			}

			if (mindmapID == null || mindmapID.trim().length() == 0) {
				absoluteFile.delete();
				return UserCommons.getHTTPStatusResponse(Status.NOT_ACCEPTABLE, "mindmaps with no id are not accepted.");
			}

			// if a mind map with the same mindmapID and revision already exists
			// --> don't create a duplicate
			session.setFlushMode(FlushMode.MANUAL);
			Mindmap mindmap = MindmapQueries.getMindmap(session, mindmapID, revision);
			if (mindmap != null && mindmap.getId() != null) {
				// a file for this mindmap should already exist
				absoluteFile.delete();
				return UserCommons.getHTTPStatusResponse(Status.OK, mindmapID);
			}
			mindmap = new Mindmap(session);

			SciploreResponseCode response = mindmap.create(user, app, allowBackup, allowContentResearch, allowInformationRetrieval, allowUsageResearch,
					allowRecommendations, revision, mindmapID, filename, filepath, filesize, file.getPath());

			if (response.getResponseCode() == SciploreResponseCode.OK && response.getResponseMessage() != null) {			
    			boolean dirty = false;
    			if (mapType != null) {
    				mindmap.setMapType(mapType);
    				dirty = true;
    			}
    			if (isLibraryMap) {
    				mindmap.setAffiliation("library");
    				dirty = true;
    			}
    			if (dirty) {
    				mindmap.save();
    			}			
    			if ((app.getBuildNumber() != null && app.getBuildNumber() >= 90 && "docear".equalsIgnoreCase(app.getName())) && mindmap.getAllowParsing()) {
    				try {
    					sendMindmapToParser(user.getId(), mindmap, absoluteFile);
    				}
    				catch (Exception e) {
    					e.printStackTrace();
    					return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "error while storing the mindmap.");
    				}
    			}
			
				RecommendationCommons.enqueueRecommendionsGeneratorTaskAfterNewMap(user);
				return UserCommons.getHTTPStatusResponse(Status.OK, mindmapID);
			}
			else {
				absoluteFile.delete();
				return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "sth went wrong.");
			}
		}
		catch (Throwable e) {
			transaction.rollback();
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "sth went wrong.");
		}
		finally {			
			try {
				if (transaction.isActive()) {
					transaction.commit();
				}
				Tools.tolerantClose(session);				
			}
			catch (Exception ex) {
				// do nth.
			}
		}
	}
	
	
	@GET
	@Path("/{email}/password")
	public Response resetPasswordRequest(@PathParam("email") String email, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		
		final User user = new User(session).getUserByEmail(email);
		try {
			if(user == null) {
				return UserCommons.getHTTPStatusResponse(Status.NOT_FOUND, "no user found for "+email);
			}
			UserPasswordRequest pwRequest = UserPasswordRequest.create(user);
			pwRequest.setSession(session);
			session.save(pwRequest);
			
			Contact contact = user.getPerson().getContacts().iterator().next();			
			rest.Tools.mail(contact.getUri(), "Docear Password Request", UserCommons.getPasswordRequestMailText(pwRequest, contact.getUri()));
			
			return UserCommons.getHTTPStatusResponse(Status.OK, "an email with further instructions has been sent to your email address.");
		}
		catch (Throwable e) {
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "sth went wrong.");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@POST
	@Path("/{email}/password")
	public Response newPasswordRequest(@PathParam("email") String email, @FormParam("token") String token,  @FormParam("password") String password, @FormParam("password_retype") String password_retype, @Context HttpServletRequest request) {
		final Session session = SessionProvider.sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			if(password == null || !password.equals(password_retype) || password.trim().length() <= 0) {
				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "The passwords you have entered are not identical.");
			}
			
			final User user = new User(session).getUserByEmail(email);
			if(user == null) {
				return UserCommons.getHTTPStatusResponse(Status.NOT_FOUND, "no user found for "+email);
			}
			UserPasswordRequest resetRequest = UserPasswordRequest.getUserPasswordRequest(session, token);
			if(resetRequest == null) {
				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "password reset token does not exist.");
			}
			resetRequest.setSession(session);
			if(resetRequest.isExpired(EXPIRATION_TIME_MILLIS) || resetRequest.isUsed()) {
				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "this operation is not valid anymore.");
			}
			if(user.getUsername().equals(resetRequest.getUser().getUsername())) {
					
				//update user credentials
				user.setPassword(password, true);
				user.setAccessToken(null);
				session.update(user);
				session.flush();
					
				//update rquest -> set as used
				resetRequest.setUsed();
				session.update(resetRequest);
				session.flush();
					
				// send confirmation mail
				Contact contact = user.getPerson().getContacts().iterator().next();
				rest.Tools.mail(contact.getUri(), "Docear Password Reset Confirmation", UserCommons.getUserResetConfirmationMail(user.getUsername(), password, contact.getUri()));
				transaction.commit();	
				return UserCommons.getHTTPStatusResponse(Status.OK, "your new password has been set.");
			}
			else {
				return UserCommons.getHTTPStatusResponse(Status.BAD_REQUEST, "user email and password reset Token don't match.");
			}
		}
		catch (Throwable e) {
			if(transaction.isActive()) {
				transaction.rollback();
			}
			e.printStackTrace();
			return UserCommons.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "sth went wrong.");
		}
		finally {
			
			Tools.tolerantClose(session);
		}
	}

	private void sendMindmapToParser(Integer userId, Mindmap mindmap, File zipFile) throws IOException {
		File mmFile = new File(PARSER_CACHE, mindmap.getId() + ".mm");

		Properties properties = new Properties();
		properties.setProperty("user_id", userId.toString());
		properties.setProperty("revision", mindmap.getId().toString());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		properties.setProperty("revision_timestamp", "" + sdf.format(mindmap.getRevision()));
		properties.setProperty("allow_content_research", "" + (mindmap.getAllowContentResearch() ? 1 : 0));
		properties.setProperty("allow_information_retrieval", "" + (mindmap.getAllowInformationRetrieval() ? 1 : 0));
		properties.setProperty("allow_usage_research", "" + (mindmap.getAllowContentResearch() ? 1 : 0));
		properties.setProperty("allow_recommendations", "" + (mindmap.getAllowRecommendations() ? 1 : 0));
		properties.setProperty("app_build", "" + mindmap.getApplication().getBuildNumber());
		properties.setProperty("affiliation", (mindmap.getAffiliation() == null ? "" : mindmap.getAffiliation()));
		if (mindmap.getMapType() != null) {
			properties.setProperty("map_type", "" + mindmap.getMapType());
		}

		File propertiesFile = new File(mmFile.getAbsolutePath() + ".properties");
		FileOutputStream fos = new FileOutputStream(propertiesFile);
		properties.store(fos, "");
		fos.close();

		ZipInputStream zinstream = new ZipInputStream(new FileInputStream(zipFile));
		fos = null;

		try {
			ZipEntry entry;
			while ((entry = zinstream.getNextEntry()) != null) {
				if (entry.getName().endsWith(".mm")) {
					fos = new FileOutputStream(mmFile);

					byte[] buf = new byte[1024];
					int n;
					while ((n = zinstream.read(buf, 0, 1024)) > -1) {
						fos.write(buf, 0, n);
					}
				}
			}
		}
		finally {
			zinstream.close();
			if (fos != null) {
				fos.close();
			}
		}

		propertiesFile.renameTo(new File(PARSER_WORKING_PATH, propertiesFile.getName()));
		mmFile.renameTo(new File(PARSER_WORKING_PATH, mmFile.getName()));

	}

	private Integer evalApplicationId(String app_name) {
		assert (app_name != null);

		if ("docear".equals(app_name.trim().toLowerCase())) {
			return 1;
		}
		return null;
	}

	@SuppressWarnings("unused")
	private Short parseVersionStatus(final String app_status) {
		assert (app_status != null);

		if ("beta".equals(app_status.trim().toLowerCase())) {
			return Application.STATUS_BETA;
		}
		if ("alpha".equals(app_status.trim().toLowerCase())) {
			return Application.STATUS_ALPHA;
		}
		if ("rc".equals(app_status.trim().toLowerCase()) || "release candidate".equals(app_status.trim().toLowerCase())) {
			return Application.STATUS_RELEASE_CANDIDATE;
		}
		if ("stable".equals(app_status.trim().toLowerCase())) {
			return Application.STATUS_STABLE;
		}
		if ("devel".equals(app_status.trim().toLowerCase())) {
			return Application.STATUS_DEVELOPMENT;
		}
		return null;
	}
}
