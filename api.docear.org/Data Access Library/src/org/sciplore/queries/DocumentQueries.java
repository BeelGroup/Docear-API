package org.sciplore.queries;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentsPdfHash;
import org.sciplore.resources.Person;
import org.sciplore.tools.Tools;

public class DocumentQueries {
	
	public static Set<String> titleBlacklist;
	static {
		titleBlacklist = new HashSet<String>();
		titleBlacklist.add("privatecommunication");
		titleBlacklist.add("americanpsychiatricassociation");
		titleBlacklist.add("psychometrictheory");
		titleBlacklist.add("annualreport");
		titleBlacklist.add("introduction");
		titleBlacklist.add("revisionreceived");
		titleBlacklist.add("andcoauthors");
		titleBlacklist.add("casestudyresearch");
		titleBlacklist.add("organization");
		titleBlacklist.add("webconsortium");
		titleBlacklist.add("introduction");
		titleBlacklist.add("andcoauthors");
		titleBlacklist.add("casestudies");
		titleBlacklist.add("outoforder");
		titleBlacklist.add("availableat");
		titleBlacklist.add("diplomathesis");
		titleBlacklist.add("lastaccessedon");
		titleBlacklist.add("availability");
		titleBlacklist.add("introduction");
		titleBlacklist.add("applphyslett");
		titleBlacklist.add("datanotshown");
		titleBlacklist.add("arxiveprints");
		titleBlacklist.add("acasestudy");
		titleBlacklist.add("lastvisited");
		titleBlacklist.add("lastaccessed");
		titleBlacklist.add("lastvisited");
		titleBlacklist.add("availablefrom");
		titleBlacklist.add("academicwork");
		titleBlacklist.add("workinggroup");
		titleBlacklist.add("introductionquot");
		titleBlacklist.add("toappearin");
		titleBlacklist.add("organisation");
		titleBlacklist.add("pressrelease");
		titleBlacklist.add("applications");
		titleBlacklist.add("retrievedon");
		titleBlacklist.add("onlinecited");
		titleBlacklist.add("dissertation");
		titleBlacklist.add("onreferring");
		titleBlacklist.add("technologies");
		titleBlacklist.add("fromtheeditor");
		titleBlacklist.add("000â€“000Â©");
		titleBlacklist.add("ahistoryof");
		titleBlacklist.add("annualreview");
		titleBlacklist.add("lastaccessed");
		titleBlacklist.add("complexanalysis");
		titleBlacklist.add("foundationnsf");
		titleBlacklist.add("finalreport");
		titleBlacklist.add("importance");
		titleBlacklist.add("physicstoday");
		titleBlacklist.add("reportonthe");
		titleBlacklist.add("accessibility");
		titleBlacklist.add("worldwideweb");
		titleBlacklist.add("implementation");
		titleBlacklist.add("availability");
		titleBlacklist.add("augustseptember");
		titleBlacklist.add("handlingeditor");
		titleBlacklist.add("efficiency");
		titleBlacklist.add("polymengngsci");
		titleBlacklist.add("doctoralthesis");
		titleBlacklist.add("intensityof");
		titleBlacklist.add("testvalidation");
		titleBlacklist.add("llection");
		titleBlacklist.add("effectiveness");
		titleBlacklist.add("phdthesis");
		titleBlacklist.add("copyright");
		titleBlacklist.add("wwwnstiorg");
		titleBlacklist.add("dateaccessed");
		titleBlacklist.add("qmethodology");
		titleBlacklist.add("lecturenotes");
		titleBlacklist.add("mastersthesis");
		titleBlacklist.add("introduction");
		titleBlacklist.add("availability");
		titleBlacklist.add("mastersthesismasterthesis");
		titleBlacklist.add("emailtoauthor");
		titleBlacklist.add("availableat2");
		titleBlacklist.add("gendercambridge");
		titleBlacklist.add("surfeddate24th");
		titleBlacklist.add("faqversion137");
	}
	
	public static List<Document> getDocuments(Session session, Integer id, String source, String search, Date dlo, Date dhi, Integer start,
			Integer maxResults, Boolean fulltexturl_unavailable, Boolean indexed, Boolean pdf_url, Boolean ordered, Integer maxDlAttempts, Integer maxRank) {
		
		Criteria criteria = getDocumentsCriteria(session, id, source, search, dlo, dhi, start, maxResults, fulltexturl_unavailable, indexed, pdf_url, ordered, maxDlAttempts, maxRank);

		@SuppressWarnings("unchecked")
		List<Document> ds = (List<Document>) criteria.list();
		
		if (ds != null) {
			System.out.println("returning: "+ds.size()+" documents");
		}
		else {
			System.out.println("returning: null documents");
		}
		
		return ds;
	}
	
