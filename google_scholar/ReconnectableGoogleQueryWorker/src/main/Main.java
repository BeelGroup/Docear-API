package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.googleparser.GoogleScholarParser;
import org.docear.googleparser.WebSearchResult;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class Main {

	private Client client = Client.create();
	private static final SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
	private static String time = dateFormatGmt.format(new Date());

	private static int sleepTime = 100;
	private static long noDataWaitTime = 15;
	private static int requestTimeOut = 1;
	private static int maxDocs = 20;
	private static boolean waitExternally = false;

	private static String CLIENT_ID = "" + System.nanoTime();
	private ScheduledExecutorService probe = Executors.newSingleThreadScheduledExecutor();
	private ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
	private Future<?> currentTask = null;
	private Future<?> lastTask = null;
	private int taskActiveCount = 0;
	private static int count;
	private static int modelsPerSession = 10;

	public Main() {
		time = dateFormatGmt.format(new Date());
		try {
			// initiate task observer
			probe.scheduleWithFixedDelay(getObserverTask(), 1, 1, TimeUnit.MINUTES);

			// get network mac address as client-id
			byte[] deviceAddress = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
			// format mac-address as hex string
			if (deviceAddress != null && deviceAddress.length > 0) {
				CLIENT_ID = "";
				for (byte b : deviceAddress) {
					int i = b;
					if (i < 0) {
						i = 128 - i;
					}
					if (CLIENT_ID.length() > 0) {
						CLIENT_ID += ":";
					}
					if (i < 16) {
						CLIENT_ID += "0";
					}
					CLIENT_ID += Integer.toHexString(i);
				}
			}
		}
		catch (Exception e) {
			System.err.println("could not retrieve mac-address ... using SystemNanoTime as Client-ID.");
		}
		Main.log("Client-ID: " + CLIENT_ID);

		// add a shutdown hook to safely close all threading
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Main.log("shutting down...");
				cleanUpWorkLoad();
				probe.shutdownNow();
				getSingleThreadExecutor().shutdownNow();
				Main.log("exit.");
			}
		});
	}

	public static void main(String... args) {
		if (args != null && args.length > 0) {
			for (String arg : args) {
				if (arg.trim().toLowerCase().startsWith("sleep=")) {
					String[] tokens = arg.split("=");
					try {
						sleepTime = Integer.parseInt(tokens[1]);

					}
					catch (Exception e) {
						System.out.println("invalid value for 'sleep': " + e.getMessage() + " \nignore parameter and set sleepTime value to 100ms ...");
					}
				}
				if (arg.trim().toLowerCase().startsWith("timeout=")) {
					String[] tokens = arg.split("=");
					try {
						requestTimeOut = Integer.parseInt(tokens[1]);

					}
					catch (Exception e) {
						System.out.println("invalid value for 'timeout': " + e.getMessage() + " \nignore parameter and set requestTimeOut value to 1s ...");
					}
				}
				else if (arg.trim().toLowerCase().startsWith("nodatawait=")) {
					String[] tokens = arg.split("=");
					try {
						noDataWaitTime = Integer.parseInt(tokens[1]);

					}
					catch (Exception e) {
						System.out.println("invalid value for 'noDataWait': " + e.getMessage()
								+ " \nignore parameter and set noDataWait value to 15 minutes ...");
					}
				}
				else if (arg.trim().toLowerCase().startsWith("maxdocs=")) {
					String[] tokens = arg.split("=");
					try {
						maxDocs = Integer.parseInt(tokens[1]);

					}
					catch (Exception e) {
						System.out.println("invalid value for 'maxDocs': " + e.getMessage() + " \nignore parameter and set maxDocs value to 20 ...");
					}
				}
				else if (arg.trim().toLowerCase().startsWith("modelsPerSession=")) {
					String[] tokens = arg.split("=");
					try {
						modelsPerSession  = Integer.parseInt(tokens[1]);

					}
					catch (Exception e) {
						System.out.println("invalid value for 'maxDocs': " + e.getMessage() + " \nignore parameter and set maxDocs value to 20 ...");
					}
				}
				else if (arg.trim().toLowerCase().startsWith("externalwait")) {
					waitExternally = true;
				}
			}
		}

		// initiate main working instance
		Main main = new Main();
		Main.log("set sleepTime to: " + sleepTime);
		Main.log("set noDataWaitTime to: " + noDataWaitTime);
		Main.log("set requestTimeOut to: " + requestTimeOut);
		try {
			// release all remaining locks for this client-id
			main.cleanUpWorkLoad();
			int noServiceCount = 0;

			try {
				int count = main.query();
				System.out.println("requesting documents from google: " + count);
				noServiceCount = 0;
			}
			catch (NotActiveException e) {
				if (waitExternally) {
					Main.log("external handling for 'no data'");
					System.exit(9999);
				}
				else {
					if (noServiceCount++ > 10) {
						System.gc();
						System.exit(0);
					}
					Main.log("no service or nothing to do ... waiting " + noDataWaitTime + " minutes ...");
					main.sleep(noDataWaitTime * 60000); // wait approximately
														// 10-15 minutes
					Main.log("trying again ...");
				}
			}

		}
		catch (GoogleCaptchaException e) {
			File file = new File("captcha_error.log");
			FileWriter fw;
			try {
				fw = new FileWriter(file, true);
				DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				fw.write(df.format(new Date()) + "\n");
				fw.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
				Main.log("exception shutdown");
			}
		}
		catch (Exception e) {
			Main.log(e.getMessage());
			Main.log("exception shutdown");
		}
		System.gc();
		System.exit(0);

	}

	private int query() throws IOException, NotActiveException {
		count = 0;
		currentTask = null;
		client = Client.create();

		Main.log("requesting new query data...");
		ClientResponse response;
		Future<ClientResponse> callFuture = getSingleThreadExecutor().submit(new Callable<ClientResponse>() {
			public ClientResponse call() throws Exception {
				WebResource webResource = client.resource("https://api.docear.org/internal/recommendations/retrieve_keywords/").queryParam("clientId",
						CLIENT_ID).queryParam("count", ""+modelsPerSession);
				Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");
				return builder.get(ClientResponse.class);
			}

			public String toString() {
				return "QueryDataRequestTask";
			}
		});
		try {
			currentTask = callFuture;
			Main.log("waiting(max 20sec)...");
			response = callFuture.get(20, TimeUnit.SECONDS);
			Main.log("query data available.");
		}
		catch (Exception e) {
			currentTask = null;
			throw new NotActiveException(e.getMessage());
		}

		currentTask = null;
		if (response.getStatus() != Status.OK.getStatusCode()) {
			throw new NotActiveException("no data available");
			// return count;
		}

		String models = null;
		final ClientResponse respEntity = response;
		Future<String> modelFuture = getSingleThreadExecutor().submit(new Callable<String>() {
			public String call() throws Exception {
				return respEntity.getEntity(String.class);
			}

			public String toString() {
				return "ReadingDataTask";
			}
		});
		try {
			currentTask = modelFuture;
			Main.log("waiting(max 20sec)...");
			models = modelFuture.get(20, TimeUnit.SECONDS);
			Main.log("query data recieved.");
		}
		catch (Exception e) {
			currentTask = null;
			throw new NotActiveException("no data recieved");
		}

		if (models == null || models.trim().length() <= 0) {
			currentTask = null;
			throw new NotActiveException("empty data");
		}

		String fields[] = models.split("\n");
		
		Main.log("got " + fields.length + " models.");
		try {
			String cookiePath = null;
			for (String model : fields) {
				currentTask = null;
				if (model.trim().length() > 0) {
					String[] tokens = model.split(":");
					final String modelId = tokens[0];
					String[] keywords = tokens[1].split(" ");
					final GoogleScholarParser parser = GoogleScholarParser.createParser("en", keywords);
					parser.setMaxResultsPerPage(maxDocs);
					parser.setRetrieveBibTex(false);
					if (cookiePath != null) {
						parser.setCookieStorePath(cookiePath);
					}

					Callable<List<WebSearchResult>> task = getNewGoogleQueryTask(parser);
					List<WebSearchResult> recommendations = null;
					Main.log("running google query task for : model_ID=" + modelId);
					currentTask = getSingleThreadExecutor().submit(task);
					try {
						Main.log("google query task wait...");
						recommendations = (List<WebSearchResult>) currentTask.get();
					}
					catch (InterruptedException e) {
					}
					catch (Exception e) {
						Main.log(e.getMessage());
					}
					currentTask = null;

					Main.log("google query task finished for : model_ID=" + modelId);
					Main.log("google response status: " + parser.getLastReponseCode());
					if (parser.getLastReponseCode() == 503) {
						resetWorkLoad();
						throw new GoogleCaptchaException("Google Captcha Request...");
					}
					count = parser.getCurrentRequestCount();

					if (recommendations != null && recommendations.size() > 0) {
						Iterator<WebSearchResult> iter = recommendations.iterator();
						int docCount = 0;
						while (iter.hasNext() && !full(docCount)) {
							WebSearchResult result = iter.next();
							Main.log("post document for: model_ID=" + modelId);
							Main.log("title: " + result.getTitle());
							currentTask = getSingleThreadExecutor().submit(getPostDocumentTask(modelId, result));
							docCount++;
							try {
								Main.log("post document wait...");
								currentTask.get(20, TimeUnit.SECONDS);
							}
							catch (Exception e) {
								Main.log(e.getMessage());
							}
							currentTask = null;
							Main.log("post document finished for : model_ID=" + modelId);
						}
					}
					else {
						Main.log("(GoogleQueryWorker) Empty recommendation");

					}
				}
				else {
					Main.log("(GoogleQueryWorker) No content in model");
				}
			}
		}
		finally {
			currentTask = null;
			cleanUpWorkLoad();
		}

		return count;
	}

	private ExecutorService getSingleThreadExecutor() {
		if (taskExecutor == null || taskExecutor.isShutdown()) {
			try {
				taskExecutor.awaitTermination(1, TimeUnit.SECONDS);
			}
			catch (Throwable e) {
			}
			taskExecutor = Executors.newSingleThreadExecutor();
		}
		return taskExecutor;
	}

	private Runnable getObserverTask() {
		return new Runnable() {
			public void run() {
				Main.log("task observer running...");
				try {
					if (currentTask == null) {
						taskActiveCount = 0;
						lastTask = null;
						return;
					}

					if (lastTask == currentTask) {
						taskActiveCount++;
						Main.log("blocking task(" + currentTask + ") warning: " + taskActiveCount);
					}
					else {
						taskActiveCount = 0;
						lastTask = currentTask;
					}

					if (taskActiveCount > 2) {
						Main.log("trying to shutdown blocking task(" + lastTask + ") ...");
						// shutdown the current running task
						lastTask.cancel(true);
						getSingleThreadExecutor().shutdownNow();
						currentTask = null;
						Main.log("task(" + lastTask + ") canceled.");
					}
				}
				catch (Throwable e) {
					Main.log(e.getMessage());
				}
				finally {
					Main.log("task observer finished.");
				}
			}
		};
	}

	private Callable<List<WebSearchResult>> getNewGoogleQueryTask(final GoogleScholarParser parser) {
		Callable<List<WebSearchResult>> task = new Callable<List<WebSearchResult>>() {
			public List<WebSearchResult> call() throws Exception {
				try {
					List<WebSearchResult> recommendations = parser.getAllPdfLinks(1);

					return recommendations;

				}
				catch (Throwable e) {
					Main.log(e.getMessage());
				}
				return null;

			}

			public String toString() {
				return "googleQueryTask";
			}
		};
		return task;
	}

	private Callable<ClientResponse> getPostDocumentTask(final String modelId, final WebSearchResult result) {
		Callable<ClientResponse> task = new Callable<ClientResponse>() {
			public ClientResponse call() throws Exception {

				MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
				formData.add("title", String.valueOf(result.getTitle()));
				formData.add("username", "pdfdownloader");
				formData.add("link", result.getLink().toExternalForm());
				formData.add("source", "scholar.google.com");
				formData.add("modelId", modelId);
				formData.add("year", (result.getYear() == null ? "" : Integer.toString(result.getYear())));
				formData.add("citeCount", (result.getCiteCount() == null ? "0" : Integer.toString(result.getCiteCount())));
				formData.add("rank", Integer.toString(result.getRank()));

				WebResource webResource = client.resource("https://api.docear.org/internal/recommendations/document");
				Builder builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");

				return builder.post(ClientResponse.class, formData);
			}

			public String toString() {
				return "PostDocumentTask: model_ID=" + modelId;
			}
		};
		sleep();
		return task;
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (Throwable e) {
		}

	}

	private void sleep() {
		sleep(sleepTime);
	}

	private boolean full(int docCount) {
		return (docCount >= maxDocs);
	}

	private void cleanUpWorkLoad() {
		try {
			Main.log("cleaning work load...");
			currentTask = null;
			Future<ClientResponse> callFuture = getSingleThreadExecutor().submit(new Callable<ClientResponse>() {
				public ClientResponse call() throws Exception {
					WebResource webResource;
					Builder builder;
					MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
					formData.add("clientId", CLIENT_ID);
					webResource = client.resource("https://api.docear.org/internal/recommendations/finish_keywords");

					builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");
					ClientResponse response = builder.post(ClientResponse.class, formData);
					System.out.println(response.getEntity(String.class));
					Main.log(response.toString());
					return response;
				}

				public String toString() {
					return "cleanUpWorkLoad";
				}
			});
			Main.log("wait " + requestTimeOut + " seconds ...");
			callFuture.get(requestTimeOut, TimeUnit.SECONDS);
			sleep();
			Main.log("cleanUpWorkLoad finished.");
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		currentTask = null;
	}

	private void resetWorkLoad() {
		try {
			Main.log("resetting work load...");
			currentTask = null;
			Future<ClientResponse> callFuture = getSingleThreadExecutor().submit(new Callable<ClientResponse>() {
				public ClientResponse call() throws Exception {
					WebResource webResource;
					Builder builder;
					MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
					formData.add("clientId", CLIENT_ID);
					webResource = client.resource("https://api.docear.org/internal/recommendations/reset_keywords");

					builder = webResource.header("accessToken", "AEF7AA6612CF44B92012982C6C8A0333");
					ClientResponse response = builder.post(ClientResponse.class, formData);
					Main.log(response.toString());
					return response;
				}

				public String toString() {
					return "resetWorkLoad";
				}
			});
			Main.log("waiting 1 seconds ...");
			callFuture.get(requestTimeOut, TimeUnit.SECONDS);
			sleep();
			Main.log("resetWorkLoad finished.");
		}
		catch (Throwable e) {
		}
		currentTask = null;
	}

	private static void log(String string) {
		String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.out.println(stamp + ": " + string);

		File logFile = new File(new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
		if (!logFile.exists()) {
			logFile.getAbsoluteFile().getParentFile().mkdirs();
			try {
				if (!logFile.createNewFile()) {
					return;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			PrintStream logPrinter = new PrintStream(new FileOutputStream(logFile, true), true, "UTF-8");
			logPrinter.println(stamp + ": " + string);
			logPrinter.flush();
			logPrinter.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	// private boolean reconnect() {
	// try {
	// log("reconnecting...");
	// final Process p = Runtime.getRuntime().exec(new String[] { "cmd.exe",
	// "/C", "RouterReconnect.exe" });
	// new Thread() {
	// public void run() {
	// InputStream is = p.getErrorStream();
	// int b = 0;
	// try {
	// while ((b = is.read()) > -1) {
	// System.err.print((char) b);
	// }
	// } catch (IOException e) {
	// }
	// }
	// }.start();
	//
	// new Thread() {
	// public void run() {
	// InputStream is = p.getInputStream();
	// int b = 0;
	// try {
	// while ((b = is.read()) > -1) {
	// System.err.print((char) b);
	// }
	// } catch (IOException e) {
	// }
	// }
	// }.start();
	// int retCode = p.waitFor();
	// if (retCode > 0) {
	// // do sth here!?
	// }
	// log("waiting for new ip...");
	// try {
	// Thread.sleep(60000);
	// } catch (Throwable e) {
	// }
	// log("start requesting...");
	// } catch (Throwable e) {
	// e.printStackTrace();
	// return false;
	// }
	// return true;
	//
	// }
}
