package org.sciplore.utilities.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsynchUtilities {
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	private static Integer singleExecCount = 0;	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					int size = executor.shutdownNow().size();
					System.out.println("Attention: " + size + " tasks were lost.");
				}
				catch (Throwable e) {
				}
			}
		});
	}
	
	public static void executeAsynch(final Runnable task) {
		try {
			if(task == null) {
				throw new NullPointerException();
			}
			executor.execute(new Runnable() {
				public void run() {
					try {
						task.run();
					} 
					catch (Throwable e) {
					}
					decSingleExecTaskCount();					
				}
			});
			incSingleExecTaskCount();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void incSingleExecTaskCount() {
		synchronized (singleExecCount) {
			singleExecCount++;
		}
		
	}
	
	private static void decSingleExecTaskCount() {
		synchronized (singleExecCount) {
			singleExecCount--;
		}
		
	}
	
	public static int getSingleExecTaskCount() {
		return singleExecCount;
	}
}
