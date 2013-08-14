package org.sciplore.xtract.result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.sciplore.xtract.textstructure.FontSpec;
import org.sciplore.xtract.textstructure.Page;
import org.sciplore.xtract.textstructure.Text;
import org.sciplore.xtract.textstructure.TextSegment;
import org.sciplore.xtract.textstructure.XMLDocument;


public class XMLResultDocument {

	public static int MIN_TITLE_LENGTH = 10;
	public static int INVALID_FONT_ID = -10;

	public static String KEYWORDS = "keywords";
	public static String INTRODUCTION = "introduction";
	public static String ABSTRACT = "abstract";
	public static String OVERVIEW = "overview";
	public static String OUTLINE = "outline";
	public static String ACKNOWLEDGMENT = "acknowledgment";
	public static String REFERENCE = "reference";
	public static String SUMMARY = "summary";
	public static String CONCLUSION = "conclusion";
	public static String APPENDIX = "appendix";

	public static String Einleitung = "einleitung";
	public static String SCHLUSSbemerkung = "schlußbemerkung";

	private XMLDocument xmlDoc;

	private int TitleFontID;
	private int sumCharsInDoc = 0;
	private int standardHeight = 0;

	private FontSpec TitleFont;
	private Vector<FontSpec> HeadingsFonts;
	private Vector<FontSpec> StandardTextFonts;

	public XMLResultDocument(XMLDocument xmlDocvar) {
		xmlDoc = xmlDocvar;
		HeadingsFonts = new Vector<FontSpec>();
		StandardTextFonts = new Vector<FontSpec>();
	}

	/*
	 * pre: before finding Title, Heading, Standard Fonts this function is used
	 * to calculate statistics extracted for the document like the: 1- the
	 * number of characters in the document
	 */
	private void calculateStatisticsPre() {
		for (Page pag : xmlDoc.getPages())
			for (FontSpec font : pag.getFonts()) {
				sumCharsInDoc += font.getTotalNumberOfCharachters();
			}
	}

	/*
	 * post: after finding Title, Heading, Standard Fonts this function is used
	 * to calculate so statistics extracted for the document like the: 1- the
	 * number of characters in the document
	 */
	private void calculateStatisticsPost() {
		Map<Integer, Integer> LineHeight = new HashMap<Integer, Integer>();
		Map<Integer, Integer> LineStart = new HashMap<Integer, Integer>();
		
		int height = 0;
		for (Page pag : xmlDoc.getPages())
			if (pag.getTexts().size() > 1) {
				int i = 0;
				Text tex1 = pag.getTexts().get(0);
				Text tex2 = null;
				for (i = 1; i < pag.getTexts().size(); i++) {
					tex2 = pag.getTexts().get(i);
					height = tex2.getTop() - tex1.getTop();
					tex1 = tex2;
					FontSpec fSpec=xmlDoc.getFontSpecification(tex1.getFontId()) ;
					if (tex1.getLeft() == tex2.getLeft()
							&& height >0
							&& tex1.getFontId() == tex2.getFontId()
							&& StandardTextFonts.contains(fSpec) ) {
						if (LineHeight.containsKey(height)) {
							Integer tempInt = LineHeight.get(height);
							LineHeight.remove(height);
							LineHeight.put(height, tempInt + 1);
						} else {
							LineHeight.put(height, 1);
						}
					}
					FontSpec fSpec2=xmlDoc.getFontSpecification(tex2.getFontId()) ;					
					if (StandardTextFonts.contains(fSpec2)&& tex2.getLeft()>=0 ) {
						if (LineStart.containsKey(tex2.getLeft())) {
							Integer tempInt = LineStart.get(tex2.getLeft());
							LineStart.remove(tex2.getLeft());
							LineStart.put(tex2.getLeft(), tempInt + 1);
						} else {
							LineStart.put(tex2.getLeft(), 1);
						}
					}
				}
			}

		// finding out what is the standard height for text lines
		int stdHeight = 0;
		int maxFreq = 0;

		
		for (Integer hei : LineHeight.keySet()) {
			//System.out.println("Hei " + hei.intValue() + "  FREQ "+ LineHeight.get(hei).intValue());
			if (LineHeight.get(hei).intValue() > maxFreq) {
				maxFreq = LineHeight.get(hei).intValue();
				stdHeight = hei.intValue();
			}
		}
		standardHeight = stdHeight;
		//System.out.println("standardHeight  " + standardHeight + "  FREQ "
		//		+ maxFreq);
		
		int maxLeftFreq = 0;
		for (Integer left : LineStart.keySet()) {
			//System.out.println("left " + left.intValue() + "  FREQ "
			//		+ LineStart.get(left).intValue());
			if (LineStart.get(left).intValue() > maxLeftFreq) {
				maxLeftFreq = LineStart.get(left).intValue();
			}
		}
		//System.out.println("standardleftt  " + stdLeft + "  FREQ "
		//		+ maxLeftFreq);
	}

