package org.sciplore.xml;

import java.sql.SQLException;
import java.util.Date;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.sciplore.resources.Publist;
import org.sciplore.resources.ResourceException;
import org.sciplore.tools.Tools;


@XmlRootElement(name="publist")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "publist", propOrder = {
    "name",
    "description",
    "entries"
})
public class XmlPublist extends XmlResource{
	
	@XmlAttribute(required = true)
	private XMLGregorianCalendar lastModified;
	@XmlAttribute(required = true)
	private XMLGregorianCalendar created;	
	@XmlAttribute(required = true)
	@XmlSchemaType(name = "unsignedLong")
	private long id;
	@XmlAttribute(required = true)
	private String href;
	private String name;
	private String description;
	//@XmlElement(required = true)
    //private Documents entries;
	
	public XmlPublist(){}
	
	public XmlPublist(Publist publist, String baseUri){
		this.setHref(baseUri + "/publist/" + publist.getId());
		this.setId(publist.getId());
		//this.setCreated(publist.g);
		this.setLastModified(publist.getLastmodified());
		this.setName(publist.getName());
		//this.setDescription(publist.get);
	}
	
	public XmlPublist(long id) throws SQLException, ResourceException, JAXBException{
		/*Connection connection = DataBaseConnection.getConnection();
		PreparedStatement statement = connection.prepareStatement("SELECT * FROM publist WHERE id = ?");
		statement.setLong(1, id);
		ResultSet result = statement.executeQuery();
		if(result.next()){
			this.setHref("http://api.spl.org/publist/" + id);
			this.setId(id);
			this.setCreated(new Date(result.getTimestamp("created").getTime()));
			this.setLastModified(new Date(result.getTimestamp("lastmodified").getTime()));
			this.setName(result.getString("name"));
			this.setDescription(result.getString("description"));
			statement = connection.prepareStatement("SELECT * FROM publist_document WHERE publist_id = ?");
			statement.setLong(1, id);
			ResultSet documents = statement.executeQuery();
			QueryDocument queryDocument = new QueryDocument(connection);
			Document document;
			while(documents.next()){
				long documentId = documents.getLong("document_id"); 				
		    	document = queryDocument.getDocumentBy(documentId);
		    	if(document != null){
		    		//his.getEntries().getDocument().add(document.getDocumentType(connection));
		    	}
			}
		}
		else{
			throw new ResourceException();
		}*/
	}	

	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public XMLGregorianCalendar getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = Tools.getXMLGregorianCalendar(created);
	}
	public XMLGregorianCalendar getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = Tools.getXMLGregorianCalendar(lastModified);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/*public void setEntries(Documents entries) {
		this.entries = entries;
	}

	public Documents getEntries() {
		if (entries == null) {
			entries = new Documents();
        }
        return this.entries;
	}	*/
}

