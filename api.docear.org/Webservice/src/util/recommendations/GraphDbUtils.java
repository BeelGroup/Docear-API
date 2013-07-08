package util.recommendations;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

public class GraphDbUtils {
	public static final String NEO4JREST_URI = "http://localhost:7474";
	private static final Client client = Client.create();

	public static Client getClient() {
		return client;
	}
	
	public static String getXmlFromRequest(String path, MultivaluedMap<String, String> params) {
		WebResource res = client.resource(NEO4JREST_URI).path(path);	
		
		for (String s : params.keySet()) {
			System.out.println("param --> "+s+" : "+params.get(s));
		}
	
		ClientResponse response = res.accept(MediaType.TEXT_PLAIN_TYPE).post(ClientResponse.class, params);
		if (response == null || !response.getClientResponseStatus().equals(Status.OK)) {
			return null;
		}
		
		// the GraphDb returns Strings in a Java String format: leading and ending '"' / '"' escaped as '\"'
		String s = response.getEntity(String.class);  
		return s.substring(1, s.length()-1).replace("\\\"", "\"");
	}
}
