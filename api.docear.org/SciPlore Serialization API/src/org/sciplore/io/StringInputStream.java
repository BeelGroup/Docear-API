package org.sciplore.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class StringInputStream extends InputStream {

	private final byte[] charArray;
	private int streamPointer = 0;
	private int streamMarker = -1;
	private int markTTL = 2048;
	
	public StringInputStream(String string) {
		this.charArray = string.getBytes();
	}
	
	public StringInputStream(String string, Charset charset) {
		this.charArray = string.getBytes(charset);
	}
	
	public StringInputStream(String string, String charsetName) throws UnsupportedEncodingException {
		this.charArray = string.getBytes(charsetName);
	}
	
	public int read() throws IOException {
		if(streamPointer < this.charArray.length) {
			if(streamMarker > 0 && (streamPointer-streamMarker) > markTTL) {
				streamMarker=-1;
			}
			return charArray[streamPointer++];
		} 
		return -1;
	}
	
	public int available() {
		return this.charArray.length-streamPointer;
	}
	
	public boolean markSupported() {
		return true;
	}
	
	public synchronized void mark(int readLimit) {
		this.streamMarker = this.streamPointer;
		this.markTTL  = readLimit;
	}
	
	public synchronized void reset() throws IOException {
		if(this.streamMarker >= 0) {
			this.streamPointer = this.streamMarker;
		}
	}
	
	public void close() throws IOException {
	}
}
