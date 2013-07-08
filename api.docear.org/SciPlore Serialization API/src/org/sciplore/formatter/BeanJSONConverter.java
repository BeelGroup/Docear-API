package org.sciplore.formatter;

import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sciplore.annotation.SciBeanImplicitValue;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class BeanJSONConverter extends JavaBeanConverter {
	private XStream context;
	
	public BeanJSONConverter(Mapper mapper, XStream xContext) {
		super(mapper);
		this.context = xContext;
	}
	
	@SuppressWarnings("unchecked")
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
		if(object instanceof Bean) {
			Bean bean = (Bean)object;
			Map<String, Method> methods = getGetterMethods(object);
			java.text.DecimalFormat escape;
			escape = new java.text.DecimalFormat();
			escape.applyPattern("\\u0000");
			
			
			for(String activeElements : bean.getActiveElements()) {				
				try{
					Method method = methods.get(activeElements.toLowerCase());
					if(method == null) continue;				
					if(method.getReturnType() == String.class) {
						Object value = method.invoke(object);						
						if(activeElements.equalsIgnoreCase("value") && object instanceof SimpleTypeElementBean) {
							if(object.getClass().isAnnotationPresent(SciBeanImplicitValue.class) && object.getClass().getAnnotation(SciBeanImplicitValue.class).value().equals(activeElements) ) {
								writer.setValue(unicodeEscape(value.toString()));
								continue;
							} else {
								writer.startNode("text");
							}
						}
						else{
							writer.startNode(activeElements.toLowerCase());
						}
						if(value != null){
							writer.setValue(unicodeEscape(value.toString()));
						} else {
							writer.setValue("");
						}
						if(activeElements.equalsIgnoreCase("value") && object instanceof SimpleTypeElementBean){
							writer.endNode();
						}
						else{
							writer.endNode();
						}
						continue;
					}
					if(canConvert(method.getReturnType())) {
						Object value = method.invoke(object);
						
						if(value == null){
							writer.startNode(activeElements.toLowerCase());
							writer.endNode();						
						}
						else{
							writer.startNode(activeElements.toLowerCase());
							marshal(value, writer, context);
							if(context.get("list") != null) {
								ObjectOutputStream oos = this.context.createObjectOutputStream(writer, activeElements.toLowerCase());
								oos.writeObject(context.get("list"));
								oos.flush();
								context.put("list", null);
							}
							writer.endNode();			
						}
						continue;
					}
					if(contains(method.getReturnType().getInterfaces(), "java.util.Collection")){
						Object value = method.invoke(object);
						if(value != null){
							Collection<Object> values = (Collection<Object>)value;
							context.put("list", values);
						}
						continue;
					}
					
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}		
		}
	}
	
	private String unicodeEscape(String text) {
		StringBuffer sb = new StringBuffer();
		char[] buffer = text.toCharArray();
		for(int i=0; i < buffer.length; i++) {
			if(buffer[i] < 0xFFFF && (buffer[i] < 32 || buffer[i] > 122) ) {
				String hex = Integer.toHexString(buffer[i]);						
				switch (hex.length()){
					case 1: sb.append("\\u000"); break;
					case 2: sb.append("\\u00"); break;
					case 3: sb.append("\\u0"); break;
					case 4: sb.append("\\u"); break;
					default: throw new RuntimeException( hex+" is tool long to be a Character");
				}
				sb.append(hex);
			} else {
				sb.append(buffer[i]);
			}
		}		
		return sb.toString();
	}
	
	private Map<String, Method> getGetterMethods(Object object) {
		Map<String, Method> result = new HashMap<String, Method>();
		Class<?> clazz = object.getClass();
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
