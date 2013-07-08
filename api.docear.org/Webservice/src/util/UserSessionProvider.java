package util;

import java.util.HashMap;
import java.util.Map;

public class UserSessionProvider {
	public class UserSession {
//		public String hash;
//		public String title;
		//also save the document_ids to prevent from malicious attempts to use a wrong document_id for the hash
		private final Map<String, Object> valueMap;		
		
		public UserSession() {
			valueMap = new HashMap<String, Object>();			
		}
		
		public void put(String key, Object value) {
			this.valueMap.put(key, value);
		}
		
		public Object get(String key) {
			return this.valueMap.get(key);
		}
		
		public void remove(String key) {
			this.valueMap.remove(key);
		}
		
		public void clear() {
			this.valueMap.clear();
		}
		
		public boolean containsKey(String key) {
			return this.valueMap.containsKey(key);
		}
		
	}

	private final static UserSessionProvider userSessionProvider = new UserSessionProvider();
	
	public static UserSessionProvider getUserSessionProvider() {
		return userSessionProvider ;
	}
	
	private final Map<Integer, UserSession> sessionMap = new HashMap<Integer, UserSession>();	
	public UserSession getUserSession(Integer user) {
		UserSession us = sessionMap.get(user);
		if (us == null) {
			us =  new UserSession();
			this.sessionMap.put(user, us);
		}
		return us;
	}
}
