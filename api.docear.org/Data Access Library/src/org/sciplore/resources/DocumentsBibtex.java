package org.sciplore.resources;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.sciplore.eventhandler.Required;
import org.sciplore.queries.DocumentsBibtexQueries;

@Entity
public class DocumentsBibtex extends Resource {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id")
	@Required
	private Document document;
	
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	private String bibtex;

	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (DocumentsBibtex)this.getSession().get(DocumentsBibtex.class, getId());
		}
		if (document == null || document.getId() == null) {
			return null;
		}		
		return DocumentsBibtexQueries.getDocumentsBibtex(this.getSession(), getDocument(), getBibtex());
		
	}

	public DocumentsBibtex() {
	}
	
	public DocumentsBibtex(Session s) {
		this.setSession(s);
	}
	
	
	public String getBibtex() {
		return bibtex;
	}

	public void setBibtex(String bibtex) {
		this.bibtex = bibtex;
	}

}
