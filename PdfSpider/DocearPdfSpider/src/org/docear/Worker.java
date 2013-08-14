package org.docear;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

public interface Worker extends Runnable {
	public void exec() throws IOException, RejectedExecutionException;
}
