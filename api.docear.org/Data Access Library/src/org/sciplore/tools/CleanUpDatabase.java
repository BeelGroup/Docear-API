package org.sciplore.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.sciplore.database.DataBaseConnection;

public class CleanUpDatabase {
	private static Connection db;
	
	public static void main(String args[]) throws Exception {
		db = DataBaseConnection.getConnection();
		try {
			cleanCitations();
			cleanContacts();
			cleanDocumentsPersons();
			cleanDocumentsFulltext();
			cleanKeywords();
		} catch(Exception e) {
			db.close();
			throw e;
		}
		db.close();
	}
	
	private static void cleanCitations() throws Exception {
		Statement stmt = db.createStatement();
		PreparedStatement stmtDelete = db.prepareStatement("DELETE FROM citations WHERE citing_document_id=? AND cited_document_id=? AND count_character=? AND count_word=? AND count_sentence=? AND count_paragraph=? AND id!=?");
		ResultSet r = stmt.executeQuery("SELECT * FROM citations");
		while(r.next()) {
			stmtDelete.setInt(1, r.getInt("citing_document_id"));
			stmtDelete.setInt(2, r.getInt("cited_document_id"));
			stmtDelete.setInt(3, r.getInt("count_character"));
			stmtDelete.setInt(4, r.getInt("count_word"));
			stmtDelete.setInt(5, r.getInt("count_sentence"));
			stmtDelete.setInt(6, r.getInt("count_paragraph"));
			stmtDelete.setInt(7, r.getInt("id"));
			stmtDelete.executeUpdate();
		}
		stmt.close();
		stmtDelete.close();
	}

	private static void cleanContacts() throws Exception {
		Statement stmt = db.createStatement();
		PreparedStatement stmtDelete = db.prepareStatement("DELETE FROM contacts WHERE person_id=? AND uri=? AND type=? AND id!=?");
		ResultSet r = stmt.executeQuery("SELECT * FROM contacts");
		while(r.next()) {
			stmtDelete.setInt(1, r.getInt("person_id"));
			stmtDelete.setString(2, r.getString("uri"));
			stmtDelete.setShort(3, r.getShort("type"));
			stmtDelete.setInt(4, r.getInt("id"));
			stmtDelete.executeUpdate();
		}
		stmt.close();
		stmtDelete.close();
	}
	
	private static void cleanDocumentsPersons() throws Exception {
		Statement stmt = db.createStatement();
		PreparedStatement stmtDelete = db.prepareStatement("DELETE FROM documents_persons WHERE document_id=? AND person_id=? AND type=? AND id!=?");
		ResultSet r = stmt.executeQuery("SELECT * FROM documents_persons");
		while(r.next()) {
			stmtDelete.setInt(1, r.getInt("document_id"));
			stmtDelete.setInt(2, r.getInt("person_id"));
			stmtDelete.setShort(3, r.getShort("type"));
			stmtDelete.setInt(4, r.getInt("id"));
			stmtDelete.executeUpdate();
		}
		stmt.close();
		stmtDelete.close();
	}
	
	private static void cleanDocumentsFulltext() throws Exception {
		Statement stmt = db.createStatement();
		PreparedStatement stmtDelete = db.prepareStatement("DELETE FROM document_fulltext WHERE document_id=? AND field=? AND text=? AND id!=?");
		ResultSet r = stmt.executeQuery("SELECT * FROM document_fulltext");
		while(r.next()) {
			stmtDelete.setInt(1, r.getInt("document_id"));
			stmtDelete.setShort(2, r.getShort("field"));
			stmtDelete.setString(3, r.getString("text"));
			stmtDelete.setInt(4, r.getInt("id"));
			stmtDelete.executeUpdate();
		}
		stmt.close();
		stmtDelete.close();
	}
	
	private static void cleanKeywords() throws Exception {
		Statement stmt = db.createStatement();
		PreparedStatement stmtDelete = db.prepareStatement("DELETE FROM keywords WHERE document_id=? AND keyword=? AND id!=?");
		ResultSet r = stmt.executeQuery("SELECT * FROM keywords");
		while(r.next()) {
			stmtDelete.setInt(1, r.getInt("document_id"));
			stmtDelete.setString(2, r.getString("keyword"));
			stmtDelete.setInt(3, r.getInt("id"));
			stmtDelete.executeUpdate();
		}
		stmt.close();
		stmtDelete.close();
	}
	
	

}
