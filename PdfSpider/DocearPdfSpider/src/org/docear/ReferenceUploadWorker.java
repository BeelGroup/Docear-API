package org.docear;

import java.io.File;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;

public class ReferenceUploadWorker {
	protected static ClientConfig config = new DefaultClientConfig();
	static {
	config.getClasses().add(MultiPartWriter.class);
	}
	protected Client client = Client.create(config);
	protected File tmpDir = new File(System.getProperty("java.io.tmpdir"));
	
	protected final boolean uploadReferences(String docId, String referenceData) {		
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			System.out.println("["+Thread.currentThread().getName()+"] documentId: "+docId);
			System.out.println("["+Thread.currentThread().getName()+"] "+referenceData);
		}
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		try {
			// construct data
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			
			if(referenceData != null) {
				formDataMultiPart.field("referencesData", referenceData.getBytes(), MediaType.MULTIPART_FORM_DATA_TYPE);
			}
			
			WebResource webResource = client.resource(Main.DOCEAR_SERVICES + "/documents/" + docId	+ "/references/?format=xml");
			Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333").type(MediaType.MULTIPART_FORM_DATA_TYPE);

			ClientResponse response = builder.post(ClientResponse.class, formDataMultiPart);

			if (response.getStatus() != 200) {
				System.out.println("docid " + docId + ", HTTP status for reference upload: " + response.getStatus() + "| " + response.getEntity(String.class));
			}
			return true;

		}
		catch (Exception e) {
			System.out.println(e + "docid: " + docId + " Warning: this is not supposed to happen!");
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean uploadReferencesByHash(String docHash, String referenceData) {
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			System.out.println("["+Thread.currentThread().getName()+"] docHash: "+docHash);
			System.out.println("["+Thread.currentThread().getName()+"] "+referenceData);
		}
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		try {
			// construct data
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			
			if(referenceData != null) {
				formDataMultiPart.field("referencesData", referenceData.getBytes(), MediaType.MULTIPART_FORM_DATA_TYPE);
			}
			
			WebResource webResource = client.resource(Main.DOCEAR_SERVICES + "/internal/document/"+docHash+"/references/?format=xml");
			Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333").type(MediaType.MULTIPART_FORM_DATA_TYPE);

			ClientResponse response = builder.post(ClientResponse.class, formDataMultiPart);

			if (response.getStatus() != 200) {
				System.out.println("docHash " + docHash + ", HTTP status for reference upload: " + response.getStatus() + "| " + response.getEntity(String.class));
			}
			return true;

		}
		catch (Exception e) {
			System.out.println(e + "docHash " + docHash + " Warning: this is not supposed to happen!");
			e.printStackTrace();
			return false;
		}
		
	}

}
