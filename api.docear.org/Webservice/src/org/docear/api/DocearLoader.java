package org.docear.api;

import importer.Importer;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import rest.ApplicationResource;
import rest.AuthenticationResource;
import rest.AuthorResource;
import rest.DocumentResource;
import rest.FulltextResource;
import rest.InternalResource;
import rest.OrganizationResource;
import rest.ServiceRessource;
import rest.ToolsResource;
import rest.UserRessource;
import rest.XrefResource;

public class DocearLoader extends ResourceConfig {
	
	public DocearLoader() {
		super(ApplicationResource.class
				,Importer.class
				,AuthenticationResource.class
				,AuthorResource.class
				,DocumentResource.class
				,FulltextResource.class
				,InternalResource.class
				,OrganizationResource.class
				,RequestLogger.class
				,ResponseLogger.class
				,ServiceRessource.class
				,ToolsResource.class
				,UserRessource.class
				,XrefResource.class
				,MultiPartFeature.class);
	}

}
