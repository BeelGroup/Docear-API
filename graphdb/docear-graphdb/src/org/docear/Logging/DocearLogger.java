package org.docear.Logging;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocearLogger {
	
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");

	public static void info(Throwable cause) {
		info(cause, null);
	}
	
	public static void info(Throwable cause, String msg) {
		print(System.out, cause, msg);
	}
	
	public static void info(String msg) {
		print(System.out, null, msg);
	}
	
	public static void error(Throwable cause) {
		error(cause, null);
	}
	
	public static void error(Throwable cause, String msg) {
		print(System.err, cause, msg);
	}
	
	public static void error(String msg) {
		print(System.err, null, msg);
	}
	
	public static String getExceptionTrace(final Throwable cause) {
		StringBuilder trace = new StringBuilder();
		StringBuilder nesting = new StringBuilder();
		if(cause != null) {
			int trials = 10;
			Throwable actualCause = cause;
			while((trials-- > 0) 
					&& actualCause.getCause() != null 
					&& actualCause.getCause() != actualCause) {
				nesting.append("\n\tover ");
				appendTopTraceElement(nesting, actualCause);
				actualCause = actualCause.getCause();
			}
			appendTopTraceElement(trace, actualCause);
			trace.append(nesting.toString());
		}
		return trace.toString();
	}

	private static void appendTopTraceElement(StringBuilder trace, Throwable cause) {
		StackTraceElement[] elements = cause.getStackTrace();
		if(elements != null && elements.length > 0) {
			trace.append(elements[0].getClassName());
			trace.append(".");
			trace.append(elements[0].getMethodName());
			trace.append(" line ");
			trace.append(elements[0].getLineNumber());
		}
	}
	
	private static void print(PrintStream printer, Throwable cause, String msg) {
		String date =  "["+dateFormat.format(new Date())+"] ";
		
		if (msg != null) {
			printer.println(date + msg);
		}
		
		if (cause != null) {
			printer.println(date+"Exception in "+getExceptionTrace(cause)+": "+cause.getMessage());
		}
	}

}
