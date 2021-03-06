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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.ddl.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.DatabaseProductNotFoundException;
import org.netbeans.lib.ddl.DatabaseSpecification;
import org.netbeans.lib.ddl.DatabaseSpecificationFactory;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.DriverSpecificationFactory;

/**
* The factory used for creating instances of Specification class. 
* SpecificationFactory collects information about available database 
* description files and is able to specify if system can control 
* the database (specified by product name or live connection) or not. 
* It also provides a list of supported databases. Information about databases
* reads from file org/netbeans/lib/ddl/DatabaseSpecification.plist. It's possible to replace it
* by setting db.specifications.file system property pointing to another one.
*
* @author Slavek Psenicka
*/
public class SpecificationFactory implements DatabaseSpecificationFactory, DriverSpecificationFactory {

    /** Database description file
    * You should use PListReader to parse it.
    */		
    private static final String dbFile = "org/netbeans/lib/ddl/resources/dbspec.plist";

    /** Driver description file
    * You should use PListReader to parse it.
    */		
    private static final String drvFile = "org/netbeans/lib/ddl/resources/driverspec.plist";

    /** Array of SpecificationFiles, found (but not read) files
    * which describes database products.
    */
    private HashMap dbSpecs;

    /** Array of SpecificationFiles, found (but not read) files
    * which describes driver products.
    */
    private HashMap drvSpecs;

    /** Debug information
    */
    private boolean debug = false;
    
    /** Constructor.
    * Reads a bunch of specification files and prepares sfiles array. Files should
    * be read from default place or from folder specified by system property named
    * "db.specifications.folder".
    */
    public SpecificationFactory () throws DDLException {
        String fileDB = System.getProperty("db.specifications.file");
        String fileDrv = System.getProperty("driver.specifications.file");
        SpecificationParser parser;

        try {
            if (fileDB == null) {
                ClassLoader cl = getClass().getClassLoader();
                InputStream stream = cl.getResourceAsStream(dbFile);
                if (stream == null) {
                    String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableToOpenStream"), new String[] {dbFile}); // NOI18N
                    throw new Exception(message);
                }
                parser = new SpecificationParser(stream);
                dbSpecs = parser.getData();
                stream.close();
            } else {
                parser = new SpecificationParser(fileDB);
                dbSpecs = parser.getData();
            }
        } catch (Exception e) {
            if (fileDB != null)
                throw new DDLException("unable to read specifications file " + fileDB + ", " + e.getMessage());
            else
                throw new DDLException("unable to read default specifications file, " + e.getMessage());
        }

        try {
            if (fileDrv == null) {
                ClassLoader cl = getClass().getClassLoader();
                InputStream stream = cl.getResourceAsStream(drvFile);
                if (stream == null) {
                    String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle").getString("EXC_UnableToOpenStream"), new String[] {drvFile}); // NOI18N
                    throw new Exception(message);
                }
                parser = new SpecificationParser(stream);
                drvSpecs = parser.getData();
                stream.close();
            } else {
                parser = new SpecificationParser(fileDrv);
                drvSpecs = parser.getData();
            }
        } catch (Exception e) {
            if (fileDrv != null)
                throw new DDLException("unable to read specifications file " + fileDrv + ", " + e.getMessage());
            else
                throw new DDLException("unable to read default specifications file, " + e.getMessage());
        }
    }

    /** Returns array of database products supported by system.
    * It returns string array only, if you need a Specification instance, use 
    * appropriate createSpecification method.
    */
    public Set supportedDatabases() {
        return dbSpecs.keySet();
    }

    /** Returns true if database (specified by databaseProductName) is
    * supported by system. Does not throw exception if it doesn't.
    */	
    public boolean isDatabaseSupported(String databaseProductName) {
        return (dbSpecs.containsKey(databaseProductName));
    }

    /** Creates instance of DatabaseSpecification class; a database-specification
    * class. This object knows about used database and can be used as
    * factory for db-manipulating commands. It connects to the database 
    * and reads database metadata. Throws DatabaseProductNotFoundException if database
    * (obtained from database metadata) is not supported. Uses given Connection
    */
    public DatabaseSpecification createSpecification(DBConnection dbcon, Connection jdbccon) throws DatabaseProductNotFoundException, DDLException {
        String pn = null;
        try {
            boolean close = (jdbccon != null ? false : true);
            Connection con = (jdbccon != null ? jdbccon : dbcon.createJDBCConnection());
            DatabaseMetaData dmd = con.getMetaData();
            pn = dmd.getDatabaseProductName().trim();

            DatabaseSpecification spec = createSpecification(dbcon, pn, con);
            if (close) con.close();
            return spec;
        } catch (SQLException e) {
            throw new DDLException("unable to connect to server");
        } catch (Exception e) {
            throw new DatabaseProductNotFoundException(pn, "unable to create specification, "+e.getMessage());
        }
    }

