package org.sciplore.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sciplore.database.DataBaseConnection;
import org.sciplore.queries.DocumentQueries;

public class CreateCleanTitle {
	public static void main(String args[]) throws SQLException {
		Connection db = DataBaseConnection.getConnection();
		Statement stmt = db.createStatement();
		PreparedStatement stmt_update = db.prepareStatement("UPDATE documents SET cleantitle=? WHERE id=?");
		ResultSet r = stmt.executeQuery("SELECT id, title FROM documents WHERE cleantitle LIKE ''");
		while(r.next()) {
			stmt_update.setString(1, DocumentQueries.generateCleanTitle(r.getString("title")));
			stmt_update.setInt(2, r.getInt("id"));
			stmt_update.executeUpdate();
		}
		stmt.close();
		stmt_update.close();
		db.close();
	}
}
