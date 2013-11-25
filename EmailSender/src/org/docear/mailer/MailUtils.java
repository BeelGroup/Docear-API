package org.docear.mailer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.sciplore.utilities.config.Config;

public class MailUtils {
	
	public static class MailSenderException {
		private InternetAddress[] emailAddresses = null;
		private Exception exception = null;
		
		public MailSenderException(Exception exception, InternetAddress[] emailAddresses) {
			this.emailAddresses = emailAddresses;
			this.exception = exception;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (InternetAddress emailAddress: emailAddresses) {
				sb.append(emailAddress.getAddress()).append(" ");
			}
			return sb.append(": ").append(exception.getMessage()).toString();
		}
	}
	
	public final static List<MailSenderException> senderExceptions = new ArrayList<MailUtils.MailSenderException>();
	
	public interface MailConfiguration {

		public Session getSession();

		public InternetAddress getFrom();

	}

	public static class SmtpMailConfiguration implements MailConfiguration {
		private final Properties props = new Properties();
		private InternetAddress from;
		private Authenticator auth;
		private boolean debug;
		
		public SmtpMailConfiguration(final String host, String from) throws AddressException {
			this(host, from, null);
		}
		
		public SmtpMailConfiguration(final String host, String from, String alias) throws AddressException {
			props.put("mail.smtp.host", host);
			this.from = new InternetAddress(from);
			setFromAlias(alias);
		}
		
		
		public Session getSession() {			
			Session session = Session.getInstance(props, getAuthenticator());			
			session.setDebug(debug);
			return session;
		}
		
		public void setAuthenticator(Authenticator auth) {
			this.auth = auth;
		}
		
		public Authenticator getAuthenticator() {
			return this.auth;
		}
		
		public void setDebugEnabled(boolean enabled) {
			this.debug = enabled;
		}
		
		public void setAuthEnabled(boolean enabled) {
			props.put("mail.smtp.auth", Boolean.toString(enabled));
		}
		
		public void setSSLEnabled(boolean enabled) {
			props.put("mail.smtp.ssl.enable", Boolean.toString(enabled));
		}

		
		public void setTLSEnabled(boolean enabled) {
			props.put("mail.smtp.starttls.enable", Boolean.toString(enabled));
		}
		
		public void setPort(int port) {
			props.put("mail.smtp.port", Integer.toString(port));
		}
		

		public InternetAddress getFrom() {
			return from;
		}
		
		public void setFromAlias(String alias) throws AddressException {
			if(alias == null) {
				this.from = new InternetAddress(from.getAddress());
			}
			else {
				try {
					this.from = new InternetAddress(from.getAddress(), alias);
				} catch (UnsupportedEncodingException e) {
					throw new AddressException(e.getMessage());
				}
			}
		}
	}
	
	public static SmtpMailConfiguration DOCEAR_MAIL_CONFIGURATION;
	static {
		final Properties p = Config.getProperties("mail.sender");
		try {
			DOCEAR_MAIL_CONFIGURATION = new SmtpMailConfiguration(p.getProperty("docear.mail.host"), p.getProperty("docear.mail.from"));
			DOCEAR_MAIL_CONFIGURATION.setFromAlias(p.getProperty("docear.mail.fromAlias"));
			DOCEAR_MAIL_CONFIGURATION.setAuthEnabled(true);
			DOCEAR_MAIL_CONFIGURATION.setDebugEnabled(Boolean.parseBoolean(p.getProperty("docear.mail.debug")));
			DOCEAR_MAIL_CONFIGURATION.setAuthenticator(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(p.getProperty("docear.mail.auth.user"), p.getProperty("docear.mail.auth.pwd"));
				}
			});
		} catch (AddressException e) {
			e.printStackTrace();
		}
		
	}

	public static boolean sendMail(String subject, String message, InternetAddress[] recipients, MailConfiguration config) {
		try {
		    // create a message
		    MimeMessage msg = new MimeMessage(config.getSession());
		    msg.setFrom(config.getFrom());
		    msg.setRecipients(Message.RecipientType.TO, recipients);
		    msg.setSubject(subject);
		    msg.setSentDate(new Date());
		    
		    msg.setText(message, "UTF-8");
		    
		    Transport.send(msg);
		    
		    senderExceptions.clear();
		    return true;
		} catch (MessagingException mex) {
		    System.out.println("\n--Exception handling in MailUtils.sendMail()");

		    mex.printStackTrace();
		    System.out.println();
		    Exception ex = mex;
		    do {
			if (ex instanceof SendFailedException) {
				senderExceptions.add(new MailSenderException(ex, recipients));
				
			    SendFailedException sfex = (SendFailedException)ex;
			    Address[] invalid = sfex.getInvalidAddresses();
			    if (invalid != null) {
				System.out.println("    ** Invalid Addresses");
				for (int i = 0; i < invalid.length; i++) 
				    System.out.println("         " + invalid[i]);
			    }
			    Address[] validUnsent = sfex.getValidUnsentAddresses();
			    if (validUnsent != null) {
				System.out.println("    ** ValidUnsent Addresses");
				for (int i = 0; i < validUnsent.length; i++) 
				    System.out.println("         "+validUnsent[i]);
			    }
			    Address[] validSent = sfex.getValidSentAddresses();
			    if (validSent != null) {
				System.out.println("    ** ValidSent Addresses");
				for (int i = 0; i < validSent.length; i++) 
				    System.out.println("         "+validSent[i]);
			    }
			}
			System.out.println();
			if (ex instanceof MessagingException)
			    ex = ((MessagingException)ex).getNextException();
			else
			    ex = null;
		    } while (ex != null);
		}
		
		return false;
	}
	
	public static InternetAddress[] parseAddress(String... emails) {
		List<InternetAddress> addrList = new ArrayList<InternetAddress>();
		if(emails != null) {
			for (String emailAddr : emails) {
				try {
					addrList.add(new InternetAddress(emailAddr));
				} catch (AddressException e) {
					System.out.println("Exception in MailUtils.parseAddress(): "+ e.getMessage());
				}
			}
		}
		return addrList.toArray(new InternetAddress[0]);
	}

}
