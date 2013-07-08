package org.sciplore.data;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameSeparator {
	public static String proofString;
	
	public NameComponents seperateName(String person) {		
		NameComponents components = new NameComponents();
		proofString = "";
		String fullName = cleanName(person);
		fullName = saveLastNamePrefix(fullName, components);
		fullName = saveLastNameSuffix(fullName, components);
		sortNames(fullName, components);
		
//		System.out.println(proofString);
		
		return components;
		
	}
	
	private void sortNames(String fullName, NameComponents components) {
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
	        			proofString += "Cannot split into first and last names";
	        		}
	    		}
			}
			if(!notLastName.isEmpty()){
				String[] nameParts = notLastName.split("[ -]+");
				List<String> names = Arrays.asList(nameParts);
				String middleNames = "";
				for(String name : names){
					if(names.indexOf(name) == 0){
						components.setFirstName(nameParts[0]);
						proofString += "First Name: "+nameParts[0]+"   ";
					}
					else{
						middleNames += name+" ";
					}
				}
				if(!middleNames.isEmpty()){					
					components.setMiddleName(middleNames.trim());
					proofString += "Middle Name: "+middleNames+"   ";
				}
			}
			if(!lastName.isEmpty()){				
				components.setLastName(lastName.trim());
				proofString += "Last Name: "+lastName+"   ";
			}
		}
		catch(IllegalStateException e){
			System.out.println("Matcher Exception");
		}
		catch(IndexOutOfBoundsException e){
			System.out.println("Matcher Exception");
		}
	}

	private String saveLastNameSuffix(String fullName, NameComponents components) {		
		try{
			Matcher matcher = Pattern.compile("(?i)^(.*) (jr|iii|iv)$", Pattern.CASE_INSENSITIVE).matcher(fullName);
			if(matcher.find()){
				String nameLastSuffix = matcher.group(2);
				fullName = matcher.group(1);
				nameLastSuffix = nameLastSuffix.trim();
				components.setLastNameSuffix(nameLastSuffix);
				proofString += "Last Name Suffix: "+nameLastSuffix+"   ";
			}
		}catch(IllegalStateException e){
			System.out.println("Matcher Exception");
		}
		catch(IndexOutOfBoundsException e){
			System.out.println("Matcher Exception");
		}
		return fullName;
	}

	private String saveLastNamePrefix(String fullName, NameComponents components){
		try{
			Matcher matcher = Pattern.compile(" ((van|de|del|da|do|el|la|di|von|der|le|zu) )+", Pattern.CASE_INSENSITIVE).matcher(fullName);
			if(matcher.find()){
				String lastNamePrefix = matcher.group();
				fullName = matcher.replaceAll(" ");
				lastNamePrefix = lastNamePrefix.trim();
				components.setLastNamePrefix(lastNamePrefix);
				proofString += "Last Name Prefix: "+lastNamePrefix+"   ";
			}
		}catch(IllegalStateException e){
			System.out.println("Matcher Exception");
		}		
		return fullName;
	}
	
	public String cleanName(final String name) {
		String fullName = name;
		proofString += "Full Name: "+fullName+"   ";
		fullName = fullName.replaceAll("[{}_()\\'\"?~*!@#$\\%^&]", "");
		fullName = fullName.replaceFirst("\\s+", " ");
		fullName = fullName.replaceFirst(" -|- ", " ");
		fullName = fullName.replaceFirst("^([A-Z])([A-Z]) ", "$1. $2.");
		fullName = fullName.replaceFirst("^([A-Z][a-z]+)\\.", "$1");
		fullName = fullName.replaceAll("\\. *", " ");		
		fullName = fullName.replaceFirst("^ +| +$", "");
		fullName = fullName.replaceFirst("\\s+", " ");
		proofString += "Cleaned Name: "+fullName+"   ";
		return fullName;
	}
	
	
	public class NameComponents {
		private String firstName;
		private String middleName;
		private String lastName;
		private String lastNamePrefix;
		private String lastNameSuffix;
		
		public String getFirstName() {
			return firstName;
		}
		
		public String getMiddleName() {
			return middleName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public String getLastNamePrefix() {
			return lastNamePrefix;
		}
		
		public String getLastNameSuffix() {
			return lastNameSuffix;
		}
		

		protected void setMiddleName(String middleName) {
			this.middleName = middleName;
		}
		
		protected void setLastName(String lastName) {
			this.lastName = lastName;
		}

		protected void setLastNamePrefix(String lastNamePrefix) {
			this.lastNamePrefix = lastNamePrefix;
		}
		
		protected void setLastNameSuffix(String lastNameSuffix) {
			this.lastNameSuffix = lastNameSuffix;
		}
		
		protected void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		
		
	}
}
