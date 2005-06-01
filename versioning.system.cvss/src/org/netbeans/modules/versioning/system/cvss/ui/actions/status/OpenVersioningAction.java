/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.status;

import org.netbeans.modules.versioning.system.cvss.ui.syncview.CvsSynchronizeTopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Open the Versioning view. It focuses recently opened
 * view unless it's not initialized yet. For uninitialized
 * view it behaves like StatusProjectsAction without
 * on-open refresh.
 * 
 * @author Petr Kuzel
 */
public class OpenVersioningAction extends StatusProjectsAction {
    
    private static final ResourceBundle loc = NbBundle.getBundle(OpenVersioningAction.class);

    public OpenVersioningAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N        
    }

    public String getName() {
        return loc.getString("BK0001");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenVersioningAction.class);
    }

    public void actionPerformed(ActionEvent e) {
        CvsSynchronizeTopComponent stc = CvsSynchronizeTopComponent.getInstance();
        if (stc.hasRoots() == false) {
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
