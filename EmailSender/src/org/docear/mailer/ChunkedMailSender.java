package org.docear.mailer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
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
import java.util.Random;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.docear.mailer.MailUtils.MailSenderException;
import org.docear.mailer.MailUtils.SmtpMailConfiguration;
import org.sciplore.utilities.config.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.mail.smtp.SMTPAddressFailedException;

public class ChunkedMailSender {
	private final Properties properties = Config.getProperties("mail.sender");
	private final static String SERVICE_HOST = "https://api.docear.org";

	public void start() {
		new Thread() {
			public void run() {
				if (System.getProperty("org.docear.debug.mail") != null && System.getProperty("org.docear.debug.mail").equals("true")) {
					runAddressTest();
				}
				else {
					int chunkSize = Integer.parseInt(properties.getProperty("docear.mail.chunk.size", "1"));
					int maxChunkRequests = Integer.parseInt(properties.getProperty("docear.mail.chunk.requestnumber", "1"));
		
					performSending(chunkSize, maxChunkRequests);
				}
			}
		}.start();
	}
	
	/**
	 * @param chunkSize
	 * @param maxChunkRequests
	 */
	private void performSending(int chunkSize, int maxChunkRequests) {
		System.out.println("starting parameters [docear.mail.chunk.size="+chunkSize+"; docear.mail.chunk.requestnumber="+maxChunkRequests+"]");
		
		Random rand = new Random();
		
		// i==-1 --> infinite loop
		for (int i=0; (maxChunkRequests<0 ? true : i<maxChunkRequests); i=(maxChunkRequests<0 ? -1 : i+1)) {
			
			Map<String, Recipient> chunk = getNextChunk(chunkSize);
			if (chunk.isEmpty()) {						
				try {
					Thread.sleep(24 * 3600 * 1000);//24h
				}
				catch (InterruptedException e) {
				}
			}
			else {
				System.out.println("got "+chunk.size()+" entries");
			}
			Iterator<Entry<String, Recipient>> iter = chunk.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Recipient> entry = iter.next();
				if (sendNotificationMail(entry.getKey(), entry.getValue())) {
					System.out.println(entry.getKey());
					postNotificationUpdate(entry.getValue(), false);
					iter.remove();					
				}
				else {
					postNotificationUpdate(entry.getValue(), true);
				}

				if (MailUtils.senderExceptions.size() > 0) {
					performEmergencyExit();
				}
				
				try {
					Thread.sleep((rand.nextInt(57)+2) * 1000); //wait from 2 to 58 seconds --> mean: 30 sec
				}
				catch (InterruptedException e) {
				}
			}

			try {
				Thread.sleep(600 * 1000); //10min
			}
			catch (InterruptedException e) {
			}
		}
	}
	
	private void runAddressTest() {
		Map<String, Recipient> chunk = new HashMap<String, Recipient>();
		
		//invalid address
		Recipient recipient = new Recipient(0, "");
		recipient.addTitle("Empty TEST Title");
		chunk.put(".jmolinas@conexion.com.py", recipient);
		
		//valid address
		recipient = new Recipient(-1, "");
		recipient.addTitle("Empty TEST Title");
		chunk.put("marcel.genzmehr@gmail.com", recipient);
		
		//2nd invalid address
		recipient = new Recipient(-2, "");
		recipient.addTitle("Empty TEST Title");
		chunk.put("jmol..inas@conexion.com", recipient);
		
		//force mail address exception
		recipient = new Recipient(-2, "");
		recipient.addTitle("Empty TEST Title");
		chunk.put("raise.mail.exception", recipient);
		
		//force exception
		recipient = new Recipient(-2, "");
		recipient.addTitle("Empty TEST Title");
		chunk.put("raise.exception", recipient);
		
		
		Iterator<Entry<String, Recipient>> iter = chunk.entrySet().iterator();
		while (iter.hasNext()) {
			MailUtils.senderExceptions.clear();
			Entry<String, Recipient> entry = iter.next();
			String emailAddr = entry.getKey();
			if("raise.mail.exception".equals(emailAddr)) {
				InternetAddress addr;
				try {
					addr = new InternetAddress("marcel.genzmehr@gmail.com");
					MailUtils.senderExceptions.add(new MailSenderException(new SendFailedException("test 1", new SMTPAddressFailedException(addr, "", 550, "blupp")), new InternetAddress[]{addr}));
					System.out.println("force invalid mail exception");
				} catch (AddressException e) {
				}				
			}
			else if("raise.exception".equals(emailAddr)) {
				InternetAddress addr;
				try {
					addr = new InternetAddress("marcel.genzmehr@gmail.com");
					MailUtils.senderExceptions.add(new MailSenderException(new IOException("test 2"), new InternetAddress[]{addr}));
					System.out.println("force shutdown exception");
				} catch (AddressException e) {
				}	
			}
			else {
				if (sendNotificationMail(emailAddr, entry.getValue())) {
					System.out.println(emailAddr);
					iter.remove();					
				}
				else {
					System.out.println("invalid mail address: "+ emailAddr);
				}
			}

			if (MailUtils.senderExceptions.size() > 0) {
				performEmergencyExit();
			}
		
			try {
				Thread.sleep(25 * 1000); //25sec
			}
			catch (InterruptedException e) {
			}
		}
		System.out.println("regular shutdown");
				
	}
	
	private void performEmergencyExit() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
		File senderExceptionsLog = new File(dateFormat.format(new Date()) + "__SenderExceptions.log");
		boolean exit = false;
		StringBuilder sb = new StringBuilder("\n");
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(senderExceptionsLog, true);
			StringWriter sw = new StringWriter();
			for (MailSenderException exception : MailUtils.senderExceptions) {
				exit = !exception.isInvalidAddressException() || exit;
				exception.print(sw);
				sw.append(System.getProperty("line.separator"));
				sw.flush();
			}
			sb.append(sw.toString());
			fw.write(sb.toString());
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
		if(exit) {
			System.out.println("=== EMERGENCY EXIT ===");
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
				MailUtils.sendMail("Docear document index notifier", "The service was stoped with an emergency.\n"+sb.toString()+"\n See the log files on the server for details.",
						MailUtils.parseAddress("core@docear.org"), emergencyConfig);
			}
			catch (AddressException e) {
				e.printStackTrace();
			}
			System.exit(100);
		}
	}

	private void postNotificationUpdate(Recipient recipient, boolean resetOnly) {
		if (recipient.getTitleList().size() > 0) {
			String queryStr = "/internal/mailer/persons/" + recipient.getPid() + "/update/?notified=" + (!resetOnly) + "&reset=true";
			try {
				URL url = new URL(SERVICE_HOST + queryStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("accessToken", properties.getProperty("docear.mail.sender.token"));
				Object o = conn.getContent();
				if(o instanceof InputStream) {
					InputStream is = (InputStream)o;
					byte[] bytes = new byte[1024];
					int len = -1;
					while((len = is.read(bytes)) > -1) {
						System.out.print(new String(bytes, 0, len));
					}
					System.out.println();
				}
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
		
		if (MailUtils.isValidMailAddress(email) && titleList.size() > 0 && titleList.size()<=150) {
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
    			message = message.replaceAll("\\{RECV_MAIL_ADDR\\}", email);
    			message = message.replaceAll("\\{PLURAL_PAPERS\\}", (plural ? "s" : ""));
    			message = message.replaceAll("\\{PLURAL_THIS\\}", (plural ? "these" : "this"));
    			message = message.replaceAll("\\{PLURAL_IS\\}", (plural ? "are" : "is"));
    			message = message.replaceAll("\\{PLURAL_WAS\\}", (plural ? "were" : "was"));
    			message = message.replaceAll("\\{INDEXING_SETTINGS_URL\\}", "http://www.doc-ear.org/my-docear/my-documents/?email=" + email + "&token=" + recipient.getToken());
    			if (System.getProperty("org.docear.debug") != null && System.getProperty("org.docear.debug").equals("true")) {
    				email = "marcel.genzmehr@gmail.com";
    			}
    			
    			return MailUtils.sendMail(properties.getProperty("docear.mail.subject"), message, MailUtils.parseAddress(email),
    					MailUtils.DOCEAR_MAIL_CONFIGURATION);
    			}
			catch(Exception e) {			
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
