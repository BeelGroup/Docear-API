package org.sciplore.formatter;

import java.io.Writer;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class SciploreXMLWriter extends PrettyPrintWriter {
	public SciploreXMLWriter(Writer writer) {
		super(writer,0, new XmlReplacerSciplore());
	}
	
	public String escapeXmlName(String name) {
//		System.out.print("escape: "+name+" --> ");
//		System.out.println(super.escapeXmlName(name));
        return super.escapeXmlName(name);
    }
}