	public void ProcessDocument() {
		calculateStatisticsPre();

		// finding the candidate fonts for the title and for the headers
		// in the first page
		FindTitleAndHeadingsInTheFirstPage();

		// if no header's font was found in the first page we check
		// the fonts in the second page
		if (HeadingsFonts.size() == 0 && xmlDoc.getPages().size() > 1) {
			//System.out.println("no headings found in the first page lets check the second one");
			Page scdPage = xmlDoc.getPages().get(1);
			FindHeadings(scdPage);

			// if no header's font was found in the first page we check
			// the fonts in the last page
			if (HeadingsFonts.size() == 0 && xmlDoc.getPages().size() > 2) {
				//System.out.println("no headings found in the first page lets check the last one");
				Page lstPage = xmlDoc.getPages().get(xmlDoc.getPages().size() - 1);
				FindHeadings(lstPage);
			}
		}

		// handling the found fonts for headers
		// and for each found font if it has more than 0.1 of the
		// text we will remove it form headers unless it is the
		// only found font
		if (HeadingsFonts.size() > 1) {
			//System.out.println("checking the found Headings");
			for (int j = 0; j < HeadingsFonts.size(); j++) {
				FontSpec fSpec = HeadingsFonts.get(j);
				if (fSpec.getTotalNumberOfCharachters() > (sumCharsInDoc * 0.1)) {
					//System.out.println("removing font from headings"+ fSpec.getId());
					if (HeadingsFonts.size() < 2) {
						//System.out.println("but we cannot remove headings "+ fSpec.getId()+ " because it is the only one");
					} else {
						HeadingsFonts.remove(fSpec);
						j -= 1;
					}
				}
			}
		}

		// finding the standard text's font
		FindStadardFont();

		calculateStatisticsPost();
		
		//correcting the text by replacing  &,<,>  by their HTML encoding
		CorrectText() ;
	}

	/*
	 * this function is used to find the 1- the font of the title 2- fonts for
	 * headers using the first page of the document
	 */
	public void FindTitleAndHeadingsInTheFirstPage() {
		Page firstPage = xmlDoc.getPages().get(0);
		int fid = 0;
		FontSpec fontTemp = null;

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (Text tex : firstPage.getTexts()) {

			fid = tex.getFontId();
			fontTemp = firstPage.getFontSpecification(fid);

			for (int j = 0; j < tex.getTextSegments().size(); j++) {
				TextSegment texSeg = tex.getTextSegments().get(j);

				if (!HeadingsFonts.contains(fontTemp)) {
					if (CheckTextForHeading(texSeg.getText())) {
						HeadingsFonts.add(fontTemp);
						// System.out.println("[" + texSeg.getSize() + "]"+
						// texSeg.getText());
					}
				}
				if (map.containsKey(fid)) {
					Integer tempInt = map.get(fid);
					map.put(fid, new Integer(tempInt.intValue()
							+ texSeg.getSize()));
				} else {
					map.put(fid, new Integer(texSeg.getSize()));
				}
			}
		}
		FontSpec maxFont = null;
		int maxSize = 0;
		int maxFontID = INVALID_FONT_ID;

		int size = 0;
		int occ = 0;

		for (Integer id : map.keySet()) {
			fontTemp = firstPage.getFontSpecification(id);
			if (fontTemp.getColor().equals("#7f7f7f")) {
				continue;
			}
			size = fontTemp.getSize();
			occ = map.get(id).intValue();
			if (size > maxSize & occ >= MIN_TITLE_LENGTH) {
				maxFontID = id;
				maxSize = size;
				maxFont = fontTemp;
			}
		}
		if (maxFontID != INVALID_FONT_ID) {
			TitleFontID = maxFontID;
			TitleFont = maxFont;
		}

	}

	/*
	 * this function is used to find the candidate fonts for the headers in a
	 * given page
	 */
	public void FindHeadings(Page page) {

		int fid = 0;
		FontSpec fontTemp = null;

		for (Text tex : page.getTexts()) {
			fid = tex.getFontId();
			fontTemp = page.getFontSpecification(fid);
			for (int j = 0; j < tex.getTextSegments().size(); j++) {
				TextSegment texSeg = tex.getTextSegments().get(j);
				if (!HeadingsFonts.contains(fontTemp)) {
					if (CheckTextForHeading(texSeg.getText()))
						HeadingsFonts.add(fontTemp);
				}
			}
		}

	}

