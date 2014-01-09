package org.sciplore.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AtomicOperationManager {
	private final ExecutorService queue;
		
	private AtomicOperationManager() {
		queue =	new ThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());
	}
	
	public int size() {
		return ((ThreadPoolExecutor)queue).getQueue().size();
	}
	
	public static AtomicOperationManager newInstance() {
		return new AtomicOperationManager() {
		};
	}
	
	public <T> AtomicOperationHandle<T> addOperation(AtomicOperation<T> op) {
		return new AtomicOperationHandle<T>(queue.submit(op.getCallable()));
	}

}
