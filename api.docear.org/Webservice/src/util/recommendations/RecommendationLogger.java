package util.recommendations;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecommendationLogger extends FileWriter {
	final static String n = System.getProperty("line.separator");
	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public RecommendationLogger(String fileName) throws IOException {
		super(fileName, true);
	}

	public void log(String entry) {
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(sdf.format(new Date()));
			sb.append(": ");
			sb.append(entry);
			sb.append(n);
			this.write(sb.toString());
			this.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
