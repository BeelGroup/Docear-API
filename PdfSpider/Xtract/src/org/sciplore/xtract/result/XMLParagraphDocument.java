package org.sciplore.xtract.result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import java.util.Vector;

import org.sciplore.xtract.resultstructure.ResultXMLDocument;
import org.sciplore.xtract.resultstructure.TextContainer;

public class XMLParagraphDocument {
	private final String KEYWORDS = "keywords";
	private final String ABSTRACT = "abstract";
	private final String INTRODUCTION = "introduction";
	private final String RELATEDWORK = "related_work";
	private final String BASICCONCEPTS = "basic_concepts";
	private final String EXPERIMENTS = "experiments";
	private final String RESULTS = "results";
	private final String DISCUSSION = "discussion";
	private final String CONCLUSION = "conclusion";
	private final String ACKNOWLEDGEMENT = "acknowledgement";
	private final String REFERENCES = "references";
	private final String OTHERS = "others";
	private final String[] paragraphNames = {KEYWORDS, ABSTRACT, INTRODUCTION, RELATEDWORK, BASICCONCEPTS, EXPERIMENTS, RESULTS, DISCUSSION, CONCLUSION, ACKNOWLEDGEMENT, REFERENCES, OTHERS};
	
	private final String[] keywordsNames = {"keywords"};
	private final String[] abstractNames = {"abstract", "kurzfassung", "overview", "outline", };
	private final String[] introductionNames = {"introduction", "einleitung"};
	private final String[] relatedWorkNames = {"related work", "previous work"};
	private final String[] basicConceptsNames = {"basic concepts", "concepts and terms", "definitions", "grundbegriffe", "definitionen"};
	private final String[] experimentsNames = {"experimental setup", "experimental evaluation", "experiments", "applied method", "versuchsaufbau", "vorgehensweise", "vorgangsweise"};
	private final String[] resultsNames = {"result", "results", "ergebnis", "ergebnisse"};
	private final String[] discussionNames = {"discussion", "proof", "diskussion", "beweis"};
	private final String[] conclusionNames = {"conclusion", "and outlook", "summary", "schlussfolgerung", "fazit", "zusammenfassung", "und ausblick"};
	private final String[] acknowledgementNames = {"acknowledgement", "danksagung"};
	private final String[] referencesNames = {"reference", "literaturverzeichnis", "referenzen", "bibliography", "reference list", "literature", "literatur"};
	private final String[] othersNames = {"appendix", "schlussbemerkung"};
	private final String[][] allNames = {keywordsNames, abstractNames, introductionNames, relatedWorkNames, basicConceptsNames, experimentsNames, resultsNames, discussionNames, conclusionNames, acknowledgementNames, referencesNames, othersNames};
	
	//private StringBuffer[] paragraphTextContents;
	
	private ResultXMLDocument xmlDoc;
	private Vector<TextContainer> textContents;
	public XMLParagraphDocument(ResultXMLDocument resXmlDoc) {
		xmlDoc = resXmlDoc;
		textContents = xmlDoc.getTextContainers();
	}

	public void generateFile(File outputFile) {
		BufferedWriter out = null;
		boolean titleFound = false;
		boolean abstractFound = false;
		boolean keywordsFound = false;
		boolean anyOtherParagraphFound = false;
		boolean anElementOpen = false;
		int currParagraphName = 11;
		try {
			out = new BufferedWriter(new FileWriter(outputFile));
			StringBuffer output = new StringBuffer("<?xml version=\"1.0\" encoding=\"" + Charset.defaultCharset().toString() + "\"?>\n" + "<document id=\"" + xmlDoc.getID() + "\">\n");
			for(int i=0; i<textContents.size(); i++) {
				TextContainer currTextContainer = textContents.get(i);
				if(currTextContainer.isTitle()) {
					if(anElementOpen) {
						output.append("</" + paragraphNames[currParagraphName] + ">\n");
					}
					titleFound = true;
					output.append("<title>" + currTextContainer.getTextContent() + "\n</title>\n");
					anElementOpen = false;
				}
				else {
					if(currTextContainer.hasName()) {
						if(anElementOpen) {
							output.append("\n</" + paragraphNames[currParagraphName] + ">\n");
						}
						anyOtherParagraphFound = true;
						currParagraphName = 11;
						label:
						for(int j=0; j<4; j++) {
							for(int k=0; k<allNames[j].length; k++) {
								if(currTextContainer.getName().toLowerCase().contains(allNames[j][k])) {
									currParagraphName = j;
									break label;
								}
							}
						}
						output.append("<" + paragraphNames[currParagraphName] + ">" + currTextContainer.getTextContent());
						anElementOpen = true;
					}
					else {
						if(titleFound && (!abstractFound) && (!anyOtherParagraphFound) && (currTextContainer.getTextContent().toLowerCase().indexOf(ABSTRACT) < 5) && (currTextContainer.getTextContent().toLowerCase().indexOf(ABSTRACT) > -1)) {
							if(anElementOpen) {
								output.append("\n</" + paragraphNames[currParagraphName] + ">\n");
							}
							abstractFound = true;
							currParagraphName = 1;
							int indexOfAbstract = currTextContainer.getTextContent().toLowerCase().indexOf(ABSTRACT);
							output.append("<" + paragraphNames[currParagraphName] + ">" + currTextContainer.getTextContent().substring(0, indexOfAbstract) + currTextContainer.getTextContent().substring(indexOfAbstract+8, currTextContainer.getTextContent().length()));
							anElementOpen = true;
						}
						else if(titleFound && (!keywordsFound) && (!anyOtherParagraphFound) && (currTextContainer.getTextContent().toLowerCase().indexOf(KEYWORDS) < 5) && (currTextContainer.getTextContent().toLowerCase().indexOf(KEYWORDS) > -1)) {
							if(anElementOpen) {
								output.append("\n</" + paragraphNames[currParagraphName] + ">\n");
							}
							keywordsFound = true;
							currParagraphName = 0;
							int indexOfKeywords = currTextContainer.getTextContent().toLowerCase().indexOf(KEYWORDS);
							output.append("<" + paragraphNames[currParagraphName] + ">" + currTextContainer.getTextContent().substring(0, indexOfKeywords) + currTextContainer.getTextContent().substring(indexOfKeywords+8, currTextContainer.getTextContent().length()));
							anElementOpen = true;
						}
						else {
							if(anElementOpen) {
								output.append("\n\n" + currTextContainer.getTextContent());
							}
							else {
								output.append("<" + paragraphNames[currParagraphName] + ">" + currTextContainer.getTextContent());
								anElementOpen = true;
							}
						}
					}
				}
			}
			if(anElementOpen) {
				output.append("\n</" + paragraphNames[currParagraphName] + ">\n");
			}
			output.append("</document>");
			out.write(output.toString());
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}
