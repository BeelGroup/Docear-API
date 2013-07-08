package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.sciplore.queries.DocumentsBibtexProxiesQueries;


@Entity
@Table(name="documents_bibtex_proxies")
public class DocumentsBibtexProxies extends Resource {
	
	@Column(nullable = false)
	private String label;
	
	@Column(nullable = false)
	private String ip;
	
	@Column(nullable = false)
	private Integer port;
	
	@Column(nullable = false)
	private Integer counter;
	
	@Column(nullable = false)
	private Date date;
	
	@Column(nullable = false)
	private int active;
	
	
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		return DocumentsBibtexProxiesQueries.getDocumentsBibtexQueries(this.getSession(),this.label);
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public Integer getPort() {
		return port;
	}


	public void setPort(Integer port) {
		this.port = port;
	}


	public Integer getCounter() {
		return counter;
	}


	public void setCounter(Integer counter) {
		this.counter = counter;
	}


	public Date getDate() {
		return date;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public int getActive() {
		return active;
	}


	public void setActive(int active) {
		this.active = active;
	}
	
	

}
