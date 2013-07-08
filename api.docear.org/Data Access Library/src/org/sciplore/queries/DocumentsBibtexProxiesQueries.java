package org.sciplore.queries;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.DocumentsBibtexProxies;

public class DocumentsBibtexProxiesQueries {
	
	public static int MAX_PROXY_REQUESTS_PER_DAY = 580;

	public static DocumentsBibtexProxies getDocumentsBibtexQueries(Session session, String label) {
		Criteria criteria = getCriteria(session, label);
		return (DocumentsBibtexProxies) criteria.uniqueResult();
	}
		
	public static DocumentsBibtexProxies getAvailableProxy(Session session) {				
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String today = sdf.format(new Date());
		
		Criteria criteria = getCriteria(session, null);
		@SuppressWarnings("unchecked")
		List<DocumentsBibtexProxies> proxies = criteria.list();
		
		for (DocumentsBibtexProxies proxy : proxies) {
			// not on the same day
			if (proxy.getDate() == null || !today.equals(sdf.format(proxy.getDate()))) {
				session.setFlushMode(FlushMode.MANUAL);
				Transaction transaction = session.beginTransaction();
				try {
					proxy.setDate(new Date());
					proxy.setCounter(0);
					session.saveOrUpdate(proxy);
					session.flush();
					transaction.commit();
					return proxy;
				}
				catch(Exception e) {
					e.printStackTrace();
					transaction.rollback();
				}
			}
			// still requests available for this proxy
			else if (proxy.getCounter() < MAX_PROXY_REQUESTS_PER_DAY) {
				return proxy;
			}
		}
		
		return null;
	}


	private static Criteria getCriteria(Session session, String label) {
		Criteria criteria = session.createCriteria(DocumentsBibtexProxies.class);
		
		criteria = criteria.add(Restrictions.eq("active", 1));
		if (label != null) {
			criteria = criteria.add(Restrictions.eq("label", label));
		}
				
		return criteria;
	}
		
}
