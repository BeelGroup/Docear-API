package org.sciplore.xtract.xmlparsers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sciplore.xtract.result.XMLResultDocument;
import org.sciplore.xtract.textstructure.FontSpec;
import org.sciplore.xtract.textstructure.Page;
import org.sciplore.xtract.textstructure.Text;
import org.sciplore.xtract.textstructure.TextSegment;
import org.sciplore.xtract.textstructure.XMLDocument;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DocumentReader extends DefaultHandler {
	private String title = null;
	/*
	 * Tags
	 */
	private  static final String PAGE = "PAGE";
	private  static final String FONTSPEC = "fontspec";
	private  static final String TEXT = "text";
	private  static final String ITALIC = "i";
	private  static final String BOLD = "b";
	/*
	 * Attributes
	 */

	private  static final String NUMBER = "number";
	private  static final String POSITION = "position";
	private  static final String TOP = "top";
	private  static final String LEFT = "left";
	private  static final String HEIGHT = "height";
	private  static final String WIDTH = "width";
	private  static final String ID = "id";
	private  static final String SIZE = "size";
	private  static final String FAMILY = "family";
	private  static final String COLOR = "color";
	private  static final String FONT = "font";

	/*
	 * Status Variables
	 */

	private  XMLDocument currXMLDoc;
	private Page currPage;
	private Text currText;
	private TextSegment currTextSegment;
	private FontSpec currFontSpec;

	private boolean currentIsItalic = false;
	private boolean currentIsBold = false;

	public String parse(File inFile, File outFile, String dtd) {
		// Use the default (non-validating) parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			// Input vorbereiten und sicherstellen das der String UTF8 kodiert
			// ist
			FileInputStream fileInputStream = new FileInputStream(inFile);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int len;
			while ((len = fileInputStream.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}
			fileInputStream.close();
			bos.close();
			byte[] data = bos.toByteArray();
			String xml = new String(data, "UTF8");
			
			for(int i = 0; i <= 0x1f; i++) {
				int index = xml.indexOf(i, 0);
				while (index > -1) {
					xml = xml.substring(0,index)+" "+xml.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
					index = xml.indexOf(i, index+1);
				}
			}
			
			InputSource input = new InputSource(new StringReader(xml));
			input.setSystemId(dtd);
//			input.setCharacterStream(new InputStreamReader(new FileInputStream(inFile), "UTF-8"));

			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(input, this);

			// System.out.println("######################################") ;
			// System.out.println(reader.currXMLDoc.toString()) ;
			XMLResultDocument xmlRes = new XMLResultDocument(currXMLDoc);
			// xmlRes.ProcessDocument() ;
			String fileName = inFile.getName();
			if (fileName.lastIndexOf('.') != -1) {
				int dotPosition = fileName.lastIndexOf('.');
				fileName = fileName.substring(0, dotPosition);
			}
			fileName = fileName.replace("&", "&amp;");
			title = xmlRes.GenerateFile(fileName, outFile).trim();
			// System.out.println(XMLpath) ;
			// System.out.println(xmlRes.getTitleFont()) ;
			// System.out.println("----Headings-----") ;
			for (int j = 0; j < xmlRes.getHeadingsFonts().size(); j++) {
				// System.out.println("Headings-----"+j) ;
				// System.out.println(xmlRes.getHeadingsFonts().get(j)) ;
			}
			// System.out.println("----Stadard Text-----") ;
			for (int j = 0; j < xmlRes.getStandardTextFonts().size(); j++) {
				// System.out.println("Stadard Text-----"+j) ;
				// System.out.println(xmlRes.getStandardTextFonts().get(j).getId())
				// ;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return title;
	}

	// ===========================================================
	// Methods in SAX DocumentHandler
	// ===========================================================

	public void startDocument() throws SAXException {
		currXMLDoc = new XMLDocument();
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String uri, String lName, String qName, Attributes attrs)
			throws SAXException {
		if (qName.equalsIgnoreCase(PAGE)) {
			String strTemp = attrs.getValue(NUMBER);
			int num = parseInt(strTemp, 0);

			String position = attrs.getValue(POSITION);

			strTemp = attrs.getValue(TOP);
			int top = parseInt(strTemp, 0);

			strTemp = attrs.getValue(LEFT);
			int left = parseInt(strTemp, 0);

			strTemp = attrs.getValue(HEIGHT);
			int height = parseInt(strTemp, 0);

			strTemp = attrs.getValue(WIDTH);
			int width = parseInt(strTemp, 0);

			currPage = new Page(num, position, top, left, height, width);
			currXMLDoc.addPage(currPage);
		} else if (qName.equalsIgnoreCase(FONTSPEC)) {
			String strTemp = attrs.getValue(ID);
			int id = parseInt(strTemp, 0);

			strTemp = attrs.getValue(SIZE);
			int size = parseInt(strTemp, 0);

			String family = attrs.getValue(FAMILY);

			String color = attrs.getValue(COLOR);

			currFontSpec = new FontSpec(id, size, family, color);

			currPage.addFont(currFontSpec);
		} else if (qName.equalsIgnoreCase(TEXT)) {
			String strTemp = attrs.getValue(TOP);
			int top = parseInt(strTemp, 0);

			strTemp = attrs.getValue(LEFT);
			int left = parseInt(strTemp, 0);

			strTemp = attrs.getValue(WIDTH);
			int width = parseInt(strTemp, 0);

			strTemp = attrs.getValue(HEIGHT);
			int height = parseInt(strTemp, 0);

			strTemp = attrs.getValue(FONT);
			int font = parseInt(strTemp, 0);

			currText = new Text(top, left, width, height, font);
			currPage.addText(currText);
			currentIsItalic = false;
			currentIsBold = false;

		} else if (qName.equalsIgnoreCase(ITALIC)) {
			currentIsItalic = true;
		} else if (qName.equalsIgnoreCase(BOLD)) {
			currentIsBold = true;
		}
	}

	public void endElement(String uri, String lName, String qName) throws SAXException {
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		String s = new String(buf, offset, len);

		org.sciplore.xtract.textstructure.TextSegment.Decoration deco = org.sciplore.xtract.textstructure.TextSegment.Decoration.none;
		if (currentIsBold && currentIsItalic)
			deco = org.sciplore.xtract.textstructure.TextSegment.Decoration.boldItalic;
		else if (currentIsBold)
			deco = org.sciplore.xtract.textstructure.TextSegment.Decoration.bold;
		else if (currentIsItalic)
			deco = org.sciplore.xtract.textstructure.TextSegment.Decoration.italic;

		currTextSegment = new TextSegment(s, deco);
		currText.addTextSegment(currTextSegment);

		int fontId = currText.getFontId();
		FontSpec fontSp = currXMLDoc.getFontSpecification(fontId);
		fontSp.addSegmentInfo(s.length());

		fontSp.addText(s);
	}

	private int parseInt(String s, int def) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

}
