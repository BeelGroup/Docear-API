package org.sciplore.eventhandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.persistence.OneToMany;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.property.DirectPropertyAccessor;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;
import org.sciplore.resources.Resource;

public class ResourceManager {
	//private static final PropertyAccessor BASIC_PROPERTY_ACCESSOR = new BasicPropertyAccessor();
	private static final PropertyAccessor DIRECT_PROPERTY_ACCESSOR = new DirectPropertyAccessor();
	private static final Hashtable<Class<? extends Resource>, Set<DependencyItem>> table = new Hashtable<Class<? extends Resource>, Set<DependencyItem>>();
	private static final Hashtable<Class<? extends Resource>, ResourceMetadata> metadataTable = new Hashtable<Class<? extends Resource>, ResourceMetadata>();
	
	
	public static ResourceManager buildNewIndex(Set<Class<? extends Resource>> annotatedClasses) {
//		System.out.println("Building DependencyTable");
		table.clear();
		ResourceManager manager = new ResourceManager();
		for(Class<? extends Resource> clazz : annotatedClasses) {
			manager.addResourceMetadataFor(clazz);			
		}
		return manager;
	}
		
	private ResourceManager() {
	}
	
	public static Set<DependencyItem> getDependenciesFor(Class<? extends Resource> resource) {
		if(metadataTable.containsKey(resource) ) {
			return metadataTable.get(resource).getDependencies();
		}
		return null;
	}
	
	public static boolean hasDependencies(Class<? extends Resource> resource) {
		if(metadataTable.containsKey(resource) && metadataTable.get(resource).hasDependencies()) {
			return true;
		}
		return false;
	}
	
	public static ResourceMetadata getResourceMetadata(Class<? extends Resource> resource) {
		return metadataTable.get(resource);
	}
	
	private void addResourceMetadataFor(Class<? extends Resource> resource) {
		if(!metadataTable.containsKey(resource)) {
			metadataTable.put(resource, new ResourceMetadata(resource));
//			System.out.println("Resource ("+resource.getName()+") added");
		}
	}	
	
	public class DependencyItem {
		private final Class<? extends Resource> fieldType;
		private final String fieldName;
		
		public DependencyItem(Class<? extends Resource> type, String name) {
			this.fieldType = type;
			this.fieldName = name;
		}

		public Class<?> getFieldType() {
			return fieldType;
		}

		public String getFieldName() {
			return fieldName;
		}
	}
	
	
	
	
	public class ResourceProperty {		
		private final Field field;
		private final Getter getter;
		private final Setter setter;
		public ResourceProperty(Class<Resource> source, final Field field) {
			this.field = field;
			this.getter = DIRECT_PROPERTY_ACCESSOR.getGetter(source, field.getName());
			this.setter = DIRECT_PROPERTY_ACCESSOR.getSetter(source, field.getName());			
		}
		
		public Object getValue(Object target) {
			return getter.get(target);
		}
		
		public void setValue(Object value, Object target, SessionFactoryImplementor factory) {
			setter.set(target, value, factory);
		}

		public Field getField() {
			return field;
		}
		
		public String getFieldName() {
			return field.getName();
		}
	}
	
	public class ResourceMetadata {
		private final Class<? extends Resource> originClass;
		private final Set<ResourceManager.DependencyItem> dependencies = new HashSet<ResourceManager.DependencyItem>();
		private final Set<ResourceManager.ResourceProperty> properties = new HashSet<ResourceManager.ResourceProperty>();
		private final Set<ResourceManager.ResourceProperty> sets = new HashSet<ResourceManager.ResourceProperty>();
		
		
		public Set<ResourceManager.ResourceProperty> getProperties() {
			return properties;
		}
		
		public Set<ResourceManager.ResourceProperty> getSets() {
			return sets;
		}

		public ResourceMetadata(Class<? extends Resource> clazz) {
			this.originClass = clazz;
			determineMetadata();
		}
		
		public Set<ResourceManager.DependencyItem> getDependencies() {
			return dependencies;
		}		
		
		public boolean hasDependencies() {
			return (dependencies.size() > 0);
		}		
		
	
		@SuppressWarnings("unchecked")
		private void determineMetadata() {
			Field[] fields = originClass.getDeclaredFields();
			for(Field field : fields) {				
				if((field.getModifiers()&(Modifier.FINAL|Modifier.STATIC)) <= 0 ) {
					if( field.isAnnotationPresent(OneToMany.class) ) {
						sets.add(new ResourceProperty((Class<Resource>) this.originClass, field));
					}
					else {
						if(!Resource.class.isAssignableFrom(field.getType())) {
							properties.add(new ResourceProperty((Class<Resource>) this.originClass, field));
						}
					}
				}
				if(field.isAnnotationPresent(Required.class)) {				 
					try {
						Class<? extends Resource> dep = (Class<? extends Resource>) field.getType();
						dependencies.add(new DependencyItem(dep, field.getName()));
//						System.out.println("Dependency added ("+originClass.getName()+"): "+dep.getName());
					} 
					catch (ClassCastException ex) {
					}
					catch (Exception e) {	
					}				
				}
			}
		}

		
		
		
	}
	
	

}
