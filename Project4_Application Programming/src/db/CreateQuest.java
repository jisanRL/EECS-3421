package db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class CreateQuest {
	private Connection connection;
	private Statement statement;
	private ResultSet resultset;
	private String day;
	private String realm; 
	private String theme; 
	private int amount; 
	private String user; 
	private String seed;
	private String loot;
	private List<String> treasure = new ArrayList<>();

	
	// ctr, connects to the database 
	public CreateQuest(String[] args) throws ParseException {
		// connection block
		try {
            Class.forName("org.postgresql.Driver");
//            connection = DriverManager.getConnection("jdbc:postgresql://db:5432/farhan95", "farhan95", "215145592");   // user and password change this and check for this  
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rrdb", "jisanreza", "root");   // user and password change this and check for this  

            if (connection != null) {
                System.out.println("Connection ok");
            } else {
                System.out.println("Connection not ok");
            } 
        } catch (Exception e) {
            // TODO: handle exception
        	e.printStackTrace();
        }
		
		// error check block 
		if (args.length != 0) {
			boolean dayCheck = dayChecker(args[0]);
			if (dayCheck) {
				day = args[0];
			} else {
				System.out.println("Date not in future, Quest Can't added\n");
				System.exit(0);
			}
			
			boolean realmCheck = realmChecker(args[1]);
			if (realmCheck) {
				realm = args[1];
			} else {
				System.out.println("Realm not found\n");
				System.exit(0);
			}
			
			treasure = lootChecker(args[2]);
			if (treasure.size() != 0) {
				Random rand = new Random();
                int index = rand.nextInt(treasure.size());
                loot = treasure.get(index);
			} else {
				System.out.println("No treasure assigned\n");
				System.exit(0);
			}
			
			theme = args[2];
			float seeed = Float.parseFloat(args[6]);
			if (seeed > -1 && seeed < 1 ) {
				seed = args[6];
			}
			
			boolean add;
			add = addQuest(this.theme, this.realm, this.day, "21:49:21");
			if (add) {
				System.out.println("Quest Added!\n");
			}
		
		} else {
			System.out.println("No arguments given\n");
			System.exit(0);
		}
	}
	
	// methods
	// test method 
//	public String getRealm() {
//		String sql = "SELECT * FROM realm;";
//		try {
//
//			statement = connection.createStatement();
//			resultset = statement.executeQuery(sql);
//			
//			while (resultset.next()) {
//				System.out.println(resultset.getString(1));
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		return "";
//	}
	
//	public String getQuest() {
//		String sql = "select * from quest;";
//		System.out.println("theme"+ "\t\t" +  "realm" + "\t\t" + "day"+ "\t\t" + "succeeded");
//		System.out.println("..................................................................");
//		try {
//			statement = connection.createStatement();
//			resultset = statement.executeQuery(sql);
//			
//			while (resultset.next()) {
//				String theme = resultset.getString("theme");
//				String realm = resultset.getString("realm");
//				String day = resultset.getString("day");
//				String succeeded = resultset.getString("succeeded");
//				System.out.println(theme + "\t" + realm + "\t" + day + "\t" + succeeded);				
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		return "";
//	}
	
	// adds to the quest table 
	public boolean addQuest(String theme, String realm, String day, String succeeded) {
		String qry = "INSERT INTO Quest (theme, realm, day, succeeded) VALUES (?, ?, ?, ?)";
		PreparedStatement pst = null;
		int row = 0;
		boolean questTest = false;
		
		try {
			pst = connection.prepareStatement(qry);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		try {
			pst.setString(1, theme);
			pst.setString(2, realm);
	        pst.setDate(3, java.sql.Date.valueOf(day));
	        pst.setTime(4, java.sql.Time.valueOf(succeeded));
	        row = pst.executeUpdate();
	        
	        if(row > 0){
	        	questTest = true;
	       }
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		return questTest;
	}
	
//	public String getloot() {
//		String sql = "select * from loot;";
//		try {
//			statement = connection.createStatement();
//			resultset = statement.executeQuery(sql);
//			while (resultset.next()) {
//				System.out.println(resultset.getString(1));				
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		return "";
//	}
	
	// adds to the loot table 
//	public void addLoot(Date day, String realm, String theme) {
//		// generate a random loot_id and add to the loot column 
//			
//	}
	
	//error checkers 
	public boolean dayChecker(String date) throws ParseException {
		Date dtf = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(date);  // get todays date
		System.out.println(dtf);  
		return new Date(amount).before(dtf);
	}
	
	public static Date dateHelper(String date) throws ParseException {
        return java.sql.Date.valueOf(date);
    }
	
	public boolean realmChecker(String realm) {
		String qry = "SELECT * FROM realm WHERE realm = ?";
		PreparedStatement pst = null;
		ResultSet rs = null;
		boolean realmTest = false;
		
		try {
			pst = connection.prepareStatement(qry);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		try {
			pst.setString(1, realm);
			rs = pst.executeQuery();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		try {
			if (rs.next()) {
				realmTest = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		try {
			rs.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		try{
            pst.close();
        }catch (Exception e) {
            System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
        }
		return realmTest;
	}
	
	public List<String> lootChecker(String amount) {
		String qry = "SELECT * FROM treasure WHERE sql <= ?";
		PreparedStatement pst = null;
		ResultSet rs = null;
		String loot = "";
		List<String> treasure = new ArrayList<>();
		
		try {
			pst = connection.prepareStatement(qry);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		try {
			pst.setInt(1, Integer.parseInt(amount));
			rs = pst.executeQuery();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
		}
		try{
            int amt = Integer.parseInt(amount);
            int total = 0;
            int cLoot;
           while (rs.next()){
        	   cLoot = rs.getInt("sql");
               treasure.add(rs.getString("treasure"));
               total += cLoot;
               if(total >= amt){
                   break;
               }
           }
        }catch (Exception e){
            System.out.println("Failed!\n");
            System.out.println(e.toString());
            System.exit(0);
        }
		return treasure;
	}
	
	public boolean seedChecker() {
		return false;
	}


	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
//		Scanner input = new Scanner(System.in);
//		System.out.println("Enter Day, Realm, Theme:");
//		System.out.println("Day");
//		String day = input.nextLine();
//		
//		System.out.println("REALM");
//		String realm = input.nextLine();
//		
//		System.out.println("THEME"); 
//		String theme = input.nextLine();
		
		
		CreateQuest cq =  new CreateQuest(args);
//		System.out.println("\nQuest Table=");
//		System.out.format(cq.getQuest());
		
//		System.out.println(cq.dayChecker());
		
//		System.out.println("\n--------------------------------------");
//		System.out.println("Loot Table=");
//		System.out.println(cq.getloot());

	}
}

