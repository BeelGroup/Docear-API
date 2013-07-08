package org.sciplore.resources;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.sciplore.eventhandler.Required;

@Entity
public class DocumentRelatedness extends Resource {
	
	public static DocumentRelatedness sync(DocumentRelatedness rel) {
		// TODO
		return rel;
	}
	
	
	
	@ManyToOne
	@JoinColumn(nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Document document1;
	@ManyToOne
	@JoinColumn(nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Document document2;
	private Double total;
	private Double textComplete;
	private Double textTitle;
	private Double textAbstract;
	private Double textRelatedWork;
	private Double textMethodology;
	private Double citationCocitation;
	private Double citationBibliographicCoupling;
	private Double citationItif;
	private Double citationCpa;
	private Double citationCoa;
	private Double mindmap;
	private Double user;
	
	public Resource getPersistentIdentity() {
		//TODO check for identity
		return this;
	}
	
	/**
	 * @return the document1
	 */
	public Document getDocument1() {
		return document1;
	}
	/**
	 * @param document1 the document1 to set
	 */
	public void setDocument1(Document document1) {
		this.document1 = document1;
	}
	/**
	 * @return the document2
	 */
	public Document getDocument2() {
		return document2;
	}
	/**
	 * @param document2 the document2 to set
	 */
	public void setDocument2(Document document2) {
		this.document2 = document2;
	}
	/**
	 * @return the total
	 */
	public Double getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(Double total) {
		this.total = total;
	}
	/**
	 * @return the textComplete
	 */
	public Double getTextComplete() {
		return textComplete;
	}
	/**
	 * @param textComplete the textComplete to set
	 */
	public void setTextComplete(Double textComplete) {
		this.textComplete = textComplete;
	}
	/**
	 * @return the textTitle
	 */
	public Double getTextTitle() {
		return textTitle;
	}
	/**
	 * @param textTitle the textTitle to set
	 */
	public void setTextTitle(Double textTitle) {
		this.textTitle = textTitle;
	}
	/**
	 * @return the textAbstract
	 */
	public Double getTextAbstract() {
		return textAbstract;
	}
	/**
	 * @param textAbstract the textAbstract to set
	 */
	public void setTextAbstract(Double textAbstract) {
		this.textAbstract = textAbstract;
	}
	/**
	 * @return the textRelatedWork
	 */
	public Double getTextRelatedWork() {
		return textRelatedWork;
	}
	/**
	 * @param textRelatedWork the textRelatedWork to set
	 */
	public void setTextRelatedWork(Double textRelatedWork) {
		this.textRelatedWork = textRelatedWork;
	}
	/**
	 * @return the textMethodology
	 */
	public Double getTextMethodology() {
		return textMethodology;
	}
	/**
	 * @param textMethodology the textMethodology to set
	 */
	public void setTextMethodology(Double textMethodology) {
		this.textMethodology = textMethodology;
	}
	/**
	 * @return the citationCocitation
	 */
	public Double getCitationCocitation() {
		return citationCocitation;
	}
	/**
	 * @param citationCocitation the citationCocitation to set
	 */
	public void setCitationCocitation(Double citationCocitation) {
		this.citationCocitation = citationCocitation;
	}
	/**
	 * @return the citationBibliographicCoupling
	 */
	public Double getCitationBibliographicCoupling() {
		return citationBibliographicCoupling;
	}
	/**
	 * @param citationBibliographicCoupling the citationBibliographicCoupling to set
	 */
	public void setCitationBibliographicCoupling(
			Double citationBibliographicCoupling) {
		this.citationBibliographicCoupling = citationBibliographicCoupling;
	}
	/**
	 * @return the citationItif
	 */
	public Double getCitationItif() {
		return citationItif;
	}
	/**
	 * @param citationItif the citationItif to set
	 */
	public void setCitationItif(Double citationItif) {
		this.citationItif = citationItif;
	}
	/**
	 * @return the citationCpa
	 */
	public Double getCitationCpa() {
		return citationCpa;
	}
	/**
	 * @param citationCpa the citationCpa to set
	 */
	public void setCitationCpa(Double citationCpa) {
		this.citationCpa = citationCpa;
	}
	/**
	 * @return the citationCoa
	 */
	public Double getCitationCoa() {
		return citationCoa;
	}
	/**
	 * @param citationCoa the citationCoa to set
	 */
	public void setCitationCoa(Double citationCoa) {
		this.citationCoa = citationCoa;
	}
	/**
	 * @return the mindmap
	 */
	public Double getMindmap() {
		return mindmap;
	}
	/**
	 * @param mindmap the mindmap to set
	 */
	public void setMindmap(Double mindmap) {
		this.mindmap = mindmap;
	}
	/**
	 * @return the user
	 */
	public Double getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(Double user) {
		this.user = user;
	}
}
