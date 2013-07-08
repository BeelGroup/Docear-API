package org.docear.graphdb.threading;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.docear.graphdb.GraphDbController;

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
		System.out.println("ControlServer started on localhost:7575");
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
							printHelp(writer);
						}
					}
				}
				finally {
					clientSocket.getInputStream().close();
					clientSocket.getOutputStream().close();
					clientSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("ControlServer shutdown");
	}

	private void printHelp(PrintStream writer) {
		writer.println("available commands:");
		writer.println("\tfreeze\t\t - stops the graphDbWorker-Thread so that no new data will be added to the graph.");
		writer.println("\tshutdown\t\t - stops the database.");
		writer.flush();
	}
}
