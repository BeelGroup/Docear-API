package org.sciplore.xml;


import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.sciplore.resources.Citation;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.Feedback;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.Keyword;
import org.sciplore.resources.Venue;

@XmlRootElement(name="document")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document", propOrder = {
    "title",
    "_abstract",
    "authors",
    "keywords",
    "fulltexts",
    "references",
    "citedBy",
    //"relatedDocuments",
    "doi",
    "venue",
    //"issn",
    //"isbn",
    //"language",
    //"series",
    "pages",
    "volume",
    //"edition",
    "number",
    "publishdate",
    "annotations",
    //"relatedness"*/
})

public class XmlDocument{	
    @XmlElement(required = true)
    protected XmlTitle title;
    @XmlElement(name = "abstract")
    protected XmlAbstract _abstract;
    protected XmlAuthors authors;
    protected XmlKeywords keywords;
    protected XmlFulltexts fulltexts;
    protected XmlReferences references;
    @XmlElement(name = "cited_by")
    protected XmlCitedBy citedBy;
    /*@XmlElement(name = "related_documents")
    protected RelatedDocumentsType relatedDocuments;*/
    protected XmlDoi doi;
    protected XmlVenue venue;
    //protected IssnType issn;
    //protected IsbnType isbn;*/
    //protected LanguageType language;
    //protected SeriesType series;
    protected XmlPages pages;
    protected XmlVolume volume;
    //protected EditionType edition;
    protected XmlNumber number;
    protected XmlPublishdate publishdate;
    protected XmlAnnotations annotations;
    //protected RelatednessType relatedness;
    @XmlAttribute(required = true)
    protected String type;
    @XmlAttribute(required = true)
    protected String hash;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedLong")
    protected Integer id;
    @XmlAttribute(required = true)
    protected String href;
    @XmlTransient
    private String baseUri;
    
    
    public XmlDocument(){}
    
    public XmlDocument(Document document, String baseUri){
    	this.baseUri = baseUri;
    	this.setHref(baseUri + ExternalizedStrings.getString("XmlDocument.href") + document.getId());
    	this.setId(document.getId());
    	this.setHash(document.getHash());
    	this.setType(document.getType());
    	this.setTitle(document.getTitle());
    	this.setAbstract(document.getAbstract());
    	this.setAuthors(document.getPersons());
    	this.setKeywords(document.getKeywords());
    	this.setFulltexts(document.getFulltextUrls());
    	this.setReferences(document.getCleanCitations());
    	this.setCitedBy(document.getRcvdCitations());
    	this.setDoi(document.getDoi());
    	this.setVenue(document);
    	this.setPages(document.getPages());
    	this.setVolume(document.getVolume());
    	this.setNumber(document.getNumber());
    	this.setPublishdate(document.getPublishedDay(), document.getPublishedMonth(), document.getPublishedYear());
    	this.setAnnotations(document.getFeedbacks());
    }
    
    public XmlDocument(Integer id, String baseUri){
    	this.baseUri = baseUri;
    	this.setHref(baseUri + ExternalizedStrings.getString("XmlDocument.href") + id);  
    	this.setId(id);
    }
    

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public XmlTitle getTitle() {		
		return title;
	}

	public void setTitle(String title) {
		this.title = new XmlTitle(this.getHref());
		this.title.setValue(title);
	}

	public XmlAbstract getAbstract() {
		return _abstract;
	}

	public void setAbstract(String _abstract) {
		this._abstract = new XmlAbstract(this.getHref());
		this._abstract.setId(getId());
		this._abstract.setValue(_abstract);
	}

	public XmlAuthors getAuthors() {
		return authors;
	}

	public void setAuthors(Set<DocumentPerson> authors) {
		this.authors = new XmlAuthors(this.getHref());
		DocumentPerson[] tempAuthors = new DocumentPerson[authors.size()];
		authors.toArray(tempAuthors);
		if(authors.size() > 1){
			boolean changed = false;
			do{
				changed = false;
				for(int i = 0; i < tempAuthors.length - 1; i++){
					if(tempAuthors[i].getRank() > tempAuthors[i+1].getRank()){
						DocumentPerson tempAuthor = tempAuthors[i];
						tempAuthors[i] = tempAuthors[i+1];
						tempAuthors[i+1] = tempAuthor;
						changed  = true;
						break;
					}					
				}
			}while(changed);
		}		
		for(DocumentPerson author : tempAuthors){			
			XmlAuthor xmlAuthor = new XmlAuthor(author.getPersonHomonym().getId(), this.baseUri);
			//xmlAuthor.setNameComplete(author.getPersonHomonym());
			this.authors.getAuthors().add(xmlAuthor);
		}
	}

