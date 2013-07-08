package org.sciplore.xml;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.sciplore.resources.Feedback;
import org.sciplore.resources.User;
import org.sciplore.tools.Tools;

@XmlRootElement(name="annotation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "annotation", propOrder = {
    "user",
    "title",
    "text",
    "children"
})
public class XmlAnnotation {

	protected XmlUser user;
    protected String title;
    protected XmlText text;
    protected XmlAnnotations children = new XmlAnnotations();
    @XmlAttribute
    protected XMLGregorianCalendar created;
    @XmlAttribute(required = true)
    protected String rating;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedLong")
    protected long parent;
    @XmlAttribute(required = true)
    protected String type;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedLong")
    protected Integer id;
    @XmlAttribute(required = true)
    protected String href;
    @XmlTransient
    private String baseUri;
    
    public XmlAnnotation(){}
    
    public XmlAnnotation(String href, Feedback feedback, String baseUri){
    	this.baseUri = baseUri;
    	this.setHref(href + "/" + feedback.getId());
    	this.setId(feedback.getId());
    	this.setCreated(Tools.getXMLGregorianCalendar(feedback.getCreated()));
    	if(feedback.getParent() != null){
    		this.setParent(feedback.getParent().getId());
    	}
    	this.setRating(feedback.getRating());
    	this.setTitle(feedback.getTitle());
    	this.setType(feedback.getType());
    	this.setText(feedback.getText());
    	this.setUser(feedback.getUser());
    	for(Feedback child : feedback.getChildren()){
    		this.getChildren().getAnnotations().add(new XmlAnnotation(href, child, this.baseUri));
    	}
    }

	public XmlUser getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = new XmlUser(user.getUsername(), this.baseUri);		
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public XmlText getText() {
		return text;
	}

	public void setText(String text) {
		this.text = new XmlText(this.getHref());
		this.text.setValue(text);
	}

	public XMLGregorianCalendar getCreated() {
		return created;
	}

	public void setCreated(XMLGregorianCalendar created) {
		this.created = created;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(Short rating) {
		this.rating = "" + rating;
	}

	public long getParent() {
		return parent;
	}

	public void setParent(long parent) {
		this.parent = parent;
	}

	public String getType() {
		return type;
	}

	public void setType(Short type) {
		switch(type){
		
		case 1:
			this.type = "standard";
			break;
		case 2:
			this.type = "found mistake";
			break;
		case 3:
			this.type = "criticsm";
			break;
		default:
			this.type = "";					
		
		}
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

	public XmlAnnotations getChildren() {
		return children;
	}
	
	public String getXML() throws JAXBException{
		StringWriter stringWriter = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(this.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(this, stringWriter);
		return stringWriter.getBuffer().toString();
	}	

}
