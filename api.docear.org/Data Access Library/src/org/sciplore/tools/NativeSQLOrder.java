package org.sciplore.tools;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;

/**
 * Native SQL for Criteria {@link Order}.
 * 
 * Usage:
 * <pre> 
 * criteria.addOrder(NativeSQLOrder.asc("whatever SQL using {alias}"));
 * criteria.addOrder(NativeSQLOrder.desc("whatever SQL using {alias}"));
 * </pre>
 * 
 * @author Sami Dalouche
 * @author medon
 * 
 * @see http://opensource.atlassian.com/projects/hibernate/browse/HHH-2381
 * 
 */
public class NativeSQLOrder extends Order {
	private static final long serialVersionUID = 1L;
	private final static String PROPERTY_NAME = "uselessAnyways";
	private boolean ascending;
	private String sql;

	/**
	 * Constructor.
	 * @param sql
	 * @param ascending
	 */
	public NativeSQLOrder(String sql, boolean ascending) {
		super(PROPERTY_NAME, ascending);
		this.sql = sql;
		this.ascending = ascending;
	}

	@Override
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		StringBuilder fragment = new StringBuilder();
		fragment.append("(");
		this.applyAliases(fragment, criteria, criteriaQuery);
		fragment.append(")");
		fragment.append(ascending ? " asc" : " desc");
		return fragment.toString();
	}
	
	/**
	 * Substitute aliases in {@link #sql}.
	 * @param res
	 * @param criteria
	 * @param criteriaQuery
	 */
	private void applyAliases(StringBuilder res, Criteria criteria, CriteriaQuery criteriaQuery) {
		int i = 0;
		int cnt = this.sql.length();
		while (i < cnt) {
			int l = this.sql.indexOf('{', i);
			if (l == -1) {
				break;
			}

			String before = this.sql.substring(i, l);
			res.append(before);

			int r = this.sql.indexOf('}', l);
			String alias = this.sql.substring(l + 1, r);
			if (alias.isEmpty() || "alias".equals(alias)) { // root alias
				res.append(criteriaQuery.getSQLAlias(criteria));
			} else {
				String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, alias);
				if (columns.length != 1)
					throw new HibernateException("SQLAliasedCriterion may only be used with single-column properties: "
							+ alias);
				res.append(columns[0]);
			}
			i = r + 1;
		}
		String after = this.sql.substring(i, cnt);
		res.append(after);
	}
	
	public static Order asc(String sql) {
		return new NativeSQLOrder(sql, true);
	}

	public static Order desc(String sql) {
		return new NativeSQLOrder(sql, false);
	}

}