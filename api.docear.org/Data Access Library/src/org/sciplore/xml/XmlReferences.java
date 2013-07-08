package org.sciplore.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "references", propOrder = {
    "document"
})
public class XmlReferences {
	
	@XmlElement(required = true)
    protected List<XmlDocument> document = new ArrayList<XmlDocument>();
	@XmlAttribute(required = true)
    protected String href;
	
	public XmlReferences(){}
	
	public XmlReferences(String href){
		this.setHref(href + ExternalizedStrings.getString("XmlReferences.href")); //$NON-NLS-1$
	}
	
    public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public List<XmlDocument> getReferences() {
		return document;
	}
	

}
