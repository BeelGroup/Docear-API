package util.recommendations;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sciplore.utilities.config.Config;

import util.Tools;

public class GraphDbUtils {
	public static final String NEO4JREST_URI; //= "http://localhost:47474";
	private static final Client client = ClientBuilder.newClient();
	static {
		final Properties p = Config.getProperties("org.mrdlib");
		NEO4JREST_URI = p.getProperty("docear.graphdb.host", "http://localhost:47474");		
	}
	
	public static Client getClient() {
		return client;
	}
	
	public static String getXmlFromRequest(String path, MultivaluedMap<String, String> params) {
		WebTarget target = client.target(NEO4JREST_URI).path(path);		
		for (String s : params.keySet()) {
			System.out.println("param --> "+s+" : "+params.get(s));
		}
		Form form = new Form(params);
		
		Response response = null;
		try {
    		response = target.request().accept(MediaType.TEXT_PLAIN_TYPE).post(Entity.form(form));
    		if (response == null || response.getStatus() != (Status.OK.getStatusCode())) {
    			return null;
    		}
    		
    		// the GraphDb returns Strings in a Java String format: leading and ending '"' / '"' escaped as '\"'
    		String s = response.readEntity(String.class);
    		
    		return s.substring(1, s.length()-1).replace("\\\"", "\"");
		}
		finally {
			Tools.tolerantClose(response);
		}
	}
}
