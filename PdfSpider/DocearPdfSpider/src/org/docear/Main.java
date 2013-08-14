package org.docear;

import java.io.File;
import java.io.PrintWriter;

import org.sciplore.beans.Document;
import org.sciplore.xtract.Xtract;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {	
		
		try {
			PrintWriter writer = new PrintWriter(new File("/home/stefan/work/testset/SPXtract_results.csv"));
    		File directory = new File("/home/stefan/work/testset/pdfs");
    		
    		for (File f : directory.listFiles()) {
    			System.out.println("using file: "+f.getName());
    			if (f.getName().toLowerCase().endsWith(".pdf")) {
    				try {
    					Long start = System.currentTimeMillis();
    					Document doc = new Xtract().xtractDocument(f);
    					writer.println(f.getAbsolutePath() + "|" + doc.getTitle() + "|" + (System.currentTimeMillis()-start));    
    					writer.flush();
    				}
    				catch(Exception e) {
    					e.printStackTrace();
    				}
    				
    			}
    		}
    		
    		writer.close();
    		
		}	
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
