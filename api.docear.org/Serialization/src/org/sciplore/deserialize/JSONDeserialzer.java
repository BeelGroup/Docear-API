package org.sciplore.deserialize;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sciplore.data.clean.NameSeparator;
import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentPerson;
import org.sciplore.resources.Person;
import org.sciplore.resources.PersonHomonym;
import org.sciplore.resources.PersonXref;

public class JSONDeserialzer {
	private Session session; 
	
	public JSONDeserialzer(Session session) throws IOException {
		if(session == null || !session.isOpen()) {
			throw new IOException("invalid Hibernate session");
		}
		this.session = session;
	}
		
	public final Session getSession() {
		return this.session;	
	}
	
	public void parseDocuments(String json) throws IOException {
		JSONArray documents;
		try {
			documents = new JSONArray(json);
		} catch (JSONException e) {
			throw new IOException("no JSON format.");
		}
		parseDocuments(documents);
	}
	
	private void parseDocuments(JSONArray documents) throws IOException{
		for(int i = 0; i < documents.length(); i++){
			try {
				JSONObject document = documents.getJSONObject(i);
				getDocument(document);
			} catch (JSONException e) {
				System.out.println("document at index[" + i + "] is no JSONObject");
			}
		}		
	}

	public Document getDocument(JSONObject document) throws IOException {
		Document parsedDocument = new Document(getSession());
		try {		
			parsedDocument.setTitle(document.getString("title"));
			parsedDocument.setAbstract(document.getString("abstract"));			
			parsedDocument.setPersons(getAuthors(document.getJSONArray("authors"), parsedDocument));
			
//		Calendar calendar = this.getPublishedDate(document.getString("date"));
//		
//		System.out.println(calendar.toString());
//		document.setPublishedDay((short)calendar.get(Calendar.DATE));
//		document.setPublishedMonth("" + (calendar.get(Calendar.MONTH) + 1));
//		document.setPublishedYear((short)calendar.get(Calendar.YEAR));
//		document.setPublishedDate(calendar.getTime());
//		document.setDoi(getDoi(item));
		} 
		catch (JSONException e) {
			throw new IOException("JSON parsing error: " + e.getMessage());
		}
		//parsedDocument = new Document(getSession()).sync(parsedDocument);
		return parsedDocument;
	}
	
	private Set<DocumentPerson> getAuthors(JSONArray authors, Document doc) throws IOException {
		Set<DocumentPerson> result = new HashSet<DocumentPerson>();		
		for(int index=0; index < authors.length(); index++) {
			try {
				DocumentPerson parsedDocPerson = new DocumentPerson(getSession());
				parsedDocPerson.setDocument(doc);
				Person person = getAuthor(authors.getJSONObject(index));
				parsedDocPerson.setPersonHomonym(createPersonHomonym(person));
			} 
			catch (JSONException e) {
				throw new IOException("author is not a JSONObject.");
			}
		}
		return result;
	}
	
	private Person getAuthor(JSONObject author) throws IOException {		
		NameSeparator nameSepa = new NameSeparator();		
		try {
			Person person = nameSepa.seperateName(author.getString("author"));
			person.setSession(getSession());
			return person;
		} 
		catch (JSONException e) {
			throw new IOException("author name is not a String.");
		}
	}
	
	
	public void parsePersons(String json) throws IOException {
		JSONArray persons;
		try {
			persons = new JSONArray(json);
		} catch (JSONException e) {
			throw new IOException("no JSON format.");
		}
		parsePersons(persons);
	}


	public void parsePersons(JSONArray persons) throws IOException {
		getPersons(persons);
	}
	
	
	public Set<Person> getPersons(JSONArray persons) throws IOException {
		Set<Person> result = new HashSet<Person>();
		for(int i=0; i < persons.length(); i++) {
			try {
				result.add(getPerson(persons.getJSONObject(i)));				
			} 
			catch (JSONException e) {
				System.out.println("Person at index(" + i + ") is no JSONObject");
			}
			catch (IOException e) {
				System.out.println("Person at index(" + i + "): " + e.getMessage());
			}
		}
		return result;
	}
	
	
	public void parsePerson(String json) throws IOException {
		JSONObject person; 
		try {
			person = new JSONObject(json);						
		} 
		catch (JSONException e) {
			throw new IOException("no JSON format.");
		}
		parsePerson(person);
	}
	
	
	public void parsePerson(JSONObject person) throws IOException {
		getPerson(person);
	}
	
