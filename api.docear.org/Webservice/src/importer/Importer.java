package importer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sciplore.data.BeanFactory;
import org.sciplore.database.SessionProvider;
import org.sciplore.deserialize.mapper.MrDlibXmlMapper;
import org.sciplore.deserialize.reader.XmlResourceReader;
import org.sciplore.formatter.Bean;
import org.sciplore.io.StringInputStream;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.Resource;
import org.sciplore.xtract.Xtract;

import util.Tools;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/import")
public class Importer {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@POST
	@Path("/resource")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadXml(@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		Long time = System.currentTimeMillis();
		Response response;
		try {
			XmlResourceReader reader = new XmlResourceReader(MrDlibXmlMapper.getDefaultMapper());
			Object resourcesObject = reader.parse(inputStream);
			List<Resource> resources = null;

			if (!(resourcesObject instanceof List)) {
				resources = new ArrayList<Resource>();
				resources.add((Resource) resourcesObject);
			} else {
				resources = (List<Resource>) resourcesObject;
			}
			response = iterateList(resources);
		} catch (Exception e) {
			response = Response.status(Status.NOT_ACCEPTABLE).build();
			e.printStackTrace();
		}

		System.out.println("Execution Time: " + ((System.currentTimeMillis() - time) / 1000) + "s");
		return response;
	}

	// public static Response uploadUrl()

