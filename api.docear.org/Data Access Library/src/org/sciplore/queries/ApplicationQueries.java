package org.sciplore.queries;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Application;

public class ApplicationQueries {

	public static List<Application> getApplicationVersions(Session session, String appName) {
		Criteria criteria = session.createCriteria(Application.class, "app");
		criteria = criteria.add(Restrictions.ilike("name", appName));
		criteria = criteria.add(Restrictions.eq("active", new Short((short) 1)));
		criteria = criteria.addOrder(Order.desc("build"));

		@SuppressWarnings("unchecked")
		List<Application> ds = (List<Application>) criteria.list();

		return ds;
	}

	public static List<Application> getLatestApplicationVersion(Session session, String appName, String minStatus) {
		Criteria criteria = session.createCriteria(Application.class, "app");
		criteria = criteria.add(Restrictions.ilike("name", appName));
		Application app = new Application();
		app.setVersionStatus(minStatus);
		if (app.getVersionStatus() != null) {
			criteria = criteria.add(Restrictions.le("versionStatus", app.getVersionStatus()));
		}
		criteria = criteria.add(Restrictions.eq("active", new Short((short) 1)));

		criteria = criteria.addOrder(Order.desc("build"));
		criteria = criteria.setMaxResults(1);

		@SuppressWarnings("unchecked")
		List<Application> ds = (List<Application>) criteria.list();

		return ds;
	}
	
	public static Application getApplicationByBuildNumber(Session session, Integer buildNumber) {
		if (buildNumber == null) {
			return null;
		}
		
		Criteria criteria = session.createCriteria(Application.class);
		criteria = criteria.add(Restrictions.eq("build", buildNumber));
		//unique result returns null if more than one application share the same build number
		try {
			return (Application) criteria.list().get(0);
		}
		catch (Exception e) {
			return null;
		}
	}
	

}
