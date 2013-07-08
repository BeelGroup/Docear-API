package org.sciplore.resources;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.sciplore.eventhandler.Required;
import org.sciplore.queries.DocumentsPdfHashQueries;

@Entity
@Table(name = "documents_pdfhash")
public class DocumentsPdfHash extends Resource {
	
	private String hash;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id")
	@Required
	private Document document;
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "documents_pdfhash_id")	
	private List<FulltextUrl> fulltextUrls = new ArrayList<FulltextUrl>();

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	@Override
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		return DocumentsPdfHashQueries.getPdfHash(getSession(), hash);
	}

	public List<FulltextUrl> getFulltextUrls() {
		return fulltextUrls;
	}

	public void addFulltextUrl(FulltextUrl fulltextUrl) {
		this.fulltextUrls.add(fulltextUrl);
	}

	
}