	private static Criteria getDocumentsCriteria (Session session, Integer id, String source, String search, Date dlo, Date dhi, Integer start,
			Integer maxResults, Boolean fulltexturl_unavailable, Boolean indexed, Boolean pdf_url, Boolean ordered, Integer maxDlAttempts, Integer maxRank) {
		
		Criteria criteria = session.createCriteria(Document.class, "doc");
		if (id!=null) {
			criteria = criteria.add(Restrictions.eq("id", id));
		}		
		if (fulltexturl_unavailable!=null && fulltexturl_unavailable) {
			criteria = criteria.add(Restrictions.isEmpty("fulltextUrls"));
		}
		if (search != null) {
			criteria = criteria.add(Restrictions.or(Restrictions.like("cleantitle", generateCleanTitle(search) + "%"),
					Restrictions.like("documentAbstract", search + "%")));
		}
		if (source != null || (dlo != null && dhi != null) || indexed != null || pdf_url != null) {
			int joinType;
			if (pdf_url == null) {
				joinType = CriteriaSpecification.FULL_JOIN;
			}
			else if (pdf_url) {
				joinType = CriteriaSpecification.INNER_JOIN;				
			}
			else {
				joinType = CriteriaSpecification.LEFT_JOIN;
			}
			
			if (pdf_url == null || pdf_url) {
				criteria = criteria.createCriteria("xrefs", "xrefs", joinType).setProjection(Projections.distinct(Projections.property("xrefs.document")));
			}
			else {
				criteria = criteria.createCriteria("xrefs", "xrefs", joinType);
			}
			
			if (maxDlAttempts != null) {
				criteria = criteria.add(Restrictions.le("dlAttempts", maxDlAttempts)); 
			}
			
			if (maxRank != null) {
				criteria = criteria.add(Restrictions.le("rank", maxRank));
			}
			
			if (source != null) {
				criteria = criteria.add(Tools.getDisjunctionFromString("source", source));
			}			
			if (dlo != null && dhi != null) {
				criteria = criteria.add(Restrictions.between("xrefs.releaseDate", dlo, dhi));
			}
			if (indexed != null) {				
				if (indexed) {
					criteria.add(Restrictions.eq("indexed", 1));						
				}
				else {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
					criteria = criteria.add(Restrictions.or(Restrictions.isNull("lastAttempt"), Restrictions.lt("lastAttempt", cal.getTime())));
					criteria = criteria.add(Restrictions.le("dlAttempts", 5));
					criteria.add(Restrictions.or(Restrictions.isNull("indexed"), Restrictions.eq("indexed", 0)));			
					criteria.addOrder(Order.asc("dlAttempts")).addOrder(Order.asc("lastAttempt")).addOrder(Order.desc("id"));
				}
			}
			if (pdf_url != null) {
				if (pdf_url) {
					criteria.add(Restrictions.isNotNull("xrefs.id"));
				}
				else {
					criteria.add(Restrictions.isNull("xrefs.id"));
				}
			}			
		}
		if (ordered!=null && ordered) {
			criteria = criteria.addOrder(Order.desc("doc.publishedYear")).addOrder(Order.desc("doc.publishedMonth"))
					.addOrder(Order.desc("doc.publishedDay")).addOrder(Order.desc("doc.id"));
		}
		
		if (maxResults != null) {
			criteria = criteria.setMaxResults(maxResults);
		}
		if (start != null) {
			criteria = criteria.setFirstResult(start);
		}
		
		return criteria;
	}
	
