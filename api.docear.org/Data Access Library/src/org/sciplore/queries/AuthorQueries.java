package org.sciplore.queries;

import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Person;
import org.sciplore.resources.PersonXref;
import org.sciplore.tools.Tools;

public class AuthorQueries {

	public static Person getAuthor(Session session, Integer id, String source) throws NullPointerException {
		Person person;
		if (source != null) {
			System.out.println("debug filter: " + source);
			person = (Person) session.createCriteria(PersonXref.class).add(Tools.getDisjunctionFromString("source", source))
					.setFetchMode("person", FetchMode.JOIN).setProjection(Projections.distinct(Projections.property("person")))
					.createCriteria("person").setFetchMode("homonyms", FetchMode.JOIN).setFetchMode("xrefs", FetchMode.JOIN)
					.add(Restrictions.eq("id", id)).setMaxResults(1).uniqueResult();
		} else {
			System.out.println("debug no filter");
			person = (Person) session.createCriteria(Person.class).add(Restrictions.eq("id", id))
					.setFetchMode("homonyms", FetchMode.JOIN).setFetchMode("xrefs", FetchMode.JOIN).setMaxResults(1)
					.uniqueResult();
		}

		if (person == null) {
			throw new NullPointerException("Author not found.");
		}

		return person;
	}

	@SuppressWarnings("unchecked")
	public static List<Person> getAuthorsByLetter(Session session, String letter, String source) {
		List<Person> persons;

		if (source != null) {
//			String[] filters = source.split(",");

			persons = (List<Person>) session.createCriteria(PersonXref.class)
					.add(Tools.getDisjunctionFromString("source", source)).setFetchMode("person", FetchMode.JOIN)
					.setProjection(Projections.distinct(Projections.property("person"))).createCriteria("person")
					.setFetchMode("homonyms", FetchMode.JOIN).setFetchMode("xrefs", FetchMode.JOIN)
					.add(Restrictions.ilike("nameLast", letter, MatchMode.START)).addOrder(Order.asc("nameLast")).list();
		} else {
			persons = (List<Person>) session.createCriteria(Person.class)
					.add(Restrictions.ilike("nameLast", letter, MatchMode.START)).list();
		}
		
		if (persons == null || persons.size() == 0) {
			throw new NullPointerException("Authors not found.");
		}
		
		return persons;
	}

}
