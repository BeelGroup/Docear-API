package org.docear.mailer;

public class Main {
	public static int LIMIT = 100;
	
	public static void main(String... args) {		
		new ChunkedMailSender().start(LIMIT);
	}
}
