package org.docear.query;

import org.docear.graphdb.QuerySession;
import org.docear.xml.UserModel;

public interface ResultGenerator {	
	public void generateResultsForUserModel(QuerySession session, int userId, UserModel userModel, String excludePdfHash) throws Exception;
	
}
