package org.docear;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.RejectedExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NewZipTextWorker extends ReferenceUploadWorker implements Worker {
	
	
	private final File file;
	private final String docHash;

	public NewZipTextWorker(File textFile, String hash) {
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
			
			XtractTask task = new XtractTask(txt, null) {
				public void finishTask(String xmlText) {
					txt.delete();
					if(xmlText != null) {
						uploadReferencesByHash(docHash, xmlText);
					}
				}
			};			
			task.run();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
