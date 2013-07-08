package org.sciplore.resources;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.sciplore.eventhandler.Required;
import org.sciplore.queries.XrefQueries;

@Entity
public class DocumentXref extends Resource {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id")
	@Required
	private Document document;
	
	private Date modified;
	private String source;
	private String sourcesId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "institution_id")
	@Required
	private Institution institution;
	private Date releaseDate;
	@OneToMany(mappedBy = "xref", fetch = FetchType.LAZY)
	private Set<DocumentXrefCategory> xrefCategories = new HashSet<DocumentXrefCategory>();
	
	@Column(nullable = false)
	private Integer dlAttempts;
	
	private Integer indexed;
	private Date lastAttempt;
	
	private Integer cite_count;
	private Integer rank;
	
	public Resource getPersistentIdentity() {
		return XrefQueries.getDocumentXref(getSession(), this);
	}

	public DocumentXref() {
	}
	
	public DocumentXref(Session s) {
		this.setSession(s);
	}
	
	public DocumentXref(Session s, String source, String sourcesId) {
		this.setSession(s);
		this.source = source;
		this.sourcesId = sourcesId;
	}
	
	/**
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * @return the modified
	 */
	public Date getModified() {
		return modified;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @return the sourcesId
	 */
	public String getSourcesId() {
		return sourcesId;
	}
	/**
	 * @param document the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/**
	 * @param modified the modified to set
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	/**
	 * @param sourcesId the sourcesId to set
	 */
	public void setSourcesId(String sourcesId) {
		this.sourcesId = sourcesId;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setDocumentXrefCategories(Set<DocumentXrefCategory> xrefCategories) {
		this.xrefCategories = xrefCategories;
	}

	public Set<DocumentXrefCategory> getDocumentXrefCategories() {
		return xrefCategories;
	}
	public Integer getIndexed() {
		return indexed;
	}

	public void setIndexed(Integer indexed) {
		this.indexed = indexed;
	}

	public Date getLastAttempt() {
		return lastAttempt;
	}

	public void setLastAttempt(Date lastAttempt) {
		this.lastAttempt = lastAttempt;
	}
	public int getDlAttempts() {
		return dlAttempts;
	}

	public void setDlAttempts(int dlAttempts) {
		this.dlAttempts = dlAttempts;
	}

	public Integer getCiteCount() {
		return cite_count;
	}

	public void setCiteCount(Integer citeCount) {
		this.cite_count = citeCount;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

}
