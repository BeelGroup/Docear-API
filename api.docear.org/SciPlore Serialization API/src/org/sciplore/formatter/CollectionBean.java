package org.sciplore.formatter;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sciplore.beans.Document;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.CustomObjectOutputStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StatefulWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;


public abstract class CollectionBean extends Bean {
	
	private List<Bean> collection = new ArrayList<Bean>();

	/**
	 * @param collection the collection to set
	 */
	public void add(Bean element) {
		if(element == null) return;
		this.collection.add(element);
		this.activateElement("collection");
	}

	/**
	 * @return the collection
	 */
	public List<Bean> getCollection() {
		return collection;
	}
	
	public String toJson(){
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver(){
		    public HierarchicalStreamWriter createWriter(Writer writer) {
		        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
		    }
		}) {
			 public ObjectOutputStream createObjectOutputStream(
			            final HierarchicalStreamWriter writer, String rootNodeName) throws IOException {
			        final StatefulWriter statefulWriter = new StatefulWriter(writer);
			        return new CustomObjectOutputStream(new CustomObjectOutputStream.StreamCallback() {
						
			            public void writeToStream(Object object) {
			                marshal(object, statefulWriter);
			            }

			            @SuppressWarnings("rawtypes")
						public void writeFieldsToStream(Map fields) throws NotActiveException {
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
			                    statefulWriter.close();
			                }
			            }
			        });
			    }
		};
		System.out.println("debug: DocumentList");
		xstream.alias(this.getClass().getSimpleName().toLowerCase(), this.getClass());
		xstream.registerConverter(new BeanJSONConverter(xstream.getMapper(),xstream));
		StringBuffer sb = new StringBuffer();
		sb.append(xstream.toXML(this));
		sb.delete(sb.length()-2, sb.length());
		sb.append(",\n  \"list\": [\n");
		
		xstream.alias("document", Document.class);
		
		for(Bean bean : collection) {
			sb.append(xstream.toXML(bean));
			sb.append(",\n");
		}
		sb.delete(sb.length()-2, sb.length()-1);
		sb.append("  ]\n");
		sb.append("}\n");
		return sb.toString();
	}

}
