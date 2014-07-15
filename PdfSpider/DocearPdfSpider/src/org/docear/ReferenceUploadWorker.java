package org.docear;

import java.io.File;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ReferenceUploadWorker {
	protected File tmpDir = new File(System.getProperty("java.io.tmpdir"));
	
	protected final boolean uploadReferences(String docId, String referenceData) {		
		if (System.getProperty("docear.detailed") != null && System.getProperty("docear.detailed").equals("true")) {
			System.out.println("["+Thread.currentThread().getName()+"] documentId: "+docId);
			System.out.println("["+Thread.currentThread().getName()+"] "+referenceData);
		}
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		return sendData(Main.DOCEAR_SERVICES + "/documents/" + docId	+ "/references/?format=xml", referenceData);		
	}
	
	public boolean uploadReferencesByHash(String docHash, String referenceData) {
		if (System.getProperty("docear.detailed") != null && System.getProperty("docear.detailed").equals("true")) {
			System.out.println("["+Thread.currentThread().getName()+"] docHash: "+docHash);
			System.out.println("["+Thread.currentThread().getName()+"] "+referenceData);
		}
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		
		return sendData(Main.DOCEAR_SERVICES + "/internal/document/"+docHash+"/references/?format=xml", referenceData);		
	}
	
	private boolean sendData(String target, String data) {
		if (data == null || data.trim().length() == 0) {
			return false;
		}
		
		Response response = null;
		try {
    		WebTarget webTarget = Main.client.target(target);
    		Invocation.Builder builder = webTarget.request(MediaType.MULTIPART_FORM_DATA_TYPE);
    		builder.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");
    		response = builder.post(Entity.entity(data, MediaType.MULTIPART_FORM_DATA_TYPE));
    		if (response.getStatus() != 200) {
				System.out.println("status " + response.getStatus() + ": "+target);
			}
			return true;
		}
		catch (Exception e) {
			System.out.println(e + " data: " + data + " -- Warning: this is not supposed to happen!");
			e.printStackTrace();
			return false;
		}
		finally {
			Main.tolerantClose(response);
		}
	}

}
