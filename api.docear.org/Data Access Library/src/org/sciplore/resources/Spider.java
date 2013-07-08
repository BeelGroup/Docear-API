package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.sciplore.eventhandler.Required;

@Entity
public class Spider extends BaseResource {
	public static Spider sync(Spider s) {
		// TODO
		return s;
	}
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Document document;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer documentId;
	@Column(nullable = false)
	private Short status;
	
	public Spider(){}
	
	public Spider(Session s){
		this.setSession(s);
	}
	
	/**
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}
	/**
	 * @return the document_id
	 */
	public Integer getDocumentId() {
		return documentId;
	}
	/**
	 * @return the status
	 */
	public Short getStatus() {
		return status;
	}
	/**
	 * @param document the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	/**
	 * @param documentId the document_id to set
	 */
	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(Short status) {
		this.status = status;
	}
}
