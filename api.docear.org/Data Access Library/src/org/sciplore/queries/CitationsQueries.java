package org.sciplore.queries;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Citation;
import org.sciplore.resources.Document;

public class CitationsQueries {

	public static boolean areCitationsAlreadyStored(Session session, Document document) {
		return !getCriteria(session, document).list().isEmpty();
	}
	
	//old method --> now it uses lucene
//	public static List<TFDocument> getBiboCouplingDocuments(Session session, List<Document> citedDocuments, Integer size) {
//		//select citing_document_id, count(distinct cited_document_id) as count from citations C JOIN documents D ON (C.cited_document_id = D.id) WHERE D.id IN (731002, 808278, 829221) GROUP BY citing_document_id ORDER BY count desc;
//		List<TFDocument> ret = null;
//		Criteria criteria = session.createCriteria(Citation.class);
//		if (citedDocuments == null || citedDocuments.size() == 0) {
//			return null;
//		}
//		try {    		
//    		criteria = criteria.add(Restrictions.in("citedDocument", citedDocuments));
//    		criteria = criteria.setProjection(Projections.projectionList()
//    		        .add(Projections.countDistinct("citedDocument"), "count")		        
//    		        .add(Projections.groupProperty("citingDocument"), "document")
//    		);		
//    		criteria = criteria.addOrder(Order.desc("count")).addOrder(NativeSQLOrder.asc("rand()"));
//    		if (size != null) {
//    			criteria = criteria.setMaxResults(size);
//    		}
//    		criteria = criteria.setResultTransformer(Transformers.aliasToBean(TFDocument.class));
//    		ret = criteria.list();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			HibernateHqlAndCriteriaToSqlTranslator translator =  new HibernateHqlAndCriteriaToSqlTranslator();
//			System.out.println("erroneous sql string: "+translator.toSql(criteria));
//		}
//		return ret;
//	}
	
	private static Criteria getCriteria(Session session, Document document) {
		Criteria criteria = session.createCriteria(Citation.class);
		
		if (document != null && document.getId() != null) {
			criteria.add(Restrictions.eq("citingDocument", document));
		}
		
		return criteria;
	}
	
	
}
