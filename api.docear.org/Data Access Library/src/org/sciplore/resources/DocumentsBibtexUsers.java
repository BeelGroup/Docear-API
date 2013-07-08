package org.sciplore.resources;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.sciplore.queries.DocumentsBibtexUsersQueries;


@Entity
@Table(name="documents_bibtex_users")
public class DocumentsBibtexUsers extends Resource {
	
	@Column(nullable = false)
	Integer user_id;

	@Column(nullable = false)
	Integer counter;
	
	@Column(nullable = false)
	Date date;
	
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		return DocumentsBibtexUsersQueries.getDocumentsBibtexUser(this.getSession(), this.user_id);
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
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
}
