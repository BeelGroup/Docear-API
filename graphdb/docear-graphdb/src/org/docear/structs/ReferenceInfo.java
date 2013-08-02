package org.docear.structs;

/**
 * This class holds information about a reference that is linked with a mindmap node.
 * @author gkapi
 */

public class ReferenceInfo {
	
	private String title;
	private String journal;
	private String authors;
	private Integer year;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getJournal() {
		return journal;
	}
	
	public void setJournal(String journal) {
		this.journal = journal;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

}
