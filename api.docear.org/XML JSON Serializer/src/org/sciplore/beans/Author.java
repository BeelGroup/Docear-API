package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.Bean;

@SciBeanElements({"name_first","name_last","name_middle","name_last_prefix","name_last_suffix","homonyms","birthday","deathday","photo","authorxrefs"})
public class Author extends Bean{
	
	private String href;
	private String id;
	private Bean name_first;
	private Bean name_middle;
	private Bean name_last;
	private Bean name_last_prefix;
	private Bean name_last_suffix;
	private Bean homonyms;
	private Bean birthday;
	private Bean deathday;
	private Bean authorxrefs;
	private Bean photo;
	private Documents documents;
	
	
	public Documents getDocuments() {
		return documents;
	}

	public void setDocuments(Documents documents) {
		this.documents = documents;
	}
	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	
	public Bean getName_First() {
		return name_first;
	}

	public void setName_First(Bean name_first) {
		this.name_first = name_first;
					
	}

	public Bean getName_Middle() {
		return name_middle;
	}

	public void setName_Middle(Bean name_middle) {
		this.name_middle = name_middle;
		
	}

	public Bean getName_Last() {
		return name_last;
	}

	public void setName_Last(Bean name_last) {
		this.name_last = name_last;
		
	}
	
	public Bean getName_Last_Prefix() {
		return name_last_prefix;
	}

	public void setName_Last_Prefix(Bean name_last_prefix) {
		this.name_last_prefix = name_last_prefix;
		
	}
	
	public Bean getName_Last_Suffix() {
		return name_last_suffix;
	}

	public void setName_Last_Suffix(Bean name_last_suffix) {
		this.name_last_suffix = name_last_suffix;
		
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public void setHomonyms(Bean homonyms) {
		this.homonyms = homonyms;
	}

	public Bean getHomonyms() {
		return homonyms;
	}
	
	public Bean getBirthday() {
		return birthday;
	}

	public void setBirthday(Bean birthday) {
		this.birthday = birthday;
	}

	public Bean getDeathday() {
		return deathday;
	}

	public void setDeathday(Bean deathday) {
		this.deathday = deathday;
	}

	public Bean getAuthorxrefs() {
		return authorxrefs;
	}

	public void setAuthorxrefs(Authorxrefs authorxrefs) {
		this.authorxrefs = authorxrefs;
	}
	
	public Bean getPhoto() {
		return photo;
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
	}

	
	
	

}
