package org.docear.mailer;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class XmlUtils {
	
public static Document getXMLDocument(InputStream is) {
		
		// instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        return db.parse(is);	        
	    } catch (Exception e) {
			e.printStackTrace();
		}
	    return null;
		
	}

	/**
	 * @param dom
	 */
	public static String getXMLStr(Document dom) {
		StringWriter out = new StringWriter();
		Transformer transf;
		try {
			transf = TransformerFactory.newInstance().newTransformer();
			transf.setOutputProperty(OutputKeys.INDENT, "yes");
			transf.setOutputProperty(OutputKeys.METHOD, "xml");
		    transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		    
		    transf.transform(new DOMSource(dom), new StreamResult(out));
		    
		    return out.toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static String normalizeStr(String str) {
		return str == null ? "" : str.trim();
	}

}
