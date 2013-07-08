/**
 * 
 */
package org.sciplore.utilities.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 *
 * @author Mario Lipinski <a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>
 */
public class Config {
	public static Properties getProperties(String name, Class<?> c) {
		// load Properties begin
		Properties p = new Properties();
		
		try {
			p.load(c.getClassLoader().getResourceAsStream(name + ".default.properties"));
		} catch (NullPointerException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String confDir = null;
		try {
			confDir = (String)((javax.naming.Context)new InitialContext().lookup("java:comp/env")).lookup("confDir");
		} catch (NamingException e1) {
		}
		
		if(confDir == null || !new File(confDir).isDirectory()) {
			try {
				confDir = new File(URLDecoder.decode(c.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")).getAbsoluteFile().getParent();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		File propFile = new File(confDir + File.separator + name + ".properties");
		if (propFile.canRead()) {
			p = new Properties(p);
			try {
				p.load(new FileReader(propFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// load Properties end
		
		return p;		
	}
	
	public static Properties getProperties(String name) {
		return getProperties(name, Config.class);
	}
}
