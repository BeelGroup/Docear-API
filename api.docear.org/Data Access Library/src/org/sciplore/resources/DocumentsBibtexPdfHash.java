package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.sciplore.eventhandler.Required;
import org.sciplore.queries.DocumentsBibtexPdfHashQueries;

@Entity
@Table(name = "documents_bibtex_pdfhash")
public class DocumentsBibtexPdfHash extends Resource {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "documents_bibtex_id")
	@Required
	private DocumentsBibtex documentsBibtex;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "documents_pdfhash_id")
	@Required
	private DocumentsPdfHash documentsPdfHash;

	@Column(name = "commit_counter")
	private int commitCounter = 0;
	@Column(name = "reject_counter")
	private int rejectCounter = 0;
	
	public DocumentsBibtex getDocumentsBibtex() {
		return documentsBibtex;
	}

	public void setDocumentsBibtex(DocumentsBibtex documentsBibtex) {
		this.documentsBibtex = documentsBibtex;
	}

	public DocumentsPdfHash getDocumentsPdfHash() {
		return documentsPdfHash;
	}

	public void setDocumentsPdfHash(DocumentsPdfHash documentsPdfHash) {
		this.documentsPdfHash = documentsPdfHash;
	}

	public int getCommitCounter() {
		return commitCounter;
	}

	public void setCommitCounter(int commitCounter) {
		this.commitCounter = commitCounter;
	}

	public int getRejectCounter() {
		return rejectCounter;
	}

	public void setRejectCounter(int rejectCounter) {
		this.rejectCounter = rejectCounter;
	}

	@Override
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		return DocumentsBibtexPdfHashQueries.getDocumentsBibtexPdfHash(getSession(), getDocumentsBibtex().getId(), getDocumentsPdfHash().getId());
	}

	
}
