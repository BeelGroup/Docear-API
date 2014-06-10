package rest;

import importer.Importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.AlreadyConnectedException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mrdlib.index.DocumentHashItem;
import org.mrdlib.index.Indexer;
import org.mrdlib.index.Searcher;
import org.sciplore.data.BeanFactory;
import org.sciplore.database.AtomicOperation;
import org.sciplore.database.AtomicOperationHandle;
import org.sciplore.database.SessionProvider;
import org.sciplore.formatter.Bean;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.User;

import util.DocumentCommons;
import util.FulltextCommons;
import util.ResourceCommons;
import util.Tools;
import util.UserCommons;

@Path("/documents")
public class DocumentResource {
	private static final int MAX_NUMBER_PER_PAGE = 1000;
	
	@GET
	@Path("/totalamount")
	public Response getTotalAmount(@Context UriInfo ui, @QueryParam("source") String source, @QueryParam("number") int number) {
		Session session = Tools.getSession();
		try {
			number = getRestrictedNumber(number);
			Long count = DocumentQueries.getDocumentCount(session, source);
			System.out.println("(mr-dlib) doc count: " + count);

			return Tools.getResponse(Status.OK, MediaType.TEXT_PLAIN_TYPE, "" + ((count / number) + 1));
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No documents in database");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/latestpublicationdate")
	public Response getLatestPublicationDate(@Context UriInfo ui, @QueryParam("source") String source) {
		Session session = Tools.getSession();
		try {
			Timestamp timestamp = DocumentQueries.getLatestPublicationDate(session, source);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			return Tools.getResponse(Status.OK, MediaType.TEXT_PLAIN_TYPE, "" + format.format(timestamp));
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No documents found");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}



	@GET
	@Path("/{id}/title")
	public Response getTitle(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try {
			Bean bean = new BeanFactory(ui, request).getTitleBean(DocumentQueries.getDocument(session, id, source));
			return Tools.getSerializedResponse(format, bean, stream);
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/{id}/abstract")
	public Response getAbstract(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try {
			Bean bean = new BeanFactory(ui, request).getAbstractBean(DocumentQueries.getDocument(session, id, source));
			return Tools.getSerializedResponse(format, bean, stream);
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/{id}/authors")
	public Response getAuthors(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try {
			Map<String, Object> params = Tools.parseQueryParameter(ui);
			params.put("id", new Integer(id));

			Bean bean = new BeanFactory(ui, request).getAuthorsBean(DocumentQueries.getDocument(session, id, source));
			return Tools.getSerializedResponse(format, bean, stream);
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/{id}/plaintext")
	public Response getPlainText(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id) {
		Session session = Tools.getSession();
		try {
			Document document = (Document) session.get(Document.class, id);
			if (document == null) {
				return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
			}
						
			File file = null;
			for (DocumentsPdfHash hash : DocumentsPdfHashQueries.getPdfHashes(session, document)) {
				file = new File(Indexer.DOCUMENT_PLAINTEXT_DIRECTORY, hash.getHash()+".zip");
				System.out.println("probing file: "+file.getAbsolutePath());
				if (file.exists()) {
					return Tools.getSerializedResponseAsStream(file);
				}
			}
		}
		catch (Exception e) {			
			e.printStackTrace();
		}
		finally {
			Tools.tolerantClose(session);
		}
		
		return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "File not found");
	}

	@GET
	@Path("/{id}/fulltexts")
	public Response getFullTexts(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try {
			Map<String, Object> params = Tools.parseQueryParameter(ui);
			params.put("id", new Integer(id));

			Bean bean = new BeanFactory(ui, request).getFulltextsBean(DocumentQueries.getDocument(session, id, source));
			return Tools.getSerializedResponse(format, bean, stream);
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/{id}/xrefs")
	public Response getXrefs(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try {
			Map<String, Object> params = Tools.parseQueryParameter(ui);
			params.put("id", new Integer(id));
			Bean bean = new BeanFactory(ui, request).getXrefsBean(DocumentQueries.getDocument(session, id, source));
			return Tools.getSerializedResponse(format, bean, stream);
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@PUT
	@Path("/{id}/xrefs/{xrefId}")
	public Response putXref(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id, @PathParam(value = "xrefId") int xrefId,
			@QueryParam("dl_attempt") String dl_attempt,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		Transaction transaction = session.beginTransaction();
		try {
			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!ResourceCommons.authenticate(request, user)) {
				return Tools.getHTTPStatusResponse(Status.UNAUTHORIZED, "not authorized access attempt.");
			}
			
			Document doc = DocumentQueries.getDocument(session, id, source);
			boolean updated = false;
			if(dl_attempt != null && dl_attempt.equals("true")) {	
				for(DocumentXref xref : doc.getXrefs()) {
					if(xref.getId() == xrefId) {
						updated = true;
						xref.setDlAttempts(xref.getDlAttempts()+1);
						xref.setLastAttempt(new Date());						
						session.saveOrUpdate(xref);				
						break;
					}	
				}
			}
			if (updated) {
				System.out.println("updated dlattempts for xref.id: "+xrefId);
			}
			else {
				System.out.println("no updated dlattempts for xref.id: "+xrefId);
			}
//			Map<String, Object> params = Tools.parseQueryParameter(ui);
//			params.put("id", new Integer(id));
//			Bean bean = new BeanFactory(ui, request).getXrefsBean(doc);
			transaction.commit();
			return Tools.getHTTPStatusResponse(Status.OK, "ok");
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
		}
		finally {
			if(transaction.isActive()) {
				transaction.rollback();
			}			
			Tools.tolerantClose(session);
		}
	}
	
	@POST
	@Path("/{id}/fulltexts")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postFulltext(@Context UriInfo ui, @Context HttpServletRequest request
			, @PathParam(value = "id") int id
			, @FormDataParam(value = "xrefId") int xref_id
			, @FormDataParam(value = "fullTextUrl") String fullTextUrl,
			@FormDataParam(value = "hash") String hash,
			@FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("xtract") InputStream xtractStream, @FormDataParam("xtract") FormDataContentDisposition xtractDetail,
			FormDataMultiPart f,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,			
			@QueryParam("stream") boolean stream) {
			
		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
		if (!ResourceCommons.authenticate(request, user)) {
			return Tools.getHTTPStatusResponse(Status.UNAUTHORIZED, "not authorized.");
		}
		
		List<String> emails = new ArrayList<String>();
		List<FormDataBodyPart> parts = f.getFields("email");
		if(parts != null) {
			for (FormDataBodyPart part : parts) {
				emails.add(part.getValue());
			}
		}
		
		Document doc = DocumentQueries.getDocument(session, id);
		//DocumentCommons.updateDocumentPersons(session, doc, emails);
		//doc = null;
		if(doc == null) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "request document with id='"+id+"' does not exist.");
		}
		
		FulltextUrl fulltext = new FulltextUrl(session);
		
		if(fullTextUrl != null && !fullTextUrl.trim().isEmpty()) {
			fulltext.setDocument(doc);
			fulltext.setFiletype(FulltextUrl.FULLTEXTURL_FILETYPE_PDF);
			fulltext.setLicence(FulltextUrl.FULLTEXTURL_LICENCE_WEB);
			fulltext.setUrl(fullTextUrl);
			fulltext.setValid((short) 1);
			fulltext.setStatus(FulltextUrl.FULLTEXTURL_STATUS_DOWNLOADED);
		}
		else {
			return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "missing or invalid parameter: fullTextUrl");
		}
		
		try {
			if("pdf".equalsIgnoreCase(format)) {			
				return DocumentCommons.handleDocumentPDFUpload(session, doc, xref_id, fulltext, inputStream);		
			}
			
			if("txt".equalsIgnoreCase(format)) {		
				if(hash == null) {
					return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "hash cannot be NULL");
				}
				Transaction transaction = session.beginTransaction();
				try {					
					
					try {
						FulltextCommons.createDocumentPlainTextFile(hash, inputStream,  false);						
					}
					catch(AlreadyConnectedException e) {
						return Tools.getHTTPStatusResponse(Status.NOT_MODIFIED, e.getMessage());
					}
					catch (IOException e) {
						return Tools.getHTTPStatusResponse(Status.NOT_MODIFIED, e.getMessage());
					}
					try {
						inputStream.close();					
					}
					catch (Throwable e) {
					}
					
					DocumentCommons.createOrUpdateFulltextHash(hash, session, doc, fulltext);
					DocumentCommons.updateDocumentPersons(session, doc, emails);
					//@Deprecated: in future not used anymore 
					//DocumentCommons.updateDocumentData(session, doc, xtractStream);
						
					
					transaction.commit();
					
					// try to index the plain text with lucene
					FulltextCommons.requestPlainTextUpdate(doc.getId(), hash, xref_id);
									
					return Tools.getHTTPStatusResponse(Status.OK, "OK");
				}
				catch (Exception e) {
					e.printStackTrace();
					return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could not store text: "+e.getMessage());
				}
				finally {
					if(transaction.isActive()) {
						transaction.rollback();
					}					
				}		
			}
			
			if("zip".equalsIgnoreCase(format)) {		
				if(hash == null) {
					return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "hash cannot be NULL");
				}
				Transaction transaction = session.beginTransaction();
				try {					
					File file;
					try {
						file = FulltextCommons.createDocumentPlainTextFile(hash, null,  false);						
					}
					catch (IOException e) {
						return Tools.getHTTPStatusResponse(Status.NOT_MODIFIED, e.getMessage());
					}
					
					FileOutputStream fos = new FileOutputStream(file);
					int bite;
					while((bite = inputStream.read()) > -1) {
						fos.write(bite);
					}
					try {
						fos.close();
						inputStream.close();
					}
					catch (Throwable e) {
					}
										
					DocumentCommons.createOrUpdateFulltextHash(hash, session, doc, fulltext);
					
					transaction.commit();
					
					// try to index the plain text with lucene
					FulltextCommons.requestPlainTextUpdate(doc.getId(), hash, xref_id);						
					
					return Tools.getHTTPStatusResponse(Status.OK, "OK");
				} catch (Exception e) {
					e.printStackTrace();
					return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could not store pdf");
				}
				finally {
					if(transaction.isActive()) {
						transaction.rollback();
					}					
				}		
			}
		}
		finally {
			Tools.tolerantClose(session);
			//System.out.println("time:     "+(System.currentTimeMillis()-timeStart));
		}
		return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "invalid format=\""+format+"\" for POST request");
	}
	
	
	@POST
	@Path("/{id}/references")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response postReferences(@Context UriInfo ui, @Context HttpServletRequest request
			, @PathParam(value = "id") final int id, 
			@FormDataParam("referencesData") final InputStream xtractStream, @FormDataParam("xtract") FormDataContentDisposition xtractDetail,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,			
			@QueryParam("stream") boolean stream) {
			
		Session session = Tools.getSession();
		session.setFlushMode(FlushMode.MANUAL);
		User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
		if (!ResourceCommons.authenticate(request, user)) {
			return Tools.getHTTPStatusResponse(Status.UNAUTHORIZED, "not authorized.");
		}
	
		
		Document doc = DocumentQueries.getDocument(session, id);
		if(doc == null) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "request document with id='"+id+"' does not exist.");
		}		
		
		try {
			if("xml".equalsIgnoreCase(format)) {				
				try {
					final String xtrString = IOUtils.toString(xtractStream, "UTF-8");
    				
    				AtomicOperation<Response> op = new AtomicOperation<Response>() {
    					@Override
    					public Response exec(Session session) {
    						Document doc = DocumentQueries.getDocument(session, id);
    						if(doc == null) {
    							return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "request document with id='"+id+"' does not exist.");
    						}
    						try {    						
    							System.out.println("requesting reference update for id: "+ id);
    							DocumentCommons.updateDocumentData(session, doc, xtrString);
				
    							return Tools.getHTTPStatusResponse(Status.OK, "OK");
    						}
    						catch (Exception e) {
    							e.printStackTrace();
    							return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "could not add references: "+e.getMessage());
    						}
    						
    					}
    				};
    				
    				System.out.println("DB postReferences update queue: "+SessionProvider.atomicManager.size());
    				AtomicOperationHandle<Response> handle = SessionProvider.atomicManager.addOperation(op);
    				
    				//wait for operation to finish
    				return handle.getResult();
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				return Tools.getHTTPStatusResponse(Status.OK, "OK");
			}
		}
		finally {
			Tools.tolerantClose(session);
			//System.out.println("time:     "+(System.currentTimeMillis()-timeStart));
		}
		return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "invalid format=\""+format+"\" for POST request");
	}

	@GET
	@Path("/{id: \\d+}")
	public Response getDocumentById(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
			@QueryParam("source") String source, @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try {
			Map<String, Object> params = Tools.parseQueryParameter(ui);
			params.put("id", new Integer(id));

			Bean bean = new BeanFactory(ui, request).getDocumentBean(DocumentQueries.getDocument(session, id, source));
			return Tools.getSerializedResponse(format, bean, stream);
		}
		catch (NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given id");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	@GET
	@Path("/{q}")
	public Response search(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "q") String q, @QueryParam("source") String source, 
			@DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("stream") boolean stream, @QueryParam("defaultField") String defaultField,
			int offset, int number) {
		
		Session session = Tools.getSession();
		try {
			if (!ResourceCommons.authenticate(request)) {
				return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED, "unauthorized.");
			}
			
			List<Integer> docIds = new ArrayList<Integer>();
			long time = System.currentTimeMillis();
			List<DocumentHashItem> items = new Searcher().search(q);
			System.out.println("lucene search time: "+(System.currentTimeMillis()-time));
			
			if (items==null || items.size() == 0) {
				throw new NullPointerException();
			}
			for (int i=0; i<Math.min(number, items.size()); i++) {
				docIds.add(items.get(i).documentId);
			}
			List<Document> docs = DocumentQueries.getDocuments(session, docIds);
			Bean bean = new BeanFactory(ui, request).getDocumentsBean(docs, new Long(docs.size()));
			return Tools.getSerializedResponse(format, bean, stream);

		}
		catch (NullPointerException e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No document found for the given query");
		}
		catch (Exception e) {
			e.printStackTrace();
			return Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "Error searching for document.");
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	

	//FIXME: merge document with specific id
	//	@POST
	//	@Path("/{id}")
	//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//	public Response addDocument(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
	//			@FormDataParam("source") String source, @FormDataParam("file") InputStream inputStream,
	//			@FormDataParam("file") FormDataContentDisposition fileDetail) {
	//
	//		if (!source.equals("probability") && !source.equals("jabref")) {
	//			return Response.status(Status.FORBIDDEN).build();
	//		}
	//
	//		String fileName = fileDetail.getFileName();
	//		System.out.println("debug fileName: " + fileName);
	//
	//		try {
	//			String fileEnding = fileName.substring(fileName.lastIndexOf(".") + 1);
	//			if (fileEnding.equalsIgnoreCase("xml")) {
	//				return Importer.uploadResource(Document.class, inputStream, fileDetail);
	//			}
	//			else {
	//				return Importer.uploadPdf(ui, request, inputStream, fileDetail, source);
	//			}
	//		}
	//		catch (Exception e) {
	//			e.printStackTrace();
	//			return Response.status(Status.BAD_REQUEST).build();
	//		}
	//	}

	@GET
	@Path("/")
	public Response getDocuments(@Context UriInfo ui, @Context HttpServletRequest request, @QueryParam("source") String source,
			@DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format, @QueryParam("page") Integer page,
			@QueryParam("number") Integer number, @QueryParam("dlo") String dlo, @QueryParam("dhi") String dhi,
			@QueryParam("fulltext_indexed") Boolean indexed,@ QueryParam("pdf_url") Boolean pdf_url, @QueryParam("max_rank") Integer maxRank,
			@QueryParam("fulltexturl_unavailable") boolean fulltexturl_unavailable, @QueryParam("stream") boolean stream) {

		if (page != null && page < 1) {
			return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "There is no page " + page + ".");
		}
		if ((dhi != null && dlo == null) || (dhi == null && dlo != null)) {
			return Tools.getHTTPStatusResponse(Status.BAD_REQUEST,
					"Forbidden combination of paramters. 'dhi' must be combined with 'dlo' and vice versa!");
		}

		Session session = Tools.getSession();
		Long count = null;

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date ddlo = null;
			Date ddhi = null;
			try {
				if (dlo != null) {
					ddlo = dateFormat.parse(dlo);
				}
				if (dhi != null) {
					ddhi = dateFormat.parse(dhi);
				}
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
			number = getRestrictedNumber(number);

			//takes too long
//			count = DocumentQueries.getDocumentCount(session, source, ddlo, ddhi, fulltexturl_unavailable, indexed, pdf_url, null);
			count = null;

			List<Document> documents;

			Integer start = null;
			if (page != null) {
				start = (page - 1) * number;
			}

			documents = DocumentQueries.getDocuments(session, null, source, null, ddlo, ddhi, start, number,
					fulltexturl_unavailable, indexed, pdf_url, true, null, maxRank);
			System.out.println("debug size: "+documents.size());

			Bean bean = new BeanFactory(ui, request).getDocumentsBean(documents, count);
			return Tools.getSerializedResponse(format, bean, stream);
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
	
//	private Response getDocumentsNotIndexed(UriInfo ui, HttpServletRequest request, int numberOf, String format, boolean stream) {
//		if(numberOf <= 0) {
//			return Tools.getHTTPStatusResponse(Status.BAD_REQUEST,
//					"Forbidden combination of paramters. 'pdf_url=1' requires the 'number' parameter to be greater than 0.");
//		}
//		Session session = Tools.getSession();
//		try {
//			List<Document> documents = DocumentQueries.getDocumentsToDownload(session, numberOf);
//			Bean bean = new BeanFactory(ui, request).getDocumentsBean(documents, DocumentQueries.getDocumentCount(session, null));			
//			return Tools.getSerializedResponse(format, bean, stream);
//		}
//		finally {
//			Tools.tolerantClose(session);
//		}
//	}

	@POST
	@Path("/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addDocuments(@Context UriInfo ui, @Context HttpServletRequest request, @PathParam(value = "id") int id,
			@FormDataParam("source") String source, @FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("filename") String fileName,
			@FormDataParam("url") String url,
			@DefaultValue(Tools.DEFAULT_FORMAT) @FormDataParam("format") String format,
			@DefaultValue("true") @FormDataParam("save") boolean saveToDatabase) {

		if (fileName == null && (url == null || url.length() == 0)) {
			try {
				fileName = fileDetail.getFileName();
			} catch (NullPointerException e) {
				return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "No filename given. Set the parameter 'filename'");
			}
		}

		System.out.println("url: " + url);
		System.out.println("filename: " + fileName);
		System.out.println("source: " + source);

		try {
			if (!source.equals("probability") && !source.equals("jabref")) {
				return Response.status(Status.FORBIDDEN).build();
			}

			if (url != null  && url.length() > 0) {
				inputStream = new URL(url).openConnection().getInputStream();
				fileName = url.substring(url.indexOf("/", -1));
			}

			String fileEnding = fileName.substring(fileName.lastIndexOf(".") + 1);
			System.out.println("ending: " + fileEnding);
			if (fileEnding.equalsIgnoreCase("xml")) {
				System.out.println("xmlupload");
				return Importer.uploadResources(Document.class, inputStream, fileDetail, format);
			}
			else {
				System.out.println("pdfupload");
				if (source.equals("jabref")) {
					saveToDatabase = false;
				}
				return Importer.uploadPdf(ui, request, inputStream, fileDetail, source, format, saveToDatabase);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	public int getRestrictedNumber(Integer number) {
		if (number == null) {
			return MAX_NUMBER_PER_PAGE;
		}
		if (number <= 0) {
			number = 1;
		}
		if (number > 1000) {
			number = MAX_NUMBER_PER_PAGE;
		}
		return number;
	}
}