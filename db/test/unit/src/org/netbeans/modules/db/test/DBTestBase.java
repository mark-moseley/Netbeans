/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.node.ConnectionNode;
import org.netbeans.modules.db.explorer.node.SchemaNode;
import org.netbeans.modules.db.explorer.node.TableListNode;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.openide.nodes.Node;
//import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
//import org.netbeans.modules.db.explorer.infos.TableListNodeInfo;
//import org.netbeans.modules.db.explorer.infos.TableNodeInfo;

/**
 * This class is a useful base test class that provides initial setup
 * to get a connecxtion and also a number of utility routines
 * 
 * @author <a href="mailto:david@vancouvering.com">David Van Couvering</a>
 */
public abstract class DBTestBase extends TestBase {

    private static final Logger LOGGER = 
            Logger.getLogger(DBTestBase.class.getName());
    
    // Change this to get rid of output or to see output
    protected static final Level DEBUGLEVEL = Level.FINE;

    private static String driverClassName;
    private static String dbUrl;
    private static String username;
    private static String password;
    private static String dbname;
    protected static String dblocation;
    private static String driverJar;
    private static Driver driver;
    
    private static final String DRIVER_PROPERTY = "db.driver.classname";
    private static final String DRIVER_JARPATH_PROPERTY = "db.driver.jarpath";
    private static final String URL_PROPERTY = "db.url";
    private static final String USERNAME_PROPERTY = "db.user";
    private static final String PASSWORD_PROPERTY = "db.password";
    private static final String DBDIR_PROPERTY = "db.dir";
    private static final String DBNAME_PROPERTY = "db.name";
    private static final String SCHEMA_NAME = "DBTESTS";

    private static String quoteString = null;
    
    // This defines what happens to identifiers when stored in db
    private static final int RULE_UNDEFINED = -1;
    public static final int LC_RULE = 0; // everything goes to lower case
    public static final int UC_RULE = 1; // everything goes to upper case
    public static final int MC_RULE = 2; // mixed case remains mixed case
    public static final int QUOTE_RETAINS_CASE = 3; // quoted idents retain case

    private static final String TEST_TABLE = "TEST";
    private static final String TEST_TABLE_ID = "ID";

    private static int    unquotedCaseRule = RULE_UNDEFINED;
    private static int    quotedCaseRule = RULE_UNDEFINED;

    private static DBProvider dbProvider;
    
    private File clusterDir;

