package org.sciplore.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.beans.Abstract;
import org.sciplore.beans.Application;
import org.sciplore.beans.ApplicationVersion;
import org.sciplore.beans.ApplicationVersions;
import org.sciplore.beans.Applications;
import org.sciplore.beans.Author;
import org.sciplore.beans.Authors;
import org.sciplore.beans.Authorxref;
import org.sciplore.beans.Authorxrefs;
import org.sciplore.beans.Birthday;
import org.sciplore.beans.Categories;
import org.sciplore.beans.Category;
import org.sciplore.beans.Comment;
import org.sciplore.beans.Comments;
import org.sciplore.beans.Deathday;
import org.sciplore.beans.Document;
import org.sciplore.beans.Documents;
import org.sciplore.beans.Doi;
import org.sciplore.beans.Fulltext;
import org.sciplore.beans.Fulltexts;
import org.sciplore.beans.Homonym;
import org.sciplore.beans.Homonyms;
import org.sciplore.beans.Id;
import org.sciplore.beans.Name;
import org.sciplore.beans.Name_First;
import org.sciplore.beans.Name_Last;
import org.sciplore.beans.Name_Last_Prefix;
import org.sciplore.beans.Name_Last_Suffix;
import org.sciplore.beans.Name_Middle;
import org.sciplore.beans.Occurence;
import org.sciplore.beans.Occurences;
import org.sciplore.beans.Organization;
import org.sciplore.beans.Photo;
import org.sciplore.beans.Reference;
import org.sciplore.beans.References;
import org.sciplore.beans.ReleaseDate;
import org.sciplore.beans.SourceId;
import org.sciplore.beans.Title;
import org.sciplore.beans.Url;
import org.sciplore.beans.Xref;
import org.sciplore.beans.Xrefs;
import org.sciplore.beans.Year;
import org.sciplore.formatter.Bean;
import org.sciplore.resources.Citation;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentXrefCategory;
import org.sciplore.resources.Feedback;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.Person;
import org.sciplore.resources.PersonHomonym;
import org.sciplore.resources.PersonXref;

public class BeanFactory {
	private ArrayList<String> showList;
	private ArrayList<String> hideList;
	private String baseURL;
	private String formatQueryParam;
	private UriInfo uriInfo;
	private boolean hideInUse = false;
	private boolean showInUse = false;
	private boolean normalizeTitle = false;

	public BeanFactory(UriInfo uriInfo, HttpServletRequest request) {
		this.uriInfo = uriInfo;
		this.baseURL = uriInfo.getBaseUri().toString();
		if (request.getHeader("referer") != null) {
			URI remoteUri = URI.create(request.getHeader("referer"));
			if (rewriteAllowed(remoteUri)) {
				this.baseURL = Tools.getRequestBase(remoteUri);
			}
		}
		String format = "";
		if (uriInfo.getQueryParameters().get("format") != null && uriInfo.getQueryParameters().get("format").size() >= 1) {
			format = uriInfo.getQueryParameters().get("format").get(0);
		}
		this.formatQueryParam = "/?format=" + format;

		parseVisibilityParams(uriInfo);
	}

	private void parseVisibilityParams(UriInfo uriInfo) {
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		if (params.containsKey("hide")) {
			hideList = new ArrayList<String>();
			List<String> values = params.get("hide");
			String[] tokens = values.get(0).split(",");
			for (String hide : tokens) {
				hideList.add(hide);
			}
		}
		else if (params.containsKey("show")) {
			showList = new ArrayList<String>();
			List<String> values = params.get("show");
			String[] tokens = values.get(0).split(",");
			for (String hide : tokens) {
				showList.add(hide);
			}
		}
	}

	private boolean rewriteAllowed(URI requestUri) {
		// TODO: remove in final version - its just for presentation purpose
		if (requestUri.getAuthority().contains("pas.sciplore.org:9080")) return true;

		System.out.print("url-base rewrite for <" + requestUri.getAuthority() + "> ");
		if (Tools.getAllowedURLRequestors().contains(requestUri.getAuthority())) {
			System.out.println(" is allowed.\n");
			return true;
		}
		System.out.println(" is forbidden.\n");
		return false;
	}

