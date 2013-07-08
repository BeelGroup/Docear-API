package org.sciplore.database;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.cfg.Configuration;
import org.sciplore.resources.Resource;

public class MrDLibConfiguration extends Configuration {
	private static final long serialVersionUID = 1L;
	private Set<Class<? extends Resource>> annotatedClasses = new HashSet<Class<? extends Resource>>(); 
	private String configPath = null;
	
	public MrDLibConfiguration() {
		super();
	}
	
	public MrDLibConfiguration(String config) {
		super();
		this.configPath = config;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Configuration addAnnotatedClass(Class annotatedClass) {
		XClass xClass = getReflectionManager().toXClass( annotatedClass );
		metadataSourceQueue.add( xClass );
		annotatedClasses.add(annotatedClass);		
		return this;
	}
	
	public Set<Class<? extends Resource>> getAnnotatedClasses() {
		return this.annotatedClasses;
	}
	
	public Configuration configure() {
		if(configPath != null) {
			super.configure(configPath);
			return this;
		}
		return super.configure();		
	}

}
