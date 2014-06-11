package util.searchengine;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.mrdlib.index.DocumentHashItem;
import org.mrdlib.index.Searcher;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.SearchDocuments;
import org.sciplore.resources.SearchDocumentsSet;
import org.sciplore.resources.SearchModel;
import org.sciplore.resources.UserModel;

import util.recommendations.UserModelItem;
import util.recommendations.UserModelKeywordItem;

public class SearchCommons {
	private final static int MAX_MODEL_SIZE = 20;
	
	public static SearchModel createSearchModel(Session session, UserModel userModel, List<UserModelItem> userModelItems) {
		long time = System.currentTimeMillis();
		StringBuilder modelBuilder = new StringBuilder();
		
		Iterator<UserModelItem> iter = userModelItems.iterator();
		
		int size = 0;
		while (iter.hasNext() && size<MAX_MODEL_SIZE) {
			UserModelItem item = iter.next();
			
			if (item instanceof UserModelKeywordItem) {
				size++;
				modelBuilder.append(item.getItem());
				modelBuilder.append(" ");
			}
		}
		
		if (size == 0) {
			return null;
		}
		
		SearchModel searchModel = new SearchModel(session);
		searchModel.setUserModel(userModel);
		searchModel.setModel(modelBuilder.toString().trim());
		searchModel.setVarSize(size);
		searchModel.setExecutionTime((int) (System.currentTimeMillis()-time));
		
		return searchModel;
	}
	
	public static List<SearchDocuments> search(Session session, String search, String defaultField, int offset, int number, SearchDocumentsSet searchDocumentsSet) {
		List<SearchDocuments> searchDocuments = getExistingSearchDocuments(searchDocumentsSet, offset, number);
		
		// compute which documents really need to be searched, when the stored ones are used
		int effectiveNumber = number - searchDocuments.size();
		int effectiveOffset = offset + searchDocuments.size();
		
		try {
			long time = System.currentTimeMillis();
			Searcher searcher = new Searcher();			
			List<DocumentHashItem> items = searcher.search(search, defaultField, effectiveOffset, effectiveNumber);
			searchDocumentsSet.setCreated(new Date());
			searchDocumentsSet.setComputationTime(System.currentTimeMillis()-time);
			searchDocumentsSet.setQuery(search);
			
			searchDocuments.addAll(getSearchDocumentsFromDocumentHashItem(session, searchDocumentsSet, items, effectiveOffset));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return searchDocuments;
	}
	
	public static List<SearchDocuments> getSearchDocumentsFromDocumentHashItem(Session session, SearchDocumentsSet searchDocSet, List<DocumentHashItem> items, int offset) {
		List<SearchDocuments> searchDocuments = new ArrayList<SearchDocuments>();
    	for (DocumentHashItem item : items) {
    		Document doc = null;
    		FulltextUrl fulltextUrl = null;
    		if (item.pdfHash != null) {
    			DocumentsPdfHash dph = DocumentsPdfHashQueries.getPdfHash(session, item.pdfHash);
    			if (dph != null) {
    				doc = dph.getDocument();
    				try {
    					fulltextUrl = dph.getFulltextUrls().get(0);
    				}
    				catch (Exception e) {
    					doc = null;
    					e.printStackTrace();
    				}
    			}
    		}
    
    		// fallback if hash unknown or null
    		if (doc == null) {
    			doc = DocumentQueries.getDocument(session, item.documentId);
    			try {
    				fulltextUrl = doc.getFulltextUrls().iterator().next();
    			}
    			catch (Exception e) {
    				continue;
    			}
    		}
    		if (fulltextUrl == null) {
    			continue;
    		}
    
    		SearchDocuments searchDoc = createSearchDocument(session, searchDocSet, fulltextUrl, item.rank, item.relevance, offset);
    		if (searchDoc != null) {
    			searchDocuments.add(searchDoc);
    		}
    	}
    	
    	return searchDocuments;
	}
	
	private static SearchDocuments createSearchDocument(Session session, SearchDocumentsSet searchDocSet, FulltextUrl fulltextUrl, int rank, Float relevance, int offset) {		
		SearchDocuments searchDoc = new SearchDocuments(session);
		searchDoc.setSearchDocumentsSet(searchDocSet);
		searchDoc.setFulltextUrl(fulltextUrl);
		searchDoc.setPresentationRank(++offset);
		searchDoc.setRelevance(relevance);

		return searchDoc;
	}

	private static List<SearchDocuments> getExistingSearchDocuments(SearchDocumentsSet searchDocumentsSet, Integer offset, int number) {
		List<SearchDocuments> searchDocuments = new ArrayList<SearchDocuments>();
		
		if (searchDocumentsSet != null && searchDocumentsSet.getSearchdocuments().size() > 0) {
    		for (SearchDocuments searchDoc : searchDocumentsSet.getSearchdocuments()) {
    			Integer index = searchDoc.getPresentationIndex();
    			if (index != null && index >= offset && index < (offset+number)) {
    				searchDocuments.add(searchDoc);
    			}
    		}
		}
		
		return searchDocuments;
	}
}
