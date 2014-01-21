package org.docear.api;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class RequestLogger extends AHttpLogger implements ContainerRequestFilter {
	
	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		log.info(context.getMethod() +" - "+context.getUriInfo().getRequestUri());
	}

}
