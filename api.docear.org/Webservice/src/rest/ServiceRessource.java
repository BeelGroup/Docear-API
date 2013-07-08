package rest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Level;
import org.hibernate.Session;
import org.sciplore.database.SessionProvider;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.resources.Application;
import org.sciplore.resources.Document;
import org.sciplore.resources.UsageStats;
import org.sciplore.resources.User;
import org.sciplore.xml.XmlApplication;
import org.sciplore.xml.XmlDocuments;
import org.sciplore.xtract.Xtract;

import util.Tools;

import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

@Path("/service")
public class ServiceRessource {
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("versioncheck/{appID}/current")
	public XmlApplication getCurrentVersion(@PathParam("appID") int appID) {	
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
    		Application currentApp = new Application(session);
    		List<Application> apps = currentApp.getApplicationByAppId(appID);
    		for(Application app : apps){
    			if(currentApp.getReleaseDate() == null || currentApp.getReleaseDate().before(app.getReleaseDate())){
    				currentApp = app;
    			}			
    		}
    		XmlApplication xmlApp = new XmlApplication(currentApp);    	
    		return xmlApp;
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	@POST
	@Path("stats/usage/{appID}/{event}/")
	public void saveEvent(@PathParam("appID") int appID,
						   @PathParam("event") short event,
						   @FormParam("version") String version,
						   @FormParam("username") String username,
						   @FormParam("eventdata") String eventdata){
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
    		Application app = new Application(session).getApplicationByAppId(appID, version);
    		User user = new User(session).getUserByEmailOrUsername(username);
    		Date date = GregorianCalendar.getInstance().getTime();
    		if(app != null){
    			UsageStats stats = new UsageStats(session, app, user, event, eventdata, date);
    			if(stats != null){
    				Tools.getHTTPStatusResponse(Status.OK, "");
    			}
    		}
    		else{
    			Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "Application not found.");
    		}
		}
		finally {
			Tools.tolerantClose(session);
		}				
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("metadata/available")
	public String getAvailibility() {
		return "" + true; //Tools.getBoolProperty("service.metadata");
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	@Path("metadata/")
	public String getDocuments(@Context UriInfo ui, FormDataMultiPart multiPart) throws Exception {
		try {
			File tempFile = getPdfFile(multiPart);
			if(tempFile != null && tempFile.exists()){
				final Session session = SessionProvider.sessionFactory.openSession();
				Tools.tolerantClose(session);
				return new Xtract().xtract(tempFile);
				/*File xmlTempFile = extractXML(tempFile);
				if(xmlTempFile.exists()) {					
					String title = extractTitle(xmlTempFile);			        
			        List<Document> documents = new Document(session).getDocuments(title, (short)0, (short)100);	
			        XmlDocuments xmlDocuments;
			        if(documents == null || documents.size() == 0){
			        	xmlDocuments = new XmlDocuments();
			        	XmlDocument xmlDocument = new XmlDocument();
			        	xmlDocument.setTitle(title);
			        	xmlDocuments.getDocuments().add(xmlDocument);
			        }
			        else{
			        	xmlDocuments = new XmlDocuments(documents, ui.getBaseUri().toString());		
			        }
			        Tools.tolerantClose(session);
			        return xmlDocuments.getXML();
				}
				else{
					String tempPath = tempFile.getPath();
					File fileToDelete = new File(tempPath.substring(0, tempPath.length() - 3) + "xml_");
					fileToDelete.delete();					
					Tools.sendHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "Could not extract Title.");
				}*/
			}			
		} catch (FileNotFoundException e) {
			Tools.sendHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, this.getClass(), e, Level.ERROR);
		}/* catch (JDOMException e) {
			Tools.sendHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, this.getClass(), e, Level.ERROR);
		}*/ catch (IOException e) {
			Tools.sendHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, this.getClass(), e, Level.ERROR);
		} catch (JAXBException e) {
			Tools.sendHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, this.getClass(), e, Level.ERROR);
		}
		return null;		
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Path("metadata/")
	public XmlDocuments getDocuments(@Context UriInfo ui, @FormParam("title")String title) {
		final Session session = SessionProvider.sessionFactory.openSession();
//        List<Document> documents = DocumentQueries.getDocuments(session, title, (short)0, (short)100);
		try {
            List<Document> documents = DocumentQueries.getDocuments(session, null, null, title, null, null, 0, 100, null, null, null, null, null, null);
            XmlDocuments xmlDocuments;
            if(documents == null || documents.size() == 0){
            	xmlDocuments = new XmlDocuments();
            }
            else{
            	xmlDocuments = new XmlDocuments(documents, ui.getBaseUri().toString());		
            }
            return xmlDocuments;
		}
		finally {
			Tools.tolerantClose(session);
		}
	}

	private File getPdfFile(FormDataMultiPart multiPart) {
		FormDataBodyPart pdfBodyPart = multiPart.getField("pdf");
		if(pdfBodyPart != null){
			BodyPartEntity pdfBodyPartEntity =  (BodyPartEntity)pdfBodyPart.getEntity();
			InputStream inputStream = pdfBodyPartEntity.getInputStream();
			File tempFile = new File("");
			try {
				tempFile = File.createTempFile("rest", ".pdf");
				BufferedInputStream bufis = new BufferedInputStream(inputStream);
				OutputStream outputStream = new FileOutputStream(tempFile);
			    byte buffer[] = new byte[1024];
			    int length;
			    while((length = bufis.read(buffer)) > 0)
			    outputStream.write(buffer,0,length);
			    outputStream.close();
			    inputStream.close();
			    bufis.close();
			} catch (IOException e) {
				return null;
			}
			return tempFile;
		}
		else{
			pdfBodyPart = multiPart.getField("gzippedpdf");
			if(pdfBodyPart != null){
				BodyPartEntity pdfBodyPartEntity =  (BodyPartEntity)pdfBodyPart.getEntity();
				InputStream inputStream = pdfBodyPartEntity.getInputStream();
				File tempFile = new File("");
				try {
					tempFile = File.createTempFile("rest", ".pdf");
					BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(inputStream));
					OutputStream outputStream = new FileOutputStream(tempFile);
				    byte buffer[] = new byte[1024];
				    int length;
				    while((length = bufis.read(buffer)) > 0)
				    outputStream.write(buffer,0,length);
				    outputStream.close();
				    inputStream.close();
				    bufis.close();
				}catch(IOException e1){
					return null;
				}
				return tempFile;
			}
		}
		return null;
	}
}
