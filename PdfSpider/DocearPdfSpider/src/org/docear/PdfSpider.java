package org.docear;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.sciplore.deserialize.mapper.MrDlibXmlMapper;
import org.sciplore.deserialize.reader.XmlResourceReader;
import org.sciplore.resources.Document;
import org.sciplore.utilities.config.Config;
import org.sciplore.xtract.Xtract;

public class PdfSpider {
	
	private static int NUMBER_OF_THREADS = 2;
	private static int NUMBER_OF_FILES = 100;
	
	public static String DOCEAR_SERVICES = "https://api.docear.org";

	private LinkedList<Document> docs;
	private LinkedList<Document> docsInProcess;

	public PdfSpider() {
		
	}

	public Document nextDocument() {
		synchronized (docs) {
			if (!docs.isEmpty()) {
				Document doc = docs.removeFirst();
				return doc;
			} else {
				return null;
			}
		}

	}

	/**
	 * @param args
	 */
	@SuppressWarnings({ })
	public static void main(String[] args) {		
		int docsReturned = 10;
	
		Properties p = Config.getProperties("org.sciplore.xtract", Xtract.class);
		for(Entry<Object, Object> entry: p.entrySet()) {
			System.out.println(entry.getKey()+"="+entry.getValue());
		}
		
		
		PdfSpider pdfSpider = new PdfSpider();
		pdfSpider.setDocsInProcess(new LinkedList<Document>());
		pdfSpider.setDocs(new LinkedList<Document>());

		//initiate threads
		LinkedList<PdfDownloadRunner> downloaderThreads= new LinkedList<PdfDownloadRunner>();
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			PdfDownloadRunner r = new PdfDownloadRunner(pdfSpider);
			downloaderThreads.add(r);
			r.start();
		}
		
		for (String arg : args) {
			if(arg.startsWith("fileCachePath=")) {
				String[] argSplit = arg.split("=");
				arg = arg.replace(argSplit[0]+"=", "").trim();
				if(arg.length() > 0) {
					FileCacheRunner fileWorker = new FileCacheRunner(arg); 
					fileWorker.start();
				}
			} 
			else if(arg.startsWith("newFileCachePath=")) {
				String[] argSplit = arg.split("=");
				arg = arg.replace(argSplit[0]+"=", "").trim();
				if(arg.length() > 0) {
					NewFileCacheRunner fileWorker = new NewFileCacheRunner(arg); 
					fileWorker.start();
				}
			}
		}
		
		System.out.println("Threads initiated.");
		while (true) {
			try {
				if (docsReturned  == 0) {
					System.out.println("less than 1 doc returned, waiting before trying again.");
					Thread.sleep(5000); //wait before checking again when reached end of list
				}
				
				LinkedList<Document> documents = retrieveDocuments(3);
				if (documents.size() == 0) {
					documents = retrieveDocuments(20);
				}
				if (documents.size() == 0) {
					documents = retrieveDocuments(null);
				}
				
				pdfSpider.setDocs(documents);
				docsReturned = documents.size();
				System.out.println(docsReturned + " docs returned from Webservice");

				//a while loop checking every 100ms whether the list of docs have all been finished
				int counter = 0;
				System.out.println("Status update: " + " remaining docs in list = " + pdfSpider.docs.size() + " docs being processed = " + pdfSpider.docsInProcess.size()  ) ;
				
				while(pdfSpider.docsInProcess.size() > 0 || pdfSpider.docs.size() > 0) {
					counter++;
					if (counter > 200){

						System.out.println("Status update: " + " remaining docs in list = " + pdfSpider.docs.size() + " docs being processed = " + pdfSpider.docsInProcess.size()  ) ;
						counter = 0;	
					}
					Thread.sleep(100);
				}

			}  catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();				
			}
		}
	}
	
	public static LinkedList<Document> retrieveDocuments(Integer maxRank) throws IOException {
		String query = DOCEAR_SERVICES+"/documents/?pdf_url=true&fulltext_indexed=false&number=" + NUMBER_OF_FILES;
		if (maxRank != null) {
			query+="&max_rank="+maxRank;
		}
		URL url = new URL (query); //see org.sciplore.queries.DocumentResource for server side		
		InputStream inputStream = url.openConnection().getInputStream();

		//code below based on importer.Importer.uploadResource(java.util.LinkedList.class, inputStream, null);
		XmlResourceReader reader = new XmlResourceReader(MrDlibXmlMapper.getDefaultMapper());
		List<Document> resource = (List<Document>) reader.parse(inputStream);
		if (resource == null) {
			return new LinkedList<Document>();
		}
		LinkedList<Document> documents = new LinkedList<Document>();
		for (Document document : resource) {
			documents.add(document);
		}
		return documents;
	}

	public List<Document> getDocs() {
		return docs;
	}

	public void setDocs(LinkedList<Document> documents) {
		this.docs = documents;
	}

	public List<Document> getDocsInProcess() {
		return docsInProcess;
	}

	public void setDocsInProcess(LinkedList<Document> docsInProcess) {
		this.docsInProcess = docsInProcess;
	}
}