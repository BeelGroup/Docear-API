package org.sciplore.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "publishdate", propOrder = {
    "year",
    "month",
    "day"
})
public class XmlPublishdate {
	
	protected String year;
    protected String month;
    protected String day;
    @XmlAttribute(name = "date_published")
    protected String datePublished;
    
    public XmlPublishdate(){}
    
    public XmlPublishdate(String href){
    	this.setDatePublished(href + ExternalizedStrings.getString("XmlPublishdate.href")); //$NON-NLS-1$
    }
    
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getDatePublished() {
		return datePublished;
	}
	public void setDatePublished(String datePublished) {
		this.datePublished = datePublished;
	}
    
    
    
}
