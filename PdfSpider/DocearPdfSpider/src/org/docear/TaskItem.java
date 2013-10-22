package org.docear;

import java.net.MalformedURLException;
import java.net.URL;

public class TaskItem {
	private final int id;
	private final int document_id;
	private final URL url;
	
	public TaskItem(String id, String document_id, String url) throws MalformedURLException {
		this.id = Integer.parseInt(id);
		this.document_id = Integer.parseInt(document_id);
		this.url = new URL(url);
	}

	public int getId() {
		return id;
	}

	public int getDocumentId() {
		return document_id;
	}

	public URL getUrl() {
		return url;
	}
	
	public String toString() {
		return "TaskItem[id="+id+";documentId="+document_id+";url="+url+"]";
	}

}
