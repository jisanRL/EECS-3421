package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

//
public class connectDB {
	private static Connection connection;
	private static Statement statement;
	private static ResultSet resultset;
	
	// ctr connects to the DB
	public connectDB() {
		
	}
	
	public static void main(String[] args) {
		
		try {
			Class.forName("org.postgresql.Driver");
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rrdb", "jisanreza", "root");   // user and password change this and check for this  
			
			if (connection != null) {
				System.out.println("Connection ok");
			} else {
				System.out.println("Connection not ok");
			}
			
			
			String sql = "select *  from quest;";
			try {
				statement = connection.createStatement();
				resultset = statement.executeQuery(sql);
				while (resultset.next()) {
					System.out.println(resultset.getString(1));				
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
//		System.out.println("hello");
	}
}
