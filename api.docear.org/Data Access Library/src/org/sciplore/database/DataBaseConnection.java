package org.sciplore.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataBaseConnection {
	private static Connection db;
	
	public static Connection getConnection() throws SQLException {
		if(db == null || db.isClosed()) {
			db = DriverManager.getConnection("jdbc:mysql://db.sciplore.org/sciplore?user=sciplore&password=MMnchuCsGz5ey8bK");
		}
		return db;
	}
	
	public static Connection getConnection(Properties props) throws SQLException {
		if(db != null && !db.isClosed()) {
			db.close();
		}
		String jdbc_host = props.getProperty("jdbc.host", "db.sciplore.org");
		String jdbc_database = props.getProperty("jdbc.database", "sciplore");
		String jdbc_user = props.getProperty("jdbc.user", "sciplore");
		String jdbc_password = props.getProperty("jdbc.password", "MMnchuCsGz5ey8bK");
		
		db = DriverManager.getConnection("jdbc:mysql://"+jdbc_host+"/"+jdbc_database+"?user="+jdbc_user+"&password="+jdbc_password);
		return db;
	}
	
	public static Connection getConnection(String host, String database, String user, String password) throws SQLException {
		if(db != null && !db.isClosed()) {
			db.close();
		}		
		db = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database+"?user="+user+"&password="+password);
		return db;
	}

	public static void closeConnection() throws SQLException {
		if(db != null && !db.isClosed()) {
			db.close();
		}
	}
}
