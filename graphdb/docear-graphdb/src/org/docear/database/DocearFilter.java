package org.docear.database;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class DocearFilter extends FilteringTokenFilter {

	private final boolean ignoreCase;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final Pattern numberOnlyPattern = Pattern.compile("^[\\d\\.,]+$"); 
	
	public DocearFilter(Version matchVersion, TokenStream stream, boolean ignoreCase) {
		super(true, stream);
		this.ignoreCase = ignoreCase;
	}

	protected boolean accept() throws IOException {
		String term = termAtt.toString().trim();
		if(term != null && term.trim().length() > 0) {
			if(ignoreCase) {
				term = term.toLowerCase();
			}
			
			// number only filter
			if(numberOnlyPattern.matcher(term).find()) {
				return false;
			}
			
			// filter specific words (should most likely be a file extension)
//			if(term.equals("pdf") 
//				|| term.equals("docx") 
//				|| term.equals("doc") 
//				|| term.equals("mm") 
//				|| term.equals("xls") 
//				|| term.equals("xlsx")
//				|| term.equals("dcr")
//				|| term.equals("zip")
//				|| term.equals("png")
//				|| term.equals("gif")
//				|| term.equals("bmp")
//				|| term.equals("jpg")
//				|| term.equals("jpeg")
//				|| term.equals("ps")
//				|| term.equals("ppt")
//				
//			) {			
//				return false;
//			}
		}
		
		// accept if no filter return earlier
		return true;
	}

}
