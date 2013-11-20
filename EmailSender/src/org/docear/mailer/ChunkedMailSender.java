package org.docear.mailer;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.sciplore.utilities.config.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ChunkedMailSender {
	private final Properties properties = Config.getProperties("mail.sender");
	private final static String SERVICE_HOST = "http://localhost:8080";//"https://api.docear.org";
	
	public void start(final int maxChunkSize) {
		new Thread() {
			public void run() {
				Map<String, Recipient> chunk = getNextChunk(maxChunkSize);
				while(!chunk.isEmpty()) {
					Iterator<Entry<String, Recipient>> iter = chunk.entrySet().iterator();
					while(iter.hasNext()) {
						Entry<String, Recipient> entry = iter.next();
						if(sendNotificationMail(entry.getKey(), entry.getValue())) {
							postNotificationUpdate(entry.getValue(), false);
							iter.remove();
						}
						else {
							postNotificationUpdate(entry.getValue(), true);
						}
					}
				}
			}
		}.start();		
	}
	
	private void postNotificationUpdate(Recipient recipient, boolean resetOnly) {
		if(recipient.getTitleList().size() > 0) {
			String queryStr = "/mailer/update/"+recipient.getPid()+"/?notified="+(!resetOnly)+"&reset=true";
			try {
				URL url = new URL(SERVICE_HOST + queryStr);
				URLConnection conn = url.openConnection();
				
				System.out.println(conn.getContent());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	private Map<String, Recipient> getNextChunk(int maxChunkSize) {
		Map<String, Recipient> chunk = new HashMap<String, Recipient>();
		
		String queryStr = "/internal/mailer/docidx/chunk/?chunksize="+maxChunkSize;
		try {
			URL url = new URL(SERVICE_HOST + queryStr);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("accessToken", properties.getProperty("docear.mail.sender.token"));
			Document doc = XmlUtils.getXMLDocument(conn.getInputStream());
			NodeList recieverList = doc.getElementsByTagName("receiver");
			for (int i = 0; i < recieverList.getLength(); i++) {
				Element receiver = (Element)recieverList.item(i);
				Recipient recipient = new Recipient(Integer.parseInt(receiver.getAttribute("pid")));
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
		if(titleList.size() > 0) {
			String message = properties.getProperty("docear.mail.message");
			StringBuilder content = new StringBuilder();
			for (String string : recipient.getTitleList()) {
				content.append(string);
				content.append("\n");
			}
			message = message.replaceAll("\\{TITLE_LIST\\}", content.toString());
			return MailUtils.sendMail(properties.getProperty("docear.mail.subject"), message, MailUtils.parseAddress("marcel.genzmehr@gmail.com"), MailUtils.DOCEAR_MAIL_CONFIGURATION);
		}
		else {
			return false;
		}
	}
	
	static class Recipient {
		private final int pid;
		private final List<String> titleList = new ArrayList<String>();
		
		public Recipient(int pid) {
			this.pid = pid;
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
