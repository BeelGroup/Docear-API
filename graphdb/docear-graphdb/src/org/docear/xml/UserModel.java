package org.docear.xml;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class UserModel implements ISimpleXmlElement {
	private final DocumentBuilderFactory docFactory;
	private final DocumentBuilder docBuilder;
	private final Document doc;
	
	private final Variables variables = new Variables();
	private final Keywords keywords = new Keywords();
	private final References references = new References();
	
	public UserModel() throws ParserConfigurationException {
		docFactory = DocumentBuilderFactory.newInstance();
		docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();
	}
	
	public String getXml() throws TransformerException {
		doc.appendChild(getElement(doc));		
				
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StringWriter destination = new StringWriter();
		StreamResult result = new StreamResult(destination);

		transformer.transform(source, result);
		return destination.toString();
	}
			
	public void addVariable(String name, String value) {
		variables.addVariable(name, value);
	}

	public Variables getVariables() {
		return variables;
	}
	
	public Keywords getKeywords() {
		return keywords;
	}
	
	public References getReferences() {
		return references;
	}

	
	@Override
	public Element getElement(final Document doc) {
		Element element = doc.createElement("user_model");		
		
		if (variables != null) {
			element.appendChild(variables.getElement(doc));
		}
		if (keywords != null) {
			element.appendChild(keywords.getElement(doc));
		}
		if (references != null) {
			element.appendChild(references.getElement(doc));
		}
		
		return element;
	}
	
	
}
