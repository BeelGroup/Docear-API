package org.docear;

import java.util.Collection;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;


public class FullTextUploadWorker extends ReferenceUploadWorker {
	
	
	protected final boolean uploadFullText(String xRefId, String docId, byte[] data, String pdfWebUrl, String hash, Collection<String> emails) {		
		if (System.getProperty("docear.detailed") != null && System.getProperty("docear.detailed").equals("true")) {
			System.out.println("["+Thread.currentThread().getName()+"] hash: "+hash);
			System.out.println("["+Thread.currentThread().getName()+"] documentId: "+docId);
			System.out.println("["+Thread.currentThread().getName()+"] xRefId: "+xRefId);
			System.out.println("["+Thread.currentThread().getName()+"] url: "+pdfWebUrl);
			System.out.println("["+Thread.currentThread().getName()+"] emails: "+emails);
		}
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		Response response = null;
		try {
			// construct data
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			formDataMultiPart.field("xrefId", xRefId);
			formDataMultiPart.field("fullTextUrl", pdfWebUrl);
			formDataMultiPart.field("downloadAttempt", "true");
			formDataMultiPart.field("hash", hash);
			formDataMultiPart.field("file", data, MediaType.MULTIPART_FORM_DATA_TYPE);
			if(emails != null) {
				for (String email : emails) {
					formDataMultiPart.field("email", email);
				}			
			}
			System.out.println("posting new document [id:"+docId+"]");
			WebTarget webTarget = Main.client.target(Main.DOCEAR_SERVICES + "/documents/" + docId	+ "/fulltexts/?format=txt");
			Invocation.Builder builder = webTarget.request(MediaType.MULTIPART_FORM_DATA_TYPE);
    		builder.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");
			
			response = builder.post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

			if (response.getStatus() != 200) {
				System.out.println("["+Thread.currentThread().getName()+"] xrefid " + xRefId + ", HTTP status for pdf upload: " + response.getStatus() + "| " + response.getEntity()
						+ " --> " + pdfWebUrl);
			}
			return true;

		}
		catch (Exception e) {
			System.out.println("["+Thread.currentThread().getName()+"] "+ e + "xrefid: " + xRefId + " Warning: this is not supposed to happen!");
			e.printStackTrace();
			return false;
		}
		finally {
			Main.tolerantClose(response);
		}
	}
}
