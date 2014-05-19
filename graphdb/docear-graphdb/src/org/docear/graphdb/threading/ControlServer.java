package org.docear.graphdb.threading;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.docear.Logging.DocearLogger;
import org.docear.graphdb.GraphDbController;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;

import scala.collection.immutable.Map;

public class ControlServer extends Thread {
	private final ServerSocket server;
	private boolean started = false;
	private final GraphDbController graphController;	
	
	public ControlServer(GraphDbController graphDBController) throws IOException {
		server = new ServerSocket(7575, 0, InetAddress.getByAddress(new byte[]{127,0,0,1}));
		this.graphController = graphDBController;
		this.setName("GraphDB CtrlServer");
	}
	
	public void run() {
		started = true;
		DocearLogger.info("ControlServer started on localhost:7575");
		while(started ) {
			try {
				Socket clientSocket = server.accept();
				try {
					Scanner reader = new Scanner(clientSocket.getInputStream());
					PrintStream writer = new PrintStream(clientSocket.getOutputStream());
					printHelp(writer);
					String commandLine = null;
					while((commandLine = reader.nextLine()) != null) {
						if("shutdown".equals(commandLine)) {
							try {
								if(graphController != null) {
									server.close();
									graphController.shutdown();									
								}								
							}
							catch (Exception e) {
								
							}
						}
						else if("freeze".equals(commandLine)) {
							try {
								if(graphController != null) {
									graphController.freeze();
									writer.println("graphDbWorker stopped.");
								}								
							}
							catch (Exception e) {
								
							}
						}
						else {
							executeQuery(commandLine, writer);
						}						
					}
				}
				finally {
					clientSocket.getInputStream().close();
					clientSocket.getOutputStream().close();
					clientSocket.close();
				}
			} catch (IOException e) {
				DocearLogger.error(e);
			}
		}
		DocearLogger.info("ControlServer shutdown");
	}

	private void executeQuery(String query, PrintStream writer) {
		ExecutionEngine engine = new ExecutionEngine(this.graphController.getGraphDatabaseService());
				
		int c = 0;
		try {
    		ExecutionResult result = engine.execute(query);
    		
    		while (result.hasNext()) {
    			c++;
    			Map<String, Object> map = result.next();
    		}
		}
		catch(Exception e) {
			e.printStackTrace(writer);
		}
		
		writer.println("results found: "+c);
		writer.flush();
	}

	private void printHelp(PrintStream writer) {
		writer.println("available commands:");
		writer.println("\tfreeze\t\t - stops the graphDbWorker-Thread so that no new data will be added to the graph.");
		writer.println("\tshutdown\t\t - stops the database.");
		writer.flush();
	}
}
