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
package org.netbeans.modules.sun.manager.jbi.actions;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Advanced action for a JBI Component.
 * 
 * @author jqian
 */
public class AdvancedAction extends NodeAction implements Presenter.Popup {
    
    public String getName() {
        return NbBundle.getMessage(AdvancedAction.class, "LBL_Advanced");  // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu(getName());
        
        ShutdownAction forceShutdownAction = 
                SystemAction.get(ShutdownAction.Force.class);
        forceShutdownAction.clearEnabledState(); // TMP FIX for #108106
        forceShutdownAction.setEnabled(forceShutdownAction.isEnabled());
        menu.add(forceShutdownAction);
        
        NodeAction forceUninstallAction = 
                SystemAction.get(UninstallAction.Force.class);
        forceUninstallAction.setEnabled(forceUninstallAction.isEnabled());
        menu.add(forceUninstallAction);
        
        return menu;
    }

    protected void performAction(Node[] activatedNodes) {
        ;
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
}