	public static Timestamp getLatestPublicationDate(Session session, String source) {
		Criteria criteria = session.createCriteria(DocumentXref.class);
		if (source != null) {
			criteria = criteria.add(Tools.getDisjunctionFromString("source", source));
		}
		return (Timestamp) criteria.setProjection(Projections.max("releaseDate")).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object[]> getDocumentNotIndexedDownloadUrls(Session session, Integer maxResults, Integer maxRank) {
		String sql = "SELECT x.id, x.document_id, x.sources_id FROM document_xref x WHERE x.dl_attempts<=5 AND (x.rank<=:maxRank OR x.rank IS NULL) AND (indexed IS NULL OR indexed=0) AND (last_attempt<:attemptDelay OR last_attempt IS NULL) ORDER BY x.dl_attempts ASC, x.rank ASC, x.last_attempt ASC, x.id DESC";
		Query query = session.createSQLQuery(sql);
		
		if (maxRank != null) {
			query.setParameter("maxRank", maxRank);
		}
		else {
			query.setParameter("maxRank", 100);
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		query.setParameter("attemptDelay", cal.getTime());
		
		
		if (maxResults != null) {
			query.setMaxResults(maxResults);
		}
		else {
			query.setMaxResults(1000);
		}
		
		return query.list();
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<Document> getDocumentsWithReferences(Session session, Person person, String source)
			throws NullPointerException {		

		Criteria criteria = session.createCriteria(DocumentPerson.class).add(Restrictions.eq("personMain", person))
				.createCriteria("document").setFetchMode("feedbacks", FetchMode.JOIN)
				.setFetchMode("fulltextUrls", FetchMode.JOIN).setFetchMode("xrefs", FetchMode.JOIN).createCriteria("xrefs");
		if (source != null) {
			criteria = criteria.add(Tools.getDisjunctionFromString("source", source));
		}
		criteria = criteria.setProjection(Projections.distinct(Projections.property("document")));

		return criteria.list();
	}
	
	
	//return null if generated title is too short
	public static String getValidCleanTitle(String title) {
		String clean = generateCleanTitle(title);
		if (clean == null || clean.length() < 11 || titleBlacklist.contains(clean)) {
			return null;
		}
		
		return clean;
	}
	
	/**
	 * Cleans a title by removing all characters apart from {@code A-Z},
	 * {@code a-z} and {@code 0-9} and makes it all lower case so the title can
	 * be compared more tolerantly. The cleaned title should always be used for
	 * title comparison instead of the real title.
	 * 
	 * @param title
	 *            the title
	 * @return the cleaned title
	 */
	public static String generateCleanTitle(String title) {
		if (title == null) {
			return null;
		}
		String clean = title.toLowerCase().replaceAll("^[^A-Za-z]*", "").replaceAll("[^A-Za-z0-9]", "").trim();
		
		String effectiveTitle = title.replace(" ", "");
		int effectiveLengthTitle = effectiveTitle.length();
		int effectiveLengthClean = clean.length();
		
		// clean title could not be generated correctly (e.g. because of cyrillic letters in the title) or if title was too short, use original title
		if (effectiveLengthClean < effectiveLengthTitle/2) {
			return effectiveTitle;
		}
		
		if (clean.length() > 1024) {
			clean = clean.substring(0, 1023);
		}
		
		return clean;
	}
	
	public static void main(String[] args) {
		System.out.println(getValidCleanTitle("private communication"));
	}

	/**
	 * Returns a Document from the database for a Document object.
	 * 
	 * @param d
	 *            the Document
	 * @return the Document object from the database or null if not found
	 */
	public static Document getDocument(Session session, Document d) {
		if (d.getId() != null) {
			return (Document) session.load(Document.class, d.getId());			
			// return getDocument(d.getId());
		}
		if (d.getTitle() != null) {
			return getDocument(session, d.getTitle()); // TODO: add author
														// matching
		}
		return null;
	}

	/**
	 * Returns the Document for an identifier.
	 * 
	 * @param id
	 *            the identifier
	 * @return the Document from the database or null if not found
	 */
	public static Document getDocument(Session session, Integer id) {
		Document d = (Document) session.get(Document.class, id);
		return d;
	}

	/**
	 * Returns a Document for a title. The cleantitle attribute is used for
	 * matching.
	 * 
	 * @param title
	 *            the title
	 * @return the Document from the database or null if not found
	 */
	public static Document getDocument(Session session, String title) {
		return (Document) session.createCriteria(Document.class).add(Restrictions.eq("cleantitle", generateCleanTitle(title)))
				.setMaxResults(1).uniqueResult();
	}

	public static Document getDocument(Session session, Integer id, String source) {
		if (source != null) {
			String[] filters = source.split(",");
			Disjunction disjunction = Restrictions.disjunction();
			for (String filter : filters) {
				disjunction.add(Restrictions.eq("source", filter));
			}

			Document d = (Document) session.createCriteria(Document.class, "doc").add(Restrictions.eq("id", id))
					.createCriteria("xrefs").add(disjunction).setMaxResults(1).uniqueResult();
			return d;
		} else {
			return getDocument(session, id);
		}
	}

	
	/*public static int getDistinctDocumentCountBySourceId(Session session, String source) {
		Criteria criteria = session.createCriteria(DocumentXref.class);
		if (source != null) {
			String[] filters = source.split(",");
			Disjunction disjunction = Restrictions.disjunction();
			for (String filter : filters) {
				disjunction.add(Restrictions.eq("source", filter));
			}

			criteria = session.createCriteria(DocumentXref.class).add(disjunction);
		} else {
			criteria = session.createCriteria(DocumentXref.class);
		}

		criteria.setProjection(Projections.distinct(Projections.property("documentId")));
		int result = criteria.list().size();

		return result;
	}*/
	
	@SuppressWarnings("unchecked")
	public static List<Document> getDocuments(Session session, List<Integer> ids) {
		if (ids.size() > 0) {
			return (List<Document>)session.createCriteria(Document.class).add(Restrictions.in("id", ids)).list();
		} else {
			return new ArrayList<Document>();
		}
	}
	
//	public static List<Document> getDocuments(Session session, String source, int start, int maxResults) {
//		return getDocuments(session, source, null, start, maxResults);
//	}
//
//	public static List<Document> getDocuments(Session session, String source, int maxResults) {
//		return getDocuments(session, source, null, null, maxResults);
//	}
//
//	public static List<Document> getDocuments(Session session, String source, String search, int maxResults) {		
//		return getDocuments(session, source, search, null, maxResults);
//	}
//
//	public static List<Document> getDocuments(Session session, String source, String search, Integer start, Integer maxResults) {
//		return getDocuments(session, source, search, null, null, start, maxResults, null);
//	}
//	
//	public static List<Document> getDocuments(Session session, Integer start, Integer maxResults, Boolean fulltexturl_unavailable) {
//		return getDocuments(session, null, null, null, null, start, maxResults, fulltexturl_unavailable);
//	}
//	
//	public static List<Document> getDocuments(Session session, String source, String search, Date dlo, Date dhi, Integer start,
//			Integer maxResults, Boolean fulltexturl_unavailable) {
//		return getDocuments(session, null, source, search, dlo, dhi, start, maxResults, fulltexturl_unavailable, null);
//	}
//	
//	
//
	
	public static List<Document> getDocuments(Session session) {
		return getDocuments(session, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}
	
//
//	public static List<Document> getDocuments(Session session, Integer maxResults) {
//		return getDocuments(session, null, null, null, maxResults);
//	}
//
//	public static List<Document> getDocuments(Session session, Integer start, Integer maxResults) {
//		return getDocuments(session, null, null, start, maxResults);
//	}
//
//	public static List<Document> getDocuments(Session session, String search) {
//		return getDocuments(session, null, search, null, null);
//	}
//	
//	public static List<Document> getDocuments(Session session, String search, Integer start, Integer maxResults) {
//		return getDocuments(session, null, search, start, maxResults);
//	}
//
//	public static List<Document> getDocuments(Session session, String source, Date dlo, Date dhi) {
//		return getDocuments(session, null, source, null, dlo, dhi, null, null, null, null);
//	}

	public static long getDocumentCount(Session session, String source) {
		if (source != null) {
			Criteria criteria = session.createCriteria(DocumentXref.class);
			criteria = criteria.add(Tools.getDisjunctionFromString("source", source));
			criteria = criteria.setProjection(Projections.distinct(Projections.property("xrefs.document")));

			return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
		} else {
			return (Long) session.createCriteria(Document.class).setProjection(Projections.rowCount()).uniqueResult();
		}
	}

	public static Long getDocumentCount(Session session, String source, Date dlo, Date dhi, Boolean fulltexturl_unavailable, Boolean indexed, Boolean pdf_url, Integer maxDlAttempts) {
		Criteria criteria = getDocumentsCriteria(session, null, source, null, dlo, dhi, null, null, fulltexturl_unavailable, indexed, pdf_url, null, maxDlAttempts, null);
		return (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
	}

	public static Document getDocumentByHashOrTitle(Session session, String hash, String title) {
		
		Document document = null;
		if (hash != null) {
			DocumentsPdfHash dph = DocumentsPdfHashQueries.getPdfHash(session, hash);
			if (dph != null) {
				document = dph.getDocument();
			}
		}
		
		if (document == null && title != null && title.trim().length()>0) {
			document = getDocument(session, title);
		}
		
		return document;
	}
	

}
