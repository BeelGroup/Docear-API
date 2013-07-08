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
import org.sciplore.resources.Institution;

import util.Tools;


@Path("/organization")
public class OrganizationResource {
	
	@GET     
    @Path("/{id}")
    public Response getOrganizationById(@Context UriInfo ui,
    						  @Context HttpServletRequest request,
    						  @PathParam(value = "id")int id, 
    						  @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
    						  @QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try{			
			Institution institution = new Institution(session).getInstitution(id);
			
			if (institution == null) {
				return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "Institution not found.");				
			}	
			
			Bean bean = new BeanFactory(ui, request).getOrganizationBean(institution);
			return Tools.getSerializedResponse(format, bean, stream);	
		}
		catch(NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, e.getMessage());
		}
		finally{
			Tools.tolerantClose(session);
		}
	}

}
