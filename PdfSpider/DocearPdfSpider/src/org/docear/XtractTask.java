package org.docear;

import java.io.File;

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
			xml = doc.toXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("["+Thread.currentThread().getName()+"] xtract finished. ("+(System.currentTimeMillis()-time)+")");
			finishTask(xml);
			
		}
	}
	
	public abstract void finishTask(String xmlText);
}
