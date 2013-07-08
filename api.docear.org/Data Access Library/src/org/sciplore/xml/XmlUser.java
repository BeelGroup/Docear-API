package org.sciplore.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "user", propOrder = {
    "value"
})
public class XmlUser {

	@XmlValue
    protected String value;
    @XmlAttribute
    protected String href;
    
    public XmlUser(){}
    
    public XmlUser(String username, String baseUri){
    	this.setHref(baseUri + ExternalizedStrings.getString("XmlUser.href") + username); //$NON-NLS-1$ //$NON-NLS-2$
    	this.setValue(username);
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
