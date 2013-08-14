package org.docear;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.FormDataMultiPart;

public class FullTextUploadWorker extends ReferenceUploadWorker {
	
	
	protected final boolean uploadFullText(String xRefId, String docId, byte[] data, String pdfWebUrl, String hash) {		
		if (System.getProperty("docear.detailed") != null && System.getProperty("docear.detailed").equals("true")) {
			System.out.println("["+Thread.currentThread().getName()+"] hash: "+hash);
			System.out.println("["+Thread.currentThread().getName()+"] documentId: "+docId);
			System.out.println("["+Thread.currentThread().getName()+"] xRefId: "+xRefId);
			System.out.println("["+Thread.currentThread().getName()+"] url: "+pdfWebUrl);
		}
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		try {
			// construct data
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			formDataMultiPart.field("xrefId", xRefId);
			formDataMultiPart.field("fullTextUrl", pdfWebUrl);
			formDataMultiPart.field("downloadAttempt", "true");
			formDataMultiPart.field("hash", hash);
			formDataMultiPart.field("file", data, MediaType.MULTIPART_FORM_DATA_TYPE);
						
			WebResource webResource = client.resource(Main.DOCEAR_SERVICES + "/documents/" + docId	+ "/fulltexts/?format=txt");
			Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333").type(MediaType.MULTIPART_FORM_DATA_TYPE);

			ClientResponse response = builder.post(ClientResponse.class, formDataMultiPart);

			if (response.getStatus() != 200) {
				System.out.println("["+Thread.currentThread().getName()+"] xrefid " + xRefId + ", HTTP status for pdf upload: " + response.getStatus() + "| " + response.getEntity(String.class)
						+ " --> " + pdfWebUrl);
			}
			return true;

		}
		catch (Exception e) {
			System.out.println("["+Thread.currentThread().getName()+"] "+ e + "xrefid: " + xRefId + " Warning: this is not supposed to happen!");
			e.printStackTrace();
			return false;
		}
	}
}
