package org.docear;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class FileCacheEmailExtractionRunner extends Thread {
	private static int TIME_BETWEEN_REQUESTS = 2000;

	private File basedir;
	private FileCacheEmailExtractionRunner self;

	private File whiteList;

	public FileCacheEmailExtractionRunner(String path, String whiteListFile) {
		setName("FileCacheEmailExtractionRunner[" + hashCode() + "]");
		basedir = new File(path);
		whiteList = new File(whiteListFile);
		self = this;
	}

	public void run() {
		int i = 0;
		long lastRequest = System.currentTimeMillis();
		
		
		LinkedHashSet<File> fileList = new LinkedHashSet<File>();
		try {
			if(whiteList.exists()) {
				try {
					System.out.println("starting to read email whitelist");
					
					Scanner scanner = new Scanner(whiteList);
					while(scanner.hasNextLine()) {
						fileList.add(new File(basedir, scanner.nextLine()+".zip"));
					}
					scanner.close();
					
					System.out.println("finished reading email whitelist with "+fileList.size()+" items");
				}
				catch (Exception e) {
				}
			}
			else {
				System.out.println("no email whitelist found!");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}		
		Iterator<File> iter = fileList.iterator();	
		while (iter.hasNext()) {
			File file = iter.next();
				
			if (!file.exists() || file.isDirectory() || (file.isFile() && !file.getName().toLowerCase().endsWith(".zip"))) {
				System.out.println("["+self.getName()+"] skipping file: " + file);
				continue;
			}
			
			try {					
				final String hash = file.getName().substring(0, file.getName().length()-4);
					
				if ((++i) % 100 == 0) {
					System.out.println("["+self.getName()+"] files indexed so far: " + i);
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
					
				try {
					EmailExtractionWorker extWorker = new EmailExtractionWorker(file, hash);
					extWorker.exec();
					if (System.getProperty("docear.debug") == null || !System.getProperty("docear.debug").equals("true")) {
						iter.remove();
					}
				} catch (Exception e) {
					System.out.println("["+self.getName()+"] file: " + file.getAbsolutePath());
					e.printStackTrace();
				}
			}
			finally {				
				saveList(whiteList, fileList);
			}

			
			System.out.println("["+self.getName()+"] FileCacheEmailExtractionRunner file time: " + (System.currentTimeMillis()-lastRequest));
			
			try {
				Thread.sleep(TIME_BETWEEN_REQUESTS);
			} catch (InterruptedException e) {
			}
		}
	}

	private void saveList(File path, Collection<File> list) {
		try {
			PrintWriter writer = new PrintWriter(path);
			for (File file : list) {
				writer.println(file.getName());
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
		}
	}

}
