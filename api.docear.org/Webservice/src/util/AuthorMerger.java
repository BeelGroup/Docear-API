package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.sciplore.database.SessionProvider;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.Person;
import org.sciplore.resources.Resource;

public class AuthorMerger {
	
	public void merge(List<List<Integer>> duplicateAuthorsLists){	
		Session session = SessionProvider.sessionFactory.openSession();		
		if(session != null){
			try{
				List<Integer> deleteAuthors = new ArrayList<Integer>();
				for(List<Integer> duplicateAuthors : duplicateAuthorsLists){
					List<Person> duplicatePersons = getPersons(session, duplicateAuthors);
					Person personToKeep = getPersonToKeep(duplicatePersons);
					@SuppressWarnings("unused")
					Set<DocumentPerson> set = personToKeep.getDocuments();
					if(duplicatePersons.remove(personToKeep)){
						for(Person duplicatePerson : duplicatePersons){
							deleteIDs(session, duplicatePerson.getHomonyms());
							deleteIDs(session, duplicatePerson.getPersonXrefs());
							deleteIDs(session, duplicatePerson.getContacts());
							int saveId = duplicatePerson.getId();
							duplicatePerson.setId(null);
							session.evict(duplicatePerson);
							duplicatePerson.setNameFirst(personToKeep.getNameFirst());
							duplicatePerson.setNameLast(personToKeep.getNameLast());
							duplicatePerson.setNameLastPrefix(personToKeep.getNameLastPrefix());
							duplicatePerson.setNameMiddle(personToKeep.getNameMiddle());
							duplicatePerson.setNameLastSuffix(personToKeep.getNameLastSuffix());
							
													
							
							
							personToKeep.setSession(session);							
							personToKeep.load();												
							session.saveOrUpdate(duplicatePerson);							
							duplicatePerson = new Person(session).getPerson(saveId);
							
							for(DocumentPerson docPers : duplicatePerson.getDocuments()){
								docPers.setSession(session);
								//docPers.setPersonMain(personToKeep);
								docPers.save();
								
								
								personToKeep.getDocuments().add(docPers);
								personToKeep.save();
							}							
							deleteAuthors.add(duplicatePerson.getId());
							session.delete(duplicatePerson);
							session.flush();							
						}
					}
				}
				for(Integer id : deleteAuthors){
					Person person = new Person(session).getPerson(id);
					if(person != null){
						session.delete(person);
						session.flush();	
					}
				}
				
				
			}
			finally{
				tolerantClose(session);
			}
		}		
	}
	
	private static void deleteIDs(Session session, Set<? extends Resource> resources) {
		for(Resource resource : resources){
			resource.setId(null);
			session.evict(resource);
		}		
	}



	private Person getPersonToKeep(List<Person> duplicatePersons) {
		Person personToKeep = null;
		for(Person person : duplicatePersons){
			if(personToKeep == null){
				personToKeep = person;
				continue;
			}
			if(person.createNameComplete().length() > personToKeep.createNameComplete().length()){
				personToKeep = person;
				continue;
			}
			if(person.createNameComplete().length() == personToKeep.createNameComplete().length()){
				if(getNullNameFields(person) < getNullNameFields(personToKeep)){
					personToKeep = person;
					continue;
				}
			}
		}
		return personToKeep;
	}
	
	



	private int getNullNameFields(Person person) {
		int count = 0;
		if(person.getNameFirst() == null){
			count++;
		}
		if(person.getNameMiddle() == null){
			count++;
		}
		if(person.getNameLastPrefix() == null){
			count++;
		}
		if(person.getNameLast() == null){
			count++;
		}
		if(person.getNameLastSuffix() == null){
			count++;
		}
		return count;
	}
	
	@SuppressWarnings("unchecked")
	private List<Person> getPersons(Session session, List<Integer> duplicateAuthors) {
		Disjunction disjunction = Restrictions.disjunction();
		for(Integer id : duplicateAuthors){
			disjunction.add(Restrictions.eq("id", id));				
		}
		return (List<Person>)session.createCriteria(Person.class)
									.add(disjunction)
									.list();
	}



	public void tolerantClose(Session session){
	      if (session.isOpen()) {	          
	    	  try {
	        	  session.flush();
	              session.close();
	          } catch (HibernateException e) {
	        	  System.out.println(Tools.getStackTraceAsString(e));
	          }
	      }
	  }

}
