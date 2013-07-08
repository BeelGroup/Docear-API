package util.recommendations.xml;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

import org.sciplore.io.StringInputStream;

import xml.XmlBuilder;
import xml.XmlElement;
import xml.XmlRootElement;

public class XmlUserModelParser {
	private XmlElement root;
	
	private Map<String, String> meta = new HashMap<String, String>();
	private Map<String, String> keywordsMeta = new HashMap<String, String>();
	private Map<String, String> referencesMeta = new HashMap<String, String>();
	
	public XmlUserModelParser(String xml) throws UnsupportedEncodingException, ClassNotFoundException, InstantiationException, IllegalAccessException, XMLException {
		parse(xml);
	}
	
	private void parse(String xml) throws UnsupportedEncodingException, ClassNotFoundException, InstantiationException, IllegalAccessException, XMLException {		
		XmlBuilder xmlBuilder = new XmlBuilder();
		IXMLReader reader = new StdXMLReader(new InputStreamReader(new StringInputStream(xml), "UTF8"));
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		parser.setBuilder(xmlBuilder);
		parser.setReader(reader);
		parser.parse();
		root = (XmlRootElement) xmlBuilder.getRoot();
		
		meta = parseMeta("meta");
		keywordsMeta = parseMeta("keywords");
		referencesMeta = parseMeta("references");
	}

	public Collection<XmlElement> getKeywords() {
		return root.findAll("keyword");
	}
	
	public Collection<XmlElement> getReferences() {
		return root.findAll("reference");
	}
	
	private Map<String, String> parseMeta(String element_name) {		
		
		Iterator<XmlElement> iterator = root.findAll("meta").iterator(); 
		if (!iterator.hasNext()) {
			return null;			
		}
		
		XmlElement metaElement = iterator.next();		
		return metaElement.getAttributes();
	}

	public Map<String, String> getMeta() {
		return meta;
	}

	public Map<String, String> getKeywordsMeta() {
		return keywordsMeta;
	}

	public Map<String, String> getReferencesMeta() {
		return referencesMeta;
	}
	
	public Integer getMeta(String name) {
		String value = meta.get(name);
		if (value != null) {
			return new Integer(value);
		}
		
		return null;
	}
	
}
