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
 *  
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModels;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob Englander
 */
public class ConnectionNode extends BaseNode {
    
    private static final String CONNECTEDICONBASE = "org/netbeans/modules/db/resources/connection.gif"; // NOI18N
    private static final String DISCONNECTEDICONBASE = "org/netbeans/modules/db/resources/connectionDisconnected.gif"; // NOI18N
    private static final String FOLDER = "Connection"; // NOI18N
    
    /** 
     * Create an instance of ConnectionNode.
     * 
     * @param dataLookup the lookup to use when creating node providers
     * @return the ConnectionNode instance
     */
    public static ConnectionNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ConnectionNode node = new ConnectionNode(dataLookup, provider);
        node.setup();
        return node;
    }
    
    // the connection
    private final DatabaseConnection connection;

    /**
     * Constructor
     * 
     * @param lookup the associated lookup
     */
    private ConnectionNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    protected void initialize() {
        // listen for change events
        connection.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("connectionComplete") || // NOI18N
                            evt.getPropertyName().equals("disconnected")) { // NOI18N
                        updateModel();
                    }
                }
            }
        );

        updateModel();
    }

    public DatabaseConnection getDatabaseConnection() {
        return connection;
    }
    
    private synchronized void updateModel() {
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    boolean connected = !connection.getConnector().isDisconnected();

                    if (connected) {
                        MetadataModel model = MetadataModels.createModel(connection.getConnection(), connection.getSchema());
                        connection.setMetadataModel(model);
                        refresh();

                    } else {
                        connection.setMetadataModel(null);
                        refresh();
                    }

                }
            }
        );
    }

    @Override
    public boolean canDestroy() {
        boolean result = true;
        
        Connection conn = connection.getJDBCConnection();
        if (conn != null) {
            try {
                result = conn.isClosed();
            } catch (SQLException e) {
                
            }
        }
        
        return result;
    }
    
    @Override
    public void destroy() {
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    try {
                        ConnectionList.getDefault().remove(connection);
                    } catch (DatabaseException e) {

                    }
                }
            }
        );
    }
    
    public String getName() {
        return connection.getName();
    }

    @Override
    public String getDisplayName() {
        return connection.getName();
    }
 
    public String getIconBase() {
        boolean disconnected = true;
        
        Connection c = connection.getConnection();
        if (c != null) {
            try {
                disconnected = c.isClosed();
            } catch (SQLException e) {
            }
        }
        
        if (disconnected) {
            return DISCONNECTEDICONBASE;
        }
        else {
            return CONNECTEDICONBASE;
        }
    }
}
