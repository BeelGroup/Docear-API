package org.sciplore.data.clean;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sciplore.resources.Person;

public class NameSeparator {
	
	public NameSeparator(){
	}
	
	public Person seperateName(String fullName){
		Person person = new Person();
		person.setNameFirst(fullName);
		return seperateName(person);
	}
	
	
	
	public Person seperateName(Person person){
		String fullName = cleanName(person);
		fullName = saveLastNamePrefix(fullName, person);
		fullName = saveLastNameSuffix(fullName, person);
		sortNames(fullName, person);
			
		return person;
		
	}
	
	private void sortNames(String fullName, Person person) {
		try{
			String notLastName = "";
			String lastName = "";
			// smith, john c
			Matcher matcher = Pattern.compile("(.+), (.+)").matcher(fullName);
			if(matcher.find()){
				notLastName = matcher.group(2);
			    lastName = matcher.group(1);
			}
			else{
				// smith j c
	            matcher = Pattern.compile("^(\\S{2,}) ((?:[a-z] )*[a-z])$").matcher(fullName);
	            if(matcher.find()){
	    			notLastName = matcher.group(2);
	    			lastName = matcher.group(1);
	    		}
	    		else{
	    			// j c smith
	            	matcher = Pattern.compile("^(.+?) (\\S+)$").matcher(fullName);
	            	if(matcher.find()){
	        			notLastName = matcher.group(1);
	        			lastName = matcher.group(2);
	        		}
	        		else{
	        			System.err.println("Cannot split into first and last names");
	        		}
	    		}
			}
			if(!notLastName.isEmpty()){
				String[] nameParts = notLastName.split("[ -]+");
				List<String> names = Arrays.asList(nameParts);
				String middleNames = "";
				for(String name : names){
					if(names.indexOf(name) == 0){
						person.setNameFirst(nameParts[0]);
						System.out.println("First Name: "+nameParts[0]+"   ");
					}
					else{
						middleNames += name+" ";
					}
				}
				if(!middleNames.isEmpty()){
					person.setNameMiddle(middleNames.trim());
					System.out.println("Middle Name: "+middleNames+"   ");
				}
			}
			if(!lastName.isEmpty()){
				person.setNameLast(lastName.trim());
				System.out.println("Last Name: "+lastName+"   ");
			}
		}
		catch(IllegalStateException e){
			System.out.println("Matcher Exception");
		}
		catch(IndexOutOfBoundsException e){
			System.out.println("Matcher Exception");
		}
	}

	private String saveLastNameSuffix(String fullName, Person person) {		
		try{
			Matcher matcher = Pattern.compile("(?i)^(.*) (jr|iii|iv)$", Pattern.CASE_INSENSITIVE).matcher(fullName);
			if(matcher.find()){
				String nameLastSuffix = matcher.group(2);
				fullName = matcher.group(1);
				nameLastSuffix = nameLastSuffix.trim();
				person.setNameLastSuffix(nameLastSuffix);
				System.out.println("Last Name Suffix: "+nameLastSuffix+"   ");
			}
		}catch(IllegalStateException e){
			System.out.println("Matcher Exception");
		}
		catch(IndexOutOfBoundsException e){
			System.out.println("Matcher Exception");
		}
		return fullName;
	}

	public String saveLastNamePrefix(String fullName, Person person){
		try{
			Matcher matcher = Pattern.compile(" ((van|de|del|da|do|el|la|di|von|der|le|zu) )+", Pattern.CASE_INSENSITIVE).matcher(fullName);
			if(matcher.find()){
				String lastNamePrefix = matcher.group();
				fullName = matcher.replaceAll(" ");
				lastNamePrefix = lastNamePrefix.trim();
				person.setNameLastPrefix(lastNamePrefix);
				System.out.println("Last Name Prefix: "+lastNamePrefix+"   ");
			}
			else{	
				if(fullName.split(" ").length > 2){
					System.out.println(fullName +"\n");						
				}				
			}
		}catch(IllegalStateException e){
			System.out.println("Matcher Exception");
		}		
		return fullName;
	}
	
	public String cleanName(Person person){
		String fullName = person.getNameFirst();
		System.out.println("Full Name: "+fullName+"   ");
		fullName = fullName.replaceAll("[{}_()\\'\"?~*!@#$\\%^&]", "");
		fullName = fullName.replaceFirst("\\s+", " ");
		fullName = fullName.replaceFirst(" -|- ", " ");
		fullName = fullName.replaceFirst("^([A-Z])([A-Z]) ", "$1. $2.");
		fullName = fullName.replaceFirst("^([A-Z][a-z]+)\\. ", "$1");
		fullName = fullName.replaceAll("\\. *", " ");		
		fullName = fullName.replaceFirst("^ +| +$", "");
		fullName = fullName.replaceFirst("\\s+", " ");
		System.out.println("Cleaned Name: "+fullName+"   ");
		return fullName;
	}

}
