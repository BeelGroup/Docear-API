package org.docear.googleparser;

import java.net.URL;

public class WebSearchResult {
	private final String title;
	private final Integer year;
	private final URL link;
	private final URL bibTexLink;
	private final int searchRank;
	private final Integer cited;
	
	public WebSearchResult(String title, int rank, URL fulltext, URL bibtex, Integer year, Integer cited) {
		this.title = title;
		this.searchRank = rank;
		this.link = fulltext;
		this.bibTexLink = bibtex;
		this.year = year;
		this.cited = cited;
	}

	public String getTitle() {
		return title;
	}

	public Integer getYear() {
		return year;
	}

	public URL getLink() {
		return link;
	}

	public URL getBibTexLink() {
		return bibTexLink;
	}

	public int getRank() {
		return searchRank;
	}
	
	public String toString() {
		return "["+getRank()+";"+getYear()+";"+getTitle()+";"+getLink()+";"+getBibTexLink()+"]";
	}

	public Integer getCiteCount() {
		return cited;
	}

}
