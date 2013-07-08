package org.docear.googleparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.MasonTagTypes;
import net.htmlparser.jericho.MicrosoftTagTypes;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.multipart.impl.MultiPartWriter;

public class GoogleScholarParser {
	private String COOKIE_STORE_PATH = null;
	// private static final String COOKIES_CONF = "firefox.cookie";
	private static String SearchResultDiv = "div class=\"gs_r\"";
	private static String RightLinkSpan = "div class=\"gs_ggs gs_fl\"";
	private static String BaseURL = "http://scholar.google.com";
	
	private Pattern yearExtractPattern = Pattern.compile("[,-]{1,1} ([0-9]{2,4}) -");

	private final static Map<String, Integer> proxyRequestMap = new HashMap<String, Integer>();
	private static List<String> proxyServers = new ArrayList<String>();
	private static String currentProxy = null;
	private static int localCount = 0;

	private static ApacheHttpClient client = ApacheHttpClient.create();

	private String[] USER_AGENTS = new String[] {
			"Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:14.0) Gecko/20100101 Firefox/14.0.1"
			,"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:19.0) Gecko/20100101 Firefox/19.0"
			,"Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:19.0) Gecko/20100101 Firefox/19.0"
			,"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0"
			,"Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:17.0) Gecko/20100101 Firefox/17.0"
			,"Mozilla/5.0 (Windows NT 6.0; rv:2.0) Gecko/20100101 Firefox/4.0 Opera 12.14"
			,"Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14"
			,"Opera/9.80 (Windows NT 6.1; U; es-ES) Presto/2.9.181 Version/12.00"
			,"Mozilla/5.0 (X11; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1 Iceweasel/15.0.1"
			,"Mozilla/5.0 (X11; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1 Iceweasel/15.0.1"
			,"Mozilla/5.0 (X11; Linux x86_64; rv:15.0) Gecko/20120724 Debian Iceweasel/15.0"
			,"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.55.3 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10"
			,"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/537.13+ (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2"
			,"Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25"
			,"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)"
			,"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)"
			,"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)"
			,"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)"
			,"Mozilla/5.0 (compatible; MSIE 10.0; Macintosh; Intel Mac OS X 10_7_3; Trident/6.0)"
			};

	private Map<String, String> COOKIES = new HashMap<String, String>();

	private int maxResultsPerPage = 20;

	private final String lang;
	private String[] searchValues;
	private TreeSet<String> excludedTitles = new TreeSet<String>();
	private int startPage;
	private int rankCounter;
	private boolean retrieveBib = false;
	private int lastReponseCode = 0;
	private boolean createCookies = false;
	private String currentUserAgent;

	private GoogleScholarParser(String lang) {
	    Logger logger = Logger.getLogger("");
	    logger.setLevel(Level.SEVERE);
		MicrosoftTagTypes.register();
		MasonTagTypes.register();
		this.lang = lang;
		loadCookieFromFile();
	}

	public static GoogleScholarParser createParser(String lang, String[] searchValues) {
		GoogleScholarParser parser = new GoogleScholarParser(lang);
		parser.setSearchValue(searchValues);
		return parser;
	}

	private void setSearchValue(String[] searchValues) {
		this.searchValues = searchValues;
	}

	private void setExcludes(TreeSet<String> excludes) {
		this.excludedTitles = excludes;
		this.excludedTitles.add(generateCleanTitle(concatSearchValues(searchValues)).toLowerCase());
	}

