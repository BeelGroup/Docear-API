package org.sciplore.queries;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.DocumentsBibtexUsers;

public class DocumentsBibtexUsersQueries {
	public static int MAX_USER_REQUESTS_PER_DAY = 50;
	
	public static DocumentsBibtexUsers getDocumentsBibtexUser(Session session, Integer user_id) {
		Criteria criteria = getCriteria(session, user_id);
		return (DocumentsBibtexUsers) criteria.uniqueResult();
	}
	
	public static boolean hasAvailableRequests(Session session, Integer user_id) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String today = sdf.format(new Date());
		
		DocumentsBibtexUsers user = getDocumentsBibtexUser(session, user_id);
		
		// user not in table yet --> create new entry during next if block
		if (user == null) {
			user = new DocumentsBibtexUsers();
		}
		// user has not used the metadata fetching feature today --> reset counter 
		if (user.getDate() == null || !today.equals(sdf.format(user.getDate()))) {
			session.setFlushMode(FlushMode.MANUAL);
			Transaction transaction = session.beginTransaction();
			try {
				user.setDate(new Date());
				user.setCounter(0);
				user.setUser_id(user_id);
				session.saveOrUpdate(user);
				session.flush();			
				transaction.commit();
				
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				transaction.rollback();
			}
		}
		
		return user.getCounter() < MAX_USER_REQUESTS_PER_DAY;
	}
	
	private static Criteria getCriteria(Session session, Integer user_id) {
		Criteria criteria = session.createCriteria(DocumentsBibtexUsers.class);
		
		if (user_id != null) {
			criteria = criteria.add(Restrictions.eq("user_id", user_id));
		}
		
		return criteria;
	}
	
}
