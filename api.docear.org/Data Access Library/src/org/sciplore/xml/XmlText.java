package org.sciplore.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "textType", propOrder = {
    "value"
})
public class XmlText {

	@XmlValue
    protected String value;
    @XmlAttribute
    protected String href;
    
    public XmlText(){}
    
    public XmlText(String href){
    	this.setHref(href + ExternalizedStrings.getString("XmlText.href")); //$NON-NLS-1$
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
