package org.sciplore.xtract.xmlparsers;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sciplore.beans.Abstract;
import org.sciplore.beans.Author;
import org.sciplore.beans.Authors;
import org.sciplore.beans.Document;
import org.sciplore.beans.Title;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HeaderResultParser extends DefaultHandler {
	private Document doc;
	private Author author;
	private Authors authors;
	private boolean isTitle;
	private boolean isAbstract;
	private boolean isAuthor;
	private boolean isName;
	private boolean isInstitution;
	private boolean isEmail;
	private boolean isAddress;
	private boolean isKeyword;
	private short personCount;
	private String currentValue;

	public Document parse(File headerExtractionResult) {
		this.doc = new Document();
		this.isTitle = false;
		this.isAbstract = false;
		this.isAuthor = false;
		this.isInstitution = false;
		this.isName = false;
		this.isEmail = false;
		this.isAddress = false;
		this.isKeyword = false;
		this.personCount = 0;
		this.currentValue = "";

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = this;
			saxParser.parse(headerExtractionResult, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return doc;
	}

	public void startDocument() throws SAXException {
		authors = new Authors();
	}

	public void endDocument() throws SAXException {
		doc.addActiveElement(authors);
	}

	public void startElement(String namespaceURI, String localName,
		String qName, Attributes atts) throws SAXException {
		currentValue = "";
		if (qName.equals("title")) {
			isTitle = true;
		}
		if (qName.equals("abstract")) {
			isAbstract = true;
		}
		if (qName.equals("author")) {
			isAuthor = true;
			author = new Author();
			author.addActiveAttribute("rank", new Integer(++personCount).toString());
		}
		if (qName.equals("name") && isAuthor) {
			isName = true;
		}
		if (qName.equals("affiliation") && isAuthor) {
			isInstitution = true;
		}
		if (qName.equals("email") && isAuthor) {
			isEmail = true;
		}
		if (qName.equals("address") && isAuthor) {
			isAddress = true;
		}
		if (qName.equals("keyword")) {
			isKeyword = true;
		}
	}

	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		currentValue = currentValue.trim();
		if (currentValue.length() > 0) {
			if (isTitle) {
				doc.addActiveElement(new Title(currentValue.trim()));
			}
			if (isAbstract) {
				Abstract a = new Abstract();
				a.setValue(currentValue.trim());
				doc.addActiveElement(a);
			}
			if (isName && currentValue.length() <= 255) {
				author.setName(currentValue);
			}
			if (isEmail) {
//				TODO
//				person.addContact(new Contact(this.session, person,
//						currentValue.trim(), Contact.CONTACT_TYPE_EMAIL));
			}
			if (isInstitution && currentValue.trim().length() <= 255) {
//				TODO
//				person.setInstitution(new Institution(this.session,
//						currentValue.trim()));
			}
			if (isAddress) {
//				TODO
//				doc.setPublishedPlace(currentValue.trim());
			}
			if (isKeyword && currentValue.length() <= 255) {
//				TODO
//				doc.addKeyword(new Keyword(this.session, doc, currentValue
//						.trim(), Keyword.KEYWORD_TYPE_AUTHOR));
			}
			if (qName.equals("title")) {
				isTitle = false;
			}
			if (qName.equals("abstract")) {
				isAbstract = false;
			}
			if (qName.equals("author")) {
				isAuthor = false;
//				TODO
//				if (institution != null) {
//					person.setInstitution(institution);
//					institution = null;
//				}
				authors.add(author);
//				TODO
//				Editors
			}
		}
		if (qName.equals("name")) {
			isName = false;
		}
		if (qName.equals("affiliation")) {
			isInstitution = false;
		}
		if (qName.equals("email")) {
			isEmail = false;
		}
		if (qName.equals("address")) {
			isAddress = false;
		}
		if (qName.equals("keyword")) {
			isKeyword = false;
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		currentValue += new String(ch, start, length);
	}
}