	public XmlKeywords getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<Keyword> keywords) {
		this.keywords = new XmlKeywords(this.getHref());
		for(Keyword keyword : keywords){
			XmlKeyword xmlKeyword = new XmlKeyword();
			xmlKeyword.setKeywordtype(keyword.getType());
			xmlKeyword.setValue(keyword.getKeyword());
			this.keywords.getKeywords().add(xmlKeyword);
		}
		
	}

	public XmlFulltexts getFulltexts() {
		return fulltexts;
	}

	public void setFulltexts(Set<FulltextUrl> fulltexts) {
		this.fulltexts = new XmlFulltexts(this.getHref());
		for(FulltextUrl fulltext : fulltexts){
			XmlFulltext xmlFulltext = new XmlFulltext(fulltext.getId(), this.baseUri);
			xmlFulltext.setLicence(fulltext.getLicence());
			xmlFulltext.setValue(fulltext.getUrl());
			this.fulltexts.getFulltexts().add(xmlFulltext);
		}
	}

	public XmlReferences getReferences() {
		return references;
	}

	public void setReferences(Set<Citation> references) {
		this.references = new XmlReferences(this.getHref());
		for(Citation reference : references){
			Document citedDocument = reference.getCitedDocument();
			XmlDocument xmlDocument = new XmlDocument(citedDocument.getId(), this.baseUri);
			xmlDocument.setHash(citedDocument.getHash());
			xmlDocument.setType(citedDocument.getType());
			xmlDocument.setTitle(citedDocument.getTitle());
			this.references.getReferences().add(xmlDocument);
		}
	}

	public XmlCitedBy getCitedBy() {
		return citedBy;
	}

	public void setCitedBy(Set<Citation> citedBy) {
		this.citedBy = new XmlCitedBy(this.getHref());
		this.citedBy.setCitationcount(citedBy.size());
		for(Citation citation : citedBy){
			Document citingDocument = citation.getCitingDocument();
			XmlDocument xmlDocument = new XmlDocument(citingDocument.getId(), this.baseUri);
			xmlDocument.setHash(citingDocument.getHash());
			xmlDocument.setType(citingDocument.getType());
			xmlDocument.setTitle(citingDocument.getTitle());
			this.citedBy.getCitingDocuments().add(xmlDocument);
		}
	}

	public XmlVenue getVenue() {
		return venue;
	}

	public void setVenue(Document document) {
		Venue venue = document.getVenue();
		this.venue = new XmlVenue(this.getHref());
		if(venue != null){
			this.venue.setId(venue.getId());
			this.venue.setValue(venue.getName());
		}
		else if(document.getParent() != null){
			Document parent = document.getParent();
			this.venue.setId(parent.getId());
			this.venue.setValue(parent.getTitle());
		}
		
	}

	public XmlDoi getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = new XmlDoi(this.getHref());
		this.doi.setValue(doi);		
	}

	public XmlPages getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = new XmlPages(this.getHref());
		this.pages.setValue(pages);
	}

	public XmlVolume getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = new XmlVolume(this.getHref());
		this.volume.setValue(volume);
	}

	public XmlNumber getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = new XmlNumber(this.getHref());
		this.number.setValue(number);
	}

	public XmlPublishdate getPublishdate() {
		return publishdate;
	}

	public void setPublishdate(Short day, String month, Short year) {
		this.publishdate = new XmlPublishdate(this.getHref());
		this.publishdate.setDay("" + day);
		this.publishdate.setYear("" + year);
		this.publishdate.setMonth(month);
	}

	public XmlAnnotations getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Set<Feedback> annotations) {
		this.annotations = new XmlAnnotations(this.getHref(), this.baseUri);
		this.annotations.add(annotations);
	}
    
	public String getXML() throws JAXBException{
		StringWriter stringWriter = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(this.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(this, stringWriter);
		return stringWriter.getBuffer().toString();
	}
	
	public String getXHTML() throws JAXBException, TransformerException{
		
		InputStream xsltFile = XmlResource.class.getResourceAsStream("document.xsl");
		
        Source xmlSource = new StreamSource(new StringReader(this.getXML()));
        Source xsltSource = new StreamSource(xsltFile);

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);
        StringWriter stringwriter = new StringWriter();
        trans.transform(xmlSource, new StreamResult(stringwriter));
        return stringwriter.toString();
	}

}
