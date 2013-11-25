package org.docear.mailer;

public class Main {
	public static int LIMIT = 1000;
	
	public static void main(String... args) {		
		new ChunkedMailSender().start(LIMIT);
	}
}
