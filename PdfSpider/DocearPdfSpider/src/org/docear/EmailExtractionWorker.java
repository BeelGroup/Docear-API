package org.docear;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.RejectedExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



public class EmailExtractionWorker extends ReferenceUploadWorker implements Worker {

	private final File file;
	private final String docHash;
	
	protected File tmpDir = new File(System.getProperty("java.io.tmpdir"));
	

	public EmailExtractionWorker(File textFile, String hash) {
		if(textFile == null || hash == null) {
			throw new NullPointerException();
		}
		this.file = textFile;
		this.docHash = hash;
	}

	public void run() {
		try {
			exec();
		}
		catch (Exception e) {
			System.out.println("["+Thread.currentThread().getName()+"] file: "+file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	@Override
	public void exec() throws IOException, RejectedExecutionException {
		System.out.println("["+Thread.currentThread().getName()+"] EmailExtractionWorker working on file: " + file.getAbsolutePath());
		
		ZipFile zipFile = new ZipFile(file);
		ZipEntry textEntry = null;
		Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
		while(entryEnum.hasMoreElements()) {
			textEntry = entryEnum.nextElement();
			if(textEntry.getName().endsWith(file.getName().replace("zip", "txt"))) {
				break;
			}
			else {
				textEntry = null;
			}
		}
								
		if (textEntry == null) {
			throw new RejectedExecutionException("skipping file (text==null): "+file.getAbsolutePath());
		}
		
		try {
			InputStream is = zipFile.getInputStream(textEntry);			
			try {
				Collection<String> emails = XtractTask.findEmailAddresses(XtractTask.loadPlainText(is));
				if(emails != null && !emails.isEmpty()) {
					uploadEmails(docHash, emails);
				}
			}
			finally {
				is.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected final boolean uploadEmails(String hash, Collection<String> emails) {		
		if (System.getProperty("docear.detailed") != null && System.getProperty("docear.detailed").equals("true")) {
			System.out.println("["+Thread.currentThread().getName()+"] hash: "+hash);
			System.out.println("["+Thread.currentThread().getName()+"] emails: "+emails);
		}
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		try {
			WebTarget webTarget = Main.client.target(Main.DOCEAR_SERVICES + "/internal/documents/" + hash	+ "/emails/");
    		Invocation.Builder builder = webTarget.request(MediaType.MULTIPART_FORM_DATA_TYPE);
    		builder.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");
    		Response response = builder.post(Entity.entity(emails, MediaType.MULTIPART_FORM_DATA_TYPE));
			
			if (response.getStatus() != 200) {
				System.out.println("["+Thread.currentThread().getName()+"] hash " + hash + ", HTTP status for emails upload: " + response.getStatus() + "| " + response.getEntity());
			}
			return true;

		}
		catch (Exception e) {
			System.out.println("["+Thread.currentThread().getName()+"] "+ e + "hash " + hash + ", Warning: this is not supposed to happen!");
			e.printStackTrace();
			return false;
		}
	}
}
