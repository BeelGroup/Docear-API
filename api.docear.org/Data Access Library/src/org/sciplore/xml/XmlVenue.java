package org.sciplore.xml;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "venue", propOrder = {
    "value"
})
public class XmlVenue {
	
	@XmlValue
    protected String value;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedLong")
    protected Integer id;
    @XmlAttribute(required = true)
    protected String href;
    
    public XmlVenue() {}
    
    public XmlVenue(String href) {
		this.setHref(href + ExternalizedStrings.getString("XmlVenue.href")); //$NON-NLS-1$
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
    
}
