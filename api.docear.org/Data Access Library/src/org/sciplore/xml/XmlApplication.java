package org.sciplore.xml;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.sciplore.resources.Application;

@XmlRootElement(name="application")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "application", propOrder = {
			"name",
			"version",
			"versionStatus",
			"releaseNote",
			"priority"
})
public class XmlApplication {
	
	public final static int STABLE = 1;
	public final static int RELEASE_CANDIDATE = 2;
	public final static int BETA = 3;
	
	private String name;
	private String version;
	private Short versionStatus;
	private String releaseNote;
	private int priority;
	
	public XmlApplication(){}
	
	public XmlApplication(Application app){
		this.setName(app.getName());
		this.setVersion(app.getVersion());
		this.setVersionStatus(app.getVersionStatus());
		this.setReleaseNote(app.getReleaseNote());
		this.setPriority(app.getPriority());
	} 
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public Short getVersionStatus() {
		return versionStatus;
	}
	
	public void setVersionStatus(Short versionStatus) {
		this.versionStatus = versionStatus;		
	}
	
	public String getReleaseNote() {
		return releaseNote;
	}
	
	public void setReleaseNote(String releaseNote) {
		this.releaseNote = releaseNote;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getXML() throws JAXBException{
		StringWriter stringWriter = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(this.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(this, stringWriter);
		return stringWriter.getBuffer().toString();
	}
}
