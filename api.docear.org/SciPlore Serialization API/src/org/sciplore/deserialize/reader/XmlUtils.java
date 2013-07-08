package org.sciplore.deserialize.reader;

import java.text.SimpleDateFormat;
import java.util.Date;

public class XmlUtils {
	public static String getXmlString(Object string) {
		if(string != null && !string.toString().trim().isEmpty()) {
			return string.toString();
		}
		return null;
	}
	
	public static Integer getXmlInteger(Object value) {
		if(value != null && !value.toString().trim().isEmpty()) {
			try {
				return Integer.parseInt(value.toString().trim());
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return null;
	}
	
	public static Short getXmlShort(Object value) {
		if(value != null && !value.toString().trim().isEmpty()) {
			try {
				return Short.valueOf(value.toString().trim());
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return null;
	}

	public static Date getXmlDate(Object value, String format) {
		if(value != null && !value.toString().trim().isEmpty()) {
			try {
				SimpleDateFormat df = new SimpleDateFormat(format);
				return df.parse(value.toString().trim());
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		return null;
	}
}
