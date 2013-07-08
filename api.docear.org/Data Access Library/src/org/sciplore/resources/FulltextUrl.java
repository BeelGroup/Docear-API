package org.sciplore.resources;

import java.io.File;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;

@Entity
public class FulltextUrl extends Resource {
	public final static Short FULLTEXTURL_FILETYPE_PDF = 1;
	public final static Short FULLTEXTURL_FILETYPE_PS = 2;
	public final static Short FULLTEXTURL_FILETYPE_PPT = 3;
	public final static Short FULLTEXTURL_FILETYPE_WORD = 4;
	public final static Short FULLTEXTURL_FILETYPE_HTML = 5;

	public final static Short FULLTEXTURL_LICENCE_WEB = 1;
	
	public final static Short FULLTEXTURL_STATUS_NEW = 0;
	public final static Short FULLTEXTURL_STATUS_DOWNLOADING = 1;
	public final static Short FULLTEXTURL_STATUS_DOWNLOADED = 2;
	public final static Short FULLTEXTURL_STATUS_WRONG = 3;
	public final static Short FULLTEXTURL_STATUS_ANALYZING = 4;
	public final static Short FULLTEXTURL_STATUS_ERROR = 5;
	public final static Short FULLTEXTURL_STATUS_CORRECT = 100;
	
	public  List<FulltextUrl> getDownloaded() {
		return getDownloaded(null);
	}
	
	@SuppressWarnings("unchecked")
	public  List<FulltextUrl> getDownloaded(Short limit) {
		Criteria crit = this.getSession().createCriteria(FulltextUrl.class)
				.add(Restrictions.eq("status", FULLTEXTURL_STATUS_DOWNLOADED));
		if(limit != null) {
			crit.setMaxResults(limit);
		}
		return (List<FulltextUrl>)crit.list();
	}
	
	public Resource getPersistentIdentity() {
		return getFulltextUrl(this);
	}
	
	public  FulltextUrl getFulltextUrl(FulltextUrl f) {
		if(f.getId() != null) {
			f.load(f.getId());
			return f;
		} else  {
			return getFulltextUrl(f.getDocument(), f.getUrl());
		}
	}
	
	public  FulltextUrl getFulltextUrl(Integer id) {
		return (FulltextUrl) this.getSession().get(FulltextUrl.class, id);
	}
	
	public  FulltextUrl getFulltextUrl(Document d, String url) {
		if(d != null && d.getId() != null) {
			return (FulltextUrl)this.getSession().createCriteria(FulltextUrl.class)
				.add(Restrictions.eq("document", d))
				.add(Restrictions.eq("url", url))
				.setMaxResults(1)
				.uniqueResult();
		}
		return null;
	}
	
	public DocumentsPdfHash getDocumentsPdfHash() {
		return documentsPdfHash;
	}

	public void setDocumentsPdfHash(DocumentsPdfHash documentsPdfHash) {
		this.documentsPdfHash = documentsPdfHash;
	}
	
		
	@ManyToOne
	@JoinColumn(name = "document_id", nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Document document;
	@Column(nullable = false)
	private Short filetype;
	
	@Column(nullable = false)
	private Short licence;
	
	private Short status;
	
	@Column(nullable = false)
	private String url;
	
	@Column(nullable = false)
	private Short valid;
	
	@ManyToOne
	@JoinColumn(name = "documents_pdfhash_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private DocumentsPdfHash documentsPdfHash; 


	public FulltextUrl() {
	}
	
	public FulltextUrl(Session s) {
		this.setSession(s);
	}
	
	public FulltextUrl(Session s, Integer id, Integer documentId, Short filetype) {
		this.setSession(s);
		this.setId(id);
		this.filetype = filetype;
	}

	public FulltextUrl delete() throws ResourceException {
		this.getSession().delete(this);
		return this;	
	}

	public void deleteFile() {
		// FIXME: move path to config
		new File("/srv/sciplore/database/" + getFilePath()).delete();
	}

	/**
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}

	public String getFilePath() {
		String path = "";
		path += (int)((document.getId()%10000000000L)/1000000000);
		path += File.separator;
		path += (int)((document.getId()%1000000000)/100000000);
		path += File.separator;
		path += (int)((document.getId()%100000000)/10000000);
		path += File.separator;
		path += (int)((document.getId()%10000000)/1000000);
		path += File.separator;
		path += (int)((document.getId()%1000000)/100000);
		path += File.separator;
		path += (int)((document.getId()%100000)/10000);
		path += File.separator;
		path += (int)((document.getId()%10000)/1000);
		path += File.separator;
		path += (int)((document.getId()%1000)/100);
		path += File.separator;
		path += (int)((document.getId()%100)/10);
		path += File.separator;
		path += document.getId() % 10;
		path += File.separator;
		path += getId();
		switch (filetype) {
			case 1:
				path += ".pdf";
				break;
			default:
				break;
		}
		return path;
	}

	/**
	 * @return the filetype
	 */
	public Short getFiletype() {
		return filetype;
	}

	/**
	 * @return the licence
	 */
	public Short getLicence() {
		return licence;
	}

	/**
	 * @return the status
	 */
	public Short getStatus() {
		return status;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the valid
	 */
	public Short getValid() {
		return valid;
	}

	/**
	 * @param document the document to set
	 */
	public FulltextUrl setDocument(Document document) {
		this.document = document;
		return this;
	}

	/**
	 * @param filetype the filetype to set
	 */
	public FulltextUrl setFiletype(Short filetype) {
		this.filetype = filetype;
		return this;
	}

	/**
	 * @param licence the licence to set
	 */
	public FulltextUrl setLicence(Short licence) {
		this.licence = licence;
		return this;
	}

	/**
	 * @param status the status to set
	 */
	public FulltextUrl setStatus(Short status) {
		this.status = status;
		return this;
	}

	/**
	 * @param url the url to set
	 */
	public FulltextUrl setUrl(String url) {
		this.url = url;
		return this;
	}
	
	/**
	 * @param valid the valid to set
	 */
	public FulltextUrl setValid(Short valid) {
		this.valid = valid;
		return this;
	}

	public FulltextUrl updateStatus(Short status) {
		this.setStatus(status);
		save();
		return this;
	}
}
