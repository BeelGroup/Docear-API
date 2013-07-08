package org.sciplore.data;


import java.text.SimpleDateFormat;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.sciplore.beans.Abstract;
import org.sciplore.beans.Author;
import org.sciplore.beans.Authors;
import org.sciplore.beans.Document;
import org.sciplore.beans.Documents;
import org.sciplore.beans.Fulltext;
import org.sciplore.beans.Fulltexts;
import org.sciplore.beans.Name_First;
import org.sciplore.beans.Name_Last;
import org.sciplore.beans.Name_Last_Prefix;
import org.sciplore.beans.Name_Last_Suffix;
import org.sciplore.beans.Name_Middle;
import org.sciplore.beans.Organization;
import org.sciplore.beans.ReleaseDate;
import org.sciplore.beans.SourceId;
import org.sciplore.beans.Title;
import org.sciplore.beans.Url;
import org.sciplore.beans.Xref;
import org.sciplore.beans.Xrefs;
import org.sciplore.formatter.Bean;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.Person;




public class DataWriter {
	
	private String baseURL;
	private UriInfo uriInfo;
	private String format;
	private String formatQueryParam;
	
	public DataWriter(UriInfo uriInfo, String format) {
		super();
		this.baseURL = uriInfo.getBaseUri().toString();
		this.uriInfo = uriInfo;
		this.setFormat(format);
		this.formatQueryParam = "/?format=" + format;
	}	

	
	
	public String getBaseURL() {
		return baseURL;
	}



	public void setBaseURL(String baseUrl) {
		this.baseURL = baseUrl;
	}
	
	

	public Documents getDocumentsBean(List<org.sciplore.resources.Document> documents, long totalAmount){
		Documents documentsBean = new Documents();		
		documentsBean.addActiveAttribute("href", this.baseURL + "documents/" + Tools.getQueryParamsAsString(this.uriInfo));
		documentsBean.addActiveAttribute("totalamount", "" + totalAmount);
		for(org.sciplore.resources.Document document : documents){
			documentsBean.add(this.getDocumentBean(document));
		}
		return documentsBean;
	}

	public Document getDocumentBean(org.sciplore.resources.Document document){
		Document documentBean = new Document();		
		documentBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + formatQueryParam);
		documentBean.addActiveAttribute("id", "" + document.getId());		
		documentBean.addActiveAttribute("hash", document.getHash());
		documentBean.addActiveAttribute("type", document.getType());
		documentBean.addActiveElement(this.getTitleBean(document));
		documentBean.addActiveElement(this.getAbstractBean(document));
		documentBean.addActiveElement(this.getAuthorsBean(document));
		documentBean.addActiveElement(this.getFulltextsBean(document));
		documentBean.addActiveElement(this.getXrefsBean(document));
		return documentBean;	
	}



	public Bean getXrefsBean(org.sciplore.resources.Document document) {
		Xrefs xrefsBean = new Xrefs();
		xrefsBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + "/xrefs" + formatQueryParam);
		
		for(DocumentXref xref : document.getXrefs()){
			xrefsBean.add(this.getXrefBean(xref));
		}
		return xrefsBean;
	}



	public Bean getXrefBean(DocumentXref xref) {
		Xref xrefBean = new Xref();
		xrefBean.addActiveAttribute("id", "" + xref.getId());
		xrefBean.addActiveAttribute("href", this.baseURL + "xref/" + xref.getId() + formatQueryParam);
		xrefBean.addActiveElement(this.getOrganizationBean(xref.getInstitution()));
		xrefBean.addActiveElement(this.getSourceIdBean(xref));
		xrefBean.addActiveElement(this.getSourceUrlBean(xref));
		xrefBean.addActiveElement(this.getReleaseDateBean(xref));
		return xrefBean;
	}



	private Bean getReleaseDateBean(DocumentXref xref) {
		ReleaseDate releaseDateBean = new ReleaseDate();
		if(xref != null && xref.getReleaseDate() != null){
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			releaseDateBean.setValue(dateFormatter.format(xref.getReleaseDate()));
		}
		return releaseDateBean;
	}



	public Bean getSourceUrlBean(DocumentXref xref) {
		Url urlBean = new Url();
		if(xref.getInstitution() != null && xref.getInstitution().getXrefBaseUrl() != null){
			urlBean.setValue(xref.getInstitution().getXrefBaseUrl() + xref.getSourcesId());
		}
		return urlBean;
	}



	public Bean getSourceIdBean(DocumentXref xref) {
		SourceId sourceIdBean = new SourceId();
		sourceIdBean.setValue(xref.getSourcesId());
		return sourceIdBean;
	}



	public Bean getOrganizationBean(org.sciplore.resources.Institution institution) {
		Organization organizationBean = new Organization();
		if(institution != null){
			organizationBean.addActiveAttribute("id", "" + institution.getId());
			organizationBean.addActiveAttribute("href", this.baseURL + "organization/" + institution.getId() + formatQueryParam);
		}
		return organizationBean;
	}



	public Bean getFulltextsBean(org.sciplore.resources.Document document) {
		Fulltexts fulltextsBean = new Fulltexts();
		fulltextsBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + "/fulltexts" + formatQueryParam);
		for(FulltextUrl fulltextUrl : document.getFulltextUrls()){
			fulltextsBean.add(this.getFulltextBean(fulltextUrl));
		}
		return fulltextsBean;
	}



	public Bean getFulltextBean(FulltextUrl fulltextUrl) {
		Fulltext fulltext = new Fulltext();
		fulltext.addActiveAttribute("id", "" + fulltextUrl.getId());
		fulltext.addActiveAttribute("href", this.baseURL + "fulltext/" + fulltextUrl.getId() + formatQueryParam);
		fulltext.addActiveAttribute("licence", "" + fulltextUrl.getLicence());
		fulltext.setValue(fulltextUrl.getUrl());		
		return fulltext;
	}



	public Bean getAbstractBean(org.sciplore.resources.Document document) {
		Abstract abstractBean = new Abstract();
		abstractBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + "/abstract" + formatQueryParam);
		abstractBean.addActiveAttribute("id", "" + document.getId());
		abstractBean.setValue(document.getAbstract());
		return abstractBean;
	}



	public Bean getAuthorsBean(org.sciplore.resources.Document document) {
		Authors authorsBean = new Authors();
		authorsBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL, document) + "/authors" + formatQueryParam);
