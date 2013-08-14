package org.sciplore.xtract.xmlparsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

// Es wird mit SAX geparst
// Umwandlung XML -> Text
public class XMLToTextParser extends DefaultHandler
{

	private Writer fw;
	private String nl = "\n";
	private String currentValue;
	
//	private char[] fi = {239 , 172 , 129 };
//	private String small_fi = new String( fi ,0 , 2);
//	private char[] ffi = {239 , 172 , 131 };
//	private String small_ffi = new String( ffi ,0 , 2);
	
	private String title;

	public XMLToTextParser()
	{
	}

	public void parse(File input, File output){
		fw = null;
		currentValue = "";
		// es wird angenommen, dass eine Ligatur vorhanden ist
		// wenn eine normale Folge von f und i gefunden wird, wird der Wert auf false gesetzt
		title = null;
		try
		{ 
			OutputStream os = new FileOutputStream(output);
			fw = new OutputStreamWriter(os,"UTF-8");  
			SAXParserFactory 	factory = SAXParserFactory.newInstance();
			SAXParser 			saxParser = factory.newSAXParser();
			DefaultHandler 		handler = this;
			InputStream fis = new FileInputStream(input); 
			saxParser.parse(fis, handler);

		}
		catch (Exception e)
		{	
			System.out.println("XMLTOTEXT FILE NOT FOUND");
			System.err.println(e);
		}
	}
	
	public void startDocument () throws SAXException
	{
	}
	
	public void endDocument () throws SAXException
	{
		try
		{
			fw.close();
		}
		catch (IOException e)
		{
			System.err.println("Konnte Datei nicht schliessen");
		}
	}
	
	public String getTitle() {
		return (title!=null ? title.trim() : "");
	}
	
	public void startElement (String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{		
		try
		{
			for (int i=0; i < atts.getLength(); i++)
			{
				if ( atts.getQName(i).equals("name") )
				{
					String value = atts.getValue(i);
//					value = sonderzeichen(value);
					fw.write(nl+nl+value+nl+nl);
				}
			}
		}
		catch (IOException e)
		{
			System.err.println("Konnte nicht in Datei schreiben (startElements)");
		}
	}

	public void endElement (String namespaceURI, String localName, String qName) {
		
		try
		{
			//if (qName.equals("title")) { title = currentValue.trim(); }
			
			if (qName.equals("title")) { title = currentValue.trim();  }
			
			if (currentValue.trim().length()>0) {
				fw.write(currentValue);
				fw.write(nl);
			}
			if ( qName.equals("title") ) {
					fw.write(nl);
			}
		}
		catch (IOException e)
		{
			System.err.println("Konnte nicht in Datei schreiben (endElement)");
		}
		currentValue = "";
	}
	
	public String sonderzeichen(String value) {
		int index = value.indexOf(8242, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"`"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8242, index+1);
		}

		index = value.indexOf(8467, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"l"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8467, index+1);
		}

		index = value.indexOf(8486, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"O"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8486, index+1);
		}

		index = value.indexOf(8592, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"-"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8592, index+1);
		}
		
		index = value.indexOf(8594, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"-"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8594, index+1);
		}

		index = value.indexOf(8704, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"V"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8704, index+1);
		}
		
		index = value.indexOf(8706, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"d"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8706, index+1);
		}
		
		index = value.indexOf(8710, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"d"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8710, index+1);
		}

		index = value.indexOf(8711, 0);
		while ( index > -1) {
			//value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			value = value.substring(0,index)+"v"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1	
			index = value.indexOf(8711, index+1);
		}
		
		index = value.indexOf(8712, 0);
		while ( index > -1) {
			try {
				//int[] t = { 8712 };
				//String ta = new String( t ,0 , 1);					
				//System.out.println("BLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: "+ta);
				//value = value.substring(0,index)+new String(value.substring(index,index+1).getBytes(), "UTF-8")+value.substring(index+1);
				value = value.substring(0,index)+"e"+value.substring(index+1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8712, index+1);
		}
		
		index = value.indexOf(8722, 0);
		while ( index > -1) {
			try {
				//value = value.substring(0,index)+new String( value.substring(index,index+1).getBytes(),"UTF-8" ) +"BLA"+value.substring(index+1);
				value = value.substring(0,index)+"-"+value.substring(index+1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8722, index+1);
		}
		
		index = value.indexOf(8727, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"*"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8727, index+1);
		}
		
		index = value.indexOf(8730, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"v"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8730, index+1);
		}
		
		index = value.indexOf(8733, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"="+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8733, index+1);
		}
		
		index = value.indexOf(8734, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"i"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8734, index+1);
		}

		index = value.indexOf(8745, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"U"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8745, index+1);
		}

		index = value.indexOf(8746, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"U"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8746, index+1);
		}
		
		index = value.indexOf(8800, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"="+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8800, index+1);
		}
		
		index = value.indexOf(8804, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"<"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8804, index+1);
		}

		index = value.indexOf(8805, 0);
		while ( index > -1) {
			value = value.substring(0,index)+">"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8805, index+1);
		}

		index = value.indexOf(8838, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"C"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(8838, index+1);
		}
		
