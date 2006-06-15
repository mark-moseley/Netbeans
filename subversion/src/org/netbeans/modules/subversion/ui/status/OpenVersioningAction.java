/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.status;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;

/**
 * Open the Subversion view. It focuses recently opened
 * view unless it's not initialized yet. For uninitialized
 * view it behaves like StatusProjectsAction without
 * on-open refresh.
 * 
 * @author Petr Kuzel
 */
public class OpenVersioningAction extends ShowAllChangesAction {
    
    public OpenVersioningAction() {
        putValue("noIconInMenu", null); // NOI18N        
//        setIcon(new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/versioning/system/cvss/resources/icons/versioning-view.png", true)));  // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(OpenVersioningAction.class, "CTL_MenuItem_OpenVersioning"); // NOI18N
    }

    /**
     * Window/Versioning should be always enabled.
     * 
     * @return true
     */ 
    public boolean isEnabled() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenVersioningAction.class);
    }

    public void actionPerformed(ActionEvent e) {
        SvnVersioningTopComponent stc = SvnVersioningTopComponent.getInstance();
        if (stc.hasContext() == false) {
            super.actionPerformed(e);
        } else {
            stc.open();
            stc.requestActive();
        }
    }

    protected boolean shouldPostRefresh() {
        return false;
    }
}
