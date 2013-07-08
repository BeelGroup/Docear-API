package org.docear.graphdb.threading;

import java.util.ArrayList;
import java.util.List;

public class GraphJobManager {
	private int LIMIT = 100;
	
	List<GraphCreatorJob> jobList = new ArrayList<GraphCreatorJob>();

	public GraphJobManager() {
		
	}
	
	public GraphJobManager(int limit) {
		this.LIMIT = limit;
	}

	public void addJob(GraphCreatorJob job) {
		
		if (job == null) {
			return;
		}
		synchronized (jobList) {
			jobList.add(job);
		}
	}

	public synchronized GraphCreatorJob next() {
		synchronized (jobList) {
			if (jobList.size() <= 0) {
				throw new IndexOutOfBoundsException();
			}
	
			return jobList.remove(0);
		}
	}

	public synchronized boolean hasJobs() {
		synchronized (jobList) {
			return jobList.size() > 0;
		}
	}

	public int remaining() {
		synchronized (jobList) {
			return jobList.size();
		}
	}
	
	public boolean acceptJobs() {
		synchronized (jobList) {
			return jobList.size() < LIMIT;
		}
	}
}