	/*
	 * this function is used to find the candidate fonts for the standard Text
	 */
	public void FindStadardFont() {
		for (Page page : xmlDoc.getPages()) {
			for (FontSpec fontSp : page.getFonts()) {
				if (!StandardTextFonts.contains(fontSp)) {
					if (fontSp.getTotalNumberOfCharachters() >= (sumCharsInDoc * 0.1)) {
						StandardTextFonts.add(fontSp);
					}
				}
			}
		}
	}

	public FontSpec getTitleFont() {
		return TitleFont;
	}

	/*
	 * this function is used to check whether a given text includes known words
	 * for headers
	 */
	private boolean CheckTextForHeading(String text) {
		text = text.toLowerCase();
		if (text.length() < 20
				&& (text.contains(KEYWORDS) || text.contains(INTRODUCTION)
						|| text.contains(ABSTRACT) || text.contains(OVERVIEW)
						|| text.contains(OUTLINE)
						|| text.contains(ACKNOWLEDGMENT)
						|| text.contains(REFERENCE) || text.contains(SUMMARY)
						|| text.contains(CONCLUSION) || text.contains(APPENDIX)
						|| text.contains(Einleitung) || text
						.contains(SCHLUSSbemerkung)))

			return true;
		else
			return false;
	}

	public Vector<FontSpec> getHeadingsFonts() {
		return HeadingsFonts;
	}

	public String GenerateFile(String DocID, File outFile) {
		ProcessDocument();
		String title = null;

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(outFile));
			/*String output = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
					"<document id=\"" + DocID + "\">" + "\n";*/
			
			String output = "<?xml version=\"1.0\" " +
					"encoding=\""+Charset.defaultCharset().toString()+"\"?>" +
					"<document id=\"" + DocID + "\">" + "\n";
			
