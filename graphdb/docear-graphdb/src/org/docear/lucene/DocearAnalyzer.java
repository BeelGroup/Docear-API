package org.docear.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.docear.Logging.DocearLogger;
import org.docear.database.DocearFilter;
import org.docear.database.DocearTermCounter;

public class DocearAnalyzer extends StopwordAnalyzerBase {
	private final boolean stopwordRemoval;
	private final boolean stemming;
	private DocearTermCounter reducedCounter;
	private DocearTermCounter origCounter;
	
	protected DocearAnalyzer(Version version, boolean removeStopWords, boolean useStemming) {
		this(version, StandardAnalyzer.STOP_WORDS_SET, removeStopWords, useStemming);
	}

	protected DocearAnalyzer(Version version, Set<?> stopwords, boolean removeStopWords, boolean useStemming) {
		super(version, stopwords);
		this.stemming = useStemming;
		this.stopwordRemoval = removeStopWords;		
	}

	public DocearAnalyzer(Version version) {
		this(version, StandardAnalyzer.STOP_WORDS_SET, true, true);
	}

	protected TokenStreamComponents createComponents(String fieldName, Reader input) {
		Tokenizer tokenizer = new StandardTokenizer(matchVersion, input);
		try {
			return new ReusableAnalyzerBase.TokenStreamComponents(tokenizer, getTokenStream(tokenizer));
		}
		catch (IOException e) {
			DocearLogger.error(e);
			return null;
		}		
	}
	
	public TokenStream getTokenStream(Tokenizer tokenizer) throws IOException {
		TokenStream stream = new StandardFilter(matchVersion, tokenizer);
		
		stream = new LengthFilter(true, stream, 3, 256);
		stream = new LowerCaseFilter(matchVersion, stream);

		origCounter = new DocearTermCounter(matchVersion, stream);
		stream = origCounter;
		
		if (stopwordRemoval) {
			stream = new StopFilter(matchVersion, stream, stopwords, true);
			stream = new StopFilter(matchVersion, stream, loadStopwords(getClass().getResourceAsStream("/germanStopwords.txt")), true);
			stream = new StopFilter(matchVersion, stream, loadStopwords(getClass().getResourceAsStream("/stopwords.txt")), true);
		}

		if (stemming) {
			// stream = new StemmingFilter(matchVersion, stream, true);
		}

		stream = new DocearFilter(matchVersion, stream, true);
		
		reducedCounter = new DocearTermCounter(matchVersion, stream); 
		stream = reducedCounter;
		
		return stream;
	}

	private Set<String> loadStopwords(InputStream inStream) {
		Set<String> stopwords = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (!line.trim().startsWith(";")) {
					stopwords.add(line.trim().toLowerCase());
				}
			}
		} catch (Exception e) {
			DocearLogger.error(e);
		}
		return stopwords;
	}
	
	public int getOriginalTermsCount() {
		return this.origCounter.getCount();
	}
	
	public int getOriginalUniqueTermsCount() {
		return this.origCounter.getUniqueCount();
	}
	
	public int getReducedTermsCount() {
		return this.reducedCounter.getCount();
	}
	
	public int getReducedUniqueTermsCount() {
		return this.reducedCounter.getUniqueCount();
	}

}