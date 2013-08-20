package org.sciplore.xtract;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.hibernate.bytecode.buildtime.ExecutionException;
import org.sciplore.beans.Document;
import org.sciplore.beans.References;
import org.sciplore.beans.Title;
import org.sciplore.utilities.config.Config;
import org.sciplore.xtract.xmlparsers.DocumentReader;
import org.sciplore.xtract.xmlparsers.HeaderResultParser;
import org.sciplore.xtract.xmlparsers.ReferenceResultParser;
import org.sciplore.xtract.xmlparsers.ResultDocumentReader;
import org.sciplore.xtract.xmlparsers.XMLToTextParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.tools.locator.FileLocator;
import de.intarsys.tools.locator.ILocator;

public class Xtract {
	private final static Logger logger = LoggerFactory.getLogger(Xtract.class);
	private File tmpDir = new File(System.getProperty("java.io.tmpdir"));
	private File dtdFile;
	private Properties p;
		
	public Xtract() throws Exception {
		p = Config.getProperties("org.sciplore.xtract", Xtract.class);
		
		dtdFile = new File(tmpDir + File.separator + "pdf2xml.dtd");
		dtdFile.deleteOnExit();
		if(!dtdFile.exists()) {
			URL dtdUrl = this.getClass().getResource("/pdf2xml.dtd");
			if (dtdUrl != null){
				try{
					dtdFile.createNewFile();
					InputStreamReader in = new InputStreamReader(dtdUrl.openStream());
					FileWriter out = new FileWriter(dtdFile);
	
					int c;
					while ((c = in.read()) != -1) {
				      out.write(c);
					}
		
				    in.close();
				    out.close();
				} catch(IOException e) {
					logger.error("Error while copying DTD: {}", e.getMessage());
					throw e;
				}
			} else {
				logger.error("Could not load DTD.");
				throw new Exception("Could not load DTD.");
			}
		}
		
		if (!p.getProperty("tmpPath").isEmpty()) {
			tmpDir = new File(p.getProperty("tmpPath"));
		}
		if (!tmpDir.isDirectory()) {
			throw new Exception("Temp Directory does not exist.");
		}
	}

	public String xtract(InputStream is) throws Exception {
		File pdf = File.createTempFile("temp", ".pdf", tmpDir);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(pdf)));
		int c;
		while ((c = is.read()) != -1) {
			out.writeByte(c);
		}
		out.close();
		String xml = xtract(pdf);
		pdf.delete();
		return xml;
	}
	
	public String xtract(File pdf) throws Exception {
		Document doc = xtractDocument(pdf);
		if (doc == null) {
			return null;
		}
		
		return doc.toXML();
	}
	
	public Document xtractDocument(File pdf) throws Exception {
		File pdfConv = File.createTempFile(basename(pdf.getName(), ".pdf"), "_converted.pdf", tmpDir);
		ILocator pdfLocator = new FileLocator(pdf);
		PDDocument pd = PDDocument.createFromLocator(pdfLocator); // PDDocument.load(pdf);
		ILocator pdfConvLocator = new FileLocator(pdfConv);
		pd.save(pdfConvLocator); //pd.save(pdfConv.getAbsolutePath());
		pd.close();
		
		File xml = File.createTempFile(basename(pdf.getName(), ".pdf"), "_pdftohtml.xml", tmpDir);
		pdfToXml(pdfConv, xml);
		File correctedXml = correctPdftohtlmXml(xml);
		File parsedXml = File.createTempFile(basename(pdf.getName(), ".pdf"), "_parsed.xml", tmpDir);
		String title = extractTitleAndParseXml(correctedXml, parsedXml, getDtdPath());
		File structuredXml = File.createTempFile(basename(pdf.getName(), ".pdf"), "_structured.xml", tmpDir);
		makeStructuredXml(parsedXml, structuredXml);
		File txt = File.createTempFile(basename(pdf.getName(), ".pdf").replace(' ', '_'), ".txt", tmpDir); // FIXME: ParsCit with spaces in filenames
		xmlToText(parsedXml, txt);
//		Document doc = extractHeader(txt);
//		if (title != null && !title.isEmpty()) {
//			doc.addActiveElement(new Title(title));
//		}
//		References r = extractCitations(txt, parsedXml);
//		doc.addActiveElement(r);
		Document doc = xtractDocumentFromTxt(txt, parsedXml, title);
		txt.delete();
		structuredXml.delete();
		parsedXml.delete();
		correctedXml.delete();
		xml.delete();
		pdfConv.delete();
		return doc;
	}
	
	public Document xtractDocumentFromTxt(File txt, File parsedXml, String title) throws Exception {
		Document doc = extractHeader(txt);
		if (doc != null) {
			if (title != null && !title.isEmpty()) {
    			doc.addActiveElement(new Title(title));
    		}
    		References r = extractCitations(txt, parsedXml);
    		if (r != null) {
    			doc.addActiveElement(r);
    		}
		}
		return doc;
	}
	
	private String basename (String name, String suffix) {
		return name.replaceFirst("^(.*?)\\" + suffix + "$", "$1");
	}

	private File correctPdftohtlmXml(File inXml) throws IOException {
		File outXml = File.createTempFile(basename(inXml.getName(), ".xml"), "_corrected.xml", tmpDir);
	
		BufferedReader in = new BufferedReader(new FileReader(inXml));
		BufferedWriter out = new BufferedWriter(new FileWriter(outXml));
		try {
			String line = null;
			while ((line = in.readLine()) != null) {
				line = line.replace("<A", "<a");
				line = line.replace("<i>", "");
				line = line.replace("</i>", "");
				line = line.replace("<b>", "");
				line = line.replace("</b>", "");
				out.write(line + "\n");
			}
		} finally {
			in.close();
			out.close();
		}
		
		inXml = outXml;
		return outXml;
	}

	/**
	 * 
	 *
	 * @return
	 */
	private String getDtdPath() {
		return dtdFile.getAbsolutePath();
	}

	private boolean execCommand(final File dir, final String... cmd) throws Exception {	
		ADestroyableExecutor destroyableExecutor = null;
		try {
    		ExecutorService executor = Executors.newSingleThreadExecutor();
    		destroyableExecutor = new ADestroyableExecutor() {			
				
    			@Override
    			public void run() {    				
    				try {
    					String cache;
    					String stdout = "";
    					String stderr = "";
    					ProcessBuilder pb = new ProcessBuilder(cmd);
    					pb.directory(dir);
    					logger.debug("Command: {}", pb.command());		
    					process = pb.start();
    					
    					BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    					BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    				
    					while ((cache = outReader.readLine()) != null) {
    						stdout += cache + "\n";
    					}
    				
    					while ((cache = errReader.readLine()) != null) {
    						stderr += cache + "\n";
    					}
    					
    					cache = null;
    					outReader.close();
    					errReader.close();
    					logger.trace("stderr: {}", stderr);
    					logger.trace("stdout: {}", stdout);		
    					if (process.waitFor() != 0) {
    						logger.debug("stderr: {}", stderr);
    						logger.debug("stdout: {}", stdout);
    						stdout = null;
    						stderr = null;    						
    						pb = null;
    						throw new Exception("Error executing command.");
    					}
    					stdout = null;
    					stderr = null;    					
    					pb = null;
					}
					catch (Exception e) {
						throw new ExecutionException(e.getMessage());
					}    				
    			}
    		};
    		
    		Future<?> future = executor.submit(destroyableExecutor);
    		future.get(300, TimeUnit.SECONDS);
    	} 
		catch (Throwable e) {
			System.out.println("org.sciplore.xtract.Xtract.execCommand(dir, cmd): "+e.getMessage());			
//			destroyableExecutor.destroy();
			System.err.println("killing perl process for: "+cmd[cmd.length-1]);
			String killCommand[] = {"pkill", "-9", "-f", cmd[cmd.length-1]};
			Runtime.getRuntime().exec(killCommand);
			return false;
    	}
		
		return true;
	}

	// Header Analyse
	private Document extractHeader(final File txt) throws Exception {
		final File xml = File.createTempFile(basename(txt.getName(), ".xml"), "_headerParseService.xml", tmpDir);
		
		if (p==null) {
			System.err.println("org.sciplore.xtract.Xtract.extractCitations(txt, xmlText): properties is null!");
			return null;
		}		
		
		
    	if (execCommand(null, "perl", "-CSD", p.getProperty("headerParseServicePath") + File.separator + "bin/extractHeader.pl", txt.getAbsolutePath(), xml.getAbsolutePath())) {				
    		// parse the XML reults
    		HeaderResultParser hrp = new HeaderResultParser();
    		// return Document object containing the obtained information
    		Document d = hrp.parse(xml);
    		xml.delete();
    		return d;
    	}
    	
    	return null;
	}

	// Reference analysis
	private References extractCitations(File txt, File xmlText) throws Exception {
		File xmlParsCit = File.createTempFile(basename(txt.getName(), ".xml"), "_ParsCit.xml", tmpDir);
		
		if (p==null) {
			System.err.println("org.sciplore.xtract.Xtract.extractCitations(txt, xmlText): properties is null!");
			return null;
		}		
		else if (xmlParsCit==null) {
			System.err.println("org.sciplore.xtract.Xtract.extractCitations(txt, xmlText): xmlParsCit is null!");
			return null;
		}
		
		if (execCommand(txt.getParentFile(), "perl", "-CSD", p.getProperty("parsCitPath") + File.separator + "bin/citeExtract.pl", txt.getAbsolutePath(), xmlParsCit.getAbsolutePath())) {

    		new File(basename(txt.getAbsolutePath(), ".txt") + ".cite").delete();
    		new File(basename(txt.getAbsolutePath(), ".txt") + ".body").delete();
    		
    		// Parsen der Ergebnisse um Wörter, etc. zu zählen
    		File xmlPos = xmlParsCit;
    		try {
    			try {
    				xmlPos = addCitationPositions(xmlText, xmlParsCit);				
    			} catch (FileNotFoundException e) {
    				e.printStackTrace();
    			} catch (XMLStreamException e) {
    				e.printStackTrace();
    			}
    	
    			// Ergebnis von ParsCit mit hinzugefügten Counts parsen
    			ReferenceResultParser rpp = new ReferenceResultParser();
    			
    			// Liste von Referenzen
    			References r = rpp.parse(xmlPos);
    			
    			return r;
    		}
    		finally {
    			xmlParsCit.delete();
    			xmlPos.delete();
    		}
		}
		
		return null;
	}

	private String extractTitleAndParseXml(File xml, File parsedXml, String dtdPath) {
		return new DocumentReader().parse(xml, parsedXml, dtdPath);
	}
	
		

	// parsen der Ergebnisse der Referenzanalyse um Wörter, etc. zu zählen
	private File addCitationPositions(File xml, File xmlParsCit) throws XMLStreamException, IOException {
		File xmlOut = File.createTempFile(basename(xmlParsCit.getName(), ".xml"), "_Positions.xml", tmpDir);
		InputStreamReader isr = new InputStreamReader(new FileInputStream(xmlParsCit));
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(isr);

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(xmlOut), "UTF-8");

		int[] counts;
		int conPos;

		while (parser.hasNext()) {
			switch (parser.getEventType()) {
			case XMLStreamConstants.START_DOCUMENT:
				writer.writeStartDocument();
				break;

			case XMLStreamConstants.END_DOCUMENT:
				writer.writeEndDocument();
				break;

			case XMLStreamConstants.START_ELEMENT:
				writer.writeStartElement(parser.getLocalName());
				for (int i = 0; i < parser.getAttributeCount(); i++) {
					writer.writeAttribute(parser.getAttributeLocalName(i),
							parser.getAttributeValue(i));
				}

				// true = Context gefunden
				if (parser.getLocalName().equals("context")) {
					// Position (Zeichen) abfragen, wurde durch ParsCit bestimmt
					conPos = Integer.parseInt(parser.getAttributeValue(0));
					if(xml != null) {
						counts = countPositions(xml, conPos); // Zählen der anderen Counts
						writer.writeAttribute("word", Integer.toString(counts[1]));
						writer.writeAttribute("sentence", Integer.toString(counts[2]));
						writer.writeAttribute("paragraph", Integer.toString(counts[3]));
						writer.writeAttribute("page", Integer.toString(counts[4]));
					}
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				writer.writeEndElement();
				break;

			case XMLStreamConstants.CHARACTERS:
				writer.writeCharacters(parser.getText());
				break;

			default:
				break;
			}
			parser.next();
		}
		return xmlOut;
	}

	// Zählt Wörter, etc.
	// geparst wird mit StAX
	private int[] countPositions(File input, int conPos) throws FileNotFoundException, XMLStreamException {
		boolean isParagraph = false; // Paragraph oder undefined
		boolean foundPosition = false; // zeigt an ob die gesuchte Position gefunden ist
		boolean isTitle = false; // zeigt an ob man sich gerade im Titel-Element befindet

		InputStreamReader isr = new InputStreamReader(new FileInputStream(input));
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(isr);

		int c_char = 0; // Zeichen zählen
		int c_page = 0; // Seiten zählen
		int c_paragraph = 0; // Absätze zählen
		int c_word = 0; // Wörter zählen
		int c_sentence = 0; // Sätze zählen

		while (parser.hasNext() && !foundPosition) {
			switch (parser.getEventType()) {
			case XMLStreamConstants.START_DOCUMENT:
				break;

			case XMLStreamConstants.END_DOCUMENT:
				break;

			case XMLStreamConstants.START_ELEMENT:
				if (parser.getLocalName().equals("page")) {
					c_page++;
					// +1 durch Leerzeilen, die bei der Umwandlung der hier
					// geparsten XML-Datei von Ammar
					// in Textdatei hinzugefügt wurden
				}
				if (parser.getLocalName().equals("Paragraph")
						|| parser.getLocalName().equals("undefined")) {
					c_paragraph++;
					// +1 durch Leerzeilen, die bei der Umwandlung der hier
					// geparsten XML-Datei von Ammar
					// in Textdatei hinzugefügt wurden
					isParagraph = true;
				}
				if (parser.getLocalName().equals("title")) {
					isTitle = true;
					c_char += 2;
					// +2 durch Leerzeilen, die bei der Umwandlung der hier
					// geparsten XML-Datei von Ammar
					// in Textdatei hinzugefügt wurden
				}
				for (int i = 0; i < parser.getAttributeCount(); i++) {
					if (parser.getAttributeLocalName(i).equals("name")) {
						String text = parser.getAttributeValue(i);
						c_char += text.length() + 3;
						// +3 durch Leerzeilen, die bei der Umwandlung der hier
						// geparsten XML-Datei von Ammar
						// in Textdatei hinzugefügt wurden
						String[] words = text.split(" ");
						c_word += words.length;
					}
				}

				break;

			case XMLStreamConstants.END_ELEMENT:
				if (parser.getLocalName().equals("Paragraph")
						|| parser.getLocalName().equals("undefined")) {
					isParagraph = false;
				}
				if (parser.getLocalName().equals("title")) {
					isTitle = false;
				}
				break;

			case XMLStreamConstants.CHARACTERS:
				if (isParagraph || isTitle) {
					String text = parser.getText();
					if ((c_char + text.length()) < conPos) {
						c_char += text.length();
						String[] words = text.split(" ");
						c_word += words.length;
						String[] sentences = text.split("\\. *[A-Z]");
						c_sentence += sentences.length;
					} else {
						foundPosition = true;
						int rel_pos = conPos - c_char;
						try {
							String[] words = text.substring(0, rel_pos).split(
									" ");
							c_word += words.length;
							String[] sentences = text.split("\\. *[A-Z]");
							c_sentence += sentences.length;
						} catch (StringIndexOutOfBoundsException e) {
						}
					}
				}
				break;

			default:
				break;
			}

			parser.next();
		}
		int[] counts = { c_char, c_word, c_sentence, c_paragraph, c_page };
		return counts;
	}

	
	private void makeStructuredXml(File in, File out) {
		new ResultDocumentReader(in, out);
	}

	/*
	 * ConvertPDFS takes as and input the path of the output directory and takes
	 * an array of the pdf files to be converted into XML files using the
	 * pdftohtml.exe tool
	 */
	private void pdfToXml(File inPdf, File outXml) throws Exception {
		if (!inPdf.canRead()) {
			logger.error("Cannot read PDF file {}", inPdf.getAbsolutePath());
			throw new Exception("Cannot read PDF file " + inPdf.getAbsolutePath());
		}
		logger.info("Converting file: " + inPdf.getAbsolutePath());
		try {
			execCommand(outXml.getParentFile(), p.getProperty("pdfToHtml", "pdftohtml"), "-i", "-noframes", "-nomerge", "-enc", "UTF-8", "-xml", inPdf.getAbsolutePath(), basename(outXml.getAbsolutePath(), ".xml"));
		} catch (Exception e) {
			logger.error("Could not convert PDF to XML: {}", e.getMessage());
		}
		
		if (!outXml.canRead()) {
			logger.error("Could not convert PDF to XML.");
		}
	}
	
	private void xmlToText(File in, File out) {
		XMLToTextParser handler = new XMLToTextParser();
		handler.parse(in, out);
	}
}