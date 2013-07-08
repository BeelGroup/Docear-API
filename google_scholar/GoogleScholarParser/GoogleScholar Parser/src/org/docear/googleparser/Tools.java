package org.docear.googleparser;

import java.util.Date;

public class Tools {
	
	private static Date lastRequestDay = new Date();
	private static int requestCounter = 0;
	
	public static boolean isRequestPossible(){
		Date now = new Date();
		if(lastRequestDay != null && (now.getTime() - lastRequestDay.getTime()) < 86400000){
            if(requestCounter < 790){
            	requestCounter++;
            	return true;
            }
            else{
            	return false;
            }
        }
		else{
			lastRequestDay = now;
			requestCounter = 1;
			return true;
		}
	}
}
