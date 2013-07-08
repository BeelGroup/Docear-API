package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.Bean;

@SciBeanElements({"title","year","doi","abstract","authors","fulltexts","xrefs", "comments", "references"})
public class Document extends Bean{
	
	private String id;
	private String href;
	private String hash;
	private String type;
	private Title title;
	private Bean doi;
	private Bean Abstract;
	private Authors authors;	
	private Bean fulltexts;	
	private Bean xrefs;
	private Bean comments;
	private Bean year;
	private Bean references;
	
	
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#setId(int)
	 */
	public void setId(String id) {
		this.id = id;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#getId()
	 */
	public String getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#setHref(java.lang.String)
	 */
	public void setHref(String href) {
		this.href = href;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#getHref()
	 */
	public String getHref() {
		return href;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#setHash(java.lang.String)
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#getHash()
	 */
	public String getHash() {
		return hash;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#setType(java.lang.String)
	 */
	public void setType(String type) {
		this.type = type;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#getType()
	 */
	public String getType() {
		return type;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#setTitle(org.sciplore.beans.Title)
	 */
	public void setTitle(Title title) {
		this.title = title;
	}
	/* (non-Javadoc)
	 * @see org.sciplore.beans.IDocument#getTitle()
	 */
	public Title getTitle() {
		return title;
	}
	
	public void setDoi(Bean doi) {
		this.doi = doi;
	}
	
	public Bean getDoi() {
		return doi;
	}
	
	public void setYear(Bean year) {
		this.year = year;
	}
	
	public Bean getYear() {
		return year;
	}
	
	/**
	 * @param authors the authors to set
	 */
	public void setAuthors(Authors authors) {
		this.authors = authors;
	}
	/**
	 * @return the authors
	 */
	public Authors getAuthors() {
		return authors;
	}
	public void setAbstract(Bean Abstract) {
		this.Abstract = Abstract;
	}
	public Bean getAbstract() {
		return Abstract;
	}
	public void setFulltexts(Bean fulltexts) {
		this.fulltexts = fulltexts;
	}
	public Bean getFulltexts() {
		return fulltexts;
	}
	public void setXrefs(Bean xrefs) {
		this.xrefs = xrefs;
	}
	public Bean getXrefs() {
		return xrefs;
	}
	public void setComments(Bean comments) {
		this.comments = comments;
	}
	public Bean getComments() {
		return comments;
	}
	public Bean getReferences() {
		return references;
	}
	public void setReferences(Bean references) {
		this.references = references;
	}
}
