package util;

import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.sciplore.database.SessionProvider;

public class DocidxNotificationCommons {
	
	private static int NOTIFICATION_MONTHS_INTERVAL = 3;
	private static int NOTIFICATION_EMAIL_CHUNK_LIMIT = 100;
	
	private class PersonIdentifier {
		private String email;
		private String idToken;
		
		public PersonIdentifier(String email, String idToken) {
			this.email = email;
			this.idToken = idToken;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getIdToken() {
			return idToken;
		}

		public void setIdToken(String idToken) {
			this.idToken = idToken;
		}		
	}

	
	public DocidxNotificationCommons() {				
	}

	public String getNotificationReceiverXMl() {
		Session session = SessionProvider.getNewSession();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			Map<PersonIdentifier, String> notificationData = getNotificationData(session);			
			return serializeNotificationData(notificationData);			
		}
		finally {
			session.close();
		}
	}
	
	private Map<PersonIdentifier, String> getNotificationData(Session session) {
		SQLQuery query = session.createSQLQuery("SELECT C.uri, P.docidx_id_token AS id_token FROM contacts C JOIN persons P ON (C.person_id = P.id) "
				+ "JOIN documents_persons DP ON (DP.person_id = P.id) "
				+ "WHERE (P.docidx_notify IS NULL OR P.docidx_notify = 1) "
				+ "AND (P.docidx_last_notified IS NULL OR P.docidx_last_notified < DATE_SUB(NOW(), INTERVAL :interval MONTH)) "
				+ "ORDER BY P.docidx_last_notified "
				+ "LIMIT :limit");
		query.setParameter("interval", NOTIFICATION_MONTHS_INTERVAL);
		query.setParameter("limit", NOTIFICATION_EMAIL_CHUNK_LIMIT);
		
		query.addScalar("uri");
		query.addScalar("id_token");
		
		List<Object[]> result = query.list();
		
		System.out.println("test");
		
		
		
		return null;
	}	
	
	private String serializeNotificationData(Map<PersonIdentifier, String> map) {
		// TODO Auto-generated method stub
		return null;
	}

}
