package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;
import org.sciplore.tools.Tools;

@Entity
public class DocumentXrefCategory extends Resource {
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_xref_id")
	@Required /*FIXME: replace with other annotation like CheckDependency to resolve fields that are only probably set*/
	private DocumentXref xref;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source")
	@Required
	private Institution source;
	
	@Column(nullable = false)
	private Short type;
	
	private String category;

	public DocumentXrefCategory() {
	}
	
	public DocumentXrefCategory(Session session) {
		this.setSession(session);
	}
	
	public DocumentXref getXref() {
		return xref;
	}

	public void setXref(DocumentXref xref) {
		this.xref = xref;
	}

	public Institution getSource() {
		return source;
	}

	public void setSource(Institution source) {
		this.source = source;
	}

	public Short getType() {
		return type;
	}

	public void setType(Short type) {
		this.type = type;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public Resource getPersistentIdentity() {
		return getDocumentXrefCategory(this);
	}
	
	public DocumentXrefCategory getDocumentXrefCategory(DocumentXrefCategory parsedDocXrefCat) {
		if(parsedDocXrefCat.getId() != null) {
			parsedDocXrefCat.load();
			return (DocumentXrefCategory) getSession().load(DocumentXrefCategory.class, parsedDocXrefCat.getId());
		} else {
			if(!Tools.empty(parsedDocXrefCat.getCategory())) {
				return getDocumentXrefCategory(parsedDocXrefCat.getCategory(), parsedDocXrefCat.getXref(), parsedDocXrefCat.getSource());
			}
		}
		return null;
	}

	private DocumentXrefCategory getDocumentXrefCategory(String category, DocumentXref xref, Institution source) {
		// TODO check this criteria for correctness
		if(xref == null || xref.getId() == null || source == null || source.getId() == null) {
			return null;
		}
		return (DocumentXrefCategory) this.getSession().createCriteria(DocumentXrefCategory.class)
			.add(Restrictions.like("category", category))
			.add(Restrictions.eq("xref", xref))
			.add(Restrictions.eq("source", source))
			.setMaxResults(1)
			.uniqueResult();
	}
}
