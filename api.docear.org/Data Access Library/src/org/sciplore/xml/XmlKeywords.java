package org.sciplore.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "keywords", propOrder = {
    "keyword"
})
public class XmlKeywords {
	
	@XmlElement(required = true)
    protected List<XmlKeyword> keyword = new ArrayList<XmlKeyword>();
    @XmlAttribute(required = true)
    protected String href;
    
    public XmlKeywords(){}
    
    public XmlKeywords(String href){
    	this.setHref(href + ExternalizedStrings.getString("XmlKeywords.href")); //$NON-NLS-1$
    }    
    
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public List<XmlKeyword> getKeywords() {
		return keyword;
	}
}