	// @SuppressWarnings("rawtypes")
	// @POST
	// @Path("/pdf")
	// @Consumes(MediaType.MULTIPART_FORM_DATA)
	// public Response uploadPdf(@FormDataParam("file") InputStream inputStream,
	// @FormDataParam("file") FormDataContentDisposition fileDetail) {
	@SuppressWarnings("rawtypes")
	public static Response uploadPdf(UriInfo ui, @Context HttpServletRequest request, InputStream inputStream, FormDataContentDisposition fileDetail,
			String source, String format, boolean saveToDatabase) {
		Long time = System.currentTimeMillis();
		String xml = "";
		Session session = SessionProvider.getNewSession();
		try {
			xml = new Xtract().xtract(inputStream);
			XmlResourceReader reader = new XmlResourceReader(MrDlibXmlMapper.getDefaultMapper());
			Document document = (Document) reader.parse(new StringInputStream(xml, "UTF8"));
			if (document == null || document.getTitle() == null) {
				throw new Exception("information about the content of the document could not be extracted.");
			}

			Transaction transaction = session.beginTransaction();
			try {
				if (saveToDatabase) {
					DocumentXref xref = new DocumentXref();
					xref.setSource(source);
					xref.setDocument(document);
					document.addXref(xref);

					session.setFlushMode(FlushMode.MANUAL);

					System.out.println("document: " + document);
					session.saveOrUpdate(document);
					session.flush();
					transaction.commit();

					document = DocumentQueries.getDocument(session, document);
				}
				Bean bean = new BeanFactory(ui, request).getDocumentBean(document);
				if (format.equals("json")) {
					System.out.println("Execution Time: " + ((System.currentTimeMillis() - time) / 1000) + "s");
					return Tools.getResponse(Status.OK, MediaType.APPLICATION_JSON_TYPE, bean.toJson());
				} else {
					System.out.println("Execution Time: " + ((System.currentTimeMillis() - time) / 1000) + "s");
					return Tools.getResponse(Status.OK, MediaType.APPLICATION_XML_TYPE, bean.toXML());
				}
			} catch (Exception e) {
				e.printStackTrace();
				transaction.rollback();
				return Response.serverError().build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		} finally {
			session.close();
		}

	}

	public static Response uploadPdf(UriInfo ui, @Context HttpServletRequest request, InputStream inputStream, String format) {
		return uploadPdf(ui, request, inputStream, null, "", format, false);
	}

	public static Response uploadResource(Class<?> resourceClass, InputStream inputStream, FormDataContentDisposition fileDetail) {
		Long time = System.currentTimeMillis();
		@SuppressWarnings("rawtypes")
		XmlResourceReader reader = new XmlResourceReader(MrDlibXmlMapper.getDefaultMapper());

		Object resource = reader.parse(inputStream);

		Response response = Response.ok().build();

		if (!resourceClass.isAssignableFrom(resource.getClass())) {
			response = Response.status(Status.NOT_ACCEPTABLE).build();
			System.err.println("could not cast webservice input '" + resource.getClass().getName() + "' to required '" + resourceClass.getName() + "'!");
		}
		Session session = SessionProvider.getNewSession();
		session.setFlushMode(FlushMode.MANUAL);
		Transaction transaction = session.beginTransaction();
		try {
			session.saveOrUpdate(resource);
			session.flush();
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			transaction.rollback();
			response = Response.serverError().build();
		} finally {
			session.close();
		}

		System.out.println("Execution Time: " + ((System.currentTimeMillis() - time) / 1000) + "s");
		return response;
	}

	private static Response iterateList(List<Resource> resources) {
		Session session = SessionProvider.getNewSession();
		session.setFlushMode(FlushMode.MANUAL);
		Transaction transaction = session.beginTransaction();
		Response response = Response.ok().build();
		try {
			for (Resource resource : resources) {
				try {
					session.saveOrUpdate(resource);
				} catch (ConcurrentModificationException e) {
					e.printStackTrace();
				}
			}
			session.flush();
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			transaction.rollback();
			response = Response.serverError().build();
		} finally {
			session.close();
		}
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Response uploadResources(Class<?> resourceClass, InputStream inputStream, FormDataContentDisposition fileDetail, String format) {
		Long time = System.currentTimeMillis();

		XmlResourceReader reader = new XmlResourceReader(MrDlibXmlMapper.getDefaultMapper());
		Object resourcesObject = reader.parse(inputStream);
		List<Resource> resources = null;

		if (resourcesObject instanceof List && ((List<Resource>) resourcesObject).size() == 0) {
			return Response.noContent().build();
		} else if (resourcesObject instanceof List) {
			Class clazz = (((List<Resource>) resourcesObject).get(0)).getClass();
			System.out.println("clazz: " + clazz);
			System.out.println("resourceClass: " + resourceClass);
			System.out.println("resourceObject: " + resourcesObject.getClass());
			if (!resourceClass.isAssignableFrom(clazz) && !resourceClass.isAssignableFrom(resourcesObject.getClass())) {
				System.err.println("could not cast webservice input '" + resourcesObject.getClass().getName() + "<" + clazz
						+ ">' to required List<?> or Object of type '" + resourceClass.getName() + "'!");
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
		} else if (!resourceClass.isAssignableFrom(resourcesObject.getClass())) {
			System.err.println("could not cast webservice input '" + resourcesObject.getClass().getName() + "' to required List<?> or Object of type '"
					+ resourceClass.getName() + "'!");
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

		if (!(resourcesObject instanceof List)) {
			resources = new ArrayList<Resource>();
			resources.add((Resource) resourcesObject);
		} else {
			resources = (List<Resource>) resourcesObject;
		}

		Response response = iterateList(resources);

		System.out.println("Execution Time: " + ((System.currentTimeMillis() - time) / 1000) + "s");

		return response;
	}

//	public static RecommendationsDocuments uploadDocumentReference(Session session, User user, UserModel model, WebSearchResult result, String source, Date time, int presentationRank, boolean auto) {
//		Transaction transaction = session.beginTransaction();
//		try {
//			Document document = new Document(session, result.getTitle());
//			if (result.getYear() != null) {
//				String year = result.getYear().toString();
//				document.setPublishedYear(Short.valueOf(year));
//			}
//			RecommendationsDocuments recommendationsDocuments = new RecommendationsDocuments(session);
//			recommendationsDocuments.setUser(user);
//			recommendationsDocuments.setUserModel(model);
//			recommendationsDocuments.setDocument(document);
//			recommendationsDocuments.setDelivered(time);
//			recommendationsDocuments.setOriginalRank(result.getRank());
//			recommendationsDocuments.setPresentationRank(presentationRank);
//			recommendationsDocuments.setAutomaticallyRequested(auto);
//
//			DocumentXref xref = new DocumentXref();
//			xref.setSource(source.toLowerCase());
//
//			xref.setSourcesId(result.getLink().toExternalForm());
//
//			xref.setDlAttempts(0);
//			xref.setDocument(document);
//			xref.setCiteCount(result.getCiteCount());
//			document.addXref(xref);
//			
//
//			session.setFlushMode(FlushMode.MANUAL);
//
//			session.saveOrUpdate(recommendationsDocuments);
//			session.flush();
//			
//			transaction.commit();
//
//			return recommendationsDocuments;
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println(model.getAlgorithm());
//			transaction.rollback();
//		}
//
//		return null;
//	}
	
	
//	public static boolean uploadGoogleAutomatedDocumentReference(Session session, GoogleDocumentQuery googleDocumentQuery, WebSearchResult result, String source, Date time, int presentationRank, boolean auto) {
//		Transaction transaction = session.beginTransaction();
//		try {
//			Document document = new Document(session, result.getTitle());
//
//			DocumentXref xref = new DocumentXref();
//			xref.setSource(source.toLowerCase());
//
//			xref.setSourcesId(result.getLink().toExternalForm());
//
//			xref.setDlAttempts(0);
//			xref.setDocument(document);
//			document.addXref(xref);
//
//			session.setFlushMode(FlushMode.MANUAL);
//
//			session.saveOrUpdate(xref);
//			session.saveOrUpdate(document);
//			session.flush();
//			transaction.commit();
//
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			transaction.rollback();
//		}
//
//		return false;
//
//	}
	
	
//	public static GoogleDocumentQuery uploadGoogleAutomatedDocumentQuery(Session session, User user, GoogleDocumentQuery googleDocumentQuery) {
//		Transaction transaction = session.beginTransaction();
//		try {
//			Document document = new Document(session, result.getTitle());
//			GoogleRecommendationsDocuments recommendationsDocuments = new GoogleRecommendationsDocuments(session);
//			recommendationsDocuments.setUser(user);
//			recommendationsDocuments.setGoogleDocumentQuery(googleDocumentQuery);
//			recommendationsDocuments.setDocument(document);
//			recommendationsDocuments.setRetrieved(time);
//			recommendationsDocuments.setOriginalRank(result.getOriginal_rank());
//
//			DocumentXref xref = new DocumentXref();
//			xref.setSource(source.toLowerCase());
//
//			xref.setSourcesId(result.getLink().toExternalForm());
//
//			xref.setDocument(document);
//			document.addXref(xref);
//
//			session.setFlushMode(FlushMode.MANUAL);
//
//			session.saveOrUpdate(recommendationsDocuments);
//			session.flush();
//			transaction.commit();
//
//			return recommendationsDocuments;
//		} catch (Exception e) {
//			e.printStackTrace();
//			transaction.rollback();
//		}
//
//		return null;
//	}

}
