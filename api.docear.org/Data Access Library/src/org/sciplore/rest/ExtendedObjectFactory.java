package org.sciplore.rest;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.sciplore.resources.Feedback;

public class ExtendedObjectFactory extends ObjectFactory {
	
	private static final String HOST = "http://dke113.cs.uni-magdeburg.de/rest";
	private String hrefPrefix;
	
	public ExtendedObjectFactory(String href) {
		hrefPrefix = HOST + href; 
	}
	
	public DocumentType getDocument(long id, String hash, String type){
		DocumentType documentType = this.createDocumentType();
		documentType.setHref(hrefPrefix);		
		documentType.setId(new BigInteger("" + id));
		documentType.setHash(hash);
		documentType.setType(type);
    	return documentType;
	}
	
	public TitleType getTitle(long id, String value){
    	TitleType titleType = this.createTitleType();
    	titleType.setHref(hrefPrefix + "/title");
    	titleType.setValue(value);
    	return titleType;
    }
	
	public AbstractType getAbstract(long id, String value){
		AbstractType abstractType = this.createAbstractType();
		abstractType.setHref(hrefPrefix + "/abstract");
		abstractType.setId(new BigInteger("" + id));
		abstractType.setValue(value);
    	return abstractType;
	}
	
	public StringBuffer getXML(DocumentType document) throws PropertyException, JAXBException{
		WebserviceType webserviceType = this.createWebserviceType();
		webserviceType.setDocument(document);
		return getXML(webserviceType);
	}
	
	public StringBuffer getXML(AnnotationsType annotations) throws PropertyException, JAXBException{
		WebserviceType webserviceType = this.createWebserviceType();
		webserviceType.setAnnotations(annotations);
		return getXML(webserviceType);
	}

	private StringBuffer getXML(WebserviceType webserviceType)throws JAXBException, PropertyException {
		StringWriter stringWriter = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance("org.sciplore.rest");
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(this.createWebservice(webserviceType), stringWriter);
		return stringWriter.getBuffer();
	}
	
	public TextType getTextType(int annotationID, String value){
		TextType textType = this.createTextType();
		textType.setHref(hrefPrefix + "/annotations/"+annotationID+"/text");
		textType.setValue(value);
		return textType;
	}
	
	public UserType getUserType(String username){
		UserType userType = this.createUserType();
		userType.setHref(HOST+"/user/"+username);
		userType.setValue(username);
		return userType;
	}
	
	public AnnotationType getAnnotationType(Feedback annotation){
		AnnotationType annotationType = this.createAnnotationType();
		annotationType.setHref(hrefPrefix + "/annotations/"+annotation.getId());
		annotationType.setId(new BigInteger(""+annotation.getId()));
		annotationType.setParent(new BigInteger(""+annotation.getParent()));
		annotationType.setRating(annotation.getRating());
		annotationType.setText(getTextType(annotation.getId(),annotation.getText()));
		annotationType.setTitle(annotation.getTitle());
		//annotationType.setType(annotation.getType());
		//annotationType.setUser(getUserType(annotation.getUser_name()));	
		return annotationType;
	}

	public AnnotationsType getAnnotationsType(List<Feedback> annotations) {
		AnnotationsType annotationsType = this.createAnnotationsType();
		annotationsType.setHref(hrefPrefix + "/annotations");
		List<AnnotationType> annotationTypes = new ArrayList<AnnotationType>();
		for(Feedback annotation : annotations){
			annotationTypes.add(getAnnotationType(annotation));
		}
		annotationsType.annotation = annotationTypes;
		return annotationsType;
	}

}