    public DBTestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getProperties();
        createDBProvider();
        createSchema();
    }
        
    protected DBProvider getDBProvider() {
        return dbProvider;
    }

    protected final void createTestTable() throws Exception {
        dbProvider.createTestTable(getConnection(), getSchema(), TEST_TABLE, TEST_TABLE_ID);
    }

    protected static final String getTestTableName() {
        return TEST_TABLE;
    }

    protected static final String getTestTableIdName() {
        return TEST_TABLE_ID;
    }
    
    protected final Connection getConnection() throws Exception {
        return getDatabaseConnection(true).getJDBCConnection();
    }

    protected final Connection reconnect() throws Exception {
        disconnect();
        return getConnection();
    }
    
    protected JDBCDriver getJDBCDriver() throws Exception {
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClassName);
        if (drivers.length > 0) {
            return drivers[0];
        }

        JDBCDriver jdbcDriver = JDBCDriver.create(driverClassName, driverClassName,
                driverClassName, new URL[]{ new URL(driverJar)});
        JDBCDriverManager.getDefault().addDriver(jdbcDriver);

        return jdbcDriver;
    }

    protected TableNode getTableNode(String tablename) throws Exception {
        ConnectionNode connectionNode = org.netbeans.modules.db.explorer.DatabaseConnection.findConnectionNode(
                getDatabaseConnection(true).getName());

        assertNotNull(connectionNode);

        //Since the node updates asynchronously, we need
        //to give it some time to let that happen before trying to find the
        // table node
        Thread.sleep(2000);


        Collection<? extends Node> children = connectionNode.getChildNodesSync();
        // DatabaseNodeInfo.printChildren("connection children", children);


        for (Node child : children) {
            if (child instanceof TableListNode) {
                Collection<? extends Node> tables = ((TableListNode)child).getChildNodesSync();

                // DatabaseNodeInfo.printChildren("tables", tables);

                for (Node table : tables) {
                    if (tablename.toLowerCase().equals(table.getDisplayName().toLowerCase())) {
                        return (TableNode)table;
                    }
                }
            } else if (child instanceof SchemaNode) {
                Collection<? extends Node> list = ((SchemaNode)child).getChildNodesSync();
                for (Node c : list) {
                    if (c instanceof TableListNode) {
                        Collection<? extends Node> tables = ((TableListNode)c).getChildNodesSync();

                        // DatabaseNodeInfo.printChildren("tables", tables);

                        for (Node table : tables) {
                            if (tablename.toLowerCase().equals(table.getDisplayName().toLowerCase())) {
                                return (TableNode)table;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    protected void disconnect() throws Exception {
        ConnectionManager.getDefault().disconnect(getDatabaseConnection(false));
    }

    protected void removeConnection() throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(false);
        if (dbconn == null) {
            return;
        }
        ConnectionManager.getDefault().disconnect(dbconn);
        ConnectionManager.getDefault().removeConnection(dbconn);
    }


    /**
     * Get the DatabaseConnection for the configured database.  This
     * method will create and register the connection the first time it is called
     */
    protected DatabaseConnection getDatabaseConnection(boolean connect) throws Exception {
        DatabaseConnection dbconn = DatabaseConnection.create(getJDBCDriver(), dbUrl, username, SCHEMA_NAME, password, false);

        return addConnection(dbconn, connect);
    }

    protected String getSchema() throws Exception {
        return SCHEMA_NAME;
    }

    protected static String getDbUrl() {
        return dbUrl;
    }

    protected static String getDriverClass() {
        return driverClassName;
    }

    protected static String getPassword() {
        return password;
    }

    protected static String getUsername() {
        return username;
    }

    /**
     * Goes through all SQLExceptions found through getNextException()
     * and builds it instead as a set of chained exceptions, so they
     * all get reported
     */
    protected static SQLException processSQLException(SQLException original) {
        SQLException next = original;
        while (next != null) {
            if (next.getNextException() != null) {
                Throwable cause = next;
                
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }

                cause.initCause(next.getNextException());
            }

            next = next.getNextException();
        }

        return original;        
    }
    protected final boolean isDerby() {
        return driverClassName.startsWith("org.apache.derby");
    }

    protected final boolean isMySQL() {
        return driverClassName.equals("com.mysql.jdbc.Driver");
    }
    
    protected final void createSchema() throws Exception {
        if (schemaExists(getSchema())) {
            dropSchema();
        }
        
        dbProvider.createSchema(getConnection(), getSchema());

        setSchema();
    }
    
    protected final void dropSchema() throws Exception {
        if (! schemaExists(getSchema())) {
            return;
        }

        if (isDerby()) {
            // Trying to remove the schema is very difficult, as it has to
            // be completely empty. Easier just to blow away the database directory.
            // Next time we connect a new db will be automatically created
            shutdownDerby();
            if (! dblocation.equals(this.getWorkDirPath())) {
                clearWorkDir();
            } else {
                deleteSubFiles(new File(dblocation));
            }
        } else {
            dbProvider.dropSchema(getConnection(), getSchema());
        }
    }

    protected static void deleteSubFiles(File file) throws IOException {
        if (file.isDirectory() && file.exists()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        } else {
            // probably do nothing - file is not a directory
        }
    }

    private static void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }

        }
        // file is a File :-)
        boolean result = file.delete();
        if (result == false ) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = "+file.getPath());
        }
    }

    protected final boolean schemaExists(String schemaName) throws Exception {
        return dbProvider.schemaExists(getConnection(), schemaName);
    }
    
    protected final void setSchema() throws Exception {
        if (isMySQL() && ! dbUrl.contains(getSchema())) {
            // Not sure why, but we need a connection directly to this database
            removeConnection();
            dbUrl = dbUrl + getSchema();
            getConnection();
        } else {
            dbProvider.setSchema(getConnection(), getSchema());
        }
    }

    protected final void dropView(String viewname) throws Exception {
        if (! viewExists(viewname)) {
            return;
        }
        
        dbProvider.dropView(getConnection(), getSchema(), viewname);
    }
    protected final void dropTable(String tablename) throws Exception {
        dbProvider.dropTable(getConnection(), getSchema(), tablename);
    }
        
    protected final String quote(String value) throws Exception {
        if ( value == null  || value.equals("") )
        {
            return value;
        }
        
        if ( quoteString == null ) {
            initQuoteString();
        }
        
        return quoteString + value + quoteString;
    }
    
    protected final boolean tableExists(String tablename) throws Exception {
        return dbProvider.tableExists(getConnection(), getSchema(), fixIdentifier(tablename));
    }
    
    protected final boolean columnExists(String tablename, String colname)
            throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        String schemaName = getSchema();
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getColumns(null, schemaName, tablename, colname);
        
        int numrows = printResults(
                rs, "columnExists(" + tablename + ", " + colname + ")");
        
        rs.close();
        
        return numrows > 0;
    }
    
    protected final boolean indexExists(String tablename, String indexname)
            throws Exception {
        indexname = fixIdentifier(indexname);
        String schemaName = fixIdentifier(getSchema());
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getIndexInfo(null, schemaName, tablename, false, false);

        while ( rs.next() ) {
            String idx = rs.getString(6);
            if ( idx.equals(indexname)) {
                return true;
            }
        }
        
        return false;
    }
    
    protected final boolean viewExists(String viewName) throws Exception {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getTables(null, getSchema(), fixIdentifier(viewName),
                new String[] {"VIEW"});
        
        return rs.next();
    }
    
    protected final boolean columnInPrimaryKey(String tablename, String colname)
        throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getPrimaryKeys(null, getSchema(), tablename);
        
        // printResults(rs, "columnInPrimaryKey(" + tablename + ", " +
        //        colname + ")");
                
        while ( rs.next() ) {
            String pkCol = rs.getString(4);
            if ( pkCol.equals(colname)) {
                return true;
            }
        }
        
        return false;
    }

    protected final void printAllTables() throws Exception {
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getTables(null, getSchema(), "%", null);
        printResults(rs, "printAllTables()");
    }

    protected final boolean columnInIndex(String tablename, String colname,
            String indexname) throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        indexname = fixIdentifier(indexname);
        return dbProvider.columnInIndex(getConnection(), getSchema(), tablename, colname, indexname);
    }
    
    protected final boolean columnInAnyIndex(String tablename, String colname)
            throws Exception {
        tablename = fixIdentifier(tablename);
        colname = fixIdentifier(colname);
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getIndexInfo(null, getSchema(), tablename, false, false);

        // printResults(rs, "columnInIndex(" + tablename + ", " + colname + 
        //    ", " + indexname + ")");

        while ( rs.next() ) {
            String ixColName = rs.getString(9);
            if ( ixColName.equals(colname) ) {
                return true;
            }
        }

        return false;        
    }
    
    protected final boolean indexIsUnique(String tablename, String indexName)
            throws Exception {
        tablename = fixIdentifier(tablename);
        indexName = fixIdentifier(indexName);
        DatabaseMetaData md = getConnection().getMetaData();
        ResultSet rs = md.getIndexInfo(null, getSchema(), tablename, false, false);
        
        // TODO - Parse results
        
        rs.close();
        return true;
    }
 
    /**
     * Fix an identifier for a metadata call, as the metadata APIs
     * require identifiers to be in proper case
     */
    protected final String fixIdentifier(String ident) throws Exception {
        if ( unquotedCaseRule == RULE_UNDEFINED ) {
            getCaseRules();
        }
        
        if ( isQuoted(ident) ) {
            switch ( quotedCaseRule ) {
                case QUOTE_RETAINS_CASE:
                    break;
                case UC_RULE:
                    ident = ident.toUpperCase();
                    break;
                case LC_RULE:
                    ident = ident.toLowerCase();
                    break;
                case MC_RULE:
                    break;
                default:
                    LOGGER.log(Level.WARNING, "Unexpected identifier rule: +" +
                            unquotedCaseRule + ", assuming case is retained");
            }
            
            return ident.substring(1, ident.length() -1);
        } else {
            switch ( unquotedCaseRule ) {
                case UC_RULE:
                    return ident.toUpperCase();
                case LC_RULE:
                    return ident.toLowerCase();
                case MC_RULE:
                    return ident;
                default:
                    LOGGER.log(Level.WARNING, "Unexpected identifier rule: +" +
                            unquotedCaseRule + ", assuming upper case");
                    return ident.toUpperCase();
            }            
        }
    }
    
    protected final boolean isQuoted(String ident) throws Exception {
        if (quoteString == null) {
            initQuoteString();
        }
        
        return ident.startsWith(quoteString) && ident.endsWith(quoteString);
    }
    
    protected final int getUnquotedCaseRule() throws Exception {
        getCaseRules();
        return unquotedCaseRule;
    }

    protected final int printResults(ResultSet rs, String queryName)
            throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numcols = rsmd.getColumnCount();
        int numrows = 0;
        
        LOGGER.log(DEBUGLEVEL, "RESULTS FROM " + queryName);
        assert(rs != null);
        
        StringBuffer buf = new StringBuffer();

        buf.append("|");        
        for ( int i = 1 ; i <= numcols ; i++ ) {
            buf.append(rsmd.getColumnName(i) + "|");
        }
        LOGGER.log(DEBUGLEVEL, buf.toString());
        
        while ( rs.next() ) {
            numrows++;
            buf = new StringBuffer();
            buf.append("|");
            for ( int i = 1 ; i <= numcols ; i++ ) {
                buf.append(rs.getString(i) + "|");
            }
            LOGGER.log(DEBUGLEVEL, buf.toString());
        }
        
        return numrows;
    }

    protected final void shutdownDerby() throws Exception {
        assertTrue(isDerby());

        String url = dbUrl + ";shutdown=true";
        url = url.replace(";create=true", "");

        DatabaseConnection conn = DatabaseConnection.create(getJDBCDriver(),
                url, getSchema(), getUsername(), getPassword(), false);

        ConnectionManager.getDefault().addConnection(conn);

        // This forces the shutdown
        try {
            ConnectionManager.getDefault().connect(conn);
        } catch (DatabaseException dbe) {
            // expected, this always happens when you shut it down
        }

        ConnectionManager.getDefault().removeConnection(conn);
    }

    @Override
    protected void tearDown() throws Exception {
        getConnection();
        dropSchema();
        removeConnection();
    }

    /**
     * Is this vendor one that doesn't (really) support schemas?  In particular, if you
     * specify a schema in the database connection creation, does it matter?
     *
     * @return
     */
    public boolean schemaNotUsed() {
        return isMySQL();
    }

    public DatabaseConnection addConnection(DatabaseConnection dbconn, boolean connect) throws DatabaseException {
        DatabaseConnection existing = ConnectionManager.getDefault().getConnection(dbconn.getName());
        if (existing != null) {
            dbconn = existing;
        } else {
            ConnectionManager.getDefault().addConnection(dbconn);
        }
        if (connect) {
            ConnectionManager.getDefault().connect(dbconn);
        }
        return dbconn;
    }

    private void initQuoteString() throws Exception {
        DatabaseMetaData md = getConnection().getMetaData();
        quoteString = md.getIdentifierQuoteString();
    }

    private void getProperties() throws Exception {
        
        clearWorkDir();
        
        dblocation = System.getProperty(DBDIR_PROPERTY, getWorkDirPath());
        
        // Add a slash for the Derby URL syntax if we are
        // requesting a specific path for database files
        if (dblocation.length() > 0) {
            dblocation = dblocation + "/";
        }
        
        driverClassName = System.getProperty(DRIVER_PROPERTY, "org.apache.derby.jdbc.EmbeddedDriver");
        dbname = System.getProperty(DBNAME_PROPERTY, "DBTESTS");
        dbUrl = System.getProperty(URL_PROPERTY, "jdbc:derby:" + dblocation + dbname + ";create=true");
        if (isMySQL() && ! (dbUrl.endsWith("/"))) {
            fail("The MySQL url needs to be of the form 'jdbc:mysql://<host>:<port>/'.  Please do not specify a database in the URL");
        }
        username = System.getProperty(USERNAME_PROPERTY, "DBTESTS");
        password = System.getProperty(PASSWORD_PROPERTY, "DBTESTS");
        driverJar = System.getProperty(DRIVER_JARPATH_PROPERTY, "nball:///db/external/derby-10.2.2.0.jar");

        driverJar = convertPath(driverJar);
    }

    private void createDBProvider() {
        if (isDerby()) {
            dbProvider = new DerbyDBProvider();
        } else if (isMySQL()) {
            dbProvider = new MySQLDBProvider();
        } else {
            dbProvider = new DefaultDBProvider();
        }
    }

    private Driver getDriver() throws Exception {
        if (driver == null) {
            URLClassLoader driverLoader = new URLClassLoader(new URL[]{new URL(driverJar)});
            driver = (Driver)driverLoader.loadClass(driverClassName).newInstance();
        }

        return driver;
    }


    private String convertPath(String urlString) throws Exception {
        if (clusterDir == null) {
            String netBeansDirs = System.getProperty("netbeans.dirs");
            if (netBeansDirs != null) {
                String[] paths = netBeansDirs.split(":");
                assert(paths.length > 0);
                
                // They are all paths to a cluster directory, just take the first
                // one.
                clusterDir = new File(paths[0]);
            } else {
                // We're running outside of NetBeans, so let's derive the cluster roo from 
                // where we find a class in the db module
                File jarFile = new File(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                clusterDir = jarFile.getParentFile().getParentFile();
            }
        }
        
        if (urlString.startsWith("nbinst")) {
            // The path to the NetBeans installation
            URL url = new URL(urlString.replace("nbinst:", "file:"));
            return "file://" + clusterDir.getAbsolutePath() + url.getPath();

        } else if (urlString.startsWith("nball")) {
            // The path to the root of the NetBeans source tree.  This only
            // works if you're testing from a source tree
            URL url = new URL(urlString.replace("nball:", "file:"));
            String rootpath = clusterDir.getParentFile().getParentFile().getParentFile().getAbsolutePath();
            return "file://" + rootpath + url.getPath();
        } else {
            return urlString;
        }
    }
    
    private void getCaseRules() throws Exception {
        DatabaseMetaData md;
        
        try {
            md = getConnection().getMetaData();
            if ( md.storesUpperCaseIdentifiers() ) {
                unquotedCaseRule = UC_RULE;
            } else if ( md.storesLowerCaseIdentifiers() ) {
                unquotedCaseRule = LC_RULE;
            } else if ( md.storesMixedCaseIdentifiers() ) {
                unquotedCaseRule = MC_RULE;
            } else {
                unquotedCaseRule = UC_RULE;
            }
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.INFO, "Exception trying to find out how " +
                    "db stores unquoted identifiers, assuming upper case: " +
                    sqle.getMessage());
            LOGGER.log(Level.FINE, null, sqle);
            
            unquotedCaseRule = UC_RULE;
        }

        try {
            md = getConnection().getMetaData();
            
            if ( md.storesLowerCaseQuotedIdentifiers() ) {
                quotedCaseRule = LC_RULE;
            } else if ( md.storesUpperCaseQuotedIdentifiers() ) {
                quotedCaseRule = UC_RULE;
            } else if ( md.storesMixedCaseQuotedIdentifiers() ) {
                quotedCaseRule = MC_RULE;
            } else {
                quotedCaseRule = QUOTE_RETAINS_CASE;
            }
        } catch ( SQLException sqle ) {
            LOGGER.log(Level.INFO, "Exception trying to find out how " +
                    "db stores quoted identifiers, assuming case is retained: " +
                    sqle.getMessage());
            LOGGER.log(Level.FINE, null, sqle);
            
            quotedCaseRule = QUOTE_RETAINS_CASE;
        }
    }
    
}
