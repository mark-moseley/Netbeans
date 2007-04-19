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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui.actions;

import java.awt.Dialog;
import javax.swing.JButton;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class PluginManagerAction extends CallableSystemAction {
    
    public void performAction () {
        JButton close = new JButton ();
        close.setDefaultCapable(false);
        Mnemonics.setLocalizedText (close,NbBundle.getMessage (PluginManagerAction.class, "PluginManager_CloseButton_Name"));
        DialogDescriptor dd = new DialogDescriptor (
                                    new PluginManagerUI (close),
                                    NbBundle.getMessage (PluginManagerAction.class, "PluginManager_Panel_Name"),
                                    true, 
                                    new JButton[] { close },
                                    close,
                                    DialogDescriptor.DEFAULT_ALIGN,
                                    null,
                                    null /*final ActionListener bl*/);
        dd.setOptions (new Object [0]);
        
        Dialog d = DialogDisplayer.getDefault ().createDialog (dd);
        d.setVisible (true);
    }
    
    public String getName () {
        return NbBundle.getMessage (PluginManagerAction.class, "PluginManagerAction_Name");
    }
    
    protected void initialize () {
        super.initialize ();
        putValue ("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous () {
        return false;
    }
    
}
