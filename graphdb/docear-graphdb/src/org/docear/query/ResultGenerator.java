package org.docear.query;

import org.docear.xml.UserModel;

public interface ResultGenerator {	
	public void generateResultsForUserModel(int userId, UserModel userModel, String excludePdfHash) throws Exception;
	
}