    /** Creates instance of DatabaseSpecification class; a database-specification
    * class. This object knows about used database and can be used as
    * factory for db-manipulating commands. It connects to database and
    * reads metadata as createSpecification(DBConnection connection), but always
    * uses specified databaseProductName. This is not recommended technique.
    */
    public DatabaseSpecification createSpecification(DBConnection connection, String databaseProductName, Connection c) throws DatabaseProductNotFoundException {
        //IBM DB2 hack
        if (databaseProductName.toUpperCase().startsWith("DB2/")) //NOI18N
            databaseProductName = "DB2/"; //NOI18N
        
        HashMap product = (HashMap) dbSpecs.get(databaseProductName);

        if (product == null)
            throw new DatabaseProductNotFoundException(databaseProductName);
        HashMap specmap = deepUnion(product, (HashMap) dbSpecs.get("GenericDatabaseSystem"), true);
        specmap.put("connection", connection);
        DatabaseSpecification spec = new Specification(specmap, c);
        specmap.put("dbproduct", databaseProductName);
        spec.setSpecificationFactory(this);

        return spec;
    }

    /** Creates instance of DatabaseSpecification class; a database-specification
    * class. This object knows about used database and can be used as
    * factory for db-manipulating commands. It connects to database and
    * reads metadata as createSpecification(DBConnection connection), but always
    * uses specified databaseProductName. This is not recommended technique.
    */
    public DatabaseSpecification createSpecification(String databaseProductName, Connection c) throws DatabaseProductNotFoundException {
        //IBM DB2 hack
        if (databaseProductName.toUpperCase().startsWith("DB2/")) //NOI18N
            databaseProductName = "DB2/"; //NOI18N
        
        HashMap product = (HashMap) dbSpecs.get(databaseProductName);
        if (product == null) throw new DatabaseProductNotFoundException(databaseProductName);
        HashMap specmap = deepUnion(product, (HashMap) dbSpecs.get("GenericDatabaseSystem"), true);
        specmap.put("dbproduct", databaseProductName);
        return new Specification(specmap, c);
    }

    public DatabaseSpecification createSpecification(Connection c) throws DatabaseProductNotFoundException, SQLException {
        return createSpecification(c, c.getMetaData().getDatabaseProductName().trim());
    }

    public DatabaseSpecification createSpecification(Connection c, String databaseProductName) throws DatabaseProductNotFoundException {
        //IBM DB2 hack
        if (databaseProductName.toUpperCase().startsWith("DB2/")) //NOI18N
            databaseProductName = "DB2/"; //NOI18N
        
        HashMap product = (HashMap) dbSpecs.get(databaseProductName);
        if (product == null) throw new DatabaseProductNotFoundException(databaseProductName);
        HashMap specmap = deepUnion(product, (HashMap) dbSpecs.get("GenericDatabaseSystem"), true);
        DatabaseSpecification spec = new Specification(specmap, c);
        spec.setSpecificationFactory(this);
        return spec;
    }

    /** Returns debug-mode flag
    */
    public boolean isDebugMode() {
        return debug;
    }

    /** Sets debug-mode flag
    */
    public void setDebugMode(boolean mode) {
        debug = mode;
    }

    /** Returns array of driver products supported by system.
    * It returns string array only, if you need a Specification instance, use 
    * appropriate createDriverSpecification method.
    */
    public Set supportedDrivers() {
        return drvSpecs.keySet();
    }

    /** Returns true if driver (specified by driverName) is
    * supported by system. Does not throw exception if it doesn't.
    */	
    public boolean isDriverSupported(String driverName) {
        return (drvSpecs.containsKey(driverName));
    }

    /** Creates instance of DriverSpecification class; a driver-specification
    * class. This object knows about used driver.
     */
    public DriverSpecification createDriverSpecification(String driverName) {
        HashMap product = (HashMap) drvSpecs.get(driverName);
        if (product == null)
            product = (HashMap) drvSpecs.get("DefaultDriver");
        HashMap specmap = deepUnion(product, (HashMap) drvSpecs.get("DefaultDriver"), true);
        DriverSpecification spec = new DriverSpecification(specmap);
        spec.setDriverSpecificationFactory(this);

        return spec;
    }

    /** Creates deep copy of Map.
    * All items will be cloned. Used internally in this object.
    */
    private HashMap deepClone(HashMap map) {
        HashMap newone = (HashMap)map.clone();
        Iterator it = newone.keySet().iterator();
        while (it.hasNext()) {
            Object newkey = it.next();
            Object deepobj = null, newobj = newone.get(newkey);
            if (newobj instanceof HashMap)
                deepobj = deepClone((HashMap)newobj);
            else if (newobj instanceof String)
                deepobj = (Object)new String((String)newobj);
            else if (newobj instanceof Vector)
                deepobj = ((Vector)newobj).clone();
            newone.put(newkey, deepobj);
        }

        return newone;
    }

    /** Joins base map with additional one.
    * Copies keys only if not present in base map. Used internally in this object.
    */
    private HashMap deepUnion(HashMap base, HashMap additional, boolean deep) {
        Iterator it = additional.keySet().iterator();
        while (it.hasNext()) {
            Object addkey = it.next();
            Object addobj = additional.get(addkey);

            //SQL92 types will be not added into databese type list
            if (addkey.equals("TypeMap"))
                continue;

            if (base.containsKey(addkey)) {
                Object baseobj = base.get(addkey);
                if (deep && (baseobj instanceof HashMap) && (addobj instanceof HashMap)) {
                    deepUnion((HashMap)baseobj, (HashMap)addobj, deep);
                }
            } else {
                if (addobj instanceof HashMap)
                    addobj = deepClone((HashMap)addobj);
                else if (addobj instanceof String)
                    addobj = (Object)new String((String)addobj);
                else if (addobj instanceof Vector)
                    addobj = ((Vector)addobj).clone();
                base.put(addkey, addobj);
            }
        }

        return base;
    }

}