	public List<WebSearchResult> getPdfLinks(int maxResults, String... excludeTitles) {
		setExcludes(getExcludeSet(excludeTitles));
		List<WebSearchResult> results = new ArrayList<WebSearchResult>();
		startPage = 0;
		rankCounter = 0;
		try {
			do {
				extractSearchResultsTo(requestNext(), results, maxResults);
			} while (results.size() < maxResults);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return results;
	}

	public List<WebSearchResult> getAllPdfLinks(int maxPages, String... excludeTitles) throws IOException {
		setExcludes(getExcludeSet(excludeTitles));
		List<WebSearchResult> results = new ArrayList<WebSearchResult>();
		startPage = 0;
		rankCounter = 0;

		do {
			extractSearchResultsTo(requestNext(), results, -1);
		} while (startPage < maxPages);

		return results;
	}

	public void setRetrieveBibTex(boolean enabled) {
		this.retrieveBib = enabled;
	}

	public boolean isRetrievingBibTex() {
		return this.retrieveBib;
	}

	public void createCookiesEnabled(boolean enabled) {
		this.createCookies = enabled;
	}

	public boolean createCookiesEnabled() {
		return this.createCookies;
	}

	private void extractSearchResultsTo(Source source, List<WebSearchResult> results, int maxResults) throws IOException {
//		List<Element> searchResultSegments = getAllElements(source, SearchResultDiv); // */source.getAllElements(SearchResultDiv);
		List<Element> searchResultSegments = source.getAllElements("class", "gs_r", false);

		//System.out.println("Encoding: " + source.getEncoding());
		if (searchResultSegments == null || searchResultSegments.size() <= 0) {
			throw new IOException("The HTML format does not contain any Search Result Divs: ");
		}

		for (Segment searchResultSegment : searchResultSegments) {
			rankCounter++;
			//List<? extends Segment> rightLinkSegments = searchResultSegment.getAllElements(RightLinkSpan);
			List<? extends Segment> rightLinkSegments = searchResultSegment.getAllElements("class", "gs_ggs gs_fl", false);
			if (rightLinkSegments.size() > 0) {

				Segment titlesegment = searchResultSegment.getFirstElement(HTMLElementName.H3);
				if (titlesegment == null) {
					System.out.println("The HTML format does not contain any H3 Headline in the Search Result Div: " + SearchResultDiv);
					continue;
				}
				titlesegment = titlesegment.getFirstElement(HTMLElementName.A);
				if (titlesegment == null) {
					System.out.println("The HTML format does not contain any A link Tag in the H3 Headline of the Search Result Div: " + SearchResultDiv);
					continue;
				}
				String title = titlesegment.getTextExtractor().toString();

				for (Segment rightLinkSegment : rightLinkSegments) {
					StartTag linkTag = rightLinkSegment.getFirstStartTag(HTMLElementName.A);
					if (linkTag == null) {
						System.out.println("The HTML format does not contain any A link Tag in the Right Link Span: " + RightLinkSpan);
						continue;
					}
					//get link file type
					Segment type = rightLinkSegment.getFirstElement("class", "gs_ctg2", false);
					if(type == null) {
						continue;
					}
					String strType = type.getTextExtractor().toString();
					if(!"[PDF]".equals(strType)) {
						continue;
					}
					String link = linkTag.getAttributeValue("href");

					Integer year = null;
					List<Element> authorSegments = searchResultSegment.getAllElements("class", "gs_a", false);
					if (authorSegments.size() > 0) {
						for (Segment authorSegment : authorSegments) {
							String line = authorSegment.getTextExtractor().toString();
							Matcher m = yearExtractPattern.matcher(line);
							if (m.find()) {
								String y = m.group();
								try {
									year = Integer.parseInt(y.substring(2, y.lastIndexOf(" ")).trim());
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								break;

							}
						}
					}
					
					List<Element> anchors = searchResultSegment.getAllElements(HTMLElementName.A);
					URL bibLink = null;
					Integer cited = null;
					for (Element a : anchors) {

						String text = a.getTextExtractor().toString();
						if (text.toLowerCase().startsWith("cited by")) {
							try {
								cited = Integer.parseInt(text.substring(8).trim());
							}
							catch (Exception e) {
							}
							break;
						}
					}
					
					if (isRetrievingBibTex()) {    										
    					try {
    						Element footline = searchResultSegment.getFirstElement("class", "gs_fl", false);
    						List<Element> citeLinks = footline.getAllElements("class", "gs_nph", false);
    						for (Element e : citeLinks) {
    							if("Cite".equals(e.getTextExtractor().toString())) {
    								bibLink = new URL(getBibTeXURL(e, null));
    							}
    						}
    					}
    					catch (Exception e) {
    					}
					}
					
					if (!isTitleExcluded(title)) {
						try {
							URL fullTextUrl = new URL(link);
							WebSearchResult result = new WebSearchResult(title, rankCounter, fullTextUrl, bibLink, year, cited);
							
							results.add(result);
							if ((maxResults > -1) && results.size() >= maxResults) {
								return;
							}
						}
						catch (MalformedURLException e) {
							continue;
						}						
					}
				}
			}
		}

	}

	private Source requestNext() throws IOException {
		GetMethod method = getURLConnection(startPage++);
		try {
			Source source = new Source(method.getResponseBodyAsStream());
			return source;
		}
		finally {
			method.releaseConnection();
		}

	}

	private TreeSet<String> getExcludeSet(String[] excludeTitles) {
		TreeSet<String> set = new TreeSet<String>();
		if (excludeTitles == null) {
			return set;
		}
		for (String title : excludeTitles) {
			set.add(generateCleanTitle(title).toLowerCase());
		}
		return set;
	}

	private boolean isTitleExcluded(String title) {
		String searchResult = generateCleanTitle(title).toLowerCase();
		return this.excludedTitles.contains(searchResult);
	}

	private String generateCleanTitle(String title) {
		if (title == null) {
			return "";
		}
		title = title.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
		if (title.length() > 1024) {
			title = title.substring(0, 1023);
		}
		return title;
	}

	public int getCurrentProxyRequestCount() {
		if (getCurrentProxy() == null) {
			return -1;
		}
		else {
			synchronized (proxyRequestMap) {
				Integer count = proxyRequestMap.get(getCurrentProxy());
				if (count == null) {
					count = 0;
					proxyRequestMap.put(getCurrentProxy(), count);
				}
				return count;
			}
		}
	}

	public int getCurrentRequestCount() {
		if (getCurrentProxy() == null) {
			return localCount;
		}
		else {
			synchronized (proxyRequestMap) {
				Integer count = proxyRequestMap.get(getCurrentProxy());
				if (count == null) {
					count = 0;
					proxyRequestMap.put(getCurrentProxy(), count);
				}
				return count;
			}
		}
	}

	private void increaseCount() {
		if (getCurrentProxy() == null) {
			localCount++;
		}
		synchronized (proxyRequestMap) {
			Integer count = proxyRequestMap.get(getCurrentProxy());
			if (count == null) {
				count = 0;
			}
			proxyRequestMap.put(getCurrentProxy(), (count + 1));
		}
	}

	public String getCurrentProxy() {
		return currentProxy;
	}

	public void setProxyList(List<String> proxyList) {
		synchronized (proxyServers) {
			if (proxyList == null) {
				throw new IllegalArgumentException("NULL");
			}
			proxyServers = proxyList;
		}
	}

	public void nextProxy() {
		synchronized (proxyServers) {
			if (proxyServers.size() == 0) {
				currentProxy = null;
				return;
			}
			if (currentProxy == null) {
				currentProxy = proxyServers.get(0);
			}
			else {
				int idx = proxyServers.indexOf(currentProxy);
				if (idx > -1 && (idx + 1) < proxyServers.size()) {
					currentProxy = proxyServers.get(idx + 1);
				}
				else {
					currentProxy = proxyServers.get(0);
				}
			}
			initiateClient();
		}
	}

	private void initiateClient() {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(GoogleScholarParser.class.getClassLoader());
		try {
			DefaultApacheHttpClientConfig cc = new DefaultApacheHttpClientConfig();
			if (getCurrentProxy() != null) {
				cc.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, "http://" + getCurrentProxy() + "/");
			}

			cc.getClasses().add(MultiPartWriter.class);
			client = ApacheHttpClient.create(cc);
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	public static void resetAllRequestCounter() {
		localCount = 0;
		synchronized (proxyRequestMap) {
			proxyRequestMap.clear();
		}
	}

	public static void resetRequestCounter(String proxyServer) {
		if (proxyServer != null) {
			synchronized (proxyRequestMap) {
				proxyRequestMap.remove(proxyServer);
			}
		}
		else {
			localCount = 0;
		}
	}

	public void setMaxResultsPerPage(int num) {
		this.maxResultsPerPage = num;
	}

	private GetMethod getURLConnection(int startPage) throws IOException {
		String searchString = "q=" + formatedSearchString(); //+ "+filetype:pdf";
		String langString = "hl=" + lang;

		GetMethod getMethod = new GetMethod(BaseURL + "/scholar?" + searchString + "&" + langString + "&start=" + (startPage * this.maxResultsPerPage)
				+ "&num=" + this.maxResultsPerPage + "&btnG=");
		getMethod.addRequestHeader("User-Agent", getCurrentUserAgent());
		getMethod.addRequestHeader("Host", "scholar.google.com");
		lastReponseCode = client.getClientHandler().getHttpClient().executeMethod(getMethod);
		if (lastReponseCode == 200) {
			increaseCount();
		}
		else {
			getMethod.releaseConnection();
			throw new IOException("(" + lastReponseCode + ") not OK: " + getMethod.getStatusText());
		}
		return getMethod;
	}
	
	private String requestUrl(URL url, Proxy proxy) throws HttpException, IOException {
		HttpURLConnection connection;
		
		connection = (HttpURLConnection) url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
		
		prepareHttpHeader(connection, BaseURL+getRequestQueryString(false));
		connection.connect();
		lastReponseCode = connection.getResponseCode();
		if (lastReponseCode == 200) {
			increaseCount();
			return getResponseContent(connection);
		}
		else {
			throw new IOException("(" + lastReponseCode + ") not OK: " + connection.getResponseMessage());
		}
		
	}

	public String getCurrentUserAgent() {
		if(this.currentUserAgent == null) {
			int rnd = new Random().nextInt(USER_AGENTS.length);
			this.currentUserAgent = USER_AGENTS[rnd];
			System.out.println("Using new UserAgent: "+ this.currentUserAgent);
		}
		return this.currentUserAgent;
	}
	
	public void resetCurrentUserAgent() {
		this.currentUserAgent = null;
	}

	public int getLastReponseCode() {
		return lastReponseCode;
	}

	private String formatedSearchString() {
		String searchFor = concatSearchValues(searchValues);
		// searchFor = URLEncoder.encode(searchFor, "UTF-8");
		return searchFor;
	}

	private String concatSearchValues(String... searchValues) {
		String searchFor = "";
		for (String searchValue : searchValues) {
			searchValue = searchValue.trim();
			for (int i = 0; i < searchValue.length(); i++) {
				char c = searchValue.charAt(i);
				if (!Character.isDigit(c) && !Character.isLetter(c)) {
					searchValue = searchValue.replace(c, ' ');
				}
			}
			try {
				if (searchFor.isEmpty()) {
					searchFor = searchFor + URLEncoder.encode(searchValue.trim(), "UTF-8");
				}
				else {
					searchFor = searchFor + "+" + URLEncoder.encode(searchValue.trim(), "UTF-8");
				}
			}
			catch (UnsupportedEncodingException e) {
			}
		}
		return searchFor;
	}

//	private String getCookieString() {
//		if (!createCookiesEnabled()) {
//			StringBuffer buffer = new StringBuffer();
//			Iterator<String> iter = COOKIES.values().iterator();
//			while (iter.hasNext()) {
//				buffer.append(iter.next());
//				if (iter.hasNext()) {
//					buffer.append("; ");
//				}
//			}
//
//			return buffer.toString();
//		}
//
//		Random random = new Random();
//		String random_hex = "";
//		for (int i = 0; i < 16; i++) {
//			random_hex = random_hex.concat(String.valueOf(random.nextInt(9)));
//		}
//		return "GSP=ID=" + random_hex + ":CF=4"; // CF=4 ensures bibtex output
//	}

	private void loadCookieFromFile() {
		try {
			if (COOKIE_STORE_PATH == null) {
				return;
			}
			FileInputStream fis = new FileInputStream(COOKIE_STORE_PATH);
			Reader reader = new InputStreamReader(fis);
			try {
				StringBuffer buffer = new StringBuffer();
				int chr;

				while ((chr = reader.read()) > -1) {
					buffer.append((char) chr);
				}
				String[] cookies = buffer.toString().split(";");
				for (String cookie : cookies) {
					String cookieName = cookie.substring(0, cookie.indexOf("="));
					this.COOKIES.put(cookieName, cookie.trim());
					System.out.println(cookieName + ": " + cookie);
				}
			}
			finally {
				reader.close();
				fis.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String dlBibTeXData(String url) {
		try {
			return dlBibTeXData(new URL(url));
		}
		catch (MalformedURLException e) {
		}
		return "";
	}

	public String dlBibTeXData(String url, Proxy proxy) {
		try {
			return dlBibTeXData(new URL(url), proxy);
		}
		catch (MalformedURLException e) {
		}
		return "";
	}

	public String dlBibTeXData(URL url, Proxy proxy) {
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
			prepareHttpHeader(connection, BaseURL+getRequestQueryString(false));
			connection.connect();

			if (connection.getResponseCode() == 200) {
//				StringBuffer buffer = new StringBuffer();
//				CharBuffer readBuffer = CharBuffer.allocate(1024);
//				InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
//				while (reader.read(readBuffer) > -1) {
//					buffer.append(readBuffer.rewind().toString());
//				}
//				return buffer.toString();
				return getResponseContent(connection);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String dlBibTeXData(URL url) {
		return dlBibTeXData(url, null);
	}

	public void setCookieStorePath(String s) {
		COOKIE_STORE_PATH = s;
		loadCookieFromFile();
	}

	public String getBibtexForTitle(String title, Proxy proxy) throws IOException {
		this.searchValues = title.split(" ");

		HttpURLConnection connection;
		connection = (HttpURLConnection) new URL(BaseURL + getRequestQueryString(false)).openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
		prepareHttpHeader(connection, BaseURL);

		connection.connect();
		try {
			int code = connection.getResponseCode();
			if (code == 200) {
//				StringBuffer site = new StringBuffer();
//				CharBuffer buffer = CharBuffer.allocate(1024);
//				InputStreamReader reader = new InputStreamReader(connection.getInputStream()/*, "UTF-8"*/);
//				while (reader.read(buffer) > -1) {
//					site.append(buffer.clear().toString());
//				}
				Source source = new Source(getResponseContent(connection));
				// find all results
				List<Element> searchResultSegments = source.getAllElements("class", "gs_r", false);
				for (Segment searchResultSegment : searchResultSegments) {
					Segment titlesegment = searchResultSegment.getFirstElement(HTMLElementName.H3);
					if (titlesegment == null) {
						continue;
					}
					titlesegment = titlesegment.getFirstElement(HTMLElementName.A);
					if (titlesegment == null) {
						continue;
					}
					String resultTitle = titlesegment.getTextExtractor().toString();

					if (!title.equals(resultTitle)) {
						continue;
					}

					Element footline = searchResultSegment.getFirstElement("class", "gs_fl", false);
					List<Element> citeLinks = footline.getAllElements("class", "gs_nph", false);
					for (Element e : citeLinks) {
						if("Cite".equals(e.getTextExtractor().toString())) {
							return dlBibTeXData(getBibTeXURL(e, proxy), proxy);
						}
					}
//					for (Element a : footer.getChildElements()) {
//						if ("Import into BibTeX".equals(a.getTextExtractor().toString())) {
//							return dlBibTeXData(BaseURL + a.getAttributeValue("href").replace("&amp;", "&"), proxy);
//						}
//					}

				}
			}
			else {
				System.out.println("org.docear.googleparser.GoogleScholarParser.getBibtexForTitle(title, proxy) --> error code: "+code);
			}	
		}
		catch(Exception e) {
			System.out.println("org.docear.googleparser.GoogleScholarParser.getBibtexForTitle(title, proxy): "+e.getMessage());
		}
		finally {
			connection.disconnect();
			connection.getInputStream().close();
		}
		return null;
	}

	private String getBibTeXURL(Element citeLink, Proxy proxy) {
		String str = citeLink.getAttributeValue("onclick");
		if(str == null) {
			return null;
		}
		int pos = str.indexOf("'");
		if(pos > -1) {
			str = str.substring(pos+1, str.indexOf("'", pos+2));
			try {
				String html = requestUrl(new URL(BaseURL + "/scholar?q=info:"+str+":scholar.google.com/&output=cite&hl=en&as_sdt=0,5"), proxy);
				Source source = new Source(html);
				List<Element> importLinks = source.getAllElements("class", "gs_citi", false);
				for (Element element : importLinks) {
					String text = element.getTextExtractor().toString();
					if ("Import into BibTeX".equals(text)) {
						return BaseURL + element.getAttributeValue("href").replace("&amp;", "&");
					}
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public Collection<WebSearchResult> getMatchingTitles(String title, Proxy proxy) throws IOException {
		List<WebSearchResult> results = new ArrayList<WebSearchResult>();
		this.searchValues = title.split(" ");
		
		HttpURLConnection connection;
		//scholar?hl=en&q=test+on+weblate&btnG=&lr=
		connection = (HttpURLConnection) new URL(BaseURL + getRequestQueryString(true)).openConnection(proxy == null ? Proxy.NO_PROXY : proxy);
		prepareHttpHeader(connection, BaseURL);

		connection.connect();
		try {
			int code = connection.getResponseCode();
			if (code == 200) {
				
//				StringBuffer site = new StringBuffer();
//				InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
//
//				int c = -1;
//				while ((c = reader.read()) > -1) {
//					site.append((char) c);
//				}
				
				String content = getResponseContent(connection);
				
				
				Source source = new Source(content);
				// find all results
				List<Element> searchResultSegments = source.getAllElements("class", "gs_r", false);
				int rank = 0;
				String firstTitle = null;
				for (Segment searchResultSegment : searchResultSegments) {
					rank++;
					Segment titlesegment = searchResultSegment.getFirstElement(HTMLElementName.H3);
					if (titlesegment == null) {
						continue;
					}
					titlesegment = titlesegment.getFirstElement(HTMLElementName.A);
					if (titlesegment == null) {
						continue;
					}
					String resultTitle = titlesegment.getTextExtractor().toString();
					
					if(firstTitle == null) {
						firstTitle = resultTitle;
					}					
					// DOCEAR - replace with soft matching
					if(!firstTitle.equals(resultTitle)) {
						continue;
					}
					URL link = null;
					List<Element> rightLinkSegments = searchResultSegment.getAllElements("class", "gs_ggs gs_fl", false);
					if (rightLinkSegments.size() > 0) {
						for (Segment rightLinkSegment : rightLinkSegments) {
							StartTag linkTag = rightLinkSegment.getFirstStartTag(HTMLElementName.A);
							if (linkTag == null) {
								continue;
							}
							try {
								link = new URL(linkTag.getAttributeValue("href")/*.replace("&amp;", "&")*/);
							}
							catch (Exception e) {
							}
							break;
						}
					}
					
					Integer year = null;
					List<Element> authorSegments = searchResultSegment.getAllElements("class", "gs_a", false);
					if (authorSegments.size() > 0) {
						for (Segment authorSegment : authorSegments) {
							String line = authorSegment.getTextExtractor().toString();
							Matcher m = yearExtractPattern.matcher(line);
							if (m.find()) {
								String y = m.group();
								try {
									year = Integer.parseInt(y.substring(2, y.lastIndexOf(" ")).trim());
								}
								catch (Exception e) {
									e.printStackTrace();
								}
								break;

							}
						}
					}

					List<Element> anchors = searchResultSegment.getAllElements(HTMLElementName.A);
					URL bibLink = null;
					Integer cited = null;
					for (Element a : anchors) {

						String text = a.getTextExtractor().toString();
						if (text.toLowerCase().startsWith("cited by")) {
							try {
								cited = Integer.parseInt(text.substring(8).trim());
							}
							catch (Exception e) {
							}
						}
					}
					try {
						Element footline = searchResultSegment.getFirstElement("class", "gs_fl", false);
						List<Element> citeLinks = footline.getAllElements("class", "gs_nph", false);
						for (Element e : citeLinks) {
							if("Cite".equals(e.getTextExtractor().toString())) {
								bibLink = new URL(getBibTeXURL(e, proxy));
								break;
							}
						}
					}
					catch (Exception e) {
					}
					if (bibLink == null) {
						System.out.println(rank + " has no bib");
						System.out.println(searchResultSegment);
					}

					results.add(new WebSearchResult(resultTitle, rank, link, bibLink, year, cited));

				}
			}
		}
		finally {
			connection.disconnect();
			connection.getInputStream().close();
		}
		return results;
	}

	

	private String getRequestQueryString(boolean inTitle) {
		
		String searchString = "q=";
		if(inTitle) {
			searchString += "allintitle%3A+\"";
		}
		searchString += formatedSearchString();
		if(inTitle) {
			searchString += "\"";
		}
		String langString = "hl=" + lang;
		return "/scholar?" + searchString + "&" + langString + "&btnG=&lr=";
	}

	private void prepareHttpHeader(HttpURLConnection connection, String referer) {
		connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.setRequestProperty("Connection", "keep-alive");
		if(referer == null) {
			referer = "http://scholar.google.com/";
		}
		connection.setRequestProperty("Referer", referer);
		connection.setRequestProperty("User-Agent", getCurrentUserAgent());
		connection.setRequestProperty("DNT", "1");
		connection.setRequestProperty("Host", "scholar.google.com");
	}
	
	private String getResponseContent(HttpURLConnection connection) {
		StringBuffer site = new StringBuffer();
		try {
			String encoding = connection.getContentEncoding();
			InputStream is = connection.getInputStream();
			if("gzip".equals(encoding)) {
				is = new GZIPInputStream(is);
			}
			String type = connection.getContentType();
			InputStreamReader reader;
			if(type != null && type.contains("UTF-8")) {
				reader = new InputStreamReader(is, "UTF-8");
			}
			else {
				reader = new InputStreamReader(is);
			}
	
			int c = -1;
			while ((c = reader.read()) > -1) {
				site.append((char) c);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return site.toString();
	}
}
