package org.sciplore.resources;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;

/**
 * Resource class for citations.
 *
 * @author Mario Lipinski &lt;<a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>&gt;
 * @see Resource
 */
@Entity
@Table(name = "citations")
public class Citation extends Resource {
		
	@ManyToOne
	@JoinColumn(name = "cited_document_id", nullable = false)
	@Required
	private Document citedDocument;
	@ManyToOne
	@JoinColumn(name = "citing_document_id", nullable = false)
	@Required
	private Document citingDocument;
	private String context;
	private Integer countChapter;
	private Integer countCharacter;
	private Integer countParagraph;
	private Integer countSentence;

	private Integer countWord;
	
	
	
	public Citation(){}
	
	public Citation(Session s){
		this.setSession(s);
	}
	
	/**
	 * Returns the cited {@link Document}.
	 * 
	 * @return the cited {@link Document}
	 */
	public Document getCitedDocument() {
		return citedDocument;
	}
	
	/**
	 * Returns the cititing {@link Document}.
	 * 
	 * @return the citing {@link Document}
	 */
	public Document getCitingDocument() {
		return citingDocument;
	}
	
	/**
	 * Returns the context.
	 * 
	 * @return the context
	 */
	public String getContext() {
		return context;
	}
	
	/**
	 * Returns the chapter count.
	 * 
	 * @return the chapter count
	 */
	public Integer getCountChapter() {
		return countChapter;
	}
	
	/**
	 * Returns the character count.
	 * 
	 * @return the character count
	 */
	public Integer getCountCharacter() {
		return countCharacter;
	}
	
	/**
	 * Returns the paragraph count.
	 * 
	 * @return the paragraph count
	 */
	public Integer getCountParagraph() {
		return countParagraph;
	}
	
	/**
	 * Returns the sentence count.
	 * @return the sentence count
	 */
	public Integer getCountSentence() {
		return countSentence;
	}
	
	/**
	 * Returns the word count.
	 * 
	 * @return the word count
	 */
	public Integer getCountWord() {
		return countWord;
	}
	
	
	/**
	 * Sets the cited {@link Document}.
	 * 
	 * @param citedDocument the cited {@link Document}
	 */
	public void setCitedDocument(Document citedDocument) {
		this.citedDocument = citedDocument;
	}
	
	/**
	 * Sets the cititing {@link Document}.
	 * 
	 * @param citingDocument the citing {@link Document}
	 */
	public void setCitingDocument(Document citingDocument) {
		this.citingDocument = citingDocument;
	}
	
	/**
	 * Sets the context.
	 * 
	 * @param context the context
	 */
	public void setContext(String context) {
		this.context = context;
	}
	
	/**
	 * Sets the chapter count.
	 * 
	 * @param countChapter the chapter count
	 */
	public void setCountChapter(Integer countChapter) {
		this.countChapter = countChapter;
	}
	
	/**
	 * Sets the character count.
	 * 
	 * @param countCharacter the character count
	 */
	public void setCountCharacter(Integer countCharacter) {
		this.countCharacter = countCharacter;
	}
	
	/**
	 * Sets the paragraph count.
	 * 
	 * @param countParagraph the paragraph count
	 */
	public void setCountParagraph(Integer countParagraph) {
		this.countParagraph = countParagraph;
	}
	
	/**
	 * Sets the sentence count.
	 * 
	 * @param countSentence the sentence count
	 */
	public void setCountSentence(Integer countSentence) {
		this.countSentence = countSentence;
	}
	
