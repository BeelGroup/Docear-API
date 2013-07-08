package org.sciplore.resources;

import java.io.Serializable;

import org.hibernate.Session;
import org.sciplore.eventhandler.ResourceContext;

public abstract class BaseResource  {
	
	private Session session;
	private boolean	synchLock = false;
	private static final ResourceContext resourceContext = new ResourceContext();
	
	public void save() {
		this.session.saveOrUpdate(this);
		this.session.flush();
	}
	
	public void load(Serializable id) {
		if(!this.session.contains(this)){
			this.session.load(this, id);
		}
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}
	
	protected void lock() {
		this.synchLock = true;
	}
	
	protected void unlock() {
		this.synchLock = false;
	}
	
	public boolean isLocked() {
		return this.synchLock;
	}
	
	public static ResourceContext getResourceContext() {
		return resourceContext;
	}	
}
