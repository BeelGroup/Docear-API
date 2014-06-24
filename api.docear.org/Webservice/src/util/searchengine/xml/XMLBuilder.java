package util.searchengine.xml;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.UriInfo;

import org.sciplore.resources.Document;
import org.sciplore.resources.SearchDocuments;
import org.sciplore.resources.SearchDocumentsPage;
import org.sciplore.resources.SearchModel;
import org.w3c.dom.Element;

import util.InternalCommons;

public class XMLBuilder {
	public static String buildSearchDocumentsXml(SearchDocumentsPage searchDocumentsPage,UriInfo uriInfo, String userName) {
		org.w3c.dom.Document dom = InternalCommons.getNewXMLDocument();
		if(dom != null) {
			Element root = dom.createElement("search_results");
			root.setAttribute("id", String.valueOf(searchDocumentsPage.getSearchDocumentsSet().getId()));
			root.setAttribute("page", String.valueOf(searchDocumentsPage.getPage()));
			root.setAttribute("documentsAvailable", String.valueOf(searchDocumentsPage.getSearchDocumentsSet().getDocumentsAvailable()));
			dom.appendChild(root);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String baseUri = uriInfo.getBaseUri().toString();
			for (SearchDocuments searchDoc : searchDocumentsPage.getSearchDocuments()) {
				Element searchResult = dom.createElement("search_result");
				searchResult.setAttribute("id", String.valueOf(searchDoc.getId()));
				searchResult.setAttribute("fulltext", baseUri+"documents/"+searchDoc.getHashId()+"/download/?userName="+userName);
				searchResult.setAttribute("created", sdf.format(searchDocumentsPage.getCreated()));

				Date clicked = searchDoc.getClicked();
				if (clicked != null) {
					searchResult.setAttribute("clicked", sdf.format(clicked));
				}
				
				Element document = dom.createElement("document");
				Document d = searchDoc.getFulltextUrl().getDocument();
				document.setAttribute("id", String.valueOf(d.getId()));
				
				Element title = dom.createElement("title");
				title.setTextContent(String.valueOf(d.getTitle()));
				document.appendChild(title);
				
				Element sourceId = dom.createElement("sourceid");
				sourceId.setTextContent(searchDoc.getFulltextUrl().getUrl());
				document.appendChild(sourceId);
				
				searchResult.appendChild(document);
				
				root.appendChild(searchResult);
			}
			
			return InternalCommons.getXMLStr(dom);
		}
		return "";
	}
	
	public static String buildSearchModelXml(SearchModel searchModel, UriInfo uriInfo) {
		org.w3c.dom.Document dom = InternalCommons.getNewXMLDocument();
		if(dom != null) {
			Element root = dom.createElement("searchmodel");
			root.setAttribute("id", String.valueOf(searchModel.getId()));
			root.setTextContent(searchModel.getModel());
			dom.appendChild(root);
		}
		
		return InternalCommons.getXMLStr(dom);
	}
}