	public Person getPerson(JSONObject jsonPerson) throws IOException {
		Person person = new Person(getSession());
		 
		System.out.println("\nDeserialize person object (JSON) ...");
		Iterator<?> iter = jsonPerson.keys();			
		while(iter.hasNext()) {
			System.out.println("found key: " + iter.next());
		}		
		
		// the name field is absolutely required
		if(!jsonPerson.isNull("name")) {
			
			/*
			 * split name and create person
			 */
			NameSeparator nameSepa = new NameSeparator();
			System.out.println("try to separate name ...");
			try {
				person = nameSepa.seperateName(jsonPerson.getString("name"));
				person.setValid((short)1);
			} 
			catch (JSONException e) {
				throw new IOException("name is not a string type.");
			}
			
			/*
			 * try to find the name and sync
			 */
			person.addHomonym(createPersonHomonym(person));
			
			
			/* 
			 * sync to get id
			 */
			
			//person = person.sync(person);				
			
			/*
			 * read source string
			 * 
			 * TODO maybe source should be a required field? with a list of allowed strings?
			 */
			String authorSource = "probweb";
			if(!jsonPerson.isNull("source")) {
				try {
					authorSource = jsonPerson.getString("source");					
				} 
				catch (JSONException e) {
					throw new IOException("source is not a string type.");
				}
			}			 
			
			/*
			 * look for given day of birth
			 */			
			person.setDob(getDateField(jsonPerson, "birthday"));
			
			/*
			 * look for given day of death
			 */
			person.setDod(getDateField(jsonPerson, "deathday"));

			/*
			 * look for given links
			 */
			if(!jsonPerson.isNull("links")) {
				JSONArray links;
				try {
					links = jsonPerson.getJSONArray("links");
				} 
				catch (JSONException e) {
					throw new IOException("'links' is not an array.");
				}
				for(int i = 0; i < links.length(); i++) {
					JSONObject linkObj = null;
					try {
						linkObj = links.getJSONObject(i);
						PersonXref pXref = getPersonXref(person, authorSource, linkObj);					
						if(pXref != null && !person.getPersonXrefs().contains(pXref)) {
							person.addXref(pXref);
						}
					}
					catch (JSONException e) {
						//throw new IOException("'links' is not an array.");
						e.printStackTrace();
					}
					catch (IOException e) {
						System.out.println("skip link (" + e.getMessage() + "): '" + linkObj + "'");
					}
					
				}
			}
			
			/*
			 * look for given homonyms
			 * TODO not yet used
			 */
//			if(!jsonPerson.isNull("homonyms")) {
//				JSONArray aliases;
//				try {
//					aliases = jsonPerson.getJSONArray("homonyms");
//				} 
//				catch (JSONException e) {
//					throw new IOException("'homonyms' is not an array.");
//				}
//				Set<PersonHomonym> homonyms = getPersonHomonyms(aliases, person);
//				
//			}
			
			return person; 		
			
		} else {
			throw new IOException("name field not found");
		}
	}
	
	public Set<PersonHomonym> getPersonHomonyms(JSONArray homonyms, Person person) throws IOException {
		Set<PersonHomonym> result = new HashSet<PersonHomonym>();
		NameSeparator nameSepa = new NameSeparator();
		for(int i=0; i < homonyms.length(); i++) {
			try {
				Person p = nameSepa.seperateName(homonyms.getString(i));
				PersonHomonym ph = createPersonHomonym(p);
				ph.setPerson(person);
				result.add(ph);
				//result.add(new PersonHomonym(getSession()).sync(ph));
			} 
			catch (JSONException e) {
				System.out.println("PersonHomonym at index(" + i + ") is not a String");
			}
		}
		return result;
	}

	private PersonHomonym createPersonHomonym(Person person) {
		PersonHomonym pHomonym = new PersonHomonym(getSession());
		pHomonym.setNameFirst(person.getNameFirst());
		pHomonym.setNameMiddle(person.getNameMiddle());
		pHomonym.setNameLastPrefix(person.getNameLastPrefix());
		pHomonym.setNameLast(person.getNameLast());
		pHomonym.setNameLastSuffix(person.getNameLastSuffix());
		pHomonym.setPerson(person);
		pHomonym.setValid((short)1);
		
		return pHomonym;		
	}
	
	private Date getDateField(JSONObject json, String fieldName) throws IOException {
		/*
		 * allowed date formats
		 */
		DateFormat format_day = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat format_year = new SimpleDateFormat("yyyy");
		/*
		 * optional field
		 */
		if(!json.isNull(fieldName)) {
			Date day = null;
			// test against 1st date format
			try {
				day = format_day.parse(json.getString(fieldName));
			} 
			catch (Exception e) {
			}
			// if 1st failed
			if(day == null) {
				// test against 2nd date format
				try {
					day = format_year.parse(json.getString(fieldName));
				} 
				catch (Exception e) {
					throw new IOException("unknown format in '" + fieldName + "'");
				}
			}
			return day;
		}
		return null;
	}

	
	private PersonXref getPersonXref(Person person, String source, JSONObject xref) throws IOException {
		PersonXref personX = new PersonXref(getSession());
		personX.setPerson(person);
		personX.setSource(source);
		
		try {
			personX.setSourcesId(xref.getString("url"));
		}
		catch (JSONException e) {
			throw new IOException("'url' field is required in link item.");
		}
		String type;
		try {
			type = xref.getString("type");
		}
		catch (JSONException e) {
			throw new IOException("'url' field is required in link item.");
		}
		if(type.equalsIgnoreCase("photo")) {
			personX.setType((short)1);
		} 
		else if(type.equalsIgnoreCase("profile")) {
			personX.setType((short)2);
		} 
		else if(type.equalsIgnoreCase("homepage")) {
			personX.setType((short)3);
		} 
		else if(type.equalsIgnoreCase("biography")) {
			personX.setType((short)4);
		} 
		else if(type.equalsIgnoreCase("article")) {
			personX.setType((short)5);
		} 
		else if(type.equalsIgnoreCase("publications")) {
			personX.setType((short)6);
		}
		else if(type.equalsIgnoreCase("honor")) {
			parseHonor(person, xref);
			return null;
		}
		else {
			throw new IOException("unknown value for field 'type' (allowed: photo, profile, homepage, biography, article, publications).");
		}
		
		//person.addXref(personX);
		return personX;
	}

	
	private void parseHonor(Person person, JSONObject xref) {
		// TODO Auto-generated method stub
		System.out.println("Honor must be implemented");
	}
}
