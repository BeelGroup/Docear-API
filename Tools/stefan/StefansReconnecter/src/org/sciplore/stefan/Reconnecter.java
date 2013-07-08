package org.sciplore.stefan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class Reconnecter {

	public String getExternalIP() {
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.dyndns.org:8245/");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine();
			return ip;
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return "";
	}

	public void sendAndRead(URL url) throws IOException {
		URLConnection con = url.openConnection();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		// String inputLine;
		// while ((inputLine = in.readLine()) != null)
		// System.out.println(inputLine);
		in.close();
	}

	private Set<String> getUsedIps(String today, String yesterday) throws IOException {
		Set<String> ips = new TreeSet<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(today));
			try {
				String line;
				while ((line = br.readLine()) != null) {
					ips.add(line.trim());
				}
			}
			finally {
				br.close();
			}
		}
		catch (Exception e) {
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(yesterday));
			;
			try {
				String line;
				while ((line = br.readLine()) != null) {
					ips.add(line.trim());
				}
			}
			finally {
				br.close();
			}
		}
		catch (Exception e) {
		}

		return ips;
	}

	public void reconnect() throws IOException {
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("admin", "B52!Lm4".toCharArray());
			}
		});

		String ip = getExternalIP();
		Date today = new Date();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fileName = "ips_" + sdf.format(today) + ".log";
		String fileNameYesterday = "ips_" + sdf.format(yesterday) + ".log";

		FileWriter writer = new FileWriter(fileName, true);
		writer.write(ip + "\n");
		writer.close();

		Set<String> usedIps = getUsedIps(fileName, fileNameYesterday);
		String newIp = ip;
		int counter = 0;
		while (ip.equals(newIp) || usedIps.contains(newIp)) {
			// nach 10 versuchen eine unverbrauchte IP zu bekommen, abbrechen
			// und 3h warten
			if (!ip.equals(newIp)) {
				counter++;
				if (counter == 10) {
					try {
						System.out.println("Always getting used IPs... sleeping for 3 hours!");
						Thread.sleep(3 * 60000);
					}
					catch (InterruptedException e) {
					}
				}
			}

			System.out.println("old: " + ip);
			ExecutorService service = Executors.newSingleThreadExecutor();
			Future<?> future = service.submit(new Runnable() {

				@Override
				public void run() {
					try {
						sendAndRead(new URL("http://192.168.1.1/Forms/DiagADSL_1?LineInfoDisplay=&DiagDSLDisconnect=PPPoE+Trennung"));
						sendAndRead(new URL("http://192.168.1.1/Forms/DiagADSL_1?LineInfoDisplay=&DiagDSLConnect=PPPoE+Verbindung"));
					}
					catch (MalformedURLException e) {
						System.err.println(e.getMessage());
					}
					catch (IOException e) {
						System.err.println(e.getMessage());
					}

				}
			});
			try {
				future.get(30, TimeUnit.SECONDS);
			}
			catch (Exception e) {
				service.shutdownNow();
			}

			try {
				Thread.sleep(20000);
			}
			catch (InterruptedException e) {
			}

			newIp = getExternalIP();

			System.out.println("new: " + newIp);

		}

		sendNewIpToWebservice(newIp);
		System.exit(0);
	}

	private void sendNewIpToWebservice(String newIp) {
		try {
			Client client = Client.create();
			WebResource webResource = client.resource("https://api.docear.org/internal/chani/").queryParam("ip", newIp);
			Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0D46");

			builder.get(ClientResponse.class);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Reconnecter reconnecter = new Reconnecter();
		reconnecter.reconnect();

	}

}
