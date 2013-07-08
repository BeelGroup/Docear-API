package org.sciplore.eventhandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.engine.EntityEntry;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.def.DefaultSaveOrUpdateEventListener;
import org.hibernate.property.BasicPropertyAccessor;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.proxy.HibernateProxy;
import org.sciplore.eventhandler.ResourceManager.DependencyItem;
import org.sciplore.eventhandler.ResourceManager.ResourceProperty;
import org.sciplore.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveOrUpdateHandler extends DefaultSaveOrUpdateEventListener {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger( DefaultSaveOrUpdateEventListener.class );
	
	private static final PropertyAccessor BASIC_PROPERTY_ACCESSOR = new BasicPropertyAccessor();

	public void onSaveOrUpdate(SaveOrUpdateEvent event) {
		final SessionImplementor source = event.getSession();
		final Object object = event.getObject();
		final Serializable requestedId = event.getRequestedId();

		if ( requestedId != null ) {
			//assign the requested id to the proxy, *before*
			//reassociating the proxy
			if ( object instanceof HibernateProxy ) {
				( ( HibernateProxy ) object ).getHibernateLazyInitializer().setIdentifier( requestedId );
			}
		}

		if ( reassociateIfUninitializedProxy( object, source ) ) {
			log.trace( "reassociated uninitialized proxy" );
			// an uninitialized proxy, noop, don't even need to
			// return an id, since it is never a save()
		}
		else {	
			if(object instanceof Resource) {
				// clear the Table before every new save attempt  
				if(Resource.getResourceContext().isTopLevel()) {
					Resource.getResourceContext().clearPeristenceTable();
				}
				// increase context depth 
				Resource.getResourceContext().incRecDepth();
				
//				System.out.println("");
//				System.out.println("onSaveOrUpdate: "+object.getClass());
				Resource toSync = (Resource) object;	
				
				if(Resource.getResourceContext().isPersistent(toSync)) {
					//System.out.println("isPersistent");
					Resource.getResourceContext().decRecDepth();
					return;
				}
				if(hasPersistentIdentity(event, toSync)) {
					if(toSync.getId() == null) {
						event.getSession().getPersistenceContext().removeEntry(toSync);
					}
					Resource.getResourceContext().decRecDepth();
					return;
				}
				
				if(ResourceManager.hasDependencies(toSync.getClass())) {					
					for(DependencyItem item : ResourceManager.getDependenciesFor(toSync.getClass()) ) {
//						System.out.println("process dependency: "+ item.getFieldName());
						try {
							
							Object obj = BASIC_PROPERTY_ACCESSOR.getGetter(toSync.getClass(), item.getFieldName()).get(toSync);
							if(obj instanceof Collection<?>) {
								Collection<Object> coll = ((Collection<Object>)obj);
								List<Object> persistentObjCache = new ArrayList<Object>();
								Iterator<Object> iter = coll.iterator();
								while (iter.hasNext()) {
									Object collObj = iter.next();
									if(collObj instanceof Resource) {
										Object value = handleDependencyResource(collObj, event, source, toSync, item);
										if(value != null) {
											iter.remove();
											persistentObjCache.add(value);
										}										
									}
								}
								for (Object pObj : persistentObjCache) {
									coll.add(pObj);
								}
							}
							else {
								Object value = handleDependencyResource(obj, event, source, toSync, item);
								if(value != null) {
									BASIC_PROPERTY_ACCESSOR.getSetter(toSync.getClass(), item.getFieldName()).set(toSync, value, source.getFactory());
								}
							}
							
						} 
						catch (MappingException e) {
							e.printStackTrace();
						} 
						catch (IllegalArgumentException e) {
							e.printStackTrace();
						}						
					}
					if(hasPersistentIdentity(event, toSync)) {
						if(toSync.getId() == null) {
							event.getSession().getPersistenceContext().removeEntry(toSync);
						}
						Resource.getResourceContext().decRecDepth();
						return;
					}
					if(!Resource.getResourceContext().isPersistent((Resource)toSync)) {
						if(!hasPersistentIdentity(event, (Resource)toSync)) {
							performSaveOrUpdate(event, source, toSync);
						}
						Resource.getResourceContext().addPersistentInstance(toSync, toSync);
						cascadeSaveOrUpdateResource(toSync, event.getSession());
					}
					
					
				} else {
					if(!Resource.getResourceContext().isPersistent((Resource)toSync)) {
						performSaveOrUpdate(event, source, toSync);					
						Resource.getResourceContext().addPersistentInstance(toSync, toSync);
						cascadeSaveOrUpdateResource(toSync, event.getSession());
					}
				}
				Resource.getResourceContext().decRecDepth();  
			} 
			else {
				performSaveOrUpdate(event, source, object);
			}
			
		}

	}
	
	private Object handleDependencyResource(Object obj, SaveOrUpdateEvent event, SessionImplementor source, Resource toSync, DependencyItem item) {
		Object value = null;
		if(obj != null && obj instanceof Resource) {
			if(!Resource.getResourceContext().isPersistent((Resource)obj)) {
				event.getSession().saveOrUpdate(obj);
			}
			value = (Resource) Resource.getResourceContext().getPersistentResource((Resource)obj);
		}
		else {
			if(obj == null) {
				return null;
			}
			event.getSession().saveOrUpdate(obj);
			EntityEntry entry = source.getPersistenceContext().getEntry(source.getPersistenceContext().unproxyAndReassociate( obj ));
			value = entry.getVersion();
		}
		return value;
		
	}

	private boolean hasPersistentIdentity(SaveOrUpdateEvent event, Resource toSync) {
		Resource res;
		if(( res = toSync.syncResource(event.getSession())) != null) {
			Resource.getResourceContext().addPersistentInstance(toSync, res);
			//System.out.println("found Persistent");				
			cascadeSaveOrUpdateResource(res, event.getSession());
			return true;
		}
		return false;
	}

	private void cascadeSaveOrUpdateResource(Resource target, Session session) {
		if(ResourceManager.getResourceMetadata(target.getClass()) == null) {
			return;
		}
		Iterator<ResourceProperty> iter = ResourceManager.getResourceMetadata(target.getClass()).getSets().iterator();
		while(iter.hasNext()) {
			if(Resource.getResourceContext().isOnCascadeStack(target)) {
				break;
			}
			Resource.getResourceContext().pushToCascadeStack(target);
			ResourceProperty property = iter.next();
//			System.out.println("Cascading: "+property.getFieldName());			
			cascadeSaveOrUpdateSet(property, target, session);
			Resource.getResourceContext().popFromCascadeStack();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void cascadeSaveOrUpdateSet(ResourceProperty property, Resource target, Session session) {
		List<Resource> resCache = new ArrayList<Resource>();
		Object value = property.getValue(target);
		if(value != null && value instanceof Set) {
			Iterator iter = ((Set)value).iterator();
			while(iter.hasNext()) {
				Resource res = (Resource)iter.next();
				if(res.getId() == null) {
					iter.remove();
					session.saveOrUpdate(res);
					if(Resource.getResourceContext().isPersistent(res)) {
						resCache.add((Resource)Resource.getResourceContext().getPersistentResource(res));
					}
				}
			}
			((Set)value).addAll(resCache);
		}
	}

	private void performSaveOrUpdate(SaveOrUpdateEvent event, SessionImplementor session, Object object) {
		//initialize properties of the event:
		final Object entity = session.getPersistenceContext().unproxyAndReassociate( object );
		
		event.setEntity( entity );
		event.setEntry( session.getPersistenceContext().getEntry( entity ) );
		event.setEntry(null);
		//return the id in the event object
		try {
			event.setResultId( performSaveOrUpdate( event ) );
		}
		catch (Throwable e) {
			//System.err.println("Error in SaveOrUpdateHandler.performSaveOrUpdate(): "+ e.getMessage());
			if ("could not insert: [org.sciplore.resources.DocumentXref]".equals(e.getMessage())) {
				System.out.println("STOP!");
			}
		}
	}
	
}
