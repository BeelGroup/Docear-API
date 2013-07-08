package rest;

import java.util.List;

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
import org.sciplore.queries.AuthorQueries;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.resources.Document;
import org.sciplore.resources.Person;

import util.Tools;

@Path("/authors")
public class AuthorResource {
		
	@GET
	@Path("/{id: [0-9]+}")
	public Response getAuthorById(@Context UriInfo ui,
						 @Context HttpServletRequest request,
						 @PathParam(value = "id")int id,
						 @QueryParam("source") String source,
						 @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,						 						 
						 @QueryParam("stream") boolean stream){
		
		Session session = Tools.getSession();		
		try {	
			Person person = AuthorQueries.getAuthor(session, id, source);
			List<Document> documents = DocumentQueries.getDocumentsWithReferences(session, person, source);
			Bean bean = new BeanFactory(ui, request).getDocumentsForAuthor(person, documents);		
			
			return Tools.getSerializedResponse(format, bean, stream);
		}
		catch(NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, e.getMessage());
		}
		finally{
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/{letter: [a-zA-Z]+}")
	public Response getAuthorsByLetter(@Context UriInfo ui,
						 @Context HttpServletRequest request,
						 @PathParam(value = "letter")String letter,						 
						 @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
						 @QueryParam("source") String source,
						 @QueryParam("stream") boolean stream){
		Session session = Tools.getSession();
		try{
			List<Person> persons = AuthorQueries.getAuthorsByLetter(session, letter, source);			
			Bean authorsBean = new BeanFactory(ui, request).getAuthorsBeanByLetter(persons,letter);
			return Tools.getSerializedResponse(format, authorsBean, stream);
			
		} catch(NullPointerException e) {
			return Tools.getHTTPStatusResponse(Status.NOT_FOUND, e.getMessage());
		}
		finally {		
			Tools.tolerantClose(session);
		}
	}
	
	@GET
	@Path("/")
	public int getAllAuthors(@Context UriInfo ui,
						 @Context HttpServletRequest request,
						 @DefaultValue(Tools.DEFAULT_FORMAT) @QueryParam("format") String format,
						 @QueryParam("source") String source,
						 @QueryParam("stream") boolean stream){
		
		this.getAuthorsByLetter(ui, request, "A", format, source, stream);
		return 0;
	}
	
	

	
	
	
	
	

}
