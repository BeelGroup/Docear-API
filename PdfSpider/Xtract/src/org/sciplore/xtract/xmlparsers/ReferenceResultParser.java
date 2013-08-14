package org.sciplore.xtract.xmlparsers;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sciplore.beans.Author;
import org.sciplore.beans.Authors;
import org.sciplore.beans.Document;
import org.sciplore.beans.Occurence;
import org.sciplore.beans.Occurences;
import org.sciplore.beans.Reference;
import org.sciplore.beans.References;
import org.sciplore.beans.Title;
import org.sciplore.beans.Year;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

// Parser für die ParsCit Ergebnisse (inkl. hinzugefügter Counts)
public class ReferenceResultParser extends DefaultHandler {
	private String currentValue;
//	private Document pub;
	private Occurence occurence;
	private short rPersonCount;
	private References refs;
	private Reference ref;
	private Document rDoc;
	private Authors rAuthors;
	private Occurences rOccurences;
//	private Vector<Document> references;
	
	public ReferenceResultParser() {
	}

	public References parse(File referenceExtractionResult){
		refs = new References();
		rPersonCount = 0;
		currentValue = "";
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = this;
			saxParser.parse(referenceExtractionResult, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return refs;
	}
	
	public void startDocument() throws SAXException {
	}
	
	public void endDocument () throws SAXException {		
	}
	
	public void startElement (String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		currentValue = "";
		
		if (qName.equals("citation")) { 
			ref = new Reference();
			rDoc = new Document();
			rOccurences = new Occurences();
			rAuthors = new Authors();
			rPersonCount = 0;
		}
		
		if (qName.equals("author") || qName.equals("editor")) { 
			rPersonCount++;
		}

		if (qName.equals("context")) {
			occurence = new Occurence();
			for (int i=0; i<atts.getLength(); i++) {
				if (atts.getQName(i).equals("position")) { occurence.addActiveAttribute("character", atts.getValue(i)); }
				if (atts.getQName(i).equals("word")) { occurence.addActiveAttribute("word", atts.getValue(i)); }
				if (atts.getQName(i).equals("sentence")) { occurence.addActiveAttribute("sentence", atts.getValue(i)); }
				if (atts.getQName(i).equals("paragraph")) { occurence.addActiveAttribute("paragraph", atts.getValue(i)); }
//				if (atts.getQName(i).equals("page")) { occurence.addActiveAttribute("page", atts.getValue(i)); }
			}
		}
	}
	
	public void endElement (String namespaceURI, String localName, String qName) throws SAXException {
		currentValue = currentValue.trim();
		if (qName.equals("citation")) {
			rDoc.addActiveElement(rAuthors);
			ref.addActiveElement(rDoc);
			ref.addActiveElement(rOccurences);
			refs.add(ref);
		}
		if(currentValue.length() > 0) {
			if (qName.equals("author") && currentValue.length() <= 255) {
				Author a = new Author(currentValue.trim());
				a.setRank(new Integer(rPersonCount).toString());
				rAuthors.add(a);
			}
			
			if (qName.equals("title") && currentValue.length() <= 255) {
				currentValue = currentValue.replaceFirst("^(.*?)\\. In.*$", "");
				currentValue = currentValue.replaceFirst("([.,]|\\s)$", "");
				rDoc.addActiveElement(new Title(currentValue.trim()));
			}
			
			if (qName.equals("date")) {
				try {
					rDoc.addActiveElement(new Year(currentValue.trim()));
				} catch (NumberFormatException e) {
				}
			}
			
			if(qName.equals("editor")) {
//				TODO
//				pub.addPerson(new DocumentPerson(new Person(currentValue.trim()), DocumentPerson.DOCUMENTPERSON_TYPE_EDITOR, personCount));
			}
			
			if(qName.equals("institution") && currentValue.trim().length() <= 255) {
//				TODO
//				pub.setInstitution(new Institution(currentValue.trim()));
			}
			
			if (qName.equals("pages") && currentValue.trim().length() <= 50) {
//				TODO
//				pub.setPages(currentValue.trim());
			}
			
			if (qName.equals("publisher")) {
//				TODO
//				pub.setPublisher(currentValue.trim());
			}
			
			if(qName.equals("tech")) {
				// ignore
			}
			
			if (qName.equals("context")) {
				rOccurences.add(occurence);
			}
			
			if (qName.equals("journal") && currentValue.trim().length() < 1024) {
//				TODO
//				currentValue = currentValue.replaceFirst("^(In )", "");
//				Venue venue = new Venue();
//				venue.setName(currentValue);
//				venue.setType(Venue.VENUE_TYPE_JOURNAL);
//				pub.setVenue(venue);
			}
			
			if(qName.equals("location") && currentValue.length() <= 250) {
//				TODO
//				pub.setPublishedPlace(currentValue);
			}
			
			if(qName.equals("note")) {
				// ignore
			}
			
			if (qName.equals("booktitle") && currentValue.trim().length() < 1024) {
//				TODO
//				Document parent = new Document(currentValue.trim());
//				parent.setType("book");
//				Venue venue = new Venue();
//				venue.setName(currentValue.trim());
//				venue.setType(Venue.VENUE_TYPE_BOOK);
//				pub.setVenue(venue);
			}
			
			if (qName.equals("volume")) {
//				TODO
//				try {
//					pub.setVolume(currentValue);
//				} catch (NumberFormatException e) {
//				}
			}
			
			if (qName.equals("number")) {
//				TODO
//				try {
//					pub.setNumber(currentValue);
//				} catch (NumberFormatException e) {
//				}
			}
		}
	}
		
	public void characters(char[] ch, int start, int length) throws SAXException {
		currentValue += new String(ch, start, length);
	}
	
	public void warning(SAXParseException exception) throws SAXException 
	{
		Message("**Parsing Warning**\n", exception);
		throw new SAXException("Warning encountered");
	}

	public void error(SAXParseException exception) throws SAXException 
	{
		Message("**Parsing Error**\n", exception);
		throw new SAXException("Error encountered");
	}

	public void fatalError(SAXParseException exception) throws SAXException 
	{
		Message("**Parsing Fatal Error**\n", exception);
		throw new SAXException("Fatal Error encountered");
	}

	private void Message(String mode, SAXParseException exception) 
	{
		System.out.println("Reference");
		System.out.println(mode + " Line: " + exception.getLineNumber() + " URI: " + exception.getSystemId() + "\n" + " Message: " + exception.getMessage());
	}	
}