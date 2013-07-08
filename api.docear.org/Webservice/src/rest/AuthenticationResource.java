package rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.sciplore.database.SessionProvider;
import org.sciplore.resources.User;

import com.sun.jersey.api.client.ClientResponse.Status;


@Path("/authenticate")
public class AuthenticationResource {
	
	@POST
	@Path("/{userName}")	
	public Response authenticateUser(@PathParam("userName") String userName, @FormParam("password") String password, @Context HttpServletRequest request) {
		System.out.println("authenticate");
		
		ResponseBuilder builder = Response.status(Status.BAD_REQUEST);
		builder.type(MediaType.TEXT_PLAIN_TYPE);
		
		String charset = request.getCharacterEncoding();
		if(charset == null){
			charset = "UTF-8";
		}
		
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			userName = URLDecoder.decode(userName, charset);
			
			User user = new User(session).getUserByEmailOrUsername(userName);
			if(user != null && user.checkCredentials(password)){
				user.setSession(session);
				String accessToken = getAccessToken(user, request.getSession());
				if(accessToken == null) {
					builder.status(Status.INTERNAL_SERVER_ERROR);
					builder.entity("Could not get user access token.");
				}
				else {
					saveAccessToken(accessToken, user, request.getSession());
					builder.status(Status.OK);					
					builder.header("accessToken", accessToken);
					builder.entity("success");
				}				
			}
			else{
				builder.status(Status.UNAUTHORIZED);
				builder.entity("Username or Password wrong.");
			}
			
		} 
		catch (UnsupportedEncodingException e1) {
			builder.status(Status.UNAUTHORIZED);
			builder.entity("Couldn't Decode Input Data.");
		} 
		finally {
			Tools.tolerantClose(session);
		}		
		return builder.build();
		
	}

	public static String getAccessToken(User user, HttpSession session) {
		String accessToken = (String)session.getAttribute("UserAccessToken");				
		if(accessToken == null) {
			accessToken	= user.getAccessToken();
			if(accessToken == null) {
				accessToken = createAccessToken(user);
			}
		}
		return accessToken;
	}
	
	public static void saveAccessToken(final String accessToken, final User user, final HttpSession session) {
		session.setAttribute("UserAccessToken", accessToken);
		user.setAccessToken(accessToken);
		user.save();
	} 
//	private static SecureRandom random = new SecureRandom();

	private static String createAccessToken(User user) {
		String accessToken = null;
		try {		
			accessToken = org.sciplore.tools.Tools.convertToSaltedMD5Digest("docear", user.getUsername()+user.getPassword());
			//new BigInteger(130, random).toString(32);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return accessToken;
	}
}
