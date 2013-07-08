package org.sciplore.formatter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sciplore.annotation.SciBeanAlias;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class BeanConverter extends JavaBeanConverter {

	public BeanConverter(Mapper mapper) {
		super(mapper);
		
		// TODO Auto-generated constructor stub
	}
	
	public static String getStrippedUnicodeString(String s) {
		String replaced = s.replaceAll("\\p{InSpecials}", "");		
		return replaced;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
		Bean bean = (Bean)object;
		Map<String, Method> methods = getGetterMethods(object);
		for(String activeElements : bean.getActiveElements()){
			
			try{
				Method method = methods.get(activeElements.toLowerCase());
				if(method == null) continue;
				if(method.getReturnType() == String.class){					
					Object value = method.invoke(object);
					if(activeElements.equalsIgnoreCase("value") && object instanceof SimpleTypeElementBean){
						if(value != null){
							try {
								String v = getStrippedUnicodeString(value.toString());
								writer.setValue(v);
							}
							catch(Exception e) {
								System.out.println("org.sciplore.formatter.BeanConverter.marshal(object, writer, context): " + value.toString());
							}
						}
					}
					else{
						if(value == null){
							writer.addAttribute(activeElements.toLowerCase(), "");
						}
						else{
							writer.addAttribute(activeElements.toLowerCase(), /*StringEscapeUtils.escapeHtml4*/(value.toString()));
						}
					}
					continue;
				}
				if(canConvert(method.getReturnType())){
					Object value = method.invoke(object);
					if(value == null){
						writer.startNode(activeElements.toLowerCase());
						writer.endNode();						
					}
					else{
						writer.startNode(activeElements.toLowerCase());
						marshal(value, writer, context);
						writer.endNode();			
					}
					continue;
				}
				if(contains(method.getReturnType().getInterfaces(), "java.util.Collection")){
					Object value = method.invoke(object);
					if(value != null){						
						Collection<Object> values = (Collection<Object>)value;
						for(Object element : values){
							SciBeanAlias beanAlias = element.getClass().getAnnotation(SciBeanAlias.class);
							if(beanAlias != null) {
								writer.startNode(beanAlias.value());
							}
							else {
								writer.startNode(element.getClass().getSimpleName().toLowerCase());
							}
							marshal(element, writer, context);
							writer.endNode();
						}							
					}
					continue;
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			//super.marshal(object, writer, context);
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Method> getGetterMethods(Object object) {
		Map<String, Method> result = new HashMap<String, Method>();
		Class clazz = object.getClass();
		while(clazz != null && clazz != Object.class && clazz != Bean.class){			
			for (Method m : clazz.getDeclaredMethods()) {		
				if (!m.getName().startsWith("get")) {
					continue;
				}								
				String name = m.getName().toLowerCase().substring(3);
				result.put(name, m);
			}
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	
	private boolean contains(Class<?>[] classes, String s) {
		for (Class<?> c : classes) {
			//System.out.println(c.getName());
			if (c.getName().equals(s))
				return true;
		}
		
		return false;
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader arg0,
			UnmarshallingContext arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz) {
		while(clazz != null && clazz != Object.class){
			if(clazz == Bean.class){				
				return true;
			}
			/*if(clazz == SimpleTypeElementBean.class){				
				return false;
			}*/
			clazz = clazz.getSuperclass();
		}
		return false;
	}

}
