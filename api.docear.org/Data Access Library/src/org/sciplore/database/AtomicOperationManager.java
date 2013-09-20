package org.sciplore.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AtomicOperationManager {
	private final ExecutorService queue;
		
	private AtomicOperationManager() {
		queue = Executors.newSingleThreadExecutor();
	}
	
	public static AtomicOperationManager newInstance() {
		return new AtomicOperationManager() {
		};
	}
	
	public <T> AtomicOperationHandle<T> addOperation(AtomicOperation<T> op) {
		return new AtomicOperationHandle<T>(queue.submit(op.getCallable()));
	}

}
