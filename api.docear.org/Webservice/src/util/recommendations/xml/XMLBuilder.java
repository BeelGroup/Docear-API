package util.recommendations.xml;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.UriInfo;

import org.sciplore.resources.Document;
import org.sciplore.resources.RecommendationsDocuments;
import org.sciplore.resources.RecommendationsDocumentsSet;
import org.sciplore.resources.RecommendationsUsersSettings;
import org.sciplore.resources.SearchModel;
import org.w3c.dom.Element;

import util.InternalCommons;

public class XMLBuilder {
	public static String buildRecommendationsXML(RecommendationsDocumentsSet recDocSet, RecommendationsUsersSettings settings, UriInfo uriInfo, String userName) {
		org.w3c.dom.Document dom = InternalCommons.getNewXMLDocument();
		if(dom != null) {
			Element root = dom.createElement("recommendations");
			root.setAttribute("id", String.valueOf(recDocSet.getId()));
			root.setAttribute("descriptor", settings.getRecommendationLabel().getValue());
			root.setAttribute("evaluationLabel", settings.getRecommendationRatingLabel().getValue());
			dom.appendChild(root);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String baseUri = uriInfo.getBaseUri().toString();
			boolean first = true;
			for (RecommendationsDocuments recDoc : recDocSet.getRecommendationsDocuments()) {
				Element recommendation = dom.createElement("recommendation");
				recommendation.setAttribute("id", String.valueOf(recDoc.getId()));
				recommendation.setAttribute("fulltext", baseUri+"user/"+userName+"/recommendations/fulltext/" + recDoc.getHashId() + "/");
				if (first && settings != null) {
					if (settings.getUsePrefix() != null && settings.getUsePrefix()) {
						recommendation.setAttribute("prefix", "[Sponsored]");
					}
					if (settings.getHighlight() != null && settings.getHighlight()) {
						recommendation.setAttribute("highlighted", "true");
					}
				}				
				first = false;
				recommendation.setAttribute("created", sdf.format(recDoc.getRecommentationsDocumentsSet().getCreated()));

				Date clicked = recDoc.getClicked();
				if (clicked != null) {
					recommendation.setAttribute("clicked", sdf.format(clicked));
				}
				
				Element document = dom.createElement("document");
				Document d = recDoc.getFulltextUrl().getDocument();
				document.setAttribute("id", String.valueOf(d.getId()));
				
				Element title = dom.createElement("title");
				title.setTextContent(String.valueOf(d.getTitle()));
				document.appendChild(title);
				
				Element sourceId = dom.createElement("sourceid");
				sourceId.setTextContent(recDoc.getFulltextUrl().getUrl());
				document.appendChild(sourceId);
				
				recommendation.appendChild(document);
				
				root.appendChild(recommendation);
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
		}
		
		return InternalCommons.getXMLStr(dom);
	}
}
