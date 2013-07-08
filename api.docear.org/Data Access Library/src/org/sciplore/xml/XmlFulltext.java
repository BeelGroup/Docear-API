package org.sciplore.xml;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fulltext", propOrder = {
    "value"
})
public class XmlFulltext {
	
	@XmlValue
    protected String value;
    @XmlAttribute(required = true)
    protected int licence;
    @XmlAttribute(required = true)
    protected String href;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedLong")
    protected Integer id;
    
    public XmlFulltext(){}
    
    public XmlFulltext(Integer id, String baseUri){
    	this.setHref(baseUri + ExternalizedStrings.getString("XmlFulltext.href") + id); //$NON-NLS-1$ //$NON-NLS-2$
    	this.setId(id);
    }
    
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getLicence() {
		return licence;
	}
	public void setLicence(int licence) {
		this.licence = licence;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

}
