package util.searchengine.xml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.sciplore.resources.Document;
import org.sciplore.resources.SearchDocuments;
import org.sciplore.resources.SearchDocumentsSet;
import org.sciplore.resources.SearchModel;
import org.w3c.dom.Element;

import util.InternalCommons;

public class XMLBuilder {
	public static String buildSearchDocumentsXml(SearchDocumentsSet searchDocSet, List<SearchDocuments> searchDocuments, UriInfo uriInfo, String userName) {
		org.w3c.dom.Document dom = InternalCommons.getNewXMLDocument();
		if(dom != null) {
			Element root = dom.createElement("search_results");
			root.setAttribute("id", String.valueOf(searchDocSet.getId()));
//			root.setAttribute("descriptor", settings.getRecommendationLabel().getValue());
//			root.setAttribute("evaluationLabel", settings.getRecommendationRatingLabel().getValue());
			dom.appendChild(root);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String baseUri = uriInfo.getBaseUri().toString();
//			boolean first = true;
			for (SearchDocuments searchDoc : searchDocuments) {
				Element searchResult = dom.createElement("search_result");
				searchResult.setAttribute("id", String.valueOf(searchDoc.getId()));
				searchResult.setAttribute("fulltext", baseUri+"documents/"+searchDoc.getHashId()+"/download/?userName="+userName);
//				if (first && settings != null) {
//					if (settings.getUsePrefix() != null && settings.getUsePrefix()) {
//						searchResult.setAttribute("prefix", "[Sponsored]");
//					}
//					if (settings.getHighlight() != null && settings.getHighlight()) {
//						searchResult.setAttribute("highlighted", "true");
//					}
//				}				
//				first = false;
				searchResult.setAttribute("created", sdf.format(searchDocSet.getCreated()));

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
