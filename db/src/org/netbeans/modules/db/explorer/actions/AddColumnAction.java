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

package org.netbeans.modules.db.explorer.actions;

import java.text.MessageFormat;

import org.openide.*;
import org.openide.nodes.*;

import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.dlg.AddTableColumnDialog;

public class AddColumnAction extends DatabaseAction {
    
    static final long serialVersionUID =5894518352294344657L;
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1)
            return true;
        else
            return false;
    }
    
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return;

        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            TableNodeInfo nfo = (TableNodeInfo)info.getParent(nodename);
            AddTableColumnDialog dlg = new AddTableColumnDialog((Specification)nfo.getSpecification(), nfo);
            if (dlg.run())
                nfo.addColumn(dlg.getColumnName());
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToAddColumn"), new String[] {exc.getMessage()}); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
