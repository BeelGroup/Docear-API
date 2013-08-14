package org.docear;

import java.io.File;

public class FileCacheRunner extends Thread {
	private static int TIME_BETWEEN_REQUESTS = 2000;

	private File basedir;

	public FileCacheRunner(String path) {
		setName("FileCacheWorker[" + hashCode() + "]");
		basedir = new File(path);
	}

	public void run() {
		int i = 0;
		long lastRequest = System.currentTimeMillis();
		for (File dir : basedir.listFiles()) {
			if (dir.isDirectory()) {
				final String docId = dir.getName();
				for (File file : dir.listFiles()) {
					if ((++i) % 100 == 0) {
						System.out.println("["+Thread.currentThread().getName()+"] files indexed so far: " + i);
					}

					long timeSinceLastRequest = System.currentTimeMillis() - lastRequest;
					// make sure that at least 300ms are between each webservice request
					if (timeSinceLastRequest < TIME_BETWEEN_REQUESTS) {
						try {
							Thread.sleep(TIME_BETWEEN_REQUESTS - timeSinceLastRequest);
						} catch (InterruptedException e) {
						}
					}
					lastRequest = System.currentTimeMillis();
					
					if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf.zip")) {
						final String xrefId = file.getName().substring(0, file.getName().lastIndexOf(".pdf.zip"));
						try {
							ZipTextWorker txtWorker = new ZipTextWorker(file, Integer.parseInt(docId), Integer.parseInt(xrefId));
							txtWorker.exec();
							if (System.getProperty("docear.debug") == null || !System.getProperty("docear.debug").equals("true")) {
								file.renameTo(new File(file.getAbsolutePath() + ".cite"));
							}
						} catch (Exception e) {
							System.out.println("["+Thread.currentThread().getName()+"] file: " + file.getAbsolutePath());
							e.printStackTrace();
							file.renameTo(new File(file.getAbsolutePath() + ".err"));
						}

					} else if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
						final String xrefId = file.getName().substring(0, file.getName().lastIndexOf(".pdf"));
						try {
							System.out.println("["+Thread.currentThread().getName()+"] PdfFileWorker working on file: " + file.getAbsolutePath());
							
							PdfFileWorker pdfWorker = new PdfFileWorker(file, Integer.parseInt(docId), Integer.parseInt(xrefId));
							pdfWorker.exec();
							if (System.getProperty("docear.debug") == null || !System.getProperty("docear.debug").equals("true")) {
								file.delete();
							}							
						} catch (Exception e) {
							System.out.println("["+Thread.currentThread().getName()+"] file: " + file.getAbsolutePath());
							e.printStackTrace();
							file.renameTo(new File(file.getAbsolutePath() + ".err"));
						}
					}
					System.out.println("["+Thread.currentThread().getName()+"] FileCacheWorker file time: " + (System.currentTimeMillis()-lastRequest));
					
					try {
						Thread.sleep(TIME_BETWEEN_REQUESTS);
					} catch (InterruptedException e) {
					}
				}
			}

		}
	}

}
