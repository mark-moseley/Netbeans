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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.modules.db.explorer.dlg.AddDriverDialog;
import org.netbeans.modules.db.explorer.driver.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverManager;
import org.netbeans.modules.db.explorer.infos.DriverListNodeInfo;

public class AddDriverAction extends DatabaseAction {
    static final long serialVersionUID =-109193000951395612L;
    
    private Dialog dialog;
    
    public void performAction(Node[] activatedNodes) {
        final AddDriverDialog dlgPanel = new AddDriverDialog();
        
        final Node[] n = activatedNodes;
        
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == DialogDescriptor.OK_OPTION) {
                    String name = dlgPanel.getName();
                    List drvLoc = dlgPanel.getDriverLocation();
                    String drvClass = dlgPanel.getDriverClass();
                    
                    StringBuffer err = new StringBuffer();
                    if (drvLoc.size() < 1)
                        err.append(bundle.getString("AddDriverDialog_MissingFile")); //NOI18N
                    if (drvClass == null || drvClass.equals("")) {
                        if (err.length() > 0)
                            err.append(", "); //NOI18N

                        err.append(bundle.getString("AddDriverDialog_MissingClass")); //NOI18N
                    }
                    if (err.length() > 0) {
                        String message = MessageFormat.format(bundle.getString("AddDriverDialog_ErrorMessage"), new String[] {err.toString()}); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
                        
                        return;
                    }
                    
                    closeDialog();
                    
                    //create driver instance and save it in the XML format
                    if (name == null || name.equals(""))
                        name = drvClass;
                    
                    try {
                        JDBCDriverManager.getDefault().addDriver(new JDBCDriver(name, drvClass, (URL[]) drvLoc.toArray(new URL[drvLoc.size()])));
                    } catch (IOException exc) {
                        //PENDING
                    }                    
                }
            }
        };

        DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, bundle.getString("AddDriverDialogTitle"), true, actionListener); //NOI18N
        Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
        descriptor.setClosingOptions(closingOptions);
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.show();
    }
    
    private void closeDialog() {
        if (dialog != null)
            dialog.dispose();
    }
}
