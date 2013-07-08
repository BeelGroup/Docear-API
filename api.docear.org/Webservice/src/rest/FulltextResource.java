package rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.hibernate.Session;
import org.sciplore.data.BeanFactory;
import org.sciplore.formatter.Bean;
import org.sciplore.resources.FulltextUrl;

import util.Tools;


@Path("/fulltext")
public class FulltextResource {
	
	@GET     
    @Path("/{id}")
    public Response getDocumentById(@Context UriInfo ui,
    						  @Context HttpServletRequest request,
    						  @PathParam(value = "id")int id, 
    						  @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
    						  @QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try{			
			FulltextUrl fulltextUrl = new FulltextUrl(session).getFulltextUrl(id);
			
			if(fulltextUrl == null){
				return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "Fulltext not found.");				
			}	
			
			Bean bean = new BeanFactory(ui, request).getFulltextBean(fulltextUrl);
			return Tools.getSerializedResponse(format, bean, stream);	
		}
		finally{
			Tools.tolerantClose(session);
		}
	}
	
	
	
}
