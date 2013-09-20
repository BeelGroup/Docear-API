package org.sciplore.database;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AtomicOperationHandle <T> {
	private final Object LOCK = new Object();
	private final Future<T> handle;
	private T result = null;
	private boolean executed = false;
	
	public AtomicOperationHandle(Future<T> handle) {
		this.handle = handle;
	}
	
	public void waitFor() throws IOException {
		synchronized (LOCK) {
			if(!executed) {
				executed = true;
				try {
					result = this.handle.get();
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}		
	}
	
	public void waitFor(long waitingTime) throws IOException, TimeoutException {
		synchronized (LOCK) {
			if(!executed) {
				executed = true;
				try {
					result = this.handle.get(waitingTime, TimeUnit.MILLISECONDS);
				}
				catch (TimeoutException e) {
					throw e;
				}
				catch (Exception e) {
					throw new IOException(e);
				}
			}
		}
	}
	
	public T getResult() throws IOException {
		synchronized (LOCK) {
			if(!executed) {
				executed = true;
				try {
					result = this.handle.get();
				} catch (Exception e) {
					throw new IOException(e);
				}	
			}
			return result;
		}
	}
	
	public T getResult(long waitingTime) throws IOException, TimeoutException {
		synchronized (LOCK) {
			if(!executed) {
				executed = true;
				try {
					result = this.handle.get(waitingTime, TimeUnit.MILLISECONDS);
				}
				catch (TimeoutException e) {
					throw e;
				}
				catch (Exception e) {
					throw new IOException(e);
				}	
			}
			return result;
		} 
	}

}
