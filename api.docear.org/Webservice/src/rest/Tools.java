package rest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.sun.jersey.api.client.ClientResponse.Status;

public class Tools {
	
	static Logger logger = Logger.getLogger("RestWebserviceLogger");
	static Level logLevel = Level.DEBUG;
	static Layout layout = new PatternLayout("%d: %x - %m%n");
	static Appender consoleAppender = new ConsoleAppender(layout);
	static Appender fileAppender;
	
	@Context
	static ServletContext context;
	
	@Resource
	static  
	WebServiceContext webServiceContext; 
	
	
	public static void sendHTTPStatusResponse(Status responseStatus, Class<? extends Object> sender, Exception e, Level level) {
		logger.setLevel(logLevel);
		logger.addAppender(consoleAppender);
		try{
			fileAppender = new FileAppender(layout, "RestWebService.log");
		}catch(IOException ex){
			logger.fatal(ex);
		}
		logger.addAppender(fileAppender);
		String logMsg = sender.getName() + ": " + getStackTraceAsString(e);
		logger.log(level, logMsg);
		sendHTTPStatusResponse(responseStatus, "Internal Server Error.");
	}
	
	public static void sendHTTPStatusResponse(int responseStatus, String msg) {
		ResponseBuilder builder = Response.status(responseStatus);		
		builder.type("text/plain");        	
		builder.entity(msg);        	
		throw new WebApplicationException(builder.build());
	}
	
	public static void sendHTTPStatusResponse(Status responseStatus, String msg) {
		ResponseBuilder builder = Response.status(responseStatus);        	
		builder.type("text/plain");        	
		builder.entity(msg);		
		throw new WebApplicationException(builder.build());
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
	
	
	  
	  final static int BUFFER = 2048;
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
	  
	  public static String getFileName (InputStream zipFile) throws IOException{		  
		  ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(zipFile));		  
		  String entryName = "";
		  if(zipInputStream.getNextEntry() != null) {			  
			  entryName = zipInputStream.getNextEntry().getName();
		  }
		  zipInputStream.close();	
		  return entryName;
	  }
	  
	  public static int getFileSize (String mindmap) throws IOException{		  
		  byte data[] = mindmap.getBytes();
		  return Math.round(data.length / 1024);
	  }
	  
	  public static void tolerantClose(Session session) {
	      if (session.isOpen()) {
	          try {
	              session.close();
	          } catch (HibernateException e) {
	              logger.warn("An error occurred while closing the session.", e);
	          }
	      }
	  }
	  
	  
	  public static String getConfig(String property){
          try {
        	  //Todo path hard coded
        	  File config = new File("C:\\Users\\Christoph Arbeit\\Documents\\Arbeit\\Hiwi\\Workspace\\Rest Webservice\\src\\config.properties");
        	  //File config = new File("/opt/tomcat/webapps/config.properties");
        	  InputStream stream  = new FileInputStream(config);
        	  Properties buildNumberPros = new Properties();
        	  buildNumberPros.load(stream);
        	  String result = buildNumberPros.getProperty(property);
        	  stream.close();
              return result;
          } catch(Exception e){        	  
        	  System.out.println(Tools.getStackTraceAsString(e));
        	  return null;
          }
      }
	  
	  public static boolean getBoolProperty(String property){
		  String prop = Tools.getConfig(property);
		  if(prop == null || prop.equalsIgnoreCase("1")){
			  return true;
		  }
		  else{
			  return false;
		  }
	  }
}
