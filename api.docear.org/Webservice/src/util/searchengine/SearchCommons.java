package util.searchengine;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
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
}
