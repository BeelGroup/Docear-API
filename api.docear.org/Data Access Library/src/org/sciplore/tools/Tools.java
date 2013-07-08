package org.sciplore.tools;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.sciplore.resources.Resource;

public class Tools {
	
	/**
	 * Cleans a String by removing all special chars, spaces and converting it to lower case 
	 * @param title
	 * @return
	 */
	
	public static final String USERS_PASSWORDSALT = "spl";
	
	
	public static Disjunction getDisjunctionFromString(String propertyName, String csvList) {
		String[] list = csvList.split(",");
		Disjunction disjunction = Restrictions.disjunction();
		for (String element : list) {
			disjunction.add(Restrictions.eq("source", element));
		}
		
		return disjunction;
	}
	
	public String cleanTitle (String title) {
		return title;
	}
	
	public int isTitleIdentical(String title1, String title2) {
		
		return 0;		
		
	}
	
	public String extractTitleFromFile(File myfile) {
		String title="";
		return title;
	}
	
	public static String convertToSaltedMD5Digest(String salt, String plainText) throws NoSuchAlgorithmException{
		
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update((salt + plainText).getBytes());
		byte[] md5Digest = md5.digest();
		
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < md5Digest.length; i++){
			stringBuilder.append(toHexString(md5Digest[i]));
		}
		return stringBuilder.toString();
	}
	
	
	
	final static int BUFFER = 1024;
	  public static void unzip (InputStream zipFile, File destinationFile) throws IOException{
		  FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
		  BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
		  
		  ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(zipFile));
	
		  if(zipInputStream.getNextEntry() != null) { 
			  int count;
			  byte data[] = new byte[BUFFER];	           
			  while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
				  bufferedOutputStream.write(data, 0, count);
			  }
			  bufferedOutputStream.flush();
			  bufferedOutputStream.close();
		  }
		  zipInputStream.close();	      
	  }
	  
	  public static StringBuilder unzip (InputStream zipFile) throws IOException{		  
		  ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(zipFile));		  
		  StringBuilder stringBuilder = new StringBuilder();
		  if(zipInputStream.getNextEntry() != null) {			  
			  byte data[] = new byte[BUFFER];	           
			  while (zipInputStream.read(data, 0, BUFFER) != -1) {				 
				  stringBuilder.append(new String(data));				  
			  }			  
		  }
		  else{
			  return unzipStringFromBytes(zipFile);
		  }
		  zipInputStream.close();	
		  return stringBuilder;
	  }
	  
	  public static StringBuilder unzipStringFromBytes( InputStream stream ) throws IOException
	  {
	    BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(stream));
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    byte[] buf = new byte[1024];
	    int len;
	    while( (len = bufis.read(buf)) > 0 )
	    {
	      bos.write(buf, 0, len);
	    }
	    String retval = bos.toString();	    
	    bufis.close();
	    bos.close();
	    return new StringBuilder(retval);
	  }
	  
	  public static int getFileSize (String mindmap) throws IOException{		  
		  byte data[] = mindmap.getBytes();
		  return Math.round(data.length / 1024);
	  }
	
	  public static String getStackTraceAsString(Exception exception) { 
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        pw.print(" [ ");
	        pw.print(exception.getClass().getName());
	        pw.print(" ] ");
	        pw.print(exception.getMessage());
	        exception.printStackTrace(pw);
	        return sw.toString();
	  }
	
	public static String toHexString(byte b) {
   		int value = (b & 0x7F) + (b < 0 ? 128 : 0);
	   
    		String ret = (value < 16 ? "0" : "");
    		ret += Integer.toHexString(value).toUpperCase();
	    
		return ret;
  	}
	
	public static int levenshtein(int cut, String s1, String s2) {
		int cost;
		boolean diff = false;

		int len1 = s1.length();
		int len2 = s2.length();

		int[][] matrix = new int[len1 + 1][len2 + 1];
		// System.out.println(matrix);

		matrix[0][0] = 0;
		for (int i = 1; i < len1 + 1; i++) {
			matrix[i][0] = i;
		}
		// ausgabe(matrix, s1, s2);
		for (int k = 1; k < len2 + 1; k++) {
			matrix[0][k] = k;
		}
		// ausgabe(matrix, s1, s2);
		int start;
		for (int row = 1; row < len2 + 1; row++) {
			start = 1;
			if ((row - cut) > start) {
				start = row - cut;
			}
			for (int col = start; col < Math.min(row + cut + 1, len1 + 1); col++) {
				if (s1.charAt(col - 1) == s2.charAt(row - 1)) {
					cost = 0;
				} else {
					cost = 1;
				}
				if (col == row + cut) {
					matrix[col][row] = Math.min(matrix[col - 1][row] + 1,
							matrix[col - 1][row - 1] + cost);
				} else if (col == row - cut) {
					matrix[col][row] = Math.min(matrix[col][row - 1] + 1,
							matrix[col - 1][row - 1] + cost);
				} else {
					matrix[col][row] = 
							Math.min(matrix[col - 1][row] + 1, 
									Math.min(matrix[col - 1][row - 1] + cost,
												matrix[col][row - 1] + 1));
				}
				// matrix[col][row] = Math.min( matrix[col-1][row]+1 , Math.min(
				// matrix[col-1][row-1]+cost , matrix[col][row-1]+1 ) );
				diff = matrix[col][row] > cut;
			}
			if (diff) {
				matrix[len1][len2] = cut;
				break;
			}
			// ausgabe(matrix, s1, s2);
		}

		// ausgabe(matrix, s1, s2);
		return matrix[len1][len2];
	}
	
	public static boolean empty(Object o) {
		if(o == null) {
			return true;
		} else {
			if(o instanceof String) {
				return o.equals("");
			}
			if(o instanceof Short || o instanceof Integer) {
				return o.equals(new Short((short)0));
			}
		}
		return false;
	}
	
	public static XMLGregorianCalendar getXMLGregorianCalendar(Date date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date.getTime());
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar();
            xmlGregorianCalendar.setYear(calendar.get(Calendar.YEAR));
            xmlGregorianCalendar.setMonth(calendar.get(Calendar.MONTH));
            xmlGregorianCalendar.setDay(calendar.get(Calendar.DAY_OF_MONTH));
            xmlGregorianCalendar.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            xmlGregorianCalendar.setMinute(calendar.get(Calendar.MINUTE));
            xmlGregorianCalendar.setSecond(calendar.get(Calendar.SECOND));
            return xmlGregorianCalendar;
        }
        catch(DatatypeConfigurationException datatypeConfigurationException) {
            datatypeConfigurationException.printStackTrace();
            return null;
        }
    }
	
	public static boolean isValidUsername(String userName){
		char[] chars = userName.toCharArray();
		for(char aChar : chars){
			if(!(Character.isLetterOrDigit(aChar) || aChar == '.' || aChar == '-' || aChar == '_')){
				return false;
			}
		}
		try{
			Double.valueOf(userName);
		}
		catch(NumberFormatException e){
			return true;
		}
		return false;
	}
	
	  public static boolean isValidEmailAddress(String aEmailAddress){
	    if (aEmailAddress == null) return false;
	    boolean result = true;
	    try {
	      @SuppressWarnings("unused")
		InternetAddress emailAddr = new InternetAddress(aEmailAddress);
	      if ( ! hasNameAndDomain(aEmailAddress) ) {
	        result = false;
	      }
	    }
	    catch (AddressException ex){
	      result = false;
	    }
	    return result;
	  }
	
	  private static boolean hasNameAndDomain(String aEmailAddress){
	    String[] tokens = aEmailAddress.split("@");
	    return 
	     tokens.length == 2 &&
	     Tools.textHasContent( tokens[0] ) && 
	     Tools.textHasContent( tokens[1] ) ;
	  }
	  
	  private static boolean textHasContent(String aText){
		  return aText != null && aText.trim().length()>0;
	  }
	  
	  public static <T extends Resource> boolean containsRecord(Collection<T> collection, T resource){
		  for(T entry : collection){
			  if(entry.getId() == resource.getId())
				  return true;
		  }
		  return false;
	  }
}