	private void showFilter(Bean bean, org.sciplore.resources.Document document) {
		if (this.showList != null && !this.showInUse) {
			this.showInUse = true;
			Map<String, Method> methods = filteredBeanGetterMethods();
			SciBeanElements beanAnno = bean.getClass().getAnnotation(SciBeanElements.class);
			if (beanAnno != null) {
				for (String key : beanAnno.value()) {
					if (this.showList.contains(key.toLowerCase())) {
						try {
							bean.addActiveElement((Bean) methods.get(key.toLowerCase()).invoke(this, document));
						}
						catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
			else {
				// TODO: do sth. here
			}
			this.showInUse = false;
		}
		else if (this.hideList == null) {
			Map<String, Method> methods = filteredBeanGetterMethods();
			SciBeanElements beanAnno = bean.getClass().getAnnotation(SciBeanElements.class);
			if (beanAnno != null) {
				for (String key : beanAnno.value()) {
					try {
						bean.addActiveElement((Bean) methods.get(key.toLowerCase()).invoke(this, document));
					}
					catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				// TODO: do sth. here
			}
		}
	}

	private void hideFilter(Bean bean, org.sciplore.resources.Document document) {
		if (this.hideList != null && !this.hideInUse) {
			this.hideInUse = true;
			try {
				Map<String, Method> methods = filteredBeanGetterMethods();
				SciBeanElements beanAnno = bean.getClass().getAnnotation(SciBeanElements.class);
				if (beanAnno != null) {
					for (String key : beanAnno.value()) {
						if (!this.hideList.contains(key.toLowerCase())) {
							try {
								bean.addActiveElement((Bean) methods.get(key.toLowerCase()).invoke(this, document));
							}
							catch (IllegalArgumentException e) {
								e.printStackTrace();
							}
							catch (IllegalAccessException e) {
								e.printStackTrace();
							}
							catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					}
				}
				else {
					// TODO: do sth. here
				}
				this.hideInUse = false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Method> filteredBeanGetterMethods() {
		Map<String, Method> result = new HashMap<String, Method>();
		Class clazz = this.getClass();
		for (Method m : clazz.getDeclaredMethods()) {
			if (!m.getName().startsWith("get") && m.getReturnType() != Bean.class) {
				continue;
			}
			String name = m.getName().toLowerCase().substring(3, m.getName().length() - 4);
			result.put(name, m);
		}
		return result;
	}

	public Application getApplicationBean(List<org.sciplore.resources.Application> applicationVersions) {
		Application applicationBean = new Application();
		org.sciplore.resources.Application firstApp = applicationVersions.get(0);
		if (firstApp != null) {
			applicationBean.addActiveAttribute("href", this.baseURL + firstApp.getName().toLowerCase() + formatQueryParam);
			applicationBean.addActiveAttribute("id", firstApp.getName().toLowerCase());
			applicationBean.addActiveAttribute("name", firstApp.getName());
		}
		applicationBean.setVersions(getApplicationVersion(applicationVersions));
		return applicationBean;
	}

	public ApplicationVersion getApplicationVersion(org.sciplore.resources.Application application) {
		ApplicationVersion versionBean = new ApplicationVersion();
		if (application.getId() != null) {
			versionBean.addActiveAttribute("href", this.baseURL + "versions/" + application.getId() + formatQueryParam);
			versionBean.addActiveAttribute("id", "" + application.getId());
		}

		versionBean.setRelease_date(application.getReleaseDate());
		versionBean.setBuild(application.getBuildNumber());
		versionBean.setMajor(application.getVersionMajor());
		versionBean.setMiddle(application.getVersionMid());
		versionBean.setMinor(application.getVersionMinor());
		versionBean.setStatus(application.getVersionStatusName());
		versionBean.setStatus_number(application.getVersionStatusNumber());
		versionBean.setRelease_note(application.getReleaseNote());
		return versionBean;
	}

	public ApplicationVersions getApplicationVersion(List<org.sciplore.resources.Application> applicationVersions) {
		ApplicationVersions versionsBean = new ApplicationVersions();
		for (org.sciplore.resources.Application application : applicationVersions) {
			versionsBean.add(getApplicationVersion(application));
		}
		return versionsBean;
	}

	public Applications getApplicationsBean(List<org.sciplore.resources.Application> applications) {
		Applications applicationsBean = new Applications();
		if (applications.size() > 0) {
			String lastName = applications.get(0).getName();
			int lastIndex = 0;
			for (int i = 0; i < applications.size(); i++) {
				org.sciplore.resources.Application app = applications.get(i);
				if (!app.getName().equals(lastName)) {
					lastName = app.getName();
					applicationsBean.add(getApplicationBean(applications.subList(lastIndex, i + 1)));
					lastIndex = i + 2;
				}
			}
		}
		return applicationsBean;
	}	

	public Documents getDocumentsBean(List<org.sciplore.resources.Document> documents, Long totalAmount, String baseUrlSuffix) {
		Documents documentsBean = new Documents();
		documentsBean.addActiveAttribute("href", this.baseURL + baseUrlSuffix + "documents/" + Tools.getQueryParamsAsString(this.uriInfo));
		if (totalAmount != null) {
			documentsBean.addActiveAttribute("totalamount", "" + totalAmount);
		}
		for (org.sciplore.resources.Document document : documents) {
			documentsBean.add(this.getDocumentBean(document));
		}
		return documentsBean;
	}

	public Documents getDocumentsBean(List<org.sciplore.resources.Document> documents, Long totalAmount) {
		return getDocumentsBean(documents, totalAmount, "");
	}

	public Document getDocumentBean(org.sciplore.resources.Document document) {
		Document documentBean = new Document();
		if (document.getId() != null) {
			documentBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + formatQueryParam);
			documentBean.addActiveAttribute("id", "" + document.getId());
		}
		documentBean.addActiveAttribute("hash", document.getHash());
		documentBean.addActiveAttribute("type", document.getType());
		hideFilter(documentBean, document);
		showFilter(documentBean, document);
		return documentBean;
	}

	public Bean getReferencesBean(org.sciplore.resources.Document document) {
		Set<Citation> citations = document.getCitations();
		if (citations.size() == 0) {
			return null;
		}

		References refsBean = new References();
		MultiValueMap<org.sciplore.resources.Document, Citation> refs = new MultiValueMap<org.sciplore.resources.Document, Citation>();
		for (Citation citation : citations) {
			// refsBean.add(getReferenceBean(citation));
			refs.put(citation.getCitedDocument(), citation);
		}
		for (org.sciplore.resources.Document key : refs.keySet()) {
			refsBean.add(getReferenceBean(refs.get(key).toArray((new Citation[0]))));
		}
		return refsBean;
	}

	private Bean getReferenceBean(Citation[] citations) {
		Reference refBean = new Reference();
		// refBean.addActiveAttribute("id", ""+ citation.getId());
		// refBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL,
		// citation[0].getCitingDocument()) + "/reference/" + citation.getId() +
		// formatQueryParam);
		refBean.addActiveElement(getReferencedDocumentBean(citations[0].getCitedDocument()));
		refBean.addActiveElement(getOccurencesBean(citations));
		return refBean;
	}

	private Bean getOccurencesBean(Citation[] citations) {
		Occurences occurences = new Occurences();
		for (Citation citation : citations) {
			occurences.add(getOccurenceBean(citation));
		}
		return occurences;
	}

	private Bean getOccurenceBean(Citation citation) {
		Occurence occurenceBean = new Occurence();
		// occurenceBean.addActiveAttribute("id", ""+ citation.getId());
		if (citation.getCountCharacter() != null) {
			occurenceBean.addActiveAttribute("character", String.valueOf(citation.getCountCharacter()));
		}
		else {
			occurenceBean.addActiveAttribute("character", "");
		}
		if (citation.getCountWord() != null) {
			occurenceBean.addActiveAttribute("word", String.valueOf(citation.getCountWord()));
		}
		else {
			occurenceBean.addActiveAttribute("word", "");
		}
		if (citation.getCountSentence() != null) {
			occurenceBean.addActiveAttribute("sentence", String.valueOf(citation.getCountSentence()));
		}
		else {
			occurenceBean.addActiveAttribute("sentence", "");
		}
		if (citation.getCountParagraph() != null) {
			occurenceBean.addActiveAttribute("paragraph", String.valueOf(citation.getCountParagraph()));
		}
		else {
			occurenceBean.addActiveAttribute("paragraph", "");
		}
		if (citation.getCountChapter() != null) {
			occurenceBean.addActiveAttribute("chapter", String.valueOf(citation.getCountChapter()));
		}
		else {
			occurenceBean.addActiveAttribute("chapter", "");
		}

		if (citation.getContext() != null && citation.getContext().trim().length() > 0) {
			occurenceBean.setValue(citation.getContext());
		}
		return occurenceBean;
	}

	private Bean getReferencedDocumentBean(org.sciplore.resources.Document document) {
		Document documentBean = new Document();
		documentBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + formatQueryParam);
		documentBean.addActiveAttribute("id", "" + document.getId());
		documentBean.addActiveAttribute("hash", document.getHash());
		documentBean.addActiveAttribute("type", document.getType());

		documentBean.addActiveElement(getTitleBean(document));
		documentBean.addActiveElement(getYearBean(document));
		documentBean.addActiveElement(getDoiBean(document));
		documentBean.addActiveElement(getAuthorsBean(document));
		return documentBean;
	}

	public Bean getXrefsBean(org.sciplore.resources.Document document) {
		Xrefs xrefsBean = new Xrefs();
		xrefsBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + "/xrefs" + formatQueryParam);

		// //TODO: applied filter-param for xref links !!! its just a quick hack
		String filter = null;
		// if(Tools.getQueryParamsAsMap(uriInfo).containsKey("filter")) {
		// filter = Tools.getQueryParamsAsMap(uriInfo).get("filter");
		// }
		// filter = "arxiv";
		for (DocumentXref xref : Tools.getSortedDocumentXrefs(document.getXrefs())) {
			// TODO: applied filter-param for xref links !!! its just a quick
			// hack
			if (filter != null) {
				if (xref.getSource().equals(filter)) {
					xrefsBean.add(this.getXrefBean(xref));
				}
			}
			else {
				xrefsBean.add(this.getXrefBean(xref));
			}
		}
		return xrefsBean;
	}

	public Bean getXrefBean(DocumentXref xref) {
		Xref xrefBean = new Xref();
		xrefBean.addActiveAttribute("id", "" + xref.getId());
		xrefBean.addActiveAttribute("href", this.baseURL + "xref/" + xref.getId() + formatQueryParam);
		// hideFilter(xrefBean,xref);
		// showFilter(xrefBean,xref);
		xrefBean.addActiveElement(this.getOrganizationBean(xref.getInstitution()));
		xrefBean.addActiveElement(this.getSourceIdBean(xref));
		xrefBean.addActiveElement(this.getSourceUrlBean(xref));
		xrefBean.addActiveElement(this.getReleaseDateBean(xref));
		xrefBean.addActiveElement(this.getCategoriesBean(xref));
		return xrefBean;
	}

	private Bean getCategoriesBean(DocumentXref xref) {
		Categories categoriesBean = new Categories();
		categoriesBean.addActiveAttribute("href", this.baseURL + "xref/" + xref.getId() + "/categories" + formatQueryParam);
		for (DocumentXrefCategory category : Tools.getSortedCategories(xref.getDocumentXrefCategories())) {
			categoriesBean.add(this.getCategoryBean(category));
		}
		return categoriesBean;
	}

	private Bean getCategoryBean(DocumentXrefCategory category) {
		Category categoryBean = new Category();
		categoryBean.addActiveAttribute("href", this.baseURL + "xref/" + category.getXref().getId() + "/category/" + category.getId() + formatQueryParam);
		categoryBean.addActiveAttribute("type", "" + category.getType());
		categoryBean.addActiveElement(this.getOrganizationBean(category.getSource()));
		Id idBean = new Id();
		idBean.setValue(category.getCategory());
		categoryBean.addActiveElement(idBean);
		return categoryBean;
	}

	private Bean getReleaseDateBean(DocumentXref xref) {
		ReleaseDate releaseDateBean = new ReleaseDate();
		if (xref != null && xref.getReleaseDate() != null) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			releaseDateBean.setValue(dateFormatter.format(xref.getReleaseDate()));
		}
		return releaseDateBean;
	}

	public Bean getSourceUrlBean(DocumentXref xref) {
		Url urlBean = new Url();
		if (xref.getInstitution() != null && xref.getInstitution().getXrefBaseUrl() != null) {
			urlBean.setValue(xref.getInstitution().getXrefBaseUrl() + xref.getSourcesId());
		}
		return urlBean;
	}

	public Bean getYearBean(org.sciplore.resources.Document document) {
		if (document.getPublishedYear() != null) {
			Year yearBean = new Year();
			yearBean.setValue(document.getPublishedYear().toString());
			return yearBean;
		}
		return null;
	}

	public Bean getSourceIdBean(DocumentXref xref) {
		SourceId sourceIdBean = new SourceId();
		sourceIdBean.setValue(xref.getSourcesId());
		return sourceIdBean;
	}

	public Bean getOrganizationBean(org.sciplore.resources.Institution institution) {
		Organization organizationBean = new Organization();
		if (institution != null) {
			organizationBean.addActiveAttribute("id", "" + institution.getId());
			organizationBean.addActiveAttribute("href", this.baseURL + "organization/" + institution.getId() + formatQueryParam);
			Name nameBean = new Name();
			nameBean.setValue(institution.getName());
			organizationBean.addActiveElement(nameBean);
			Url urlBean = new Url();
			urlBean.setValue(institution.getUrl());
			organizationBean.addActiveElement(urlBean);
		}
		return organizationBean;
	}

	public Bean getFulltextsBean(org.sciplore.resources.Document document) {
		Fulltexts fulltextsBean = new Fulltexts();
		fulltextsBean.addActiveAttribute("href", Tools.getDocumentHref(this.baseURL, document) + "/fulltexts" + formatQueryParam);
		for (FulltextUrl fulltextUrl : document.getFulltextUrls()) {
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
		abstractBean.setValue(document.getAbstract());
		return abstractBean;
	}

	public Bean getAuthorsBean(org.sciplore.resources.Document document) {
		Authors authorsBean = new Authors();
		authorsBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL, document) + "/authors" + formatQueryParam);
		for (DocumentPerson author : document.getPersons()) {
			authorsBean.add(this.getAuthorBean(author));
		}
		return authorsBean;
	}

	public Bean getAuthorsBeanByLetter(List<Person> authors, String letter) {
		Authors authorsBean = new Authors();
		authorsBean.addActiveAttribute("href", this.baseURL + "authors/" + letter + formatQueryParam);
		for (Person p : authors) {
			Author authorBean = new Author();
			authorBean.addActiveAttribute("href", this.baseURL + "authors/" + p.getId() + formatQueryParam);
			authorBean.addActiveAttribute("id", "" + p.getId());
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
			authorBean.addActiveElement(nameLastPrefix);
			authorBean.addActiveElement(nameLast);
			authorBean.addActiveElement(nameLastSuffix);
			// add author to list
			authorsBean.add(authorBean);
		}
		return authorsBean;
	}

	public Bean getAuthorBean(DocumentPerson author) {
		Author authorBean = new Author();
		authorBean.addActiveAttribute("href", this.baseURL + "authors/" + author.getPersonHomonym().getPerson().getId() + formatQueryParam);
		authorBean.addActiveAttribute("id", "" + author.getPersonHomonym().getPerson().getId());
		if (author.getRank() != null) {
			authorBean.addActiveAttribute("rank", "" + author.getRank());
		}
		Name_First nameFirst = new Name_First();
		nameFirst.setValue(author.getPersonHomonym().getNameFirst());
		Name_Middle nameMiddle = new Name_Middle();
		nameMiddle.setValue(author.getPersonHomonym().getNameMiddle());
		Name_Last nameLast = new Name_Last();
		nameLast.setValue(author.getPersonHomonym().getNameLast());
		Name_Last_Prefix nameLastPrefix = new Name_Last_Prefix();
		nameLast.setValue(author.getPersonHomonym().getNameLast());
		Name_Last_Suffix nameLastSuffix = new Name_Last_Suffix();
		nameLast.setValue(author.getPersonHomonym().getNameLast());
		authorBean.addActiveElement(nameFirst);
		authorBean.addActiveElement(nameMiddle);
		authorBean.addActiveElement(nameLastPrefix);
		authorBean.addActiveElement(nameLast);
		authorBean.addActiveElement(nameLastSuffix);
		return authorBean;
	}

	private Bean getHomonymsBean(Person person) {
		Homonyms homonymsBean = new Homonyms();
		homonymsBean.addActiveAttribute("href", this.baseURL + "author/" + person.getId() + "/homonyms" + formatQueryParam);
		for (PersonHomonym homonym : person.getHomonyms()) {
			homonymsBean.add(this.getHomonymBean(homonym));
		}
		return homonymsBean;
	}

	private Bean getHomonymBean(PersonHomonym homonym) {
		Homonym homonymBean = new Homonym();
		homonymBean.addActiveAttribute("href", this.baseURL + "homonym/" + homonym.getId() + formatQueryParam);
		Name_First nameFirst = new Name_First();
		nameFirst.setValue(homonym.getNameFirst());
		Name_Middle nameMiddle = new Name_Middle();
		nameMiddle.setValue(homonym.getNameMiddle());
		Name_Last nameLast = new Name_Last();
		nameLast.setValue(homonym.getNameLast());
		Name_Last_Prefix nameLastPrefix = new Name_Last_Prefix();
		nameLastPrefix.setValue(homonym.getNameLastPrefix());
		Name_Last_Suffix nameLastSuffix = new Name_Last_Suffix();
		nameLastSuffix.setValue(homonym.getNameLastSuffix());
		homonymBean.addActiveElement(nameFirst);
		homonymBean.addActiveElement(nameMiddle);
		homonymBean.addActiveElement(nameLastPrefix);
		homonymBean.addActiveElement(nameLast);
		homonymBean.addActiveElement(nameLastSuffix);
		return homonymBean;
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
		authorBean.addActiveElement(nameLastPrefix);
		authorBean.addActiveElement(nameLast);
		authorBean.addActiveElement(nameLastSuffix);
		if (author.getDob() != null) {
			authorBean.addActiveElement(getAuthorBirthdayBean(author));
		}
		if (author.getDod() != null) {
			authorBean.addActiveElement(getAuthorDeathdayBean(author));
		}
		for (PersonXref px : author.getPersonXrefs()) {
			if (px.getType() != null && px.getType() == 1) {
				authorBean.addActiveElement(getAuthorPhotoBean(px));
				break;
			}
		}
		authorBean.addActiveElement(getAuthorXrefsBean(author));
		authorBean.addActiveElement(getHomonymsBean(author));
		Documents docs = this.getDocumentsBean(documents, new Long(documents.size()));
		authorBean.setDocuments(docs);
		authorBean.addActiveElement(docs);
		return authorBean;
	}

	private Photo getAuthorPhotoBean(PersonXref xref) {
		Photo photo = new Photo();
		photo.addActiveAttribute("href", this.baseURL + "authors/" + xref.getPerson().getId() + "/photo" + formatQueryParam);
		photo.setValue(xref.getSourcesId());
		return photo;
	}

	private Deathday getAuthorDeathdayBean(Person person) {
		Deathday dday = new Deathday();
		dday.addActiveAttribute("href", this.baseURL + "authors/" + person.getId() + "/deathday" + formatQueryParam);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		dday.setValue(format.format(person.getDod()));
		return dday;
	}

	private Birthday getAuthorBirthdayBean(Person person) {
		Birthday bday = new Birthday();
		bday.addActiveAttribute("href", this.baseURL + "authors/" + person.getId() + "/birthday" + formatQueryParam);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		bday.setValue(format.format(person.getDob()));
		return bday;
	}

	private Authorxrefs getAuthorXrefsBean(Person person) {
		Authorxrefs xrefs = new Authorxrefs();
		xrefs.addActiveAttribute("href", this.baseURL + "authors/" + person.getId() + "/authorxrefs" + formatQueryParam);
		for (PersonXref personXref : Tools.getSortedPersonXrefs(person.getPersonXrefs())) {
			if (personXref.getSourcesId() != null) {
				xrefs.add(this.getAuthorXrefBean(personXref));
			}
		}
		return xrefs;
	}

	private Authorxref getAuthorXrefBean(PersonXref personXref) {
		Authorxref xref = new Authorxref();
		xref.addActiveAttribute("href", this.baseURL + "authorxref/" + personXref.getId() + formatQueryParam);
		xref.addActiveAttribute("type", "" + personXref.getType());
		Url url = new Url();
		url.setValue(personXref.getSourcesId());
		xref.addActiveElement(url);
		return xref;
	}

	public boolean isNormalizeTitle() {
		return normalizeTitle;
	}

	public void setNormalizeTitle(boolean normalizeTitle) {
		this.normalizeTitle = normalizeTitle;
	}

	public Bean getTitleBean(org.sciplore.resources.Document document) {
		String title = new String(document.getTitle()); // copy title String, do
														// not use a reference
		if (this.normalizeTitle) {
			title = normalizeTitle(title);
		}
		Title titleBean = new Title();
		titleBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL, document) + "/title" + formatQueryParam);
		titleBean.setValue(title);
		return titleBean;
	}

	// if title consists mainly of upper case chars: normalize to use upper case
	// for first letter, else lower case
	public String normalizeTitle(String title) {
		char[] chars = title.toCharArray();

		int uppers = 0;
		for (char c : chars) {
			if (Character.isLetter(c)) {
				if (Character.isUpperCase(c)) {
					uppers++;
				}
				else if (Character.isLowerCase(c)) {
					uppers--;
				}
			}
		}

		// at least 50 percent of the title is written in upper case -->
		// normalize the Letters
		if (uppers > 0) {
			boolean newWord = true;
			for (int i = 0; i < chars.length; i++) {
				if (Character.isLetter(chars[i])) {
					if (newWord) {
						newWord = false;
					}
					else {
						chars[i] = Character.toLowerCase(chars[i]);
					}
				}
				else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
					newWord = true;
				}
			}
			return String.valueOf(chars);
		}

		return title;
	}

	public String capitalize(String string) {
		char[] chars = string.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			}
			else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') { // You
																								// can
																								// add
																								// other
																								// chars
																								// here
				found = false;
			}
		}
		return String.valueOf(chars);
	}

	public Bean getDoiBean(org.sciplore.resources.Document document) {
		Doi doiBean = new Doi();
		doiBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL, document) + "/doi" + formatQueryParam);
		doiBean.setValue(document.getDoi());
		return doiBean;
	}

	public Bean getCommentsBean(org.sciplore.resources.Document document) {
		Comments commentsBean = new Comments();
		commentsBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL, document) + "/comments" + formatQueryParam);
		for (Feedback comment : Tools.getSortedComments(document.getFeedbacks())) {
			commentsBean.add(this.getCommentBean(comment));
		}
		return commentsBean;
	}

	private Bean getCommentBean(Feedback comment) {
		Comment commentBean = new Comment();
		commentBean.addActiveAttribute("href", Tools.getDocumentHref(baseURL, comment.getDocument()) + "/comment/" + comment.getId() + formatQueryParam);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		commentBean.addActiveAttribute("created", dateFormatter.format(comment.getCreated()));
		commentBean.setValue(comment.getText());
		return commentBean;
	}

}
