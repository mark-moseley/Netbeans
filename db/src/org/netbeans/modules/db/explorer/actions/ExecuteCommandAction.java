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

package org.netbeans.modules.db.explorer.actions;

import java.util.*;

import org.openide.*;
import org.openide.nodes.*;

import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.dataview.*;

public class ExecuteCommandAction extends DatabaseAction {
    protected boolean enable(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return false;

        ConnectionNodeInfo info = (ConnectionNodeInfo)node.getCookie(ConnectionNodeInfo.class);
        if (info != null)
            return (info.getConnection() != null);
        
        return true;
    }

    public void performAction (Node[] activatedNodes) {
        Node node;

        if (activatedNodes != null && activatedNodes.length > 0) {
            try {
                node = activatedNodes[0];
                DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);

                DataViewWindow win = new DataViewWindow(info, ""); //NOI18N
                win.open();
                win.requestActive();
            } catch(Exception e) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(bundle().getString("DataViewFetchErrorPrefix") + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
            }
        }
    }
}
