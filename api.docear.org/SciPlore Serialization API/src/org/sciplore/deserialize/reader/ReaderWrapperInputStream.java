package org.sciplore.deserialize.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ReaderWrapperInputStream extends InputStream {
	private Reader reader;

	public ReaderWrapperInputStream(Reader reader) {
		this.reader = reader;
	}

	public int read() throws IOException {
		return reader.read();
	}
}
