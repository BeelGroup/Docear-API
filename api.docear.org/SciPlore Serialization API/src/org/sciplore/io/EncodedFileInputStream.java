package org.sciplore.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.LinkedList;

public class EncodedFileInputStream extends InputStream {

	private final ByteStreamBuffer buffer = new ByteStreamBuffer();
	private int streamPointer = 0;
	private int streamMarker = -1;
	private int markTTL = 2048;
	private final InputStream stream;
	private String charEncoding = null; 
	
	public EncodedFileInputStream(File file) throws FileNotFoundException {
		//this.charArray = string.getBytes();
		stream = new FileInputStream(file);
	}
	
	public EncodedFileInputStream(File file, Charset charset) throws FileNotFoundException {
		//this.charArray = string.getBytes(charset);
		stream = new FileInputStream(file);
	}
	
	public EncodedFileInputStream(File file, String charsetName) throws UnsupportedEncodingException, FileNotFoundException {
		//this.charArray = string.getBytes(charsetName);
		stream = new FileInputStream(file);
		"encodingTest".getBytes(charsetName);
		charEncoding = charsetName;
	}
	
	public int read() throws IOException {
		if(stream.available() != 0) {
			buffer.add(stream.read());
		}
		if(streamPointer < this.buffer.size()) {
			if(streamMarker > 0 && (streamPointer-streamMarker) > markTTL) {
				streamMarker=-1;
			}
			return buffer.get(streamPointer++);
		} 
		return -1;
	}
	
	public int available() throws IOException {
		if(stream.available() != 0) {
			return stream.available();
		}
		return buffer.size()-streamPointer;
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
	
	
	/***************************************************
	 * 
	 * @author mag
	 *
	 ***************************************************/
	private class ByteStreamBuffer {
		private LinkedList<Byte> buffer = new LinkedList<Byte>();  
		public void add(Integer read) {
			if(read != null) {
				byte[] b;
				if(charEncoding != null) {
					try {
						b = Character.valueOf((char)read.intValue()).toString().getBytes(charEncoding);
					} catch (UnsupportedEncodingException e) {
						return;
					}
				} else {
					b = Character.valueOf((char)read.intValue()).toString().getBytes();
				}
				addToBuffer(b);
			}
		}
		
		private void addToBuffer(byte[] bytes) {
			for(byte b : bytes) {
				buffer.add(b);
			}
			
		}

		public byte get(int index) {
			return buffer.get(index);
		}
		
		public int size() {
			return buffer.size();
		}		
	}

}
