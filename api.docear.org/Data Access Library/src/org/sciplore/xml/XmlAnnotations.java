package org.sciplore.xml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.sciplore.resources.Feedback;


@XmlRootElement(name="annotations")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "annotations", propOrder = {
    "annotation"
})
public class XmlAnnotations {

	@XmlElement(required = true)
    protected List<XmlAnnotation> annotation = new ArrayList<XmlAnnotation>();
    @XmlAttribute(required = true)
    protected String href;
    @XmlTransient
    private String baseUri;
    
    public XmlAnnotations(){}
    
    public XmlAnnotations(String href, String baseUri){
    	this.baseUri = baseUri;
    	this.setHref(href + "/annotations");
    }

    public void add(Set<Feedback> annotations){
    	for(Feedback annotation : annotations){
			if(annotation.getParent() == null){				
				this.getAnnotations().add(new XmlAnnotation(this.getHref(), annotation, this.baseUri));
			}		
    	}
	}	
    
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public List<XmlAnnotation> getAnnotations() {
		return annotation;
	}
	
	public String getXML() throws JAXBException{
		StringWriter stringWriter = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(this.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(this, stringWriter);
		return stringWriter.getBuffer().toString();
	}	
}
