package org.docear.mailer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;

import org.docear.mailer.MailUtils.MailSenderException;
import org.docear.mailer.MailUtils.SmtpMailConfiguration;
import org.sciplore.utilities.config.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ChunkedMailSender {
	private final Properties properties = Config.getProperties("mail.sender");
	private final static String SERVICE_HOST = "https://api.docear.org";

	public void start() {
		new Thread() {
			public void run() {
				int chunkSize = Integer.parseInt(properties.getProperty("docear.mail.chunk.size", "1"));
				int maxChunkRequests = Integer.parseInt(properties.getProperty("docear.mail.chunk.requestnumber", "1"));
				
				System.out.println("starting parameters [docear.mail.chunk.size="+chunkSize+"; docear.mail.chunk.requestnumber="+maxChunkRequests+"]");
				
				// i==-1 --> infinite loop
				for (int i=0; (maxChunkRequests<0 ? true : i<maxChunkRequests); i=(maxChunkRequests<0 ? -1 : i+1)) {
					
					Map<String, Recipient> chunk = getNextChunk(chunkSize);
					if (chunk.isEmpty()) {						
						try {
							sleep(24 * 3600 * 1000);
						}
						catch (InterruptedException e) {
						}
					}
					else {
						System.out.println("got "+chunk.size()+" entries");
					}
//					while (!chunk.isEmpty()) 
					{
						Iterator<Entry<String, Recipient>> iter = chunk.entrySet().iterator();
						while (iter.hasNext()) {
							Entry<String, Recipient> entry = iter.next();
							if (sendNotificationMail(entry.getKey(), entry.getValue())) {
								postNotificationUpdate(entry.getValue(), false);
								iter.remove();
								System.out.println(entry.getKey());
							}
							else {
								postNotificationUpdate(entry.getValue(), true);
							}

							if (MailUtils.senderExceptions.size() > 0) {
								performEmergencyExit();
							}
						
							try {
								sleep(1000);
							}
							catch (InterruptedException e) {
							}
						}
					}

					try {
						sleep(600 * 1000);
					}
					catch (InterruptedException e) {
					}
				}
			}

		}.start();
	}

	private void performEmergencyExit() {
		System.out.println("=== EMERGENCY EXIT ===");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
		File senderExceptionsLog = new File(dateFormat.format(new Date()) + "__SenderExceptions.log");

		FileWriter fw = null;

		try {
			fw = new FileWriter(senderExceptionsLog, true);
			for (MailSenderException exception : MailUtils.senderExceptions) {
				fw.write(exception.toString());
				fw.write(System.getProperty("line.separator"));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				fw.flush();
				fw.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			SmtpMailConfiguration emergencyConfig = new SmtpMailConfiguration("mail.ovgu.de", "emergency@docear.org");
			emergencyConfig.setAuthEnabled(true);
			emergencyConfig.setPort(587);
			emergencyConfig.setTLSEnabled(true);
			emergencyConfig.setAuthenticator(new Authenticator() {

				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("genzmehr", "FarbKopiererV49");
				}
			});			
			MailUtils.sendMail("Docear document index notifier", "The service was stoped with an emergency. See the log files on the server for details.",
					MailUtils.parseAddress("core@docear.org"), emergencyConfig);
		}
		catch (AddressException e) {
			e.printStackTrace();
		}
		
		System.exit(100);
	}

	private void postNotificationUpdate(Recipient recipient, boolean resetOnly) {
		if (recipient.getTitleList().size() > 0) {
			String queryStr = "/internal/mailer/persons/" + recipient.getPid() + "/update/?notified=" + (!resetOnly) + "&reset=true";
			try {
				URL url = new URL(SERVICE_HOST + queryStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("accessToken", properties.getProperty("docear.mail.sender.token"));
				System.out.println(conn.getContent());
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(600 * 1000);
				}
				catch (InterruptedException ex) {
				}

				postNotificationUpdate(recipient, resetOnly);
			}
		}

	}

	private Map<String, Recipient> getNextChunk(int maxChunkSize) {
		Map<String, Recipient> chunk = new HashMap<String, Recipient>();

		String queryStr = "/internal/mailer/persons/chunk/?chunksize=" + maxChunkSize;
		try {
			URL url = new URL(SERVICE_HOST + queryStr);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("accessToken", properties.getProperty("docear.mail.sender.token"));
			Document doc = XmlUtils.getXMLDocument(conn.getInputStream());
			NodeList recieverList = doc.getElementsByTagName("receiver");
			for (int i = 0; i < recieverList.getLength(); i++) {
				Element receiver = (Element) recieverList.item(i);
				Recipient recipient = new Recipient(Integer.parseInt(receiver.getAttribute("pid")), receiver.getAttribute("token"));
				NodeList titleList = receiver.getElementsByTagName("document");
				for (int j = 0; j < titleList.getLength(); j++) {
					recipient.addTitle(titleList.item(j).getTextContent());
				}
				chunk.put(receiver.getAttribute("address"), recipient);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return chunk;
	}

	private boolean sendNotificationMail(String email, Recipient recipient) {
		List<String> titleList = recipient.getTitleList();
		
		if (titleList.size() > 0 && titleList.size()<=150) {
			String message = properties.getProperty("docear.mail.message");
			StringBuilder content = new StringBuilder();    			
			for (String string : recipient.getTitleList()) {
				content.append("    - ");
				content.append(string);
				content.append("\n");
			}
			boolean plural = titleList.size() > 1;			
			try {
    			message = message.replaceAll("\\{TITLE_COUNT\\}", String.valueOf(titleList.size()));
    			message = message.replaceAll("\\{TITLE_LIST\\}", content.toString().replaceAll("\\$", "\\\\\\$"));
    			message = message.replaceAll("\\{PLURAL_PAPERS\\}", (plural ? "s" : ""));
    			message = message.replaceAll("\\{PLURAL_THIS\\}", (plural ? "these" : "this"));
    			message = message.replaceAll("\\{PLURAL_IS\\}", (plural ? "are" : "is"));
    			message = message.replaceAll("\\{INDEXING_SETTINGS_URL\\}",
    					"https://www.docear.org/my-docear/my-documents/?email=" + email + "&token=" + recipient.getToken());
    			if (System.getProperty("org.docear.debug") != null && System.getProperty("org.docear.debug").equals("true")) {
    				email = "marcel.genzmehr@gmail.com";
    			}
    			
    			return MailUtils.sendMail(properties.getProperty("docear.mail.subject"), message, MailUtils.parseAddress(email),
    					MailUtils.DOCEAR_MAIL_CONFIGURATION);
    			}
			catch(Exception e) {				
				//System.err.println(email + " :\n" + content);
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
		
	}

	static class Recipient {
		private final int pid;
		private final String token;
		private final List<String> titleList = new ArrayList<String>();

		public Recipient(int pid, String token) {
			this.pid = pid;
			this.token = token;
		}

		public String getToken() {
			return token;
		}

		public int getPid() {
			return pid;
		}

		public List<String> getTitleList() {
			return Collections.unmodifiableList(this.titleList);
		}

		public void addTitle(String title) {
			this.titleList.add(title);
		}

	}

}
