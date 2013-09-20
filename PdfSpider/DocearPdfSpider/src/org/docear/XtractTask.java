package org.docear;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
	private final Set<String> emailIndex = new HashSet<String>();
	
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
			
			org.sciplore.beans.Document doc = xtrct.xtractDocumentFromTxt(txt, null, this.title);
			
			String text = loadPlainText(txt);
			
    		//use the first 30% of the text only
    		text = text.substring(0, (int)(text.length()*.3));
			
    		//search for emails in text
    		findEmailAddresses(text);
    		
			xml = doc.toXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("["+Thread.currentThread().getName()+"] xtract finished. ("+(System.currentTimeMillis()-time)+")");
			finishTask(xml);
			
		}
	}
	
	private static final Pattern emailPattern = Pattern.compile("[A-Z0-9._%+-]+@(?:[A-Z0-9-]+\\.)+[A-Z]{2,3}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	
	private String loadPlainText(File file) {
		StringBuilder builder = new StringBuilder();
		try {
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
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
	
	private void findEmailAddresses(String text) {
		Matcher matcher = emailPattern.matcher(text);
		while(matcher.find()) {
			String email = text.substring(matcher.start(), matcher.end());
			emailIndex.add(email);
		}
	}
	
	public Set<String> getEmailAddr() {
		return emailIndex;
	}
	
	public abstract void finishTask(String xmlText);
}
