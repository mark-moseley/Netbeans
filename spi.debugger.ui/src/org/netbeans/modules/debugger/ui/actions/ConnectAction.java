/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.debugger.ui.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
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
    }
    
    public void actionPerformed (ActionEvent evt) {
        final ConnectorPanel cp = new ConnectorPanel ();
        DialogDescriptor descr = new DialogDescriptor (
            cp,
            NbBundle.getBundle (ConnectAction.class).getString 
                ("CTL_Connect_to_running_process"),
            true, // modal
            new ConnectListener (cp)
        );
        descr.setHelpCtx (new HelpCtx ("debug.attachvm.window")); // NOI18N
//            (d [0] = TopManager.getDefault ().createDialog (descr)).show ();
        (dialog = DialogDisplayer.getDefault ().createDialog (descr)).setVisible(true);
    }


    // innerclasses ............................................................
    private class ConnectListener implements ActionListener {
        
        ConnectorPanel connectorPanel;
        
        ConnectListener (ConnectorPanel connectorPanel) {
            this.connectorPanel = connectorPanel;
        }
        
        public void actionPerformed (ActionEvent e) {
            boolean tryAgain = false;
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


