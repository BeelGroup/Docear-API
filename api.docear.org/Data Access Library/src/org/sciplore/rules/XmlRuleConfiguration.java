package org.sciplore.rules;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.XMLConfiguration;

public class XmlRuleConfiguration extends ModificationRuleManager{
		
	private void parseXmlFile(InputStream stream) {		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(stream);
			
			NodeList nodeList = dom.getElementsByTagName("entity");
			
			for(int i=0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NamedNodeMap nodeMap = node.getAttributes();
				if(node.hasChildNodes()) {
					handleAttributeRules(node, nodeMap);
				}
				else {
					ConfigurationRuleMapper mapper = new ConfigurationRuleMapper(getRule(nodeMap));
					addMapperToManager(nodeMap.getNamedItem("class").getTextContent(), mapper);	
				}
			}
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
	}
	
	private void handleAttributeRules(Node node, NamedNodeMap nodeMap) {		
		ConfigurationRuleMapper mapper = new ConfigurationRuleMapper(getRule(nodeMap));			
		NodeList nodeList = node.getChildNodes();
		for(int i=0; i < nodeList.getLength(); i++) {
			Node attrNode = nodeList.item(i);
			if(attrNode.getNodeType() == Node.ELEMENT_NODE) {
				NamedNodeMap attrMap = attrNode.getAttributes();
				mapper.addAttributeRule(attrMap.getNamedItem("name").getTextContent().toString(), getRule(attrMap));
			}
		}
		addMapperToManager(nodeMap.getNamedItem("class").getTextContent(), mapper);	
	}
	
	private void addMapperToManager(String className, RuleMapper mapper) {
		try {
			this.addRuleMapper(Class.forName(className), mapper);
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	private ModificationRule getRule(NamedNodeMap attrMap) {
		Attr action;
		if((action = (Attr) attrMap.getNamedItem("action")) != null) {
			if(action.getValue().equalsIgnoreCase("accept")) {
				if(attrMap.getNamedItem("if_priority") != null) {
					return ModificationRule.ACCEPT_PRIORITIZED;
				} 
				else {
					return ModificationRule.ACCEPT;
				}
			} 
			else if(action.getValue().equalsIgnoreCase("accept-all")) {
				return ModificationRule.ACCEPT;
			}
		}
		return ModificationRule.DISCARD;
	}

	public static ModificationRuleManager getRuleManager(String uri) {
		System.out.println("debug getRuleManager");
		XmlRuleConfiguration manager = new XmlRuleConfiguration();
		try {
			manager.parseXmlFile(XmlRuleConfiguration.class.getResourceAsStream(uri));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return manager;
	}
}
