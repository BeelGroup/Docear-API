package org.docear;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;

public class PdfDownloadRunner extends Thread {
	private Main pdfSpider;
	protected File tmpDir = new File(System.getProperty("java.io.tmpdir"));

	public PdfDownloadRunner(Main spider) {
		super.setName("PdfDownloadRunner@" + hashCode());
		pdfSpider = spider;
	}

	public void run() {
		while (true) {
			TaskItem doc = null;

			doc = pdfSpider.nextDocument();

			if (doc != null) {

				synchronized (pdfSpider.getDocsInProcess()) {
					pdfSpider.getDocsInProcess().add(doc);
				}

				downloadDocuments(doc);

				synchronized (pdfSpider.getDocsInProcess()) {
					pdfSpider.getDocsInProcess().remove(doc);
				}

			}

			try {
				Thread.sleep(1000);
				// System.out.println("waiting for other threads to finish");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	private static URL redirectRecommendationLink(URL original) throws IOException, MalformedURLException {
		URL page = original;
		URLConnection connection;
		connection = original.openConnection();
		if (connection instanceof HttpURLConnection) {
			HttpURLConnection hconn = (HttpURLConnection) connection;
			hconn.setInstanceFollowRedirects(false);

			int response = hconn.getResponseCode();
			boolean redirect = (response >= 300 && response <= 399);

			/*
			 * In the case of a redirect, we want to actually change the URL that was input to the new, redirected URL
			 */
			if (redirect) {
				String loc = connection.getHeaderField("Location");
				if (loc.startsWith("http", 0)) {
					page = new URL(loc);
				} else {
					page = new URL(page, loc);
				}
			}
			// else {
			// if(response == 200) {
			// String content = getStringFromStream(connection.getInputStream(),
			// "UTF-8");
			// String searchPattern =
			// "<meta http-equiv=\"REFRESH\" content=\"0;url=";
			// int pos = content.indexOf(searchPattern);
			// if(pos >= 0) {
			// String loc = content.substring(pos+searchPattern.length());
			// loc = loc.substring(0,loc.indexOf("\""));
			// if (loc.startsWith("http", 0)) {
			// page = new URL(loc);
			// } else {
			// page = new URL(page, loc);
			// }
			// }
			// }
			// }
		}
		return page;
	}

	boolean isFinished = false;

	private void downloadDocuments(final TaskItem xref) {		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			final File tempFile =	File.createTempFile(String.valueOf(xref.getId()), ".pdf", tmpDir);
			//tempFile.createNewFile();	
			
			Future<?> future = executor.submit(new Runnable() {

				@Override
				public void run() {

					long timeOverAll = System.currentTimeMillis();
					try {
						// now attempt to download
						final URL pdfLocationWeb = xref.getUrl();
						//String fileName = URLString.substring(URLString.lastIndexOf("/") + 1);

						// use id to differentiate files of the same name
//						if (fileName.contains(".pdf#page=") | fileName.contains(".pdf?")) {
//							fileName = fileName.substring(0, fileName.lastIndexOf(".pdf")).concat(".pdf");
//
//						}

						updateXref(xref);

						final URL realUrl = redirectRecommendationLink(pdfLocationWeb);
						FileUtils.copyURLToFile(realUrl, tempFile, 10000, 30000);
						System.out.println("[" + Thread.currentThread().getName() + "] Time dl: " + (System.currentTimeMillis() - timeOverAll));

						PdfFileWorker pdfWorker = new PdfFileWorker(tempFile, xref.getDocumentId(), xref.getId(), realUrl);
						pdfWorker.exec();

					} catch (Throwable e) {
						System.err.println("org.docear.PdfDownloadRunner.downloadDocuments(...).new Runnable() {...}.run(): "+e.getMessage());
						System.out.println("[" + Thread.currentThread().getName() + "] xrefid: " + xref.getId() + ", " + e
								+ ". Skipping and set indexed=null.");
					} finally {
						tempFile.delete();
						System.out.println("[" + Thread.currentThread().getName() + "] Time overAll: " + (System.currentTimeMillis() - timeOverAll));
					}
				}
			});
			future.get(300, TimeUnit.SECONDS);				
		} 
		catch (Throwable e) {
			System.out.println("[" + Thread.currentThread().getName() + "] xrefid: " + xref.getId() + ", " + e + ". Skipping and set indexed=null.");
		}
		finally {
			try {
				executor.shutdownNow();
			} catch (Throwable t) {
			}
		}
		
	}

	private boolean updateXref(TaskItem xref) {
		if (System.getProperty("docear.debug") != null && System.getProperty("docear.debug").equals("true")) {
			return true;
		}
		
		Response response = null;
		try {
			WebTarget webTarget = Main.client.target(Main.DOCEAR_SERVICES + "/documents/" + String.valueOf(xref.getDocumentId()) + "/xrefs/" 
					+ String.valueOf(xref.getId()) + "/?dl_attempt=true");
			
			Invocation.Builder builder = webTarget.request(MediaType.MULTIPART_FORM_DATA_TYPE);
    		builder.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");

			response = builder.put(Entity.entity("", MediaType.MULTIPART_FORM_DATA));

			if (response.getStatus() != 200) {
				System.out.println("[" + Thread.currentThread().getName() + "] xrefid " + xref.getId() + ", HTTP status for database update: "
						+ response.getStatus());
			}
			return true;

		} 
		catch (Exception e) {
			System.out.println("[" + Thread.currentThread().getName() + "] " + e + "xrefid: " + xref.getId() + "Warning: this is not supposed to happen!");
			e.printStackTrace();
			return false;
		}
		finally {
			Main.tolerantClose(response);
		}
	}

}
