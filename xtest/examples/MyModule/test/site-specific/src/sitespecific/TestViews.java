/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package sitespecific;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;


/** Example of site specific test case.
 */
public class TestViews extends NbTestCase {
    
    
    public TestViews(String testName) {
        super(testName);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(TestViews.class));
    }
    
    /** URL of database */
    private String url;
    /** driver class */
    private String driver;
    /** username */
    private String username;
    /** password */
    private String password;
    /* database connection */
    private Connection con;
    
    /** Sets connection properties from system properties.
     */
    public void setUp() {
        driver = System.getProperty("driver");
        if(driver==null) {
            fail("Property driver hasn't been set");
        }
        url = System.getProperty("url");
        if(url==null) {
            fail("Property url hasn't been set");
        }
        username = System.getProperty("username");
        if(username==null) {
            fail("Property username hasn't been set");
        }
        password = System.getProperty("password");
        if(password==null) {
            fail("Property password hasn't been set");
        }
    }
    
    /** Test checks whether given database contains some view and
     * if so, it prints their names to a log file. It writes also
     * connection parameters to log file.
     */
    public void testViews() throws Exception {
        log("driver="+driver);
        log("url="+url);
        log("username="+username);
        log("password="+password);
        createConnection();
        DatabaseMetaData meta = con.getMetaData();
        ResultSet tables = meta.getTables(null, null, null, null);
        boolean empty = true;
        while(tables.next()) {
            empty = false;
            String tableType = tables.getString("TABLE_TYPE");
            String tableName = tables.getString("TABLE_NAME");
            if(tableType.equals("VIEW")) {
                // write name of view to log file
                log(tableName);
            }
        }
        assertTrue("Database doesn't contain any view", !empty);
    }
    
    /** Creates connection to a database specified by given url.
     */
    private void createConnection() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            fail("Driver not found: "+e.getMessage());
        }
        try {
            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            fail("Connection not established: "+e.getMessage());
        }
    }
}
