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

package org.netbeans.modules.db.explorer.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.util.RequestProcessor;

public class DriverNode extends LeafNode implements PropertyChangeListener {
    
    public void setInfo(DatabaseNodeInfo info) {
        super.setInfo(info);
        DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);
        if (drv != null) {
            info.put(DatabaseNodeInfo.NAME, drv.getName());
            info.put(DatabaseNodeInfo.URL, drv.getURL());
            info.put(DatabaseNodeInfo.ADAPTOR_CLASSNAME, drv.getDatabaseAdaptor());
            info.addDriverListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        DatabaseNodeInfo info = getInfo();
        String pname = evt.getPropertyName();
        Object newval = evt.getNewValue();
        DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);
        if (pname.equals(DatabaseNodeInfo.NAME)) drv.setName((String)newval);
        if (pname.equals(DatabaseNodeInfo.URL)) drv.setURL((String)newval);
        if (pname.equals(DatabaseNodeInfo.PREFIX)) drv.setDatabasePrefix((String)newval);
        if (pname.equals(DatabaseNodeInfo.ADAPTOR_CLASSNAME)) drv.setDatabaseAdaptor((String)newval);
    }
    
    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Driver"); //NOI18N
    }

    public void destroy() throws IOException {
        getInfo().delete();
        DatabaseNodeInfo parent = getInfo().getParent();
        super.destroy();
        try{
            parent.refreshChildren();
        } catch (Exception ex){
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }

}
