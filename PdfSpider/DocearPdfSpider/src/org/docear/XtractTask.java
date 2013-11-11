package org.docear;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sciplore.xtract.Xtract;

public abstract class XtractTask {
	private static Xtract xtrct;
	private static String xtractInitializationError;
	static {
		try {
		xtrct = new Xtract();
		}
		catch (Exception e) {
			e.printStackTrace();
			xtractInitializationError = e.getMessage();
		}		
	}
	private final File txt;
	private final String title;
	private Collection<String> emailIndex = new HashSet<String>();
	
	public XtractTask(File txtFile, String title) {
		if(txtFile == null) {
			throw new NullPointerException();
		}
		if(xtrct == null) {
			throw new RuntimeException(xtractInitializationError);
		}
		txt = txtFile;
		this.title = title;
	}
	
	public final void run() {
		String xml = null;
		long time = System.currentTimeMillis();
		try {
			System.out.println("["+Thread.currentThread().getName()+"] running xtract on "+txt+"...");
			
			String text = loadPlainText(txt);
			
    		//use the first 30% of the text only
//    		text = text.substring(0, (int)(text.length()*.3));
			
    		//search for emails in text
    		emailIndex = (Set<String>) findEmailAddresses(text);
			
			org.sciplore.beans.Document doc = xtrct.xtractDocumentFromTxt(txt, null, this.title);
			    		
			xml = doc.toXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("["+Thread.currentThread().getName()+"] xtract finished. ("+(System.currentTimeMillis()-time)+")");
			finishTask(xml);
			
		}
	}
	
	/**
	 * <ul> <li>this will not work with UTF-8 characters in the email address --> ASCII is sufficient for us</li>   
	 * <li>filter only works on 2-3 letter top-level-domain email addresses (e.g. edu, com, org, ...)</li></ul>
	 */
	private static final Pattern emailPattern = Pattern.compile("[A-Z0-9._%+-]+@(?:[A-Z0-9-]+\\.)+[A-Z]{2,3}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		
	/**
	 * this will not work with UTF-8 characters --> ASCII is sufficient for us
	 * @param file
	 * @return the plain text (ASCII only) of the file
	 */
	public static String loadPlainText(File file) {
		try {
			return loadPlainText(new FileInputStream(file));
		} catch (Exception ex) {
			return "";
		}
	}
	
	/**
	 * this will not work with UTF-8 characters --> ASCII is sufficient for us
	 * @param InputStream
	 * @return the plain text (ASCII only) of the file
	 */
	public static String loadPlainText(InputStream is) {
		StringBuilder builder = new StringBuilder();
		try {
			InputStreamReader reader = new InputStreamReader(is);
			try {
				int chr = -1;
				while((chr = reader.read()) > -1 ) {
					builder.append((char)chr);
				}
			}
			finally {
				try {
					reader.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			builder.delete(0, builder.length());
		}	
		return builder.toString();
	}
	
	public static Collection<String> findEmailAddresses(String text) {
		Matcher matcher = emailPattern.matcher(text);
		Set<String> emails = new HashSet<String>();
		while(matcher.find()) {
			String email = text.substring(matcher.start(), matcher.end());
			emails.add(email);
		}
		return emails;
	}
	
	public Collection<String> getEmailAddr() {
		return emailIndex;
	}
	
	public abstract void finishTask(String xmlText);
}
