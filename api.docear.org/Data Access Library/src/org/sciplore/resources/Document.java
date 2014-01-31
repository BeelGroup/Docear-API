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
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.sciplore.queries.DocumentQueries;

/**
 * Resource class for documents.
 *
 * @author Mario Lipinski &lt;<a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>&gt;
 * @see Resource
 */
@Entity
@Table(name = "documents")

public class Document extends Resource {
	@OneToMany(mappedBy = "document")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private Set<Alert> alerts = new HashSet<Alert>();
	// referenced objects
	@OneToMany(mappedBy = "parent")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private Set<Document> children = new HashSet<Document>();
	@OneToMany(mappedBy = "citingDocument")
	private Set<Citation> citations = new HashSet<Citation>(); // Citations by this document
	@Column(nullable = false)
	private String cleantitle;
	@Column(name = "abstract")
	private String documentAbstract;
	private String doi;
	private Short edition;
	// TODO: fancy stuff for relatedness
	@OneToMany(mappedBy = "document")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private Set<Feedback> feedbacks = new HashSet<Feedback>();
	private Integer flags;
	
	@OneToMany(mappedBy = "document")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private Set<FulltextUrl> fulltextUrls = new HashSet<FulltextUrl>();
	
	@Column(nullable = false)
	private String hash = ""; // FIXME
	// database fields
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "institution_id")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private Institution institution;
	private String isbn;
	private String issn;	
	@OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private Set<Keyword> keywords = new HashSet<Keyword>();
	private String language;
	private String number;
	private String pages;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent")
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	private Document parent;	
	@OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
	@OrderBy("rank")
	private Set<DocumentPerson> persons = new HashSet<DocumentPerson>();
	private Short publishedDay;
	private String publishedMonth;
	private String publishedPlace;
	private Short publishedYear;
	private Date publishedDate;
	private String publisher;
	@OneToMany(mappedBy = "citedDocument")
	private Set<Citation> rcvdCitations = new HashSet<Citation>(); // Citations where this document has been cited
	private String series;
	@Column(nullable = false)
	private String title;
	private String type;
	@Column(nullable = false)
	private Short valid = 1;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "venue_id")
	private Venue venue;
	private String volume;
	
	@OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
	private Set<DocumentXref> xrefs = new HashSet<DocumentXref>();
	
	public Resource getPersistentIdentity() {
		return DocumentQueries.getDocument(getSession(), this);
	}
	@OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
	private Set<DocumentsBibtex> documentsBibtex = new HashSet<DocumentsBibtex>();

	/**
	 * Generate an empty Document.
	 */
	public Document() {
	}
		
	public Document(Session s) {
		this.setSession(s);
	}

	/**
	 * Generate a Document with identifier and title.
	 * 
	 * @param id the identifier
	 * @param title the title
	 */
	public Document(Session s, Integer id, String title) {
		this.setSession(s);
		setId(id);
		setTitle(title);
	}

	/**
	 * Generate a {@link Document} with an identifier.
	 * 
	 * @param id the identifier
	 */
	public Document(Session s, Integer id) {
		this.setSession(s);
		this.setId(id);
	}

	/**
	 * Generate a {@code Document} with title.
	 * 
	 * @param title the title
	 */
	public Document(Session s, String title) {
		this.setSession(s);
		setTitle(title);
	}	
	
	/**
	 * Add an {@link Alert} to the {@code Document}.
	 *
	 * @param a the alert
	 * @see Alert
	 */
	public void addAlert(Alert a) {
		alerts.add(a);
	}
	
	/**
	 * Add a child to the {@code Document}.
	 *
	 * @param c the child
	 */
	public void addChild(Document c) {
		children.add(c);
	}

	/**
	 * Add a {@link Citation} from this document.
	 *
	 * @param c the {@link Citation}
	 * @see Citation
	 */
	public void addCitation(Citation c) {
		citations.add(c);
	}

	/**
	 * Add a {@link Feedback} to the document. 
	 *
	 * @param f the {@link Feedback}
	 * @see Feedback
	 
	private void addFeedback(Feedback f) {
		feedbacks.add(f);
	}
	*/
	/**
	 * Add a fulltext element to the document.
	 *
	 * @param f the fulltext element.
	 * @see DocumentFulltext
	 
	private void addFulltext(DocumentFulltext f) {
		fulltexts.add(f);
	}
	*/
	/**
	 * Add a fulltext URL to the document.
	 *
	 * @param f the fulltext URL
	 * @see FulltextUrl
	 
	private void addFulltextUrl(FulltextUrl f) {
		fulltextUrls.add(f);
	}
	*/
	/**
	 * Add a {@link Keyword} to the document.
	 *
	 * @param k the {@link Keyword}
	 * @see Keyword
	 */
	public void addKeyword(Keyword k) {
		keywords.add(k);
	}

	/**
	 * Add a person to the document using a {@link DocumentPerson} object.
	 *
	 * @param p the {@link DocumentPerson} object
	 * @see DocumentPerson
	 */
	public void addPerson(DocumentPerson p) {
		persons.add(p);
	}

	/**
	 * Add a person to the document using a {@link Person} object to construct the 
	 * {@link DocumentPerson}.
	 *
	 * @param p the {@link Person} object
	 * @see Person
	 * @see DocumentPerson
	 */
	public void addPerson(PersonHomonym p) {
		persons.add(new DocumentPerson(this.getSession(), p));
	}
	
	/**
	 * Add a received {@link Citation} of this {@code Document}.
	 *
	 * @param c the received {@link Citation}
	 * @see Citation
	 */
	public void addRcvdCitation(Citation c) {
		rcvdCitations.add(c);
	}

	/**
	 * Add a {@link DocumentXref cross reference} to the {@code Document}.
	 *
	 * @param x the cross reference
	 * @see DocumentXref
	 */
	public void addXref(DocumentXref x) {
		x.setDocument(this);
		xrefs.add(x);
	}

	/**
	 * Returns the abstract
	 *
	 * @return the abstract
	 */
	public String getAbstract() {
		return documentAbstract;
	}

	/**
	 * Returns the {@link Alert}s
	 * 
	 * @return the {@link Alert}s
	 * @see Alert
	 */
	public Set<Alert> getAlerts() {
		return alerts;
	}

	/**
	 * Returns the children.
	 * 
	 * @return the children
	 */
	public Set<Document> getChildren() {
		return children;
	}

	/**
	 * Returns the {@link Citation}s by this {@code Document}.
	 * 
	 * @return the {@link Citation}s
	 * @see Citation
	 */
	public Set<Citation> getCitations() {
		return citations;
	}
	
	/**
	 * Returns the {@link Citation}s by this {@code Document} without double Entries.
	 * 
	 * @return the {@link Citation}s
	 * @see Citation
	 */
	public Set<Citation> getCleanCitations() {
		Set<Citation> cleanCitations = new HashSet<Citation>();
		boolean isDuplicate = false;
		for(Citation citation : citations){
			for(Citation cleanCitation : cleanCitations){
				if(citation.getCitedDocument().getId() == cleanCitation.getCitedDocument().getId()){
					isDuplicate = true;
					break;
				}
			}
			if(!isDuplicate) cleanCitations.add(citation);	
			isDuplicate = false;
		}
		return cleanCitations;
	}

	/**
	 * Returns the clean title.
	 * 
	 * @return the cleantitle
	 */
	public String getCleantitle() {
		return cleantitle;
	}

	/**
	 * Returns the digital object identifier.
	 * 
	 * @return the digital object identifier
	 */
	public String getDoi() {
		return doi;
	}
	
	/**
	 * Returns the edition.
	 * 
	 * @return the edition
	 */
	public Short getEdition() {
		return edition;
	}

	/**
	 * Returns the feedbacks.
	 * 
	 * @return the feedbacks
	 * @see Feedback
	 */
	public Set<Feedback> getFeedbacks() {
		return feedbacks;
	}

	/**
	 * Returns the flags.
	 * 
	 * @return the flags
	 */
	public Integer getFlags() {
		return flags;
	}

	/**
	 * Returns the fulltext URLs.
	 * 
	 * @return the fulltext URLs
	 * @see FulltextUrl
	 */
	public Set<FulltextUrl> getFulltextUrls() {
		return fulltextUrls;
	}

	/**
	 * Returns the hash.
	 * 
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	

	/**
	 * Returns the institution.
	 * 
	 * @return the institution
	 * @see Institution
	 */
	public Institution getInstitution() {
		return institution;
	}

	/**
	 * Returns the ISBN.
	 * 
	 * @return the ISBN
	 */
	public String getIsbn() {
		return isbn;
	}

	/**
	 * Returns the ISSN.
	 * @return the ISSN
	 */
	public String getIssn() {
		return issn;
	}

	/**
	 * Returns the keywords.
	 * 
	 * @return the keywords
	 */
	public Set<Keyword> getKeywords() {
		return keywords;
	}

	/**
	 * Returns the langugae.
	 * 
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Returns the number of the collection this document was published in.
	 * 
	 * @return the number of the collection
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Returns the pages this document was published at in the collection.
	 * @return the pages
	 */
	public String getPages() {
		return pages;
	}

	/**
	 * Returns the parent document.
	 * 
	 * @return the parent document
	 */
	public Document getParent() {
		return parent;
	}

	/**
	 * Returns the persons.
	 * 
	 * @return the persons
	 * @see DocumentPerson
	 */
	public Set<DocumentPerson> getPersons() {
		return persons;
	}

	/**
	 * Returns the day of publication.
	 * 
	 * @return the day of publication
	 */
	public Short getPublishedDay() {
		return publishedDay;
	}

	/**
	 * Returns the month of publication.
	 * 
	 * @return the month of publication
	 */
	public String getPublishedMonth() {
		return publishedMonth;
	}

	/**
	 * Returns the place of publication.
	 * 
	 * @return the place of publication
	 */
	public String getPublishedPlace() {
		return publishedPlace;
	}

	/**
	 * Returns the year of publication.
	 * 
	 * @return the year of publication
	 */
	public Short getPublishedYear() {
		return publishedYear;
	}

	/**
	 * Returns the publisher.
	 * 
	 * @return the publisher
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * Returns the citations this document received.
	 * 
	 * @return the received citation.
	 * @see Citation
	 */
	public Set<Citation> getRcvdCitations() {
		return rcvdCitations;
	}

	/**
	 * Returns the series of the collection this document was published in.
	 * 
	 * @return the series of the collection
	 */
	public String getSeries() {
		return series;
	}

	/**
	 * Returns the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the type. The type usually is one of the following:
	 * <ul>
	 * 	<li>article</li>
	 *  <li>book</li>
	 *  <li>incollection</li>
	 *  <li>inproceedings</li>
	 *  <li>mastersthesis</li>
	 *  <li>phdthesis</li>
	 *  <li>proceedings</li>
	 *  <li>www</li>
	 * </ul>
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns information about the validity of the record.
	 * @return the validity
	 */
	public Short getValid() {
		return valid;
	}

	/**
	 * Returns the venue.
	 * 
	 * @return the venue
	 * @see Venue
	 */
	public Venue getVenue() {
		return venue;
	}

	/**
	 * Returns the volume of the collection this document was published in.
	 * 
	 * @return the volume of the collection
	 */
	public String getVolume() {
		return volume;
	}	

	/**
	 * Returns the cross references.
	 * 
	 * @return the cross references
	 * @see DocumentXref
	 */
	public Set<DocumentXref> getXrefs() {
		return xrefs;
	}

	/**
	 * Sets the abstract. 
	 *
	 * @param documentAbstract the abstract
	 */
	public void setAbstract(String documentAbstract) {
		this.documentAbstract = documentAbstract;
	}

	/**
	 * Sets the alerts.
	 * 
	 * @param alerts the alerts
	 * @see Alert
	 */
	public void setAlerts(Set<Alert> alerts) {
		this.alerts = alerts;
	}

	/**
	 * Sets the children.
	 * 
	 * @param children the children
	 */
	public void setChildren(Set<Document> children) {
		this.children = children;
	}

	/**
	 * Sets the citations by this document.
	 * 
	 * @param citations the citations by this document
	 * @see Citation
	 */
	public void setCitations(Set<Citation> citations) {
		this.citations = citations;
	}

	/**
	 * Sets the clean title.
	 * 
	 * @param cleantitle the cleantitle
	 */
	public void setCleantitle(String cleantitle) {
		this.cleantitle = cleantitle.substring(0, Math.min(cleantitle.length(), 1024));		
	}

	/**
	 * Sets the digital object identifier.
	 * 
	 * @param doi the digital object identifier
	 */
	public void setDoi(String doi) {
		this.doi = doi;
	}

	/**
	 * Sets the edition of the collection this document was published in.
	 * 
	 * @param edition the edition of the collection
	 */
	public void setEdition(Short edition) {
		this.edition = edition;
	}

	/**
	 * Sets the feedbacks.
	 * 
	 * @param feedbacks the feedbacks
	 * @see Feedback
	 */
	public void setFeedbacks(Set<Feedback> feedbacks) {
		this.feedbacks = feedbacks;
	}
	
	public void addFeedback(Feedback feedback) {
		this.feedbacks.add(feedback);		
	}
	
	/**
	 * Sets the flags.
	 * 
	 * @param flags the flags
	 */
	public void setFlags(Integer flags) {
		this.flags = flags;
	}

	/**
	 * Sets the fulltext URLs.
	 * 
	 * @param fulltextUrls the fulltext URLs
	 * @see FulltextUrl
	 */
	public void setFulltextUrls(Set<FulltextUrl> fulltextUrls) {
		this.fulltextUrls = fulltextUrls;
	}
	
	/**
	 * adds a fulltext URL.
	 * 
	 * @param fulltextUrl the fulltext URL object
	 * @see FulltextUrl
	 */
	public void addFulltextUrl(FulltextUrl fulltextUrl) {
		this.fulltextUrls.add(fulltextUrl);
	}

	/**
	 * Sets the hash.
	 * 
	 * @param hash the hash
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}
	

	/**
	 * Sets the institution.
	 * 
	 * @param institution the institution
	 * @see Institution
	 */
	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	/**
	 * Sets the ISBN.
	 * 
	 * @param isbn the isbn
	 */
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	/**
	 * Sets the ISSN.
	 * 
	 * @param issn the issn
	 */
	public void setIssn(String issn) {
		this.issn = issn;
	}
	
	/**
	 * Sets the keywords.
	 * 
	 * @param keywords the keywords
	 * @see Keyword
	 */
	public void setKeywords(Set<Keyword> keywords) {
		this.keywords = keywords;
	}

	/**
	 * Sets the language.
	 * 
	 * @param language the language
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Sets the number of the collection this document was published in.
	 * 
	 * @param number the number of the collection
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Sets the pages where this document has been published in the collection.
	 * 
	 * @param pages the pages in the collection
	 */
	public void setPages(String pages) {
		this.pages = pages;
	}

	/**
	 * Sets the parent document.
	 * 
	 * @param parent the parent document
	 */
	public void setParent(Document parent) {
		this.parent = parent;
	}

	/**
	 * Sets the persons.
	 * 
	 * @param persons the persons
	 * @see DocumentPerson
	 */
	public void setPersons(Set<DocumentPerson> persons) {
		this.persons = persons;
	}

	/**
	 * Sets the day of publication.
	 * 
	 * @param publishedDay the day of publication
	 */
	public void setPublishedDay(Short publishedDay) {
		this.publishedDay = publishedDay;
	}

	/**
	 * Sets the month of publication.
	 * 
	 * @param publishedMonth the month of publication
	 */
	public void setPublishedMonth(String publishedMonth) {
		this.publishedMonth = publishedMonth;
	}

	/**
	 * Sets the place of publication.
	 * 
	 * @param publishedPlace the place of publication
	 */
	public void setPublishedPlace(String publishedPlace) {
		this.publishedPlace = publishedPlace;
	}
	
	/**
	 * Sets the year of publication.
	 * 
	 * @param publishedYear the year of publication
	 */
	public void setPublishedYear(Short publishedYear) {
		this.publishedYear = publishedYear;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	/**
	 * Sets the publisher.
	 * 
	 * @param publisher the publisher
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * Sets the citations received by this document.
	 * 
	 * @param rcvdCitations the received citations
	 * @see Citation
	 */
	public void setRcvdCitations(Set<Citation> rcvdCitations) {
		this.rcvdCitations = rcvdCitations;
	}

	/**
	 * Sets the series of the collection where this document has been published.
	 * 
	 * @param series the series of the collection
	 */
	public void setSeries(String series) {
		this.series = series;
	}
	
	/**
	 * Sets the title. This also updates the clean title.
	 * 
	 * @param title the title
	 */
	public void setTitle(String title) {
		this.title = title.substring(0, Math.min(title.length(), 2047));
		setCleantitle(DocumentQueries.generateCleanTitle(title));
	}

	/**
	 * Sets the type. The type usually is one of the following:
	 * <ul>
	 * 	<li>article</li>
	 *  <li>book</li>
	 *  <li>incollection</li>
	 *  <li>inproceedings</li>
	 *  <li>mastersthesis</li>
	 *  <li>phdthesis</li>
	 *  <li>proceedings</li>
	 *  <li>www</li>
	 * </ul>
	 * 
	 * @param type the type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Sets information about the validity of the record.
	 * 
	 * @param valid the validity
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}

	/**
	 * Sets the venue.
	 * 
	 * @param venue the venue
	 * @see Venue
	 */
	public void setVenue(Venue venue) {
		this.venue = venue;
	}
	
	/**
	 * Sets the volume of the collection this document has been published in.
	 * 
	 * @param volume the volume of the collection
	 */
	public void setVolume(String volume) {
		this.volume = volume;
	}
	
	/**
	 * Sets the cross references.
	 * 
	 * @param xrefs the cross references.
	 * @see DocumentXref
	 */
	public void setXrefs(Set<DocumentXref> xrefs) {
		this.xrefs = xrefs;
	}

	/**
	 * Returns a string representation of this document. Currently this is only 
	 * the title.
	 * 
	 * @return string representation of this document
	 */
	public String toString() {
		return title;
	}

	public Set<DocumentsBibtex> getDocumentsBibtex() {
		return documentsBibtex;
	}

	public void setDocumentsBibtex(Set<DocumentsBibtex> documentsBibtex) {
		this.documentsBibtex = documentsBibtex;
	}



	
}
