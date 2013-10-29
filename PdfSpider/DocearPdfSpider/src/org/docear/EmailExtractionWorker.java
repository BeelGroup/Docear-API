package org.docear;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.RejectedExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.multipart.FormDataMultiPart;

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
		System.out.println("["+Thread.currentThread().getName()+"] ZipTextWorker working on file: " + file.getAbsolutePath());
		
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
			final File txt = File.createTempFile(file.getName().replace(" ", "_").replace(".", "_"), ".txt", tmpDir);
			
			OutputStream os = new FileOutputStream(txt);
			InputStream is = zipFile.getInputStream(textEntry);
			while(is.available() > 0) {
				byte[] buffer = new byte[is.available()];
				int length = is.read(buffer); 
				os.write(buffer,0,length);
			}
			os.flush();
			os.close();
			is.close();
			try {
				Collection<String> emails = XtractTask.findEmailAddresses(XtractTask.loadPlainText(txt));
				uploadEmails(docHash, emails);
			}
			finally {
				txt.delete();
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
			// construct data
			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
			formDataMultiPart.field("hash", hash);
			if(emails != null) {
				for (String email : emails) {
					formDataMultiPart.field("email", email);
				}			
			}
						
			WebResource webResource = client.resource(Main.DOCEAR_SERVICES + "/internal/documents/" + hash	+ "/emails/");
			Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333").type(MediaType.MULTIPART_FORM_DATA_TYPE);

			ClientResponse response = builder.post(ClientResponse.class, formDataMultiPart);

			if (response.getStatus() != 200) {
				System.out.println("["+Thread.currentThread().getName()+"] hash " + hash + ", HTTP status for emails upload: " + response.getStatus() + "| " + response.getEntity(String.class));
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
