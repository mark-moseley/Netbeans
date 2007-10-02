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

package org.netbeans.modules.visualweb.dataconnectivity.naming;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.netbeans.modules.visualweb.dataconnectivity.utils.SampleDatabaseCreator;


/**
 * Waits for the JDBCDriverManager to register the Derby driver
 * @author John Baker
 */
public class DerbyWaiter {

    private static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N

    private boolean registered;  
    private boolean isMigration; // if user is migrating settings
    
    private final JDBCDriverListener jdbcDriverListener = new JDBCDriverListener() {
        public void driversChanged() {
            registerConnections();
        }
    };
    
    public DerbyWaiter(boolean migration) {
        isMigration = migration;
        if (JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET).length == 0) {
            JDBCDriverManager.getDefault().addDriverListener(jdbcDriverListener);
        } else  {
            registerConnections();
        }
        
        // Create databases if they had been removed
        if (!DerbyDatabases.databaseExists("travel")) {
            SampleDatabaseCreator.createDatabase("travel", "startup/samples/travel.zip"); //NOI18N
        }

        if (!DerbyDatabases.databaseExists("vir")) {
            SampleDatabaseCreator.createDatabase("vir", "startup/samples/vir.zip"); //NOI18N
        }
    }
    
    private synchronized void registerConnections() {
        if (registered) {
            return;
        }
        
        JDBCDriver[] drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
        if (isMigration) {
            DatabaseSettingsImporter.getInstance().locateAndRegisterDrivers();
            DatabaseSettingsImporter.getInstance().locateAndRegisterConnections(true);
            if (drvsArray.length > 0) {
                SampleDatabaseCreator.createAll("travel", "travel", "travel", "TRAVEL", "startup/samples/travel.zip", true, "localhost", 1527);   //NOI18N    
                SampleDatabaseCreator.createAll("vir", "vir", "vir", "VIR", "startup/samples/vir.zip", true, "localhost", 1527);   //NOI18N              
            }
            return;
        }        

        // Register sample database
        if (drvsArray.length > 0) {
            if (ConnectionManager.getDefault().getConnection("jdbc:derby://localhost:1527/travel [travel on TRAVEL]") == null) {
                SampleDatabaseCreator.createAll("travel", "travel", "travel", "TRAVEL", "startup/samples/travel.zip", true, "localhost", 1527); //NOI18N
            }
            if (ConnectionManager.getDefault().getConnection("jdbc:derby://localhost:1527/vir [vir on VIR]") == null) {
                SampleDatabaseCreator.createAll("vir", "vir", "vir", "VIR", "startup/samples/vir.zip", true, "localhost", 1527); //NOI18N
            }
            
            registered = true;
            JDBCDriverManager.getDefault().removeDriverListener(jdbcDriverListener);                        
        }                
    }
}

  

