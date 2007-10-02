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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
/*
 * SampleDatabaseCreator.java
 *
 * Created on July 28, 2006, 4:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.dataconnectivity.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.filesystems.FileLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.modules.derby.spi.support.DerbySupport;

/**
 *
 * @author John Baker
 */
public class SampleDatabaseCreator  {

    public static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    public static final String DRIVER_DISP_NAME_NET = "Java DB (Network)"; // NOI18N

    /** Creates a new instance of SampleDatabaseUtils */
    public SampleDatabaseCreator() {
    }


    // Create database and connection then extract zip file containing the schema
    public static void createAll(String database, String username, String password, String schema, String sampleZipFile, boolean rememberPassword, String server, int port) {

        try {
            if (DerbyDatabases.isDerbyRegistered()) {
                SampleDatabaseCreator sample = new SampleDatabaseCreator();
                if (!DerbyDatabases.databaseExists(database)) {
                    sample.registerDatabase(database, username, schema.toUpperCase(), password, rememberPassword, server, port);
                    sample.extractSampleDatabase(database, sampleZipFile);
                }
                // if userdir is deleted and sample databases exist then resurrect the connections
                else
                    sample.registerDatabase(database, username, schema.toUpperCase(), password, rememberPassword, server, port);              
            }
            // if Java DB server isn't registered, do nothing. Once Java EE 5 server is registered then Java DB will be automatically registered
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } catch (DatabaseException de) {
            de.printStackTrace();
        }


    }


    /**
     * Extracts the sample database under the given name in the Derby system home.
     * Does not overwrite an existing database.
     *
     * <p>Not public because used in tests.</p>
     */
    private void extractSampleDatabase(String databaseName, String zipFile) throws IOException{

        File systemHomeFile = DerbyDatabases.getSystemHome();
        if (systemHomeFile == null) { // NOI18N
            throw new IllegalStateException("The derby.system.home directory is not set"); // NOI18N
        }

        File sourceFO = InstalledFileLocator.getDefault().locate(zipFile, null, false); // NOI18N
        FileObject systemHomeFO = FileUtil.toFileObject(systemHomeFile);
        FileObject sampleFO = systemHomeFO.getFileObject(databaseName); // NOI18N
        if (sampleFO == null) {
            sampleFO = systemHomeFO.createFolder(databaseName);
            this.extractZip(sourceFO, sampleFO);
        }
    }

    /**
     * Registers in the Database Explorer the specified database
     * on the local Derby server.
     */
    private DatabaseConnection registerDatabase(String databaseName, String user, String schema, String password, boolean rememberPassword, String server, int port) throws DatabaseException {
        JDBCDriver drivers[] = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
        if (drivers.length == 0) {
            throw new IllegalStateException("The " + DRIVER_DISP_NAME_NET + " driver was not found"); // NOI18N
        }
        
        DatabaseConnection dbconn = DatabaseConnection.create(drivers[0], "jdbc:derby://" + server + ":" + port +  "/" + databaseName, user, schema, password, rememberPassword); // NOI18N
        ConnectionManager.getDefault().addConnection(dbconn);
        return dbconn;
    }
    
    // Unzips sample database file from modules/ext
    public static void extractZip(File source, FileObject target) throws IOException {
        FileInputStream is = new FileInputStream(source);
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze;
            
            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();
                
                // if directory, create
                if (ze.isDirectory()) {
                    FileUtil.createFolder(target, name);
                    continue;
                }
                
                // if file, copy
                FileObject fd = FileUtil.createData(target, name);
                FileLock lock = fd.lock();
                try {
                    OutputStream os = fd.getOutputStream(lock);
                    try {
                        FileUtil.copy(zis, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            is.close();
        }
    }               
    
    public static void createDatabase (String database,  String sampleZipFile) {
        try {
            if (DerbyDatabases.isDerbyRegistered()) {
                SampleDatabaseCreator sample = new SampleDatabaseCreator();
                if (!DerbyDatabases.databaseExists(database)) {
                    sample.extractSampleDatabase(database, sampleZipFile);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
