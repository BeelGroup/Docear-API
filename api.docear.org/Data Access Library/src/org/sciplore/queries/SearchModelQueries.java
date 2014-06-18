package org.sciplore.queries;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.SearchModel;
import org.sciplore.resources.User;

public class SearchModelQueries {

	public static SearchModel getLatestUnusedSearchModel(Session session, User user) {
		Criteria crit = session.createCriteria(SearchModel.class, "sm");
		crit.createAlias("userModel", "um");
		crit.add(Restrictions.eq("um.user", user));
		crit.addOrder(Order.desc("sm.id"));		
		crit.setMaxResults(1);
		SearchModel latest = (SearchModel) crit.uniqueResult();

		crit = session.createCriteria(SearchModel.class, "sm");
		crit.createAlias("userModel", "um");
		crit.add(Restrictions.eq("um.user", user));
		crit.add(Restrictions.isNull("delivered"));
		crit.addOrder(Order.desc("sm.id"));	
		crit.setMaxResults(1);		
		SearchModel latestUnused = (SearchModel) crit.uniqueResult();
		
		if (latest.getId() != latestUnused.getId()) {
			latestUnused.setOld(true);
		}

		return latestUnused;
	}

}

