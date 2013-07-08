package org.sciplore.xml;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.sciplore.resources.Document;

@XmlRootElement(name="documents")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documents", propOrder = {
    "document"
})
public class XmlDocuments {
	
	@XmlElement(required = true)
    protected List<XmlDocument> document = new ArrayList<XmlDocument>();
	
	public XmlDocuments(){}
	
	public XmlDocuments(List<Document> documents, String baseUri){
		for(Document document : documents){
			this.getDocuments().add(new XmlDocument(document, baseUri));
		}
	}

	public List<XmlDocument> getDocuments() {
		return document;
	}
	
	public String getXML() throws JAXBException{
		StringWriter stringWriter = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(this.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(this, stringWriter);
		return stringWriter.getBuffer().toString();
	}
	
	public String getXHTML() throws JAXBException, TransformerException{
		
		InputStream xsltFile = XmlResource.class.getResourceAsStream("document.xsl");
		
        Source xmlSource = new StreamSource(new StringReader(this.getXML()));
        Source xsltSource = new StreamSource(xsltFile);

        // the factory pattern supports different XSLT processors
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);
        StringWriter stringwriter = new StringWriter();
        trans.transform(xmlSource, new StreamResult(stringwriter));
        return stringwriter.toString();
	}

}
