package org.docear.graphdb;

import javax.servlet.http.HttpServletRequest;

import org.neo4j.server.rest.security.SecurityFilter;
import org.neo4j.server.rest.security.SecurityRule;

public class DocearSecurityRule implements SecurityRule
{

    public static final String REALM = "WallyWorld"; // as per RFC2617 :-)

    @Override
    public boolean isAuthorized( HttpServletRequest request ) {
    	if("46.4.84.169".equals(request.getRemoteAddr())) {
    		return true;
    	}
    	else if("127.0.0.1".equals(request.getRemoteAddr())) {
    		return true;
    	}
        return false; // always fails - a production implementation performs
                      // deployment-specific authorization logic here
    }

    @Override
    public String forUriPath() {
        return SecurityRule.DEFAULT_DATABASE_PATH;
    }

    @Override
    public String wwwAuthenticateHeader() {
        return SecurityFilter.basicAuthenticationResponse(REALM);
    }
}