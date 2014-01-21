package org.docear.api;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class ResponseLogger extends AHttpLogger implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext reqContext, ContainerResponseContext respContext) throws IOException {
		log.info("returned "+ reqContext.getMethod() +" - "+reqContext.getUriInfo().getRequestUri());
	}

}
