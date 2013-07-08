package rest;

import importer.Importer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import util.Tools;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/tools")
public class ToolsResource {
	@GET
	@Path("/pdfextract")
	public Response addDocuments(@Context UriInfo ui,
			@Context HttpServletRequest request, 
			@DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
			@QueryParam("url") String url) {
	
		if (url == null || url.length() == 0) {
			return util.Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "No file given.");
		}
		InputStream inputStream = null;
		
		if (url != null  && url.length() > 0) {
			try {
				inputStream = new URL(url).openConnection().getInputStream();
			} catch (IOException e) {
				return util.Tools.getHTTPStatusResponse(Status.INTERNAL_SERVER_ERROR, "File could not be opened.");
			}
		}
		return Importer.uploadPdf(ui, request, inputStream, format);
	}
	
	@POST
	@Path("/pdfextract")
	public Response addDocuments2(@Context UriInfo ui,
			@Context HttpServletRequest request, 
			@DefaultValue(Tools.DEFAULT_FORMAT) @FormParam("format") String format,
			@FormParam("url") String url) {
		System.out.println("Normal Post");
		return addDocuments(ui, request, format, url);
	}
	
	@POST
	@Path("/pdfextract")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addDocuments(@Context UriInfo ui, @Context HttpServletRequest request, 
			@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("url") String url,
			@DefaultValue(Tools.DEFAULT_FORMAT) @FormDataParam("format") String format) {
		System.out.println("Form Post");
	
		try {
			if (url != null && url.length() > 0) {
				inputStream = new URL(url).openConnection().getInputStream();
			}
			return Importer.uploadPdf(ui, request, inputStream, format);
		}
		catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
}
