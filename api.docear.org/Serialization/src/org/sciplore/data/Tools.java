package org.sciplore.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.sciplore.resources.Document;
import org.sciplore.resources.DocumentXref;
import org.sciplore.resources.DocumentXrefCategory;
import org.sciplore.resources.Feedback;
import org.sciplore.resources.Person;
import org.sciplore.resources.PersonXref;

public class Tools {
	
	public static PersonXref[] getSortedPersonXrefs(Set<PersonXref> personXrefs) {
		MultiValueMap<Short, PersonXref> multiMap = new MultiValueMap<Short, PersonXref>();
		
		for (PersonXref personXref : personXrefs) {
			multiMap.put(personXref.getType(), personXref);
		}
		
		return multiMap.getSortedList(true).toArray(new PersonXref[]{});
//		
//		PersonXref[] temppersonxrefs = new PersonXref[personXrefs.size()];
//		personXrefs.toArray(temppersonxrefs);
//		if(personXrefs.size() > 1){
//			boolean changed = false;
//			do{
//				changed = false;
//				for(int i = 0; i < temppersonxrefs.length - 1; i++){
//					if(temppersonxrefs[i+1].getType() == null){
//						break;
//					}
//					if(temppersonxrefs[i].getType() == null && temppersonxrefs[i+1].getType() != null){
//						PersonXref temppersonxref = temppersonxrefs[i];
//						temppersonxrefs[i] = temppersonxrefs[i+1];
//						temppersonxrefs[i+1] = temppersonxref;
//						changed  = true;
//						break;
//					}
//					if(temppersonxrefs[i].getType() < temppersonxrefs[i+1].getType()){
//						PersonXref temppersonxref = temppersonxrefs[i];
//						temppersonxrefs[i] = temppersonxrefs[i+1];
//						temppersonxrefs[i+1] = temppersonxref;
//						changed  = true;
//						break;
//					}					
//				}
//			}while(changed);
//		}
//		return temppersonxrefs;
	}
	
	public static DocumentXref[] getSortedDocumentXrefs(Set<DocumentXref> xrefs) {
		MultiValueMap<Date, DocumentXref> multiMap = new MultiValueMap<Date, DocumentXref>();

		for (DocumentXref xref : xrefs) {
			multiMap.put(xref.getReleaseDate(), xref);
		}
		
		return multiMap.getSortedList(true).toArray(new DocumentXref[]{});		
	}
	
	public static Feedback[] getSortedComments(Set<Feedback> feedbacks) {
		MultiValueMap<Date, Feedback> multiMap = new MultiValueMap<Date, Feedback>();
		
		for (Feedback feedback : feedbacks) {
			multiMap.put(feedback.getCreated(), feedback);
		}
		
		return multiMap.getSortedList(true).toArray(new Feedback[]{});
	}
	
//	public static DocumentPerson[] getSortedPersons(Set<DocumentPerson> persons) {
//		MultiValueMap<Short, DocumentPerson> multiMap = new MultiValueMap<Short, DocumentPerson>();
//		
//		for (DocumentPerson documentPerson : persons) {
//			multiMap.put(documentPerson.getRank(), documentPerson);
//		}
//		
//		System.out.println("sortedList");
//		
//		return multiMap.getSortedList(false).toArray(new DocumentPerson[]{});		
//	}
	
	public static List<DocumentXrefCategory> getSortedCategories(Set<DocumentXrefCategory> categories) {
		List<DocumentXrefCategory> result = new ArrayList<DocumentXrefCategory>();
		Map<Integer, List<DocumentXrefCategory>> sortedMap = new HashMap<Integer, List<DocumentXrefCategory>>();
		for(DocumentXrefCategory category : categories){
			Integer key = category.getSource().getId();
			if(sortedMap.containsKey(key)){
				sortedMap.get(key).add(category);
			}
			else{
				List<DocumentXrefCategory> list = new ArrayList<DocumentXrefCategory>();
				list.add(category);
				sortedMap.put(key, list);
			}
		}
		for(Integer key : sortedMap.keySet()){
			List<DocumentXrefCategory> list = sortedMap.get(key);
			DocumentXrefCategory[] tempCategories = new DocumentXrefCategory[list.size()];
			list.toArray(tempCategories);
			if(list.size() > 1){
				boolean changed = false;
				do{
					changed = false;
					for(int i = 0; i < tempCategories.length - 1; i++){
						if(tempCategories[i].getType() > tempCategories[i+1].getType()){
							DocumentXrefCategory tempCategory = tempCategories[i];
							tempCategories[i] = tempCategories[i+1];
							tempCategories[i+1] = tempCategory;
							changed  = true;
							break;
						}					
					}
				}while(changed);
			}
			result.addAll(Arrays.asList(tempCategories));
		}		
		return result;
	}
	
	public static String getNameComplete(Person person) {
		StringBuilder builder = new StringBuilder();
		if(person.getNameFirst() != null){
			builder.append(person.getNameFirst() + " ");
		}
		if(person.getNameMiddle() != null){
			builder.append(person.getNameMiddle() + " ");
		}
		if(person.getNameLast() != null){
			builder.append(person.getNameLast());
		}
		String result = builder.toString().trim();
		return result;
	}
	
	public static String getQueryParamsAsString(UriInfo uriInfo){
		MultivaluedMap<String, String> map = uriInfo.getQueryParameters();	
		String result = "?";
		for(Entry<String, List<String>> entry : map.entrySet()){
			if(entry.getValue().size() > 0){
				if(result != "?"){
					result = result + "&";
				}
				result = result + entry.getKey() + "=";
				for(String value : entry.getValue()){
					result = result + value;
					if(entry.getValue().indexOf(value) + 1 < entry.getValue().size()){
						result = result + "+";
					}
				}				
			}
		}
		if(result != "?"){
			return result;
		}
		else{
			return "";
		}
	}
	
	public static Map<String, String> getQueryParamsAsMap(UriInfo uriInfo){
		Map<String, String> result = new HashMap<String, String>();
		MultivaluedMap<String, String> map = uriInfo.getQueryParameters();	
		for(Entry<String, List<String>> entry : map.entrySet()){
			if(entry.getValue().size() > 0){
				String valueStr = "";
				boolean first = true;
				for(String value : entry.getValue()){
					if(!first) {
						valueStr += ";";
					}
					valueStr += value;
				}
				System.out.println("(mr-dlib) request param: " + entry.getKey() + "=" + valueStr);
				result.put(entry.getKey(), valueStr);
			}
		}
		return result;
	}
	
	public static String getDocumentHref(String baseUrl, Document document) {
		if (document == null || document.getId() == null) {
			return null;
		}
		return baseUrl + "documents/" + document.getId();
	}
	
	public static List<String> getAllowedURLRequestors() {
		List<String> list = new ArrayList<String>();
		File f = new File("conf/known_hosts.properties");
		
		if(f.exists()) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(f));
				String urls = props.getProperty("allow");
				for(String token : urls.split(";")) {
					list.add(token);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		return list;
	}
	
	public static String getRequestBase(URI requestUri) {
		StringBuffer ret = new StringBuffer();
		ret.append(requestUri.getScheme());
		ret.append("://");
		ret.append(requestUri.getAuthority());		
		ret.append("/");
		return ret.toString();
	}


}
