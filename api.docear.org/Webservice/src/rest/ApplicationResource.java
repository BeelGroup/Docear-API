package rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.sciplore.data.BeanFactory;
import org.sciplore.database.SessionProvider;
import org.sciplore.formatter.Bean;
import org.sciplore.queries.ApplicationQueries;
import org.sciplore.resources.Application;

import util.ResourceCommons;
import util.Tools;
import util.UserCommons;

import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/applications")
public class ApplicationResource {

	@GET
	@Path("/{appName}")
	public Response getSingleApplication(@PathParam("appName") String appName,
			@DefaultValue(ResourceCommons.DEFAULT_FORMAT) @QueryParam("format") String outputFormat, @QueryParam("stream") boolean stream, @Context UriInfo ui,
			@Context HttpServletRequest request) {

		ResponseBuilder builder = Response.status(Status.BAD_REQUEST);
		builder.type(ResourceCommons.getOutputMediaType(outputFormat));

		String charset = request.getCharacterEncoding();
		if (charset == null) {
			charset = "UTF-8";
		}

		Session session = SessionProvider.sessionFactory.openSession();

		try {
			List<Application> apps = ApplicationQueries.getApplicationVersions(session, appName);
			if (apps.size() > 0) {
				Bean bean = new BeanFactory(ui, request).getApplicationBean(apps);
				return Tools.getSerializedResponse(outputFormat, bean, stream);
			}
			else {
				builder.type(MediaType.TEXT_PLAIN_TYPE);
				builder.status(Status.NO_CONTENT);
				builder.entity("no versions found for '" + appName + "'");
			}
		}
		finally {
			Tools.tolerantClose(session);
		}
		return builder.build();

	}

	@GET
	@Path("/{appName}/versions/latest")
	public Response getLatestSingleApplicationVersion(@PathParam("appName") String appName,
			@DefaultValue(ResourceCommons.DEFAULT_FORMAT) @QueryParam("format") String outputFormat, @QueryParam("stream") boolean stream,
			@QueryParam("minStatus") String minStatus, @Context UriInfo ui, @Context HttpServletRequest request) {

		final Session session = SessionProvider.sessionFactory.openSession();
		session.setFlushMode(FlushMode.MANUAL);
		UserCommons.storeApplicationNumber(session, request);

		ResponseBuilder builder = Response.status(Status.BAD_REQUEST);
		builder.type(ResourceCommons.getOutputMediaType(outputFormat));

		String charset = request.getCharacterEncoding();
		if (charset == null) {
			charset = "UTF-8";
		}

		List<Application> apps;
		try {
			apps = ApplicationQueries.getLatestApplicationVersion(session, appName, minStatus);
		}
		catch(Exception e) {
			apps = null;
		}
		finally {
			Tools.tolerantClose(session);
		}
		
		if (apps == null || apps.size() == 0) {
			builder.type(MediaType.TEXT_PLAIN_TYPE);
			builder.status(Status.NO_CONTENT);
			builder.entity("no versions found");
		}
		else {
			Bean bean = new BeanFactory(ui, request).getApplicationBean(apps);
			return Tools.getSerializedResponse(outputFormat, bean, stream);			
		}
		
		return builder.build();

	}

}
