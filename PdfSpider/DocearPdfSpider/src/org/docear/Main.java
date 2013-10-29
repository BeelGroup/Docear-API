package org.docear;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sciplore.utilities.config.Config;
import org.sciplore.xtract.Xtract;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {
	
	private static int NUMBER_OF_THREADS = 2;
	private static int NUMBER_OF_FILES = 100;
	
	public static String DOCEAR_SERVICES = "https://api.docear.org";
	//public static String DOCEAR_SERVICES = "http://localhost:8080";

	private LinkedList<TaskItem> docs;
	private LinkedList<TaskItem> docsInProcess;

	public Main() {
		
	}

	public TaskItem nextDocument() {
		synchronized (docs) {
			if (!docs.isEmpty()) {
				TaskItem doc = docs.removeFirst();
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
		
		
		Main pdfSpider = new Main();
		pdfSpider.setDocsInProcess(new LinkedList<TaskItem>());
		pdfSpider.setDocs(new LinkedList<TaskItem>());

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
			else if(arg.startsWith("emailFileCachePath=")) {
				String[] argSplit = arg.split("=");
				arg = arg.replace(argSplit[0]+"=", "").trim();
				if(arg.length() > 0) {
					String whiteList = "./emailFileFilter.lst";
					FileCacheEmailExtractionRunner emailWorker = new FileCacheEmailExtractionRunner(arg, whiteList.toString()); 
					emailWorker.start();
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
				
				LinkedList<TaskItem> documents = retrieveDocuments(3);
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
	
	public static LinkedList<TaskItem> retrieveDocuments(Integer maxRank) throws IOException {
		String query = DOCEAR_SERVICES+"/documents/?pdf_url=true&fulltext_indexed=false&number=" + NUMBER_OF_FILES;
		
		query = DOCEAR_SERVICES+"/internal/xrefs/pdf_urls/?number=" + NUMBER_OF_FILES;
		
		if (maxRank != null) {
			query+="&max_rank="+maxRank;
		}
		URL url = new URL (query);		
		InputStream inputStream = url.openConnection().getInputStream();
		
		
		LinkedList<TaskItem> resource = parseDom(getXMLDocument(inputStream));
		if (resource == null) {
			return new LinkedList<TaskItem>();
		}
		
		return resource;
	}
	
	private static LinkedList<TaskItem> parseDom(Document xmlDocument) {
		LinkedList<TaskItem> items = new LinkedList<TaskItem>();
		if(xmlDocument != null) {
			NodeList xrefs = xmlDocument.getElementsByTagName("xref");
			for(int i=0; i<xrefs.getLength(); i++) {
				NamedNodeMap xref = xrefs.item(i).getAttributes();
				try {
					items.add(new TaskItem(xref.getNamedItem("id").getNodeValue(), xref.getNamedItem("document_id").getNodeValue(), xref.getNamedItem("url").getNodeValue()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (DOMException e) {
					e.printStackTrace();
				}
			}
		}
		return items;
	}

	public static Document getXMLDocument(InputStream inputStream) {
		
		// instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        return db.parse(inputStream);	        
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    } catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
		
	}
	

	public List<TaskItem> getDocs() {
		return docs;
	}

	public void setDocs(LinkedList<TaskItem> documents) {
		this.docs = documents;
	}

	public List<TaskItem> getDocsInProcess() {
		return docsInProcess;
	}

	public void setDocsInProcess(LinkedList<TaskItem> docsInProcess) {
		this.docsInProcess = docsInProcess;
	}
}