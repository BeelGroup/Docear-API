package org.sciplore.tools;


import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class SimpleRestrictions {
	
	public static Criterion eq(String propertyName, Object value) {
		if (value == null) {
			return Restrictions.isNull(propertyName);
		}
		else {
			return Restrictions.eq(propertyName, value);
		}
	}
}
