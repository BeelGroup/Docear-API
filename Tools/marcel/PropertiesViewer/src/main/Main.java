package main;

import gui.MainWindow;

import java.io.File;

public class Main {
	public static void main(String... args) {
		MainWindow mainWnd = new MainWindow();
		if(args.length > 0) {
			mainWnd.open(new File(args[0]));
		}
	}
}
