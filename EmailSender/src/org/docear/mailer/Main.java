package org.docear.mailer;

public class Main {	
	public static void main(String... args) {		
		new ChunkedMailSender().start();
	}
}
