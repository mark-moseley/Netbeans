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
package org.netbeans.modules.db.dataview.meta;

import java.sql.Connection;
import java.util.Iterator;

import javax.imageio.spi.ServiceRegistry;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.spi.DBConnectionProvider;
import org.openide.util.Mutex;

/**
 * DBConnectionFactory is used to serve out DB Session The actual physical
 * connection handling is implemented by classes that extend this class. This
 * class is a singleton.
 * 
 * @author Ahimanikya Satapathy
 */
public class DBConnectionFactory {

    private static volatile DBConnectionFactory INSTANCE = null;

    /**
     * Serves out service handles using the Singleton pattern. Enforces only one
     * instance of this class in the system.
     * 
     * @return a service handle
     */
    public static DBConnectionFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (DBConnectionFactory.class) {
                if (INSTANCE == null) {
                    if (INSTANCE == null) {
                        INSTANCE = new DBConnectionFactory();
                    }
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Constructor, keep it protected. the package.
     */
    protected DBConnectionFactory() {
    }

    /**
     * Releases the given connection
     * 
     * @param connectionName name of connection
     * @param con Connection to be released
     */
    public void closeConnection(Connection con) {
        DBConnectionProvider connectionProvider = findDBConnectionProvider();
        if (connectionProvider != null) {
            connectionProvider.closeConnection(con);
        }
    }

    /**
     * Gets a new Connection using the given DBConnectionParameters for
     * configuration data.
     * 
     * @param conDef DBConnectionParameter containing connection config
     * @param cl ClassLoader to use to load JDBC driver
     * @return new Connection instance
     * @throws DBException if error occurs while constructing connection
     */
    public Connection getConnection(DatabaseConnection dbConn) throws DBException {
        DBConnectionProvider connectionProvider = findDBConnectionProvider();
        if (connectionProvider != null) {
            return connectionProvider.getConnection(dbConn);
        } else {
            return showConnectionDialog(dbConn);
        }
    }

    private Connection showConnectionDialog(final DatabaseConnection dbConn) {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {

            public Void run() {
                ConnectionManager.getDefault().showConnectionDialog(dbConn);
                return null;
            }
        });

        synchronized (DBConnectionFactory.class) {
            if (dbConn != null) {
                return dbConn.getJDBCConnection();
            }
        }
        return null;
    }
    
    private DBConnectionProvider findDBConnectionProvider() {
        Iterator<DBConnectionProvider> it = ServiceRegistry.lookupProviders(DBConnectionProvider.class);
        if (it.hasNext()) {
            return it.next();
        }

        /*
         * This gives the user/module/components that use etlengine DBConnection
         * factory an option to associate a required class loader with the
         * DBConnectionFactory class. Our requirement is to get the classLoader
         * whose getResources() should be able to point to
         * META-INF/services/org.netbeans.modules.db.model.spi.DBConnectionProvider
         */
        ClassLoader loader = DBConnectionFactory.class.getClassLoader();
        /*
         * the default class loader used by the
         * ServiceRegistry.lookupProviders() is the current thread Context
         * ClassLoader, however this cause problem when we try to get the
         * DBConnectionProvider from within a JBI component. For jbi component
         * when the service request is handled,the class loader happens to be
         * webAppClassloader (i.e.Thread.currentThread().getContextClassLoader()
         * is webAppClassloader ) . This result in a failure of locating the SPI
         * DBConnectionProvider. So in order to avoid using the
         * webAppClassloader the above mechanism has been used
         */
        it = ServiceRegistry.lookupProviders(DBConnectionProvider.class, loader);
        if (it.hasNext()) {
            return it.next();
        }

        return null;
    }
}
