package org.sciplore.resources;

import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import org.sciplore.database.SessionProvider;
import org.sciplore.eventhandler.ResourceManager;
import org.sciplore.eventhandler.ResourceManager.ResourceProperty;
import org.sciplore.rules.ModificationRule;
import org.sciplore.rules.RuleMapper;

@MappedSuperclass
public abstract class Resource extends BaseResource{
		
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(nullable = false)
	private Integer id;
	
	public void load() {
		if(!this.getSession().contains(this)){
			this.getSession().load(this, getId());
		}
	}	

	public Resource setId(Integer id) {
		this.id = id;
		return this;
	}

	public Integer getId() {
		return id;
	}
	
	public void save() {
		super.save();
	}
	
	/**
	 * 
	 * @return a persistent <code>Resource object</code> with similar properties to this, or <code>null</code> if no object could be found
	 */
	public abstract Resource getPersistentIdentity();
	
	/**
	 * {@inheritDoc}
	 */
	public Resource syncResource(Session session) {
		this.setSession(session);
		Resource persistentIdentity = getPersistentIdentity();
		
		if(persistentIdentity != null) {
			modifyByRules(persistentIdentity);
		}
		return persistentIdentity;
	}
	
	private void modifyByRules(Resource target) {
		Iterator<ResourceProperty> iter = ResourceManager.getResourceMetadata(this.getClass()).getProperties().iterator();
		while(iter.hasNext()) {
			ResourceProperty property = iter.next();
			modifyField(target, property);
		}
		// iterate through sets - different process than the other attributes
		iter = ResourceManager.getResourceMetadata(this.getClass()).getSets().iterator();
		while(iter.hasNext()) {
			ResourceProperty property = iter.next();
			if(property != null) {
				modifySet(property, target);
			}
		}
	}

	private void modifyField(Resource target, ResourceProperty property) {
		RuleMapper mapper = SessionProvider.ruleManager.getRuleMapper(this.getClass());
		if (mapper == null) {
			return;
		}
		switch (mapper.getRule(property.getFieldName())) {
			case ACCEPT: {
				doAccept(property, target);
				break;
			}
			case ACCEPT_PRIORITIZED: {					
				doPrioritizedAccept(property, target);
				break;
			}
			default: {
				//do nth - discard modification
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void modifySet(ResourceProperty property, Resource target) {
		RuleMapper mapper = SessionProvider.ruleManager.getRuleMapper(this.getClass());
		if (mapper == null) {
			return;
		}
		if(mapper.getRule(property.getFieldName()) == ModificationRule.DISCARD) {
			return;
		}
		Object valueTransient = property.getValue(this);
		Object valuePersistent = property.getValue(target);
		if(valueTransient != null && valueTransient instanceof Set && valuePersistent != null && valuePersistent instanceof Set) {
			Iterator<?> iter = ((Set<?>)valueTransient).iterator();
			while(iter.hasNext()) {
				((Set)valuePersistent).add(iter.next());
			}
		}
	}
	
	private void doAccept(ResourceProperty property, Resource target) {
		Object value = property.getValue(this);
		if(value != null) {
//			System.out.println(this.getClass()+": setValue("+property.getFieldName()+","+value+")");
			property.setValue(value, target, (SessionFactoryImplementor) this.getSession().getSessionFactory());
		}
	}
	
	protected void doPrioritizedAccept(ResourceProperty property, Resource target) {
		//TODO implement prioritized treatment
		doAccept(property, target);
	}
}
