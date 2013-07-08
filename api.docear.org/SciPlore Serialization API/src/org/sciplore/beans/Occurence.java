package org.sciplore.beans;

import org.sciplore.formatter.SimpleTypeElementBean;

public class Occurence extends SimpleTypeElementBean {
	private String character;
	private String word;
	private String sentence;
	private String paragraph;
	private String chapter;
	
	public String getCharacter() {
		return character;
	}
	public void setCharacter(String character) {
		this.character = character;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getSentence() {
		return sentence;
	}
	public void setSentence(String sentence) {
		this.sentence = sentence;
	}
	public String getParagraph() {
		return paragraph;
	}
	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}
	public String getChapter() {
		return chapter;
	}
	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

}
