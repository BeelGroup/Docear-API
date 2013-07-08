//package rest;
//
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.WebApplicationException;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.core.Response.ResponseBuilder;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import com.sun.jersey.api.client.ClientResponse.Status;
//
//@Path("/login")
//public class Login {	
//	private static final String DOLOGINURL = "/login.do";
//	
//	@GET     
//    @Path("/")
//	public int doLogin(@Context HttpServletRequest request, @Context HttpServletResponse response) {
//		
//		try {
//			String scheme = request.getScheme();			
//			String serverName = request.getServerName();
//			int portNumber = request.getServerPort();
//			
//			URL url = new URL(scheme+"://"+serverName+":"+portNumber+DOLOGINURL);
//			System.out.println("debug url: "+url);
//			String userName = request.getParameter("userName");
//			String password = request.getParameter("password");
//			
//			userName = "marissa";
//			password = "koala";
//			
//			
////			Enumeration<String> en = request.getSession().getAttributeNames();
////			while (en.hasMoreElements()) {
////				System.out.println("debug session_attribute: "+en.nextElement());
////			}
//			
//			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		    if (authentication.getPrincipal() instanceof UserDetails) {
//		      UserDetails details = (UserDetails) authentication.getPrincipal();
//		      System.out.println("drin" + details + " auth: "+authentication.isAuthenticated());
//		      return 0;
//		    } 
//		    else {
//		    	System.out.println("else " + authentication.getPrincipal() + " auth: "+authentication.isAuthenticated());
//		    }
//		    
////			Object o = request.getSession().getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY);
////			if ( o != null && !(o instanceof UnapprovedClientAuthenticationException)) {
////				System.out.println("debug success");
////				return;
////			}
////			else {
////				System.out.println("debug failure");
////			}
//			
//			System.out.println("debug :" + request.getSession().getAttributeNames());
//			
//			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			conn.setRequestMethod("POST");
//			conn.setRequestProperty("userName", userName);
//			conn.setRequestProperty("password", password);
//			
//			
//			
//			try {
//				String ret = "";
//				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//							
//				char[] cbuf = new char[5000];
//						
//				int readChars;
//				while ((readChars = reader.read(cbuf)) > -1) {
//					//TODO better use Stringbuffer for 'ret'
//					ret += String.valueOf(cbuf, 0, readChars);
//				}
//				System.out.println("debug ret: "+ret);;
//			}
//			catch (IOException e) {
//				throw new IOException(conn.getResponseCode()+": "+conn.getResponseMessage());
//			}
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		ResponseBuilder builder = Response.status(Status.OK);		
//		builder.type("text/plain");   
//		builder.type(MediaType.TEXT_PLAIN_TYPE);
//		builder.entity("Login");        	
//		throw new WebApplicationException(builder.build());
//	}
//}
