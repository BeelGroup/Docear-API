package util.searchengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

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
	private final static int MIN_MODEL_SIZE = 5;
	
	public static SearchModel createSearchModel(Session session, User user, UserModel userModel, List<UserModelItem> userModelItems) {
		long time = System.currentTimeMillis();
		StringBuilder modelBuilder = new StringBuilder();
		
		Iterator<UserModelItem> iter = userModelItems.iterator();
		
		int size = 0;
		
		Random random = new Random();
		//
		int maximumSize = random.nextInt(MAX_MODEL_SIZE - MIN_MODEL_SIZE + 1) + MIN_MODEL_SIZE;
		while (iter.hasNext() && size < maximumSize) {
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
		
		SearchDocumentsPage searchDocumentsPage = new SearchDocumentsPage(session);
		try {
			searchDocumentsSet.setQuery(search);
			searchDocumentsSet.setVarQuerySize(computeVarQuerySize(search));
			searchDocumentsSet.setSearchModel(searchModel);
			
			searchDocumentsPage.setCreated(new Date());
			searchDocumentsPage.setComputationTime(System.currentTimeMillis()-time);
			searchDocumentsPage.setDocumentsPerPage(number);
			searchDocumentsPage.setPage(page);
			searchDocumentsPage.setSearchDocumentsSet(searchDocumentsSet);
			
			Searcher searcher = new Searcher();		
			// 100 == maximum number of documents for paginator (10 pages with 10 results)
			Collection<DocumentHashItem> items = searcher.search(search, defaultField, effectiveOffset, 100, number);
			if (items != null && items.size()>0) {
				searchDocumentsPage.getSearchDocumentsSet().setDocumentsAvailable(items.iterator().next().documentsAvailable);
			}
			else {
				searchDocumentsPage.getSearchDocumentsSet().setDocumentsAvailable(0);
			}
			searchDocumentsPage.setSearchDocuments(getSearchDocumentsFromDocumentHashItem(session, searchDocumentsPage, items, effectiveOffset));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return searchDocumentsPage;
	}
	
	private static int computeVarQuerySize(String query) {
		int size = 0;
		query = query.toLowerCase().trim();
		
		for (String s : query.split(" ")) {
			if (!s.equals("and") && !s.equals("or")) {
				size++;
			}
		}
		
		return size;
	}
	
	public static SortedSet<SearchDocuments> getSearchDocumentsFromDocumentHashItem(Session session, SearchDocumentsPage searchDocPage, Collection<DocumentHashItem> items, int offset) {
		SortedSet<SearchDocuments> searchDocuments = new TreeSet<SearchDocuments>();
		
		List<DocumentHashItem> list = new ArrayList<DocumentHashItem>(items);
		if (searchDocPage.getPage() == 1) {
			Collections.shuffle(list);
		}
		else {
			Collections.sort(list);
		}
		
		int i=1;
    	for (DocumentHashItem item : list) {    		
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
    		searchDoc.setPresentationRank(i++);
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
		searchDoc.setOriginalRank(rank);
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
