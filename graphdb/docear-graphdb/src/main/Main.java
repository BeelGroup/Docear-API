package main;

import java.io.File;

import org.docear.graphdb.GraphDbController;

public class Main {
	private static final String DB_PATH = "/home/stefan/work/docear-graph.db";
	private static final String MAPS_PATH = "/home/stefan/work/mindmap-parser";


	public static void main(String[] args) {
		File f = new File(MAPS_PATH);
		if (!f.exists()) {
			f.mkdirs();
		}
		
		GraphDbController worker = new GraphDbController(DB_PATH, MAPS_PATH);
//		worker.clearDb();
		worker.init();
//		worker.initControlFrame();
		worker.start();
	}
}

