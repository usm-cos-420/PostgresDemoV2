package edu.usm.cos420;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.util.Properties;

/* 
 * Code to demonstrate creation, deletion of tables. Also, records are inserted. 
 * 
 * This code requires that a postgres (or mysql) jdbc jar file be included. Use
 * Maven can be used to pull these dependencies. 
 * Code can be run in the command window with : 
 *     mvn exec:java -Dexec.mainClass="edu.usm.cos420.JdbcExample
 * 
 * When we connect to a cloud server, the cloud server DB needs to be public
 * and the web site this application code is running on needs to be explicitly 
 * listed in allowable sites on cloud server (see class tutorial) 
 *  
 * This code also uses properties to store the db server information and un/pw
 * This serves two purposes : 1) You can easily change the properties to point 
 * to a new (your) db server 2) it is a model for protecting un/pw information 
 * (you do not have to put un/pw in the source code)
 * 
 */

public class JdbcExample {

    private static Properties properties = new Properties();;

    // load the properties; expect to find properties file in the resource folder
    public static void loadProps(InputStream in) {
		try {
            properties.load(in);
        } catch (IOException e) {
            System.out.println( "No config.properties was found.");
            e.printStackTrace();
        }
	}
	
    /*
     *  Code to get DB connection properties and attempt jdbc connection
     *  We will use the default postgres db, but code could be changed to connect to 
     *  another DB using properties
     *  This code belongs in a class focused DB operations 
     */
	public static Connection createDBConnection() throws ClassNotFoundException, SQLException
	{
		String strDbUser = properties.getProperty("jdbc.username");        // database login username
        String strDbPassword = properties.getProperty("jdbc.password");    // database login password
        String remoteHost = properties.getProperty("remote.host");     // remote host url (ie cloud server url	
        String remotePort = properties.getProperty("remote.port");     // remote post number
        String databaseName = properties.getProperty("databaseName");
        Connection con;
        
        System.out.println("Trying connection ");
    
		Class.forName("org.postgresql.Driver");
//    	Class.forName("com.mysql.jdbc.Driver");
//        con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:"+tunnelPort+"/cos420?user="+strDbUser+"&password="+strDbPassword);
		String connectString = "jdbc:postgresql://" + remoteHost +":" + remotePort + "/"+databaseName;  
		con = DriverManager.getConnection(connectString , strDbUser,strDbPassword);

    	if(!con.isClosed())
          System.out.println("Successfully connected to Postgres server using TCP/IP...");

        return con;
	}

    /*
     *  Code to demonstrate using db meta-info to determine the existence of 
     *  a nurses table and create it if does not exist
     *  This code belongs in a class focused DB operations 
     */

	public static void emptyTable(Connection con, String tableName) throws SQLException
	{
	    Statement st = null;
	
        String createStr = "CREATE TABLE " + tableName +  " (userId INTEGER, firstName VARCHAR(30), lastName VARCHAR(30), countryCode VARCHAR(10), primary key(userID))";
    	String dropStr = "DROP TABLE " + tableName;
    	
        DatabaseMetaData dbm = con.getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
 
        st = con.createStatement();      

        if (tables != null && tables.next()) {
        // Table exists
        	st.executeUpdate(dropStr);  
        	System.out.println("Got rid of old table");
        }

        st.executeUpdate(createStr);
	
	}
	
	/*
     *  Code to demonstrate using adding data to DB
     *  This code belongs in a class focused DB operations 
     */

	public static void addToTable(Connection con, String tableName) throws SQLException
	{
	    Statement st = null;

	    st = con.createStatement();
	      
	    String sql = "INSERT INTO " + tableName + " VALUES (1, 'Jorn', 'Klungsoyr', 'Ghana')";
	    st.executeUpdate(sql);
		
	    // scrollable datasets allow you to march through a set of records iteratively
	    // here, we insert a record 
	    
	    st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

        ResultSet uprs = st.executeQuery("SELECT * FROM " + tableName);

		uprs.moveToInsertRow();
		
		uprs.updateInt("userId", 2);
		uprs.updateString("firstName", "Sally");
		uprs.updateString("lastName", "Saviour");
		uprs.updateString("countryCode", "Ghana");
		uprs.insertRow();

	}

	/*
     *  Code to demonstrate using extracting data from DB
     *  This code belongs in a class focused DB operations 
     */

	public static void displayTable(Connection con, String tableName) throws SQLException 
	{
		Statement st = null;
		ResultSet rs = null;

		st = con.createStatement();      

		rs = st.executeQuery("SELECT userID, firstName, lastName, countryCode FROM " + tableName);

		while(rs.next()) {
			// can retrieve by position
			int userId = rs.getInt(1);
			// or by column name 
			String firstName = rs.getString("firstname");
			String lastName = rs.getString(3);
			String countryCode = rs.getString("countryCode");

			System.out.println(userId + ". " + lastName + ", " +
					firstName + " (" + countryCode + ")");
		}

	}
	
	public static void main(String[] args) 
	{

		Connection con = null;

		InputStream in = JdbcExample.class.getClassLoader().getResourceAsStream("config.properties");
		loadProps(in);

		System.out.println("properties loaded ");
		
		try {

			con = createDBConnection();
	        String tableName = properties.getProperty("tableName");
			emptyTable(con, tableName);
			addToTable(con, tableName);
            displayTable(con, tableName);
            
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
			}
		}
	}
}