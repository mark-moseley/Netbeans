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
package org.netbeans.modules.db.explorer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.explorer.infos.DriverNodeInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExtendedDelete;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.explorer.ExtendedDelete.class)
public class DriverExtendedDeleteImpl implements ExtendedDelete {

    public DriverExtendedDeleteImpl() {
    }

    public boolean delete(Node[] nodes) throws IOException {
        JDBCDriver[] jdbcDrivers = getJDBCDrivers(nodes);
        if (jdbcDrivers == null) {
            return false;
        }
        DatabaseConnection firstConnection = findFirstConnection(jdbcDrivers);
        if (firstConnection == null) {
            return false;
        }
        if (!canDeleteDrivers(jdbcDrivers, firstConnection)) {
            return true;
        }
        for (int i = 0; i < nodes.length; i++) {
            try {
                nodes[i].destroy();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return true;
    }

    /**
     * Returns the drivers represented by the given nodes,
     * or null if not all nodes represent drivers.
     */
    private static JDBCDriver[] getJDBCDrivers(Node[] nodes) {
        JDBCDriver[] jdbcDrivers = new JDBCDriver[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            DriverNodeInfo driverInfo = (DriverNodeInfo)nodes[i].getLookup().lookup(DriverNodeInfo.class);
            if (driverInfo == null) {
                return null;
            }
            jdbcDrivers[i] = driverInfo.getJDBCDriver();
            if (jdbcDrivers[i] == null) {
                return null;
            }
        }
        return jdbcDrivers;
    }

    /**
     * Returns true if at least one of the given drivers is used by
     * a registered connection.
     */
    private static DatabaseConnection findFirstConnection(JDBCDriver[] jdbcDrivers) {
        DatabaseConnection[] dbconns = ConnectionList.getDefault().getConnections();
        for (int i = 0; i < jdbcDrivers.length; i++) {
            // first try to find connections which refer to this driver by name
            for (int j = 0; j < dbconns.length; j++) {
                if (jdbcDrivers[i].getName().equals(dbconns[j].getDriverName())) {
                    return dbconns[j];
                }
            }
            // ... no such connection, but the driver might still be referred to by class
            // (e.g., after removing the driver for a certain class and registering it back
            // with the same class, but a different name)
            for (int j = 0; j < dbconns.length; j++) {
                if (jdbcDrivers[i].getClassName().equals(dbconns[j].getDriver())) {
                    return dbconns[j];
                }
            }
        }
        return null;
    }

    private static boolean canDeleteDrivers(JDBCDriver[] jdbcDrivers, DatabaseConnection firstConnection) {
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); // NOI18N
        String message, title;
        if (jdbcDrivers.length == 1) {
            String format = bundle.getString("MSG_ConfirmDeleteDriver"); // NOI18N
            message = MessageFormat.format(format, new Object[] { jdbcDrivers[0].getDisplayName(), firstConnection.getDatabaseConnection().getDisplayName() });
            title = bundle.getString("MSG_ConfirmDeleteDriverTitle"); // NOI18N
        } else {
            String format = bundle.getString("MSG_ConfirmDeleteDrivers"); // NOI18N
            message = MessageFormat.format(format, new Object[] { new Integer(jdbcDrivers.length) });
            title = bundle.getString("MSG_ConfirmDeleteDriversTitle"); // NOI18N
        }
        return DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION)) == NotifyDescriptor.YES_OPTION;
    }
}
