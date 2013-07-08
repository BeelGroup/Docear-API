package org.sciplore.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fulltexts", propOrder = {
    "fulltext"
})
public class XmlFulltexts {
	
	@XmlElement(required = true)
    protected List<XmlFulltext> fulltext = new ArrayList<XmlFulltext>();
	@XmlAttribute(required = true)
    protected String href;
	
	public XmlFulltexts(){}
	
	public XmlFulltexts(String href){
		this.setHref(href + ExternalizedStrings.getString("XmlFulltexts.href")); //$NON-NLS-1$
	}
	
    public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public List<XmlFulltext> getFulltexts() {
		return fulltext;
	}
	
}
