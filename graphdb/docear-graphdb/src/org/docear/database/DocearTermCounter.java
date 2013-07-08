package org.docear.database;

import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class DocearTermCounter extends FilteringTokenFilter {

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private int counter = 0; 
	private HashSet<String> terms = new HashSet<String>();
	
	public DocearTermCounter(Version matchVersion, TokenStream stream) {
		super(true, stream);
	}

	protected boolean accept() throws IOException {
		String term = termAtt.toString().trim();
		if(term != null && term.trim().length() > 0) {
			counter ++;
			terms.add(term);
		}		
		
		return true;
	}
	
	public int getCount() {
		return counter;
	}
	
	public int getUniqueCount() {
		return terms.size();
	}
}
