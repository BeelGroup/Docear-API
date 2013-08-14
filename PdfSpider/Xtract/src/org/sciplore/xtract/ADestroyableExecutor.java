package org.sciplore.xtract;

public abstract class ADestroyableExecutor implements Runnable {
	protected Process process;
	
	public void destroy() {
		if (process != null) {
			process.destroy();
		}
	}
	
	
}
