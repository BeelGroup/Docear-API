package org.sciplore.tools;

import org.hibernate.Criteria;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;

public class CriteriaToSqlConverter {
	
	public static String generateSQL(Criteria criteria) {
		CriteriaImpl criteriaImpl = (CriteriaImpl)criteria;
		SessionImplementor session = criteriaImpl.getSession();
		SessionFactoryImplementor factory = session.getFactory();
		CriteriaQueryTranslator translator=new CriteriaQueryTranslator(factory,criteriaImpl,criteriaImpl.getEntityOrClassName(),CriteriaQueryTranslator.ROOT_SQL_ALIAS);
		String[] implementors = factory.getImplementors( criteriaImpl.getEntityOrClassName() );

		CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable)factory.getEntityPersister(implementors[0]), 
		                        translator,
		                        factory, 
		                        criteriaImpl, 
		                        criteriaImpl.getEntityOrClassName(), 
		                        session.getLoadQueryInfluencers()   );

		return walker.getSQLString();
    }

}