			boolean titleFound = false;
			for (int p=0; p<xmlDoc.getPages().size(); p++) {
				Page page = xmlDoc.getPages().get(p);
				output += "<page id=\"" + (p+1) + "\">" + "\n";
				if (p==0)
				{
					output += "<head>" + "\n";
				}
				
				for (int t = 0; t < page.getTexts().size(); t++) {
					Text tec1 = page.getTexts().get(t);
					FontSpec fSpec = xmlDoc.getFontSpecification(tec1.getFontId());
					//the case where the title is found for the first time
					if (TitleFont == fSpec && !titleFound) {
						titleFound = true;
						output += "<title>";
						title = tec1.getText();
						int tt = t;
						for (tt = t + 1; tt < page.getTexts().size(); tt++) {
							if (page.getTexts().get(tt).getFontId() == TitleFontID) {
								title += " " + page.getTexts().get(tt).getText();
							} else {
								break;
							}
						}
						t = tt - 1;
						output += title.trim() + "</title></head>" + "\n";
					} else {
						//the case where the title is not found yet
						//so the text will be undefined text
						if (!titleFound) {
							//output += "<undefined>" + "\n";
							String tex = tec1.getText();
							int tt = t;
							
							
							int minLeft=tec1.getLeft() ;
							Text tec2 = null;
							int charcount=0 ; 
							for (tt = t + 1; tt < page.getTexts().size(); tt++) {
								tec2 = page.getTexts().get(tt);
								FontSpec fSpectec2=xmlDoc.getFontSpecification(tec2.getFontId()) ;
								if (!HeadingsFonts.contains(fSpectec2)
					&& TitleFont.getId() != fSpectec2.getId()
					&& (tec2.getTop()<(tec1.getTop()+standardHeight+6))					
					&&((checkTheSameLine(tec1, tec2)) ||(!(tec1.getText().trim().endsWith(".") && tec2.getLeft()>(minLeft+3) )))) 
								{
									if (tex.length()!=0 && (charcount + page.getTexts().get(tt).getText().length() ) > 50)
									{
										tex +="\n" +  page.getTexts().get(tt).getText();
										charcount=page.getTexts().get(tt).getText().length() ;
									}
									else
									{
										tex +=" " +  page.getTexts().get(tt).getText();
										charcount+=page.getTexts().get(tt).getText().length() ;
									}
									
									minLeft=Math.min(minLeft,tec2.getLeft()) ;
									tec1 = tec2;						
								} else {
									break;
								}
							}
							t = tt - 1;
							if (tex.trim().length()!=0)
							{
								output +="<undefined>" + tex + "</undefined>" + "\n";							
							}
						} else {
							//the title is found and we have a heading
							//whose font type is both a heading and a standard text
							if (HeadingsFonts.contains(fSpec)
									&& StandardTextFonts.contains(fSpec)) {
								String tex = "";
								output += "<Paragraph name=\"" + tec1.getText().replace("\"", "&quot;")
										+ "\">" + "\n";
								int tt = t;
								if(t + 1 < page.getTexts().size() ){
									tec1 = page.getTexts().get(t + 1);
								}
								else{
									tec1 = page.getTexts().get(t);
								}
								Text tec2 = null;
								
								int minLeft=tec1.getLeft() ;
								
								int charcount=0 ; 
								for (tt = t + 1; tt < page.getTexts().size(); tt++) {
									tec2 = page.getTexts().get(tt);
								
									FontSpec fSpectec2=xmlDoc.getFontSpecification(tec2.getFontId()) ;
									
									if (!HeadingsFonts.contains(fSpectec2)
											&& (tec2.getTop()<(tec1.getTop()+standardHeight+6))					
											&&((checkTheSameLine(tec1, tec2)) ||(!(tec1.getText().trim().endsWith(".") && tec2.getLeft()>(minLeft+3) )))) 
									{
										if (tex.length()!=0 && (charcount + page.getTexts().get(tt).getText().length() ) > 50)
										{
											tex +="\n" +  page.getTexts().get(tt).getText();
											charcount=page.getTexts().get(tt).getText().length() ;
										}
										else
										{
											tex +=" "+ page.getTexts().get(tt).getText();
											charcount+=page.getTexts().get(tt).getText().length() ;
										}
											
										minLeft=Math.min(minLeft,tec2.getLeft()) ;
									} else {
										break;
									}
								}
								t = tt - 1;
								output += tex + "</Paragraph>" + "\n";
							//the title is found and we have a heading
							} else if (HeadingsFonts.contains(fSpec)) {
								String tex = tec1.getText();
								int tt = t;
								for (tt = t + 1; tt < page.getTexts().size(); tt++) {
									if (page.getTexts().get(tt).getFontId() == fSpec
											.getId()) {								
										if (tex.length()!=0)
											tex +=" " +  page.getTexts().get(tt).getText();
										else
											tex += page.getTexts().get(tt).getText();										
									} else {
										break;
									}
								}
								t = tt - 1;
								output += "<Paragraph name=\"" + tex.replace("\"", "&quot;") + "\">" + "\n";
								tex = "";
								
								if (t + 1 < page.getTexts().size()) {
									tt = t + 1;
									tec1 = page.getTexts().get(tt);
									tex = tec1.getText();
									fSpec = xmlDoc.getFontSpecification(tec1
											.getFontId());
									
									int minLeft=tec1.getLeft() ;
									Text tec2 = null;				
									int charcount=0 ; 
									for (tt = t + 2; tt < page.getTexts().size(); tt++) {
										tec2 = page.getTexts().get(tt);
										FontSpec fSpectec2=xmlDoc.getFontSpecification(tec2.getFontId()) ;
										
										if (!HeadingsFonts.contains(fSpectec2)
							&& (tec2.getTop()<(tec1.getTop()+standardHeight+6))					
							&&((checkTheSameLine(tec1, tec2)) ||(!(tec1.getText().trim().endsWith(".") && tec2.getLeft()>(minLeft+3) )))) 
										{
											if (tex.length()!=0 && (charcount + page.getTexts().get(tt).getText().length() ) > 50)
											{
												tex +="\n" + page.getTexts().get(tt).getText();
												charcount=page.getTexts().get(tt).getText().length() ;
											}
											else
											{
												tex +=" "+ page.getTexts().get(tt).getText();	
												charcount+=page.getTexts().get(tt).getText().length() ;
											}
											
											minLeft=Math.min(minLeft,tec2.getLeft()) ;
											tec1 = tec2;
										} else {
											break;
										}
									}
									t = tt - 1;
								}
	
								output += tex + "</Paragraph>" + "\n";
							// for standard font only
							} else if (StandardTextFonts.contains(fSpec)) {
								String tex = tec1.getText();
								output += "<Paragraph >" ;
								int tt = t;
								int minLeft=tec1.getLeft() ;
								Text tec2 = null;
								int charcount=0 ; 
								for (tt = t + 1; tt < page.getTexts().size(); tt++) {
									tec2 = page.getTexts().get(tt);
									FontSpec fSpectec2=xmlDoc.getFontSpecification(tec2.getFontId()) ;
									if (!HeadingsFonts.contains(fSpectec2)
						&& (tec2.getTop()<(tec1.getTop()+standardHeight+6))					
						&&((checkTheSameLine(tec1, tec2)) ||(!(tec1.getText().trim().endsWith(".") && tec2.getLeft()>(minLeft+3) )))) 
									{
										if (tex.length()!=0 && (charcount + page.getTexts().get(tt).getText().length() ) > 50)
										{
											tex +="\n" + page.getTexts().get(tt).getText();
											charcount=page.getTexts().get(tt).getText().length() ;
										}
										else
										{
											tex +=" " + page.getTexts().get(tt).getText();
											charcount+=page.getTexts().get(tt).getText().length() ;
										}
										minLeft=Math.min(minLeft,tec2.getLeft()) ;
										tec1 = tec2;
									} else {
										break;
									}
								}
								t = tt - 1;
								output += tex + "</Paragraph>" + "\n";
							//for undefined text but after the title and the head
							} else {
								//output += "<undefined>" + "\n";
								String tex = tec1.getText();
								int tt = t;
								int minLeft=tec1.getLeft() ;
								Text tec2 = null;
								int charcount=0 ; 
								for (tt = t + 1; tt < page.getTexts().size(); tt++) {
									tec2 = page.getTexts().get(tt);
									FontSpec fSpectec2=xmlDoc.getFontSpecification(tec2.getFontId()) ;
									if (!HeadingsFonts.contains(fSpectec2)
											&& (tec2.getTop()<(tec1.getTop()+standardHeight+6))					
											&&((checkTheSameLine(tec1, tec2)) ||(!(tec1.getText().trim().endsWith(".") && tec2.getLeft()>(minLeft+3) )))) 
									{
										if (tex.length()!=0 && (charcount + page.getTexts().get(tt).getText().length() ) > 50)
										{
											tex +="\n" +  page.getTexts().get(tt).getText();
											charcount=page.getTexts().get(tt).getText().length() ;
										}
										else
										{
											tex +=" " + page.getTexts().get(tt).getText();
											charcount+=page.getTexts().get(tt).getText().length() ;
										}
										minLeft=Math.min(minLeft,tec2.getLeft()) ;
										tec1 = tec2;
									} else {
										break;
									}
								}
								t = tt - 1;
								if (tex.trim().length()!=0)
								{
									output +="<undefined>" + tex + "</undefined>" + "\n";							
								}
								//output += tex + "</undefined>" + "\n";
							}
						}
					}
				}

				if (p==0 && !titleFound)
				{
					output += "</head>" + "\n";
					titleFound=true ;
				}
				output += "</page>" + "\n";
		}
			