/*			
		index = value.indexOf(967, 0);
		//System.out.println(value);
		while ( index > -1) {
			value = value.substring(0,index)+value.charAt(index)+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			//index = value.indexOf(small_fi, 0);
			index = value.indexOf(967, index+1);
		}
*/		
		index = value.indexOf(920, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"O"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(920, index+1);
		}

		index = value.indexOf(928, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"P"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(928, index+1);
		}

		index = value.indexOf(931, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"S"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(931, index+1);
		}

		index = value.indexOf(934, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"F"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(934, index+1);
		}

		index = value.indexOf(945, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"a"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(945, index+1);
		}
		
		index = value.indexOf(946, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"b"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(946, index+1);
		}
			
		index = value.indexOf(948, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"d"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(948, index+1);
		}
		
		index = value.indexOf(949, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"e"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(949, index+1);
		}
		
		index = value.indexOf(951, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"n"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(951, index+1);
		}

		index = value.indexOf(952, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"t"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(952, index+1);
		}
		
		index = value.indexOf(955, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"l"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(955, index+1);
		}
		
		index = value.indexOf(957, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"v"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(957, index+1);
		}

		index = value.indexOf(958, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"X"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(958, index+1);
		}
		
		index = value.indexOf(960, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"p"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(960, index+1);
		}

		index = value.indexOf(961, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"r"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(961, index+1);
		}
		
		index = value.indexOf(963, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"o"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(963, index+1);
		}
		
		index = value.indexOf(964, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"t"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(964, index+1);
		}
		
		index = value.indexOf(965, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"u"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(965, index+1);
		}
		
		index = value.indexOf(966, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"f"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(966, index+1);
		}

		index = value.indexOf(967, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"x"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(967, index+1);
		}

		index = value.indexOf(969, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"w"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(969, index+1);
		}
		
		index = value.indexOf(981, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"F"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(981, index+1);
		}
		
		index = value.indexOf(729, 0);
		while ( index > -1) {
			value = value.substring(0,index)+"."+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
			index = value.indexOf(729, index+1);
		}
		
		return value;
	}
		
	public void characters 	(char[] ch, int start, int length) throws SAXException 
	{	
		String value = new String(ch, start, length);
//		value = sonderzeichen(value);
		currentValue += value;
	}

	public void ignorableWhitespace (char[] c, int start, int length)
	{
	}
	
	public void warning(SAXParseException exception) throws SAXException 
	{
		Message("**Parsing Warning**\n", exception);
		throw new SAXException("Warning encountered");
	}


	public void error(SAXParseException exception) throws SAXException 
	{
		Message("**Parsing Error**\n", exception);
		throw new SAXException("Error encountered");
	}


	public void fatalError(SAXParseException exception) throws SAXException 
	{
		Message("**Parsing Fatal Error**\n", exception);
		throw new SAXException("Fatal Error encountered");
	}


	private void Message(String mode, SAXParseException exception) 
	{
		System.out.println("ToText");
		System.out.println(mode + " Line: " + exception.getLineNumber() + " URI: " + exception.getSystemId() + "\n" + " Message: " + exception.getMessage());
		
	}

	

}

