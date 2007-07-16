/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import org.netbeans.modules.db.explorer.DbUtilities;

import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import org.netbeans.lib.ddl.impl.Specification;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.dlg.CreateTableDialog;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableOwnerOperations;

public class CreateTableAction extends DatabaseAction {
    static final long serialVersionUID =-7008851466327604724L;
    
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;

        
        final DatabaseNodeInfo xnfo = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        final String nodeName = node.getName();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                try {
                    TableOwnerOperations nfo = (TableOwnerOperations) xnfo.getParent(nodename);
                    Specification spec = (Specification) xnfo.getSpecification();
                    CreateTableDialog dlg = new CreateTableDialog(spec, (DatabaseNodeInfo) nfo);
                    if (dlg.run())
                        try {
                            nfo.addTable(dlg.getTableName());
                        } catch ( DatabaseException de ) {
                            //PENDING
                        }
                } catch(Exception exc) {
                    DbUtilities.reportError(bundle().getString("ERR_UnableToCreateTable"), exc.getMessage());
                }
            }
        }, 0);       
    }
}
