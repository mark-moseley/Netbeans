/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.text.MessageFormat;

import org.openide.util.Utilities;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.DatabaseOption;
import org.netbeans.modules.db.explorer.driver.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverManager;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.nodes.RootNode;

public class DriverListNodeInfo extends DatabaseNodeInfo implements DriverOperations {
    static final long serialVersionUID =-7948529055260667590L;
    
    private JDBCDriverManager dm = JDBCDriverManager.getDefault();
    
    private final PropertyChangeListener connectionListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals("add") || event.getPropertyName().equals("remove")) { //NOI18N
                //PENDING
            }
        }
    };
    
    protected void initChildren(Vector children) throws DatabaseException {
        dm.addPropertyChangeListener(connectionListener);
        JDBCDriver[] drvs = dm.getDrivers();
        boolean win = Utilities.isWindows();
        String file;
        for (int i = 0; i < drvs.length; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < drvs[i].getURLs().length; j++) {
                if (j != 0)
                    sb.append(", "); //NOI18N
                file = drvs[i].getURLs()[j].getFile();
                if (win)
                    file = file.substring(1);
                sb.append(file);
            }
            DatabaseDriver drv = new DatabaseDriver(drvs[i].getName(), drvs[i].getClassName(), sb.toString());
            DriverNodeInfo chinfo = (DriverNodeInfo) DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.DRIVER);
            if (chinfo != null && drv != null) {
                chinfo.setDatabaseDriver(drv);
                children.add(chinfo);
            }
        }
    }

    /** Adds driver specified in drv into list.
    * Creates new node info and adds node into node children.
    */
    public void addDriver(DatabaseDriver drv) throws DatabaseException {
        DatabaseOption option = RootNode.getOption();
        Vector drvs = option.getAvailableDrivers();
        if (!drvs.contains(drv))
            drvs.add(drv);
        else {
            String message = MessageFormat.format(bundle.getString("EXC_DriverAlreadyExists"), new String[] {drv.toString()}); // NOI18N
            throw new DatabaseException(message);
        }

        DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
        DriverNodeInfo ninfo = (DriverNodeInfo)createNodeInfo(this, DatabaseNodeInfo.DRIVER);
        ninfo.setDatabaseDriver(drv);
        chld.createSubnode(ninfo, true);
    }
    
}