		output += "</document>" + "\n";
		output = removeLigature(output);
		out.write(output + "\n");
			
		out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return title;
	}

	private String removeLigature(String value) {
		int index = value.indexOf(64257, 0);
			while ( index > -1) {
				value = value.substring(0,index)+"fi"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
				index = value.indexOf(64257, index+1);
			}
			index = value.indexOf(64256, 0);
			while ( index > -1) {
				value = value.substring(0,index)+"ff"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
				index = value.indexOf(64256, index+1);
			}
			
			index = value.indexOf(64258, 0);
			while ( index > -1) {
				value = value.substring(0,index)+"fl"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
				index = value.indexOf(64258, index+1);
			}
			
			index = value.indexOf(64259, 0);
			while ( index > -1) {
				value = value.substring(0,index)+"ffi"+value.substring(index+1); //substring(a, b) liefert String von Position a bis b-1
				index = value.indexOf(64259, index+1);
			}
		return value;
	}
	
	private boolean checkTheSameLine(Text tex1, Text tex2) {
		int topDifference = Math.abs(tex2.getTop() - tex1.getTop());
		int leftDifference = Math.abs(tex1.getLeft() + tex1.getWidth()
				- tex2.getLeft());
		if ((topDifference < (standardHeight - 2)) && (leftDifference <= 3))
			return true;
		else
			return false;
	}

	public Vector<FontSpec> getStandardTextFonts() {
		return StandardTextFonts;
	}

	private void CorrectText() {

		for (Page pag : xmlDoc.getPages())
			for (Text tex : pag.getTexts()) {
				for (TextSegment texseg : tex.getTextSegments()) {
					String temp=texseg.getText() ;
					temp=temp.replace("&", "&amp;") ;
					temp=temp.replace("<", "&lt;") ;
					temp=temp.replace(">", "&gt;") ;
					texseg.setText(temp) ;
				}
			}
	}
}