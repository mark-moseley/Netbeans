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

package org.netbeans.api.db.explorer.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.util.DataComboBoxModel;
import org.netbeans.modules.db.util.DataComboBoxSupport;
import org.openide.util.NbBundle;

/**
 * This class contains utility methods for working with and/or displaying
 * database connections in the UI. Currently it provides a method for
 * populating a combo box with the list of database connections from
 * {@link ConnectionManager}.
 *
 * @author Andrei Badea
 *
 * @since 1.18
 */
public final class DatabaseExplorerUIs {

    private DatabaseExplorerUIs() {
    }

    /**
     * Populates and manages the contents of the passed combo box. The combo box
     * contents consists of the database connections defined in
     * the passes instance of {@link ConnectionManager} and a Add Database Connection
     * item which displays the New Database Connection dialog when selected.
     *
     * <p>This method may cause the replacement of the combo box model,
     * thus the caller is recommended to register a
     * {@link java.beans.PropertyChangeListener} on the combo box when
     * it needs to check the combo box content when it changes.</p>
     *
     * @param comboBox combo box to be filled with the database connections.
     */
    public static void connect(JComboBox comboBox, ConnectionManager connectionManager) {
        DataComboBoxSupport.connect(comboBox, new ConnectionDataComboBoxModel(connectionManager));
    }

    private static final class ConnectionDataComboBoxModel implements DataComboBoxModel {

        private final ConnectionManager connectionManager;
        private final ConnectionComboBoxModel comboBoxModel;

        public ConnectionDataComboBoxModel(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            this.comboBoxModel = new ConnectionComboBoxModel(connectionManager);
        }

        public String getItemTooltipText(Object item) {
            return ((DatabaseConnection)item).toString();
        }

        public String getItemDisplayName(Object item) {
            return ((DatabaseConnection)item).getDisplayName();
        }

        public void newItemActionPerformed() {
            Set oldConnections = new HashSet(Arrays.asList(connectionManager.getConnections()));
            connectionManager.showAddConnectionDialog(null);

            // try to find the new connection
            DatabaseConnection[] newConnections = connectionManager.getConnections();
            if (newConnections.length == oldConnections.size()) {
                // no new connection, so...
                return;
            }
            for (int i = 0; i < newConnections.length; i++) {
                if (!oldConnections.contains(newConnections[i])) {
                    comboBoxModel.addSelectedConnection(newConnections[i]);
                    break;
                }
            }
        }

        public String getNewItemDisplayName() {
            return NbBundle.getMessage(DatabaseExplorerUIs.class, "LBL_NewDbConnection");
        }

        public ComboBoxModel getListModel() {
            return comboBoxModel;
        }
    }

    private static final class ConnectionComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private final ConnectionManager connectionManager;
        private final List connectionList; // must be ArrayList

        private Object selectedItem; // can be anything, not just a database connection

        public ConnectionComboBoxModel(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;

            connectionList = new ArrayList();
            connectionList.addAll(Arrays.asList(connectionManager.getConnections()));
            Collections.sort(connectionList, new ConnectionComparator());
        }

        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }

        public Object getElementAt(int index) {
            return connectionList.get(index);
        }

        public int getSize() {
            return connectionList.size();
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

        public void addSelectedConnection(DatabaseConnection dbconn) {
            selectedItem = dbconn;
            connectionList.add(dbconn);
            Collections.sort(connectionList, new ConnectionComparator());
            fireContentsChanged(this, 0, connectionList.size());
        }
    }

    private static final class ConnectionComparator implements Comparator {

        public boolean equals(Object that) {
            return that instanceof ConnectionComparator;
        }

        public int compare(Object dbconn1, Object dbconn2) {
            if (dbconn1 == null) {
                return dbconn2 == null ? 0 : -1;
            } else {
                if (dbconn2 == null) {
                    return 1;
                }
            }

            String dispName1 = ((DatabaseConnection)dbconn1).getDisplayName();
            String dispName2 = ((DatabaseConnection)dbconn2).getDisplayName();
            if (dispName1 == null) {
                return dispName2 == null ? 0 : -1;
            } else {
                return dispName2 == null ? 1 : dispName1.compareToIgnoreCase(dispName2);
            }
        }
    }
}
