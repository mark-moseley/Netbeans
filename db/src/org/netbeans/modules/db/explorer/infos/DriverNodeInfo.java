/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.util.Vector;
import java.text.MessageFormat;

import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.nodes.RootNode;

public class DriverNodeInfo extends DatabaseNodeInfo {
        
    static final long serialVersionUID =6994829681095273161L;
    
    public DatabaseDriver getDatabaseDriver() {
        return (DatabaseDriver)get(DatabaseNodeInfo.DBDRIVER);
    }

    public void setDatabaseDriver(DatabaseDriver drv) {
        put(DatabaseNodeInfo.NAME, drv.getName());
        put(DatabaseNodeInfo.URL, drv.getURL());
        put(DatabaseNodeInfo.PREFIX, drv.getDatabasePrefix());
        put(DatabaseNodeInfo.ADAPTOR_CLASSNAME, drv.getDatabaseAdaptor());
        put(DatabaseNodeInfo.DBDRIVER, drv);
    }

    public void delete() throws IOException {
        try {
            DatabaseDriver drv = getDatabaseDriver();
            Vector drvs = RootNode.getOption().getAvailableDrivers();
            int idx = drvs.indexOf(drv);
            if (idx != -1)
                drvs.removeElementAt(idx);
            else {
                String message = MessageFormat.format(bundle.getString("EXC_DriverNotFound"), new String[] {drv.toString()}); // NOI18N
                throw new DatabaseException(message);
            }
            
            // refresh list of drivers after driver delete action
            getParent().refreshChildren();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public String getIconBase() {
        return (String) get("iconbaseprefered"); //NOI18N
//        return (String) get("iconbasepreferednotinstalled"); //NOI18N
    }

    public void setIconBase(String base) {
        put("iconbaseprefered", base); //NOI18N
//        put("iconbasepreferednotinstalled", base); //NOI18N
    }

}
