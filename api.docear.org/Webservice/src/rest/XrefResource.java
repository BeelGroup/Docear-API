package rest;

import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.data.BeanFactory;
import org.sciplore.database.SessionProvider;
import org.sciplore.formatter.Bean;
import org.sciplore.queries.XrefQueries;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.User;

import util.ResourceCommons;
import util.Tools;
import util.UserCommons;


@Path("/xref")
public class XrefResource {
	
	@GET     
    @Path("/{id}")
    public Response getXrefById(@Context UriInfo ui,
    						  @Context HttpServletRequest request,
    						  @PathParam(value = "id")int id, 
    						  @QueryParam("source") String source,
    						  @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
    						  @QueryParam("stream") boolean stream) {
		Session session = Tools.getSession();
		try {			
			DocumentXref xref = XrefQueries.getDocumentXref(session, id, source);
			
			if(xref == null){				
				return Tools.getHTTPStatusResponse(Status.NOT_FOUND, "Xref not found.");				
			}	
		
			Bean bean = new BeanFactory(ui, request).getXrefBean(xref);
			return Tools.getSerializedResponse(format, bean, stream);	
		}
		catch(NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, e.getMessage());
		}
		finally{
			Tools.tolerantClose(session);
		}
	}
	@POST
	//@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/update")
	public Response xrefUpdate(@FormParam("xrefid") Integer xrefid, @FormParam("if_indexed") Integer if_indexed, @FormParam("username") String userName, @Context HttpServletRequest request) {
		
		try {
			final Session session = SessionProvider.sessionFactory.openSession();

			User user = new User(session).getUserByEmailOrUsername("pdfdownloader");
			if (!userName.equals("pdfdownloader") | !ResourceCommons.authenticate(request, user)) {
				System.out.println("(PdfSpider) Rejected unauthorised attempt to update xrefid " + xrefid + " in database");
				return UserCommons.getHTTPStatusResponse(Status.UNAUTHORIZED, "no valid access token.");
			}
			Criteria criteria = session.createCriteria(DocumentXref.class, "xref").add(Restrictions.eq("id", xrefid));
			DocumentXref xref = (DocumentXref) criteria.setMaxResults(1).uniqueResult();
			xref.setSession(session);
			xref.setDlAttempts(xref.getDlAttempts() + 1);
			xref.setLastAttempt(new GregorianCalendar().getTime());
			xref.setIndexed(if_indexed);
			xref.save();
			System.out.println("(PdfSpider) xrefid " + xrefid + " successfuly updated in database ");
			return  Response.status(Status.ACCEPTED).build();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
			System.out.println("(PdfSpider) xrefid " + xrefid + " failed to be updated in database - this should not happen!");
			return Response.status(Status.BAD_REQUEST).build();
			
		}
		
	}
	
	
	
	
}