//		for(DocumentPerson author : Tools.getSortedPersons(document.getPersons())){
//			authorsBean.add(this.getAuthorBean(author));
//		}
		for(DocumentPerson author : document.getPersons()){
			authorsBean.add(this.getAuthorBean(author));
		}
		return authorsBean;
	}
	
	public Bean getAuthorsBeanByLetter(List<Person> authors, String letter) {
		Authors authorsBean = new Authors();
		authorsBean.addActiveAttribute("href", this.baseURL + "authorindex/"+ letter + formatQueryParam);
		for (Person p : authors) {
			Author authorBean = new Author();
			authorBean.addActiveAttribute("href", this.baseURL + "author/" + p.getId() + formatQueryParam);
			authorBean.addActiveAttribute("id", "" + p.getId());
			// build name
			Name_First nameFirst = new Name_First();
			nameFirst.setValue(p.getNameFirst());
			Name_Middle nameMiddle = new Name_Middle();
			nameMiddle.setValue(p.getNameMiddle());
			Name_Last nameLast = new Name_Last();
			nameLast.setValue(p.getNameLast());
			Name_Last_Prefix nameLastPrefix = new Name_Last_Prefix();
			nameLast.setValue(p.getNameLast());
			Name_Last_Suffix nameLastSuffix = new Name_Last_Suffix();
			nameLast.setValue(p.getNameLast());
			authorBean.addActiveElement(nameFirst);
			authorBean.addActiveElement(nameMiddle);
			authorBean.addActiveElement(nameLast);
			authorBean.addActiveElement(nameLastPrefix);
			authorBean.addActiveElement(nameLastSuffix);
			// add author to list
			authorsBean.add(authorBean); 
		}		
		return authorsBean;
	}



	public Bean getAuthorBean(DocumentPerson author) {
		Author authorBean = new Author();
		authorBean.addActiveAttribute("href", this.baseURL + "author/" + author.getPersonMain().getId() + formatQueryParam);
		authorBean.addActiveAttribute("id", "" + author.getPersonMain().getId());
		Name_First nameFirst = new Name_First();
		nameFirst.setValue(author.getPersonMain().getNameFirst());
		Name_Middle nameMiddle = new Name_Middle();
		nameMiddle.setValue(author.getPersonMain().getNameMiddle());
		Name_Last nameLast = new Name_Last();
		nameLast.setValue(author.getPersonMain().getNameLast());
		Name_Last_Prefix nameLastPrefix = new Name_Last_Prefix();
		nameLast.setValue(author.getPersonMain().getNameLast());
		Name_Last_Suffix nameLastSuffix = new Name_Last_Suffix();
		nameLast.setValue(author.getPersonMain().getNameLast());
		authorBean.addActiveElement(nameFirst);
		authorBean.addActiveElement(nameMiddle);
		authorBean.addActiveElement(nameLast);
		authorBean.addActiveElement(nameLastPrefix);
		authorBean.addActiveElement(nameLastSuffix);
		return authorBean;
	}
	
	public Bean getDocumentsForAuthor(Person author, List<org.sciplore.resources.Document> documents) {
		Author authorBean = new Author();
		authorBean.addActiveAttribute("href", this.baseURL + "author/" + author.getId() + formatQueryParam);
		authorBean.addActiveAttribute("id", "" + author.getId());
		Name_First nameFirst = new Name_First();
		nameFirst.setValue(author.getNameFirst());
		Name_Middle nameMiddle = new Name_Middle();
		nameMiddle.setValue(author.getNameMiddle());
		Name_Last nameLast = new Name_Last();
		nameLast.setValue(author.getNameLast());
		Name_Last_Prefix nameLastPrefix = new Name_Last_Prefix();
		nameLast.setValue(author.getNameLast());
		Name_Last_Suffix nameLastSuffix = new Name_Last_Suffix();
		nameLast.setValue(author.getNameLast());
		authorBean.addActiveElement(nameFirst);
		authorBean.addActiveElement(nameMiddle);
		authorBean.addActiveElement(nameLast);
		authorBean.addActiveElement(nameLastPrefix);
		authorBean.addActiveElement(nameLastSuffix);
		Documents docs = this.getDocumentsBean(documents, documents.size());
		authorBean.setDocuments(docs);
		authorBean.addActiveElement(docs);
		return authorBean;
	}
	
	


	public Bean getTitleBean(org.sciplore.resources.Document document) {
		Title titleBean = new Title();
		titleBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL, document) + "/title" + formatQueryParam);
		titleBean.setValue(document.getTitle());
		return titleBean;
	}



	public void setFormat(String format) {
		this.format = format;
	}



	public String getFormat() {
		return format;
	}

}
