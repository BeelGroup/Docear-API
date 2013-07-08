package org.sciplore.deserialize.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sciplore.io.StringInputStream;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlResourceReader<K> extends ObjectReader {

	public XmlResourceReader(ObjectCreatorMapper creatorMapper) {
		super(creatorMapper);
	}

	/**
	 * {@inheritDoc}
	 */
	protected Document getDom(InputStream is) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			//Using factory get an instance of document builder			
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file			 
			Document doc = db.parse(is);
//			Document doc = db.parse("sample.xml");
			doc.normalize();
			return doc;
		}
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch(SAXException se) {
			se.printStackTrace();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param file
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public K parse(File file) {
		try {
			return (K)parse(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param uri
	 * @return
	 */
	public K parse(String uri) {
		return parse(new File(uri));
	}
	
	/**
	 * @param uri
	 * @return
	 */
	public K parse(URI uri) {
		return parse(new File(uri));
	}
	
	/**
	 * @param content
	 * @param charsetName
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@SuppressWarnings("unchecked")
	public K parseContent(String content, String charsetName) throws UnsupportedEncodingException {
		return (K)parse(new StringInputStream(content, charsetName));
	}
}
