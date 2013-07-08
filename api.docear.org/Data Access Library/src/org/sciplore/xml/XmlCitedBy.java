package org.sciplore.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cited_by", propOrder = {
    "document"
})
public class XmlCitedBy {
	
	@XmlElement(required = true)
    protected List<XmlDocument> document = new ArrayList<XmlDocument>();
	@XmlAttribute(required = true)
    protected String href;
    @XmlAttribute(required = true)
    protected int citationcount;
    
    public XmlCitedBy(){}
    
    public XmlCitedBy(String href){
    	this.setHref(href + ExternalizedStrings.getString("XmlCitedBy.href")); //$NON-NLS-1$
    }
	
    public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public int getCitationcount() {
		return citationcount;
	}
	public void setCitationcount(int citationcount) {
		this.citationcount = citationcount;
	}
	public List<XmlDocument> getCitingDocuments() {
		return document;
	}
}
