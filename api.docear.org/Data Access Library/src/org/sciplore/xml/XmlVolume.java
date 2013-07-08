package org.sciplore.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "volume", propOrder = {
    "value"
})
public class XmlVolume {
	
	@XmlValue
    protected String value;
    @XmlAttribute(required = true)
    protected String href;
    
    public XmlVolume(){}
    
    public XmlVolume(String href){
    	this.setHref(href + ExternalizedStrings.getString("XmlVolume.href")); //$NON-NLS-1$
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
