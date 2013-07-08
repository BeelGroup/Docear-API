package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.mrdlib.index.Indexer;
import org.sciplore.database.SessionProvider;
import org.sciplore.formatter.Bean;

import com.sun.jersey.multipart.FormDataBodyPart;

public class Tools {
	
	public static final String DEFAULT_FORMAT = "xml";

	static Logger logger = Logger.getLogger("RestWebserviceLogger");
	static Level logLevel = Level.DEBUG;
	static Layout layout = new PatternLayout("%d: %x - %m%n");
	static Appender consoleAppender = new ConsoleAppender(layout);
	static Appender fileAppender;

	public static String XML = "xml";
	public static String JSON = "json";
	public static String HTML = "html";

	public static Response getSerializedResponse(String format, Bean bean, Boolean stream) {
		if (stream != null && stream) {
			return Tools.getSerializedResponseAsStream(format, bean);
		}
		else if (isUnknownFormat(format)) {
			return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "Unknown format requested.");
		}
		else if (format.equalsIgnoreCase(JSON)) {
			String json = bean.toJson();
			return Tools.getResponse(Status.OK, MediaType.APPLICATION_JSON_TYPE, json);
		}
		else {
			String xml = bean.toXML();
			return Tools.getResponse(Status.OK, MediaType.APPLICATION_XML_TYPE, xml);
		}
	}

	public static Response getSerializedResponseAsStream(String format, final Bean bean) {
		if (isUnknownFormat(format)) {
			return Tools.getHTTPStatusResponse(Status.BAD_REQUEST, "Unknown format requested.");
		}
		else if (format.equalsIgnoreCase(JSON)) {
			StreamingOutput output = new StreamingOutput() {
				@Override
				public void write(OutputStream stream) throws IOException, WebApplicationException {
					bean.toJsonStream(stream);
				}
			};
			return Tools.getResponse(Status.OK, MediaType.APPLICATION_OCTET_STREAM_TYPE, output);
		}
		else {
			StreamingOutput output = new StreamingOutput() {
				@Override
				public void write(OutputStream stream) throws IOException, WebApplicationException {
					bean.toXMLStream(stream);
				}
			};
			return Tools.getResponse(Status.OK, MediaType.APPLICATION_OCTET_STREAM_TYPE, output);
		}
	}

	public static boolean isUnknownFormat(String format) {
		return format == null || format.isEmpty() || !(format.equalsIgnoreCase(XML) || format.equalsIgnoreCase(JSON));
	}

	public static Response getHTTPStatusResponse(Status responseStatus, String msg) {
		ResponseBuilder builder = Response.status(responseStatus);
		builder.type("text/plain");
		if(!responseStatus.equals(Status.NO_CONTENT)) { 
			builder.entity(msg);
		}
		return builder.build();
	}

	public static Response getResponse(Status responseStatus, MediaType type, Object content) {
		ResponseBuilder builder = Response.status(responseStatus);		
		builder.type(type);
		builder.entity(content);
		return builder.build();
	}

	public static Response sendHTTPStatusResponse(Status responseStatus, Class<? extends Object> sender, Exception e, Level level) {
		logger.setLevel(logLevel);
		logger.addAppender(consoleAppender);
		try {
			fileAppender = new FileAppender(layout, "RestWebService.log");
		}
		catch (IOException ex) {
			logger.fatal(ex);
		}
		logger.addAppender(fileAppender);
		String logMsg = sender.getName() + ": " + getStackTraceAsString(e);
		logger.log(level, logMsg);
		return getHTTPStatusResponse(responseStatus, "Internal Server Error.");
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

	// FIXME: replace with new getSession from SessionProvider and connection
	// properties
	public static Session getSession() {
		final Session session = SessionProvider.sessionFactory.openSession();
		if (session != null) {
			return session;
		}
		else {
			Tools.getHTTPStatusResponse(Status.SERVICE_UNAVAILABLE, "Could not connect to database");
		}
		return null;
	}
	
	public static Indexer getLuceneIndexer() throws IOException {
		return SessionProvider.getLuceneIndexer();
	}

	public static Properties getTestConnectionProperties() {
		Properties props = new Properties();
		props.setProperty("connection.driver_class", "com.mysql.jdbc.Driver");
		props.setProperty("connection.url", "jdbc:mysql://bkl01.sciplore.org/mr_dlib");
		props.setProperty("connection.username", "mr_dlib");
		props.setProperty("connection.password", "Jjrm8JQzx6f3nPMP");
		// props.getProperty("connection.autocommit", null);
		props.setProperty("dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
		return props;
	}
	
	public static void tolerantClose(Session session) {
		if (session.isOpen()) {
			try {
				session.close();
			}
			catch (HibernateException e) {
				logger.warn("An error occurred while closing the session.", e);
			}
		}
	}

	public static Map<String, Object> parseQueryParameter(UriInfo uriInfo) {
		Map<String, Object> result = new HashMap<String, Object>();
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		for (String key : params.keySet()) {
			if (!params.getFirst(key).isEmpty()) {
				try {
					if (key.equals("dhi")) {
						result.put(key, format.parse(params.getFirst(key)));
					}
					else if (key.equals("dlo")) {
						result.put(key, format.parse(params.getFirst(key)));
					}
					else if (key.equals("number")) {
						result.put(key, new Integer(params.getFirst(key)));
					}
					else if (key.equals("stream")) {
						result.put(key, new Boolean(params.getFirst(key)));
					}
					else if (key.equals("page")) {
						result.put(key, new Integer(params.getFirst(key)));
					}
					else {
						result.put(key, params.getFirst(key));
					}
					System.out.println("(mr-dlib) parsed param: " + key + "=" + result.get(key));
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static Map<String, Object> parseRequestParameter(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, String[]> params = request.getParameterMap();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		for (String key : params.keySet()) {
			if (!params.get(key)[0].isEmpty()) {
				try {
					if (key.equals("dhi")) {
						result.put(key, format.parse(params.get(key)[0]));
					}
					else if (key.equals("dlo")) {
						result.put(key, format.parse(params.get(key)[0]));
					}
					else if (key.equals("number")) {
						result.put(key, new Integer(params.get(key)[0]));
					}
					else if (key.equals("stream")) {
						result.put(key, new Boolean(params.get(key)[0]));
					}
					else if (key.equals("page")) {
						result.put(key, new Integer(request.getParameter(key)));
					}
					else {
						result.put(key, params.get(key)[0]);
					}
					System.out.println("(mr-dlib) parsed param: " + key + "=" + result.get(key));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static String getUploadData(FormDataBodyPart p) {
		StringBuffer sb = new StringBuffer();
		InputStream file = p.getValueAs(InputStream.class);
		byte[] buffer = new byte[1024];
		try {
			int readBytes = 0;
			while ((readBytes = file.read(buffer)) >= 0) {
				sb.append(new String(buffer, 0, readBytes));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

}
