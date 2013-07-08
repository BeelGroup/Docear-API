package org.sciplore.formatter;


import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.CustomObjectOutputStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StatefulWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;

public abstract class Bean{
	
	private List<String> activeElements = new ArrayList<String>();
	
	public String toXML(){
		XStream xstream = this.getXMLXstream();
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		return xml + xstream.toXML(this);
	}
	
	public OutputStream toXMLStream(OutputStream stream){
		XStream xstream = this.getXMLXstream();	
		try {
			stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
		} catch (IOException e) {			
			e.printStackTrace();
		}
		xstream.toXML(this, stream);
		return stream;
	}
	
	public String toJson(){
		XStream xstream = this.getJsonXstream();
		return xstream.toXML(this);
	}
	
	public OutputStream toJsonStream(OutputStream stream){
		XStream xstream = this.getJsonXstream();		
		xstream.toXML(this, stream);
		return stream;
	}
	
	private XStream getXMLXstream(){
		XStream xstream = new XStream(new Dom4JDriver(){
		    public HierarchicalStreamWriter createWriter(Writer out) {
		    	SciploreXMLWriter ppw = new SciploreXMLWriter(out);
		        return (HierarchicalStreamWriter) ppw;
		    }
		});		
		xstream.alias(this.getClass().getSimpleName().toLowerCase(), this.getClass());			
		xstream.registerConverter(new BeanConverter(xstream.getMapper()));
		return xstream;
	}
	
	private XStream getJsonXstream(){
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver(){
		    public HierarchicalStreamWriter createWriter(Writer writer) {
		        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
		    }
			}) {
		 public ObjectOutputStream createObjectOutputStream(
		            final HierarchicalStreamWriter writer, String rootNodeName) throws IOException {
		        final StatefulWriter statefulWriter = new StatefulWriter(writer);
//			        if(rootNodeName != null && rootNodeName.length() > 0) {
//			        	statefulWriter.startNode(rootNodeName, null);
//			        }
		        return new CustomObjectOutputStream(new CustomObjectOutputStream.StreamCallback() {
					
		            public void writeToStream(Object object) {
		                marshal(object, statefulWriter);
		            }

		            public void writeFieldsToStream(@SuppressWarnings("rawtypes") Map fields) throws NotActiveException {
		                throw new NotActiveException("not in call to writeObject");
		            }

		            public void defaultWriteObject() throws NotActiveException {
		                throw new NotActiveException("not in call to writeObject");
		            }

		            public void flush() {
		                statefulWriter.flush();
		            }

		            public void close() {
		                if (statefulWriter.state() != StatefulWriter.STATE_CLOSED) {
		                    //statefulWriter.endNode();
		                    statefulWriter.close();
		                }
		            }
		        });
		    }
		};		
		xstream.alias(this.getClass().getSimpleName().toLowerCase(), this.getClass());
		xstream.registerConverter(new BeanJSONConverter(xstream.getMapper(),xstream));		
		xstream.addImplicitCollection(CollectionBean.class, "collection");
		return xstream;
	}
	
	public String getBeanName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * @param bean the bean to be activated
	 */
	public void addActiveElement(Bean bean) {			
		for (Method m : this.getClass().getDeclaredMethods()) {		
			if (!m.getName().startsWith("set") && !m.getName().startsWith("add")) {
				continue;
			}
			
			try {
				if(m.getName().startsWith("set")){
					String name = m.getName().toLowerCase().substring(3);
					if(name.equalsIgnoreCase(bean.getClass().getSimpleName())){
						m.invoke(this, bean);
						this.activateElement(bean.getClass().getSimpleName());					
					}
				}
				if(m.getName().startsWith("add")){
					String name = m.getName().toLowerCase().substring(3);
					if(name.equalsIgnoreCase(bean.getClass().getSimpleName()+"s")){
						m.invoke(this, bean);
						this.activateElement(bean.getClass().getSimpleName()+"s");					
					}
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
		}	
	}
	
	public void addActiveAttribute(String elementName, String value){
		for (Method m : this.getClass().getDeclaredMethods()) {		
			if (!m.getName().startsWith("set")) {
				continue;
			}			
			try {
				String name = m.getName().toLowerCase().substring(3);
				if(name.equalsIgnoreCase(elementName)){
					m.invoke(this, value);
					this.activateElement(elementName);					
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
		}
	}
	
	public void setElementActive(Class<Bean> clazz){
		for (Method m : this.getClass().getDeclaredMethods()) {		
			if (!m.getName().startsWith("set") && !m.getName().startsWith("add")) {
				continue;
			}
			if(m.getName().startsWith("set")){
				String name = m.getName().toLowerCase().substring(3);
				if(name.equalsIgnoreCase(clazz.getSimpleName())){					
					this.activateElement(clazz.getSimpleName());					
				}
			}
			if(m.getName().startsWith("add")){
				String name = m.getName().toLowerCase().substring(3);
				if(name.equalsIgnoreCase(clazz.getSimpleName()+"s")){					
					this.activateElement(clazz.getSimpleName()+"s");					
				}
			}	
		}	
	}
	
	public void setAttributeActive(String elementName){
		for (Method m : this.getClass().getDeclaredMethods()) {		
			if (!m.getName().startsWith("set") && !m.getName().startsWith("add")) {
				continue;
			}			
			String name = m.getName().toLowerCase().substring(3);
			if(name.equalsIgnoreCase(elementName)){					
				this.activateElement(elementName);					
			}			
		}	
	}
	
	public void setAllElementsActive(){
		for (Method m : this.getClass().getDeclaredMethods()) {		
			if (!m.getName().startsWith("set") && !m.getName().startsWith("add")) {
				continue;
			}
			String name = m.getName().toLowerCase().substring(3);
			this.activateElement(name.toLowerCase());			
		}	
	}



	protected void activateElement(String elementName) {
		elementName = elementName.toLowerCase();
		if(!this.activeElements.contains(elementName)){
			this.activeElements.add(elementName);
		}		
	}

	/**
	 * @return the activeMethods
	 */
	public List<String> getActiveElements() {
		return activeElements;
	}

}
