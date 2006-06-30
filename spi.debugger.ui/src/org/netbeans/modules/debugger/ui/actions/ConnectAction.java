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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.debugger.ui.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
* Connects debugger to some currently running VM.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class ConnectAction extends AbstractAction {
    
    Dialog dialog;

    
    public ConnectAction () {
        putValue (
            Action.NAME, 
            NbBundle.getMessage (
                ConnectAction.class, 
                "CTL_Connect"
            )
        );
        putValue (
            Action.SMALL_ICON, 
            Utils.getIcon (
                "org/netbeans/modules/debugger/resources/actions/Attach" // NOI18N
            )
        );
        putValue (
            "iconBase", // NOI18N
            "org/netbeans/modules/debugger/resources/actions/Attach.gif" // NOI18N
        );
    }
    
    public void actionPerformed (ActionEvent evt) {
        final ConnectorPanel cp = new ConnectorPanel ();
        DialogDescriptor descr = new DialogDescriptor (
            cp,
            NbBundle.getMessage (ConnectAction.class, "CTL_Connect_to_running_process"),
            true, // modal
            new ConnectListener (cp)
        );
        (dialog = DialogDisplayer.getDefault ().createDialog (descr)).setVisible(true);
    }


    // innerclasses ............................................................
    private class ConnectListener implements ActionListener {
        
        ConnectorPanel connectorPanel;
        
        ConnectListener (ConnectorPanel connectorPanel) {
            this.connectorPanel = connectorPanel;
        }
        
        public void actionPerformed (ActionEvent e) {
            if (e.getSource ().equals (DialogDescriptor.OK_OPTION)) {
                connectorPanel.ok ();
            } else
            if (e.getSource ().equals (DialogDescriptor.CANCEL_OPTION)) {
                connectorPanel.cancel ();
            }
            dialog.setVisible (false);
            dialog.dispose ();
        }
    }
}


