package util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;
import org.sciplore.database.SessionProvider;
import org.sciplore.resources.User;

public class ResourceCommons {

	public static final String DEFAULT_FORMAT = "xml";
	
	public static boolean authenticate(HttpServletRequest request) {
		if (System.getProperty("docear_debug") != null && System.getProperty("docear_debug").equals("true")) {
			return true;
		}
		String userName = request.getHeader("userName");
		final Session session = SessionProvider.sessionFactory.openSession();
		try {
			User user = new User(session).getUserByEmailOrUsername(userName);
			if (user == null) {
				return false;
			}
			return authenticate(request, user);
		}
		finally {
			Tools.tolerantClose(session);
		}
	}
	
	public static boolean authenticate(HttpServletRequest request, User user) {
		if (System.getProperty("docear_debug") != null && System.getProperty("docear_debug").equals("true")) {
			return true;
		}
		String requestAccessToken = request.getHeader("accessToken");
		String userAccessToken = (String)request.getSession().getAttribute("UserAccessToken");
		if(user == null) {
			return false;
		}
		if(userAccessToken == null) {
			userAccessToken = user.getAccessToken();
		}
		if (requestAccessToken == null || userAccessToken == null || !requestAccessToken.equals(userAccessToken)) {
			return false;
		}
		return true;
	}
	
	public static MediaType getOutputMediaType(String outputFormat) {
		if(ResourceCommons.DEFAULT_FORMAT.equalsIgnoreCase(outputFormat)) {
			return MediaType.TEXT_XML_TYPE;
		} 
		else if("json".equalsIgnoreCase(outputFormat)) {
			return MediaType.APPLICATION_JSON_TYPE;
		}
		return MediaType.TEXT_PLAIN_TYPE;
	}
}
