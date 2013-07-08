package org.sciplore.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "authors", propOrder = {
    "author"
})
public class XmlAuthors {
	
	@XmlElement(required = true)
    protected List<XmlAuthor> author = new ArrayList<XmlAuthor>();
    @XmlAttribute(required = true)
    protected String href;
    
    public XmlAuthors(){}
    
    public XmlAuthors(String href){
    	setHref(href + ExternalizedStrings.getString("XmlAuthors.href")); //$NON-NLS-1$
    }
    
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public List<XmlAuthor> getAuthors() {
		return author;
	}
}
