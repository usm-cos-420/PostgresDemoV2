package edu.usm.cos420;

import edu.usm.cos420.*;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.util.Properties;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 *   The only reason this test class is included is to demonstrate 
 *   unit tests on the databases. Even with that, it only shows one 
 *   way of many possibilities and is interacting with a application 
 *   that has been design only to demonstrate. In the code below, we do operations 
 *   on a different table name (_nurses) that is defined in the test 
 *   properties. This approach is slow (connections take time) and if 
 *   if the tests end up taking too much time, mock objects can be used 
 *   (mockito, ...)
 */
public class TestJdbcExample {

	private static Properties properties;
	private static Connection con;
	private static String tableName;
	
	@Before
	public void setUp() throws Exception {

		InputStream in = TestJdbcExample.class.getClassLoader().getResourceAsStream("config.properties");
		JdbcExample.loadProps(in);
		con = JdbcExample.createDBConnection();
		tableName = "_nurses";
	}

	@After
	public void tearDown() throws Exception {
		try {
			if (con != null)
				con.close();
		} catch (SQLException e) {
		}
	}

	
	/*
	 * note : parameters for emptyTable come from attributes of class
	 */
	@Test
	public void testEmptyTable() throws SQLException 
	{
		JdbcExample.emptyTable(con, tableName);
        DatabaseMetaData dbm = con.getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
 
        Boolean tn = tables.next();
        assertTrue(tables != null && tn);

	}

}
