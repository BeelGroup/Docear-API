package org.sciplore.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.sciplore.resources.Person;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "author", propOrder = {
    "nameComplete"
})
public class XmlAuthor {
	
	@XmlElement(name = "name_complete", required = true)
    protected String nameComplete;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedLong")
    protected Integer id;
    @XmlAttribute
    protected String href;
    
    public XmlAuthor() {}	
    
    public XmlAuthor(Integer id, String baseUri) {		
		this.setHref(baseUri + ExternalizedStrings.getString("XmlAuthor.href") + id); 
		this.setId(id);
	}
	public String getNameComplete() {
		return nameComplete;
	}
	
	public void setNameComplete(String nameComplete) {
		this.nameComplete = nameComplete;
	}
	
	public void setNameComplete(Person author) {
		StringBuilder builder = new StringBuilder();
		if(author.getNameFirst() != null){
			builder.append(author.getNameFirst() + " ");
		}
		if(author.getNameMiddle() != null){
			builder.append(author.getNameMiddle() + " ");
		}
		if(author.getNameLast() != null){
			builder.append(author.getNameLast());
		}
		this.setNameComplete(builder.toString());
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
