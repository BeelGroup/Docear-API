package util.searchengine;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.mrdlib.index.DocumentHashItem;
import org.mrdlib.index.Searcher;
import org.sciplore.queries.DocumentQueries;
import org.sciplore.queries.DocumentsPdfHashQueries;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.FulltextUrl;
import org.sciplore.resources.SearchDocuments;
import org.sciplore.resources.SearchDocumentsPage;
import org.sciplore.resources.SearchDocumentsSet;
import org.sciplore.resources.SearchModel;
import org.sciplore.resources.User;
import org.sciplore.resources.UserModel;

import util.recommendations.UserModelItem;
import util.recommendations.UserModelKeywordItem;

public class SearchCommons {
	private final static int MAX_MODEL_SIZE = 20;
	
	public static SearchModel createSearchModel(Session session, User user, UserModel userModel, List<UserModelItem> userModelItems) {
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
		searchModel.setUser(user);
		searchModel.setModel(modelBuilder.toString().trim());
		searchModel.setVarSize(size);
		searchModel.setExecutionTime((int) (System.currentTimeMillis()-time));
		
		return searchModel;
	}
	
	public static SearchDocumentsPage search(Session session, String search, String defaultField, int page, int number, SearchDocumentsSet searchDocumentsSet, SearchModel searchModel) {
		long time = System.currentTimeMillis();
		if (searchDocumentsSet != null && searchDocumentsSet.getId() != null) {
			SearchDocumentsPage searchDocumentsPage = getExistingSearchDocumentsPage(searchDocumentsSet, page);
			if (searchDocumentsPage != null) {
				return searchDocumentsPage;
			}
		}	
		
		// compute which documents really need to be searched, when the stored ones are used
		int effectiveOffset = (page-1) * number;
		int effectiveNumber = number;
		
		SearchDocumentsPage searchDocumentsPage = new SearchDocumentsPage(session);
		try {
			searchDocumentsSet.setQuery(search);
			searchDocumentsSet.setSearchModel(searchModel);
			
			searchDocumentsPage.setCreated(new Date());
			searchDocumentsPage.setComputationTime(System.currentTimeMillis()-time);
			searchDocumentsPage.setDocuments_per_page(number);
			searchDocumentsPage.setPage(page);
			searchDocumentsPage.setSearchDocumentsSet(searchDocumentsSet);
			
			Searcher searcher = new Searcher();		
			Collection<DocumentHashItem> items = searcher.search(search, defaultField, effectiveOffset, effectiveNumber);
			searchDocumentsPage.setSearchDocuments(getSearchDocumentsFromDocumentHashItem(session, searchDocumentsPage, items, effectiveOffset));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return searchDocumentsPage;
	}
	
	public static Set<SearchDocuments> getSearchDocumentsFromDocumentHashItem(Session session, SearchDocumentsPage searchDocPage, Collection<DocumentHashItem> items, int offset) {
		Set<SearchDocuments> searchDocuments = new HashSet<SearchDocuments>();
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
    
    		SearchDocuments searchDoc = createSearchDocument(session, searchDocPage, fulltextUrl, item.rank, item.relevance, offset);
    		if (searchDoc != null) {
    			searchDocuments.add(searchDoc);
    		}
    	}
    	
    	return searchDocuments;
	}
	
	private static SearchDocuments createSearchDocument(Session session, SearchDocumentsPage searchDocPage, FulltextUrl fulltextUrl, int rank, Float relevance, int offset) {		
		SearchDocuments searchDoc = new SearchDocuments(session);
		searchDoc.setSearchDocumentsPage(searchDocPage);
		searchDoc.setFulltextUrl(fulltextUrl);
		searchDoc.setOriginalRank(offset+rank);
		searchDoc.setPresentationRank(rank);
		searchDoc.setRelevance(relevance);
		rank++;

		return searchDoc;
	}

	private static SearchDocumentsPage getExistingSearchDocumentsPage(SearchDocumentsSet searchDocumentsSet, Integer page) {		
		if (searchDocumentsSet != null && searchDocumentsSet.getSearchDocumentsPage().size() > 0) {
    		for (SearchDocumentsPage searchDocPage : searchDocumentsSet.getSearchDocumentsPage()) {
    			if (searchDocPage.getPage() == page) {
    				return searchDocPage;
    			}
    		}
		}
		
		return null;
	}

	public static void click(Session session, SearchDocuments searchDoc) {
		searchDoc.setClicked(new Date(System.currentTimeMillis()));
		
		session.saveOrUpdate(searchDoc);
		session.flush();
		
		
	}
}