	/**
	 * Sets the word count.
	 * 
	 * @param countWord the word count
	 */
	public void setCountWord(Integer countWord) {
		this.countWord = countWord;
	}
	
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		//a lot of papers have a lot of citations --> we can't do a select for every tuple
		//--> one select in the corresponding web service class: if citations for this document are already in the table, then discard all citations 
		return null;
	}
	
	/**
	 * Returns a matching Citation object from the database for an Citation
	 * object.
	 *
	 * @param c the Citation object
	 * @return the Citation object from the database or null if not found
	 */
	public  Citation getCitation(Citation c) {
		if(c.getId() != null) {
			return getCitation(c.getId());
		} else {
			return getCitation(c.getCitedDocument(), c.getCitingDocument(), 
					c.getCountChapter(), c.getCountCharacter(), c.getCountParagraph(),
					c.getCountSentence(), c.getCountWord());
		}
	}
	
	/**
	 * Returns a Citation object from the database for an identifier.
	 *
	 * @param id the identifier
	 * @return the Citation object or null if not found
	 */
	public  Citation getCitation(Integer id) {
		return (Citation) this.getSession().get(Citation.class, id);
	}
	
	/**
	 * Returns a Citation object from the database for a cited {@link Document}, a 
	 * citing {@link Document}, a chapter cound, a character count, a paragraph count, 
	 * a sentence count and a word count.
	 *
	 * @param cited the cited {@link Document}
	 * @param citing the citing {@link Document}
	 * @param chapter the chapter count
	 * @param character the character count
	 * @param paragraph the paragraph count
	 * @param sentence the sentence count
	 * @param word the word count
	 * @return the Citation object or null if not found
	 * @see Document
	 */
	public  Citation getCitation(Document cited, Document citing, 
			Integer chapter, Integer character, Integer paragraph, 
			Integer sentence, Integer word) {		
		if(cited != null && citing != null && cited.getId() != null && citing.getId() != null) {			
			return (Citation)this.getSession().createCriteria(Citation.class)
				.add(Restrictions.eq("citedDocument", cited))
				.add(Restrictions.eq("citingDocument", citing))
				.add(chapter != null ? Restrictions.eq("countChapter", chapter) : Restrictions.isNull("countChapter"))
				.add(character != null ? Restrictions.eq("countCharacter", character) : Restrictions.isNull("countCharacter"))
				.add(paragraph != null ? Restrictions.eq("countParagraph", paragraph) : Restrictions.isNull("countParagraph"))
				.add(sentence != null ? Restrictions.eq("countSentence", sentence) : Restrictions.isNull("countSentence"))
				.add(word != null ? Restrictions.eq("countWord", word) : Restrictions.isNull("countWord"))
				.setMaxResults(1)
				.uniqueResult();
		}
		return null;
	}
	
	/**
	 * Returns a list of all citations in database.
	 *
	 * @return all citations.
	 */
	@SuppressWarnings("unchecked")
	public  List<Citation> getCitations() {
		List<Citation> cs = (List<Citation>) this.getSession().createCriteria(Citation.class)
			.list();
		return cs;
	}
	
	/**
	 * Synchronizes a Citation object with a record from the database. If the 
	 * object does not exist, it is added to the database.
	 * In any case related objects are synchronized as well.
	 *
	 * @param cit the citation
	 * @return synchronized Citation which is stored in the database
	 
	public  Citation sync(Citation cit) {
		Citation c = this.getCitation(cit);
		if (c == null) {
			c = cit;
			if (!Tools.empty(c.getCitedDocument())) {
				c.setCitedDocument(Document.sync(c.getCitedDocument()));
			}
			if (!Tools.empty(c.getCitingDocument())) {
				c.setCitingDocument(Document.sync(c.getCitingDocument()));
			}
		} else {
			if(Tools.empty(c.getCitedDocument()) && !Tools.empty(cit.getCitedDocument())) {
				c.setCitedDocument(Document.sync(cit.getCitedDocument()));
			}
			if(Tools.empty(c.getCitingDocument()) && !Tools.empty(cit.getCitingDocument())) {
				c.setCitingDocument(Document.sync(cit.getCitingDocument()));
			}
			if(Tools.empty(c.getContext()) && !Tools.empty(c.getContext())) {
				c.setContext(cit.getContext());
			}
			if(Tools.empty(c.getCountChapter()) && !Tools.empty(c.getCountChapter())) {
				c.setCountChapter(cit.getCountChapter());
			}
			if(Tools.empty(c.getCountCharacter()) && !Tools.empty(c.getCountCharacter())) {
				c.setCountCharacter(cit.getCountCharacter());
			}
			if(Tools.empty(c.getCountParagraph()) && !Tools.empty(c.getCountParagraph())) {
				c.setCountParagraph(cit.getCountParagraph());
			}
			if(Tools.empty(c.getCountSentence()) && !Tools.empty(c.getCountSentence())) {
				c.setCountSentence(cit.getCountSentence());
			}
			if(Tools.empty(c.getCountWord()) && !Tools.empty(c.getCountWord())) {
				c.setCountWord(cit.getCountWord());
			}
			if(Tools.empty(c.getId()) && !Tools.empty(c.getId())) {
				c.setId(cit.getId());
			}
		}
		return c;
	}*/
	
}
