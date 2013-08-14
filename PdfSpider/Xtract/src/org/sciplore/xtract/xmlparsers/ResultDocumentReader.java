package org.sciplore.xtract.xmlparsers;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.sciplore.xtract.result.XMLParagraphDocument;
import org.sciplore.xtract.resultstructure.ResultXMLDocument;
import org.sciplore.xtract.resultstructure.TextContainer;

public class ResultDocumentReader extends DefaultHandler {
	protected static final String DOCUMENT = "document";
	protected static final String TITLE = "title";
	protected static final String PARAGRAPH = "Paragraph";
	protected static final String UNDEFINED = "undefined";
	
	protected static final String ID = "id";
	protected static final String NAME = "name";
	
	private ResultXMLDocument currResXmlDoc;
	private TextContainer currTextContainer;
	
	boolean elementOpen = false;
	boolean titleOpen = false;
	public ResultDocumentReader(File in, File out) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(in, this);
		} catch(SAXException ex) {
			ex.printStackTrace();
		} catch(IOException ix) {
			ix.printStackTrace();
		} catch(ParserConfigurationException jx) {
			jx.printStackTrace();
		}
		XMLParagraphDocument xmlPara = new XMLParagraphDocument(currResXmlDoc);
		xmlPara.generateFile(out);
	}
	public void startDocument() throws SAXException {
		currResXmlDoc = new ResultXMLDocument();
		currTextContainer = new TextContainer();
	}
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equals(PARAGRAPH)) {
			elementOpen = true;
			currTextContainer.setParagraph();
			if(attributes.getLength() > 0) {
				currTextContainer.setName(attributes.getValue(0));
			}
		}
		else if(qName.equals(UNDEFINED)) {
			elementOpen = true;
			currTextContainer.setUndefined();
		}
		else if(qName.equals(DOCUMENT)) {
			if(attributes.getLength() > 0) {
				String id = attributes.getValue(0);
				currResXmlDoc.setID(id);
			}
		}
		else if(qName.equals(TITLE)) {
			titleOpen = true;
			elementOpen = true;
			currTextContainer.setTitle();
		}
	}
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals(PARAGRAPH)) {
			elementOpen = false;
			currResXmlDoc.addTextContainer(currTextContainer);
			currTextContainer = new TextContainer();
		}
		else if(qName.equals(UNDEFINED)) {
			elementOpen = false;
			currResXmlDoc.addTextContainer(currTextContainer);
			currTextContainer = new TextContainer();
		}
		else if(qName.equals(TITLE)) {
			titleOpen = false;
			currResXmlDoc.addTextContainer(currTextContainer);
			currTextContainer = new TextContainer();
		}
	}
	public void characters(char[] ch, int start, int length) throws SAXException {
		String s = new String(ch, start, length);
		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		if(titleOpen) {
			currResXmlDoc.setTitle(currResXmlDoc.getTitle() + s);
		}
		if(elementOpen) {
			currTextContainer.setTextContent(currTextContainer.getTextContent() + s);
		}
	}
}
