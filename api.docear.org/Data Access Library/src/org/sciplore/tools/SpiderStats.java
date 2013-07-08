package org.sciplore.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sciplore.database.DataBaseConnection;

public class SpiderStats {
	public static void main(String args[]) throws SQLException, InterruptedException {
		Connection db = DataBaseConnection.getConnection();
		PreparedStatement stmt = db.prepareStatement("SELECT COUNT(1) FROM spider WHERE status=1");
		ResultSet r = null;
		int cnt = 0;
		for(int i=0; i<10; i++) {
			r = stmt.executeQuery();
			if(r.next()) {
				System.out.println(r.getInt(1) - cnt);
				cnt = r.getInt(1);
			}
			r.close();
			Thread.sleep(60*1000);
		}
		stmt.close();
		DataBaseConnection.closeConnection();
	}
}
