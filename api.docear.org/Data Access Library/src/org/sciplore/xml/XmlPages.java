package org.sciplore.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pages", propOrder = {
    "value"
})
public class XmlPages {
	
	@XmlValue
    protected String value;
    @XmlAttribute(required = true)
    protected String href;
    
    public XmlPages(){}
    
    public XmlPages(String href){
    	this.setHref(href + ExternalizedStrings.getString("XmlPages.href")); //$NON-NLS-1$
    }
    
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}

}
