package org.docear;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

import org.docear.pdf.PdfDataExtractor;

public class PdfFileWorker extends FullTextUploadWorker implements Worker {
	
	private final File file;
	private final String documentId;
	private final String xRefId;
	private final URL url;

	public PdfFileWorker(File pdfFile, Integer documentId, Integer xRefId) {
		this(pdfFile, documentId, xRefId, null);
	}
	
	public PdfFileWorker(File pdfFile, Integer documentId, Integer xRefId, URL url) {
		if(pdfFile == null || documentId == null || xRefId == null) {
			throw new NullPointerException();
		}
		this.file = pdfFile;
		this.documentId = String.valueOf(documentId);
		this.xRefId = String.valueOf(xRefId);
		this.url = url;
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

	public void exec() throws IOException, RejectedExecutionException {
		PdfDataExtractor extractor = null;
		try {	
			extractor = new PdfDataExtractor(file);
		}
		catch(Exception e) {
			System.err.println("org.docear.PdfFileWorker.exec(): "+e.getMessage());
		}
		final String hash = extractor.getUniqueHashCode();
		if (hash == null) {
			throw new RejectedExecutionException("skipping file (hash==null): "+file.getAbsolutePath());
		}

		final String text = extractor.extractPlainText();
		
		if (text == null) {
			throw new RejectedExecutionException("skipping file (text==null): "+file.getAbsolutePath());							
		}
		
		try {
			final File txt = File.createTempFile(file.getName().replace(" ", "_").replace(".", "_"), ".txt", tmpDir);
			PrintStream ps = new PrintStream(new FileOutputStream(txt), false, "UTF-8");
			ps.print(text);
			ps.flush();
			ps.close();
			XtractTask task = new XtractTask(txt, null) {
				public void finishTask(String xmlText) {
					txt.delete();
					if(xmlText != null) {
						uploadReferences(documentId, xmlText);
					}
				}
			};			
			task.run();
		}
		catch(Exception e) {
			System.err.println("org.docear.PdfFileWorker.exec(): "+e.getMessage());
		}
		
		if(!uploadFullText(xRefId, documentId, text.getBytes(), (url==null?"offline":url.toString()), hash)) {
			throw new IOException("fulltext could not be uploaded.");
		}
	}

}
