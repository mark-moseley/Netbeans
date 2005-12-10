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
package org.netbeans.modules.collab.ui.actions;

import java.awt.event.*;

import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.collab.ui.*;
import com.sun.collablet.CollabManager;

/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class ShowCollabExplorerAction extends SystemAction {
    public boolean isEnabled() {
        return true;
    }

    public String getName() {
        return NbBundle.getMessage(ShowCollabExplorerAction.class, "LBL_ShowCollabExplorerAction_Name");
    }

    protected String iconResource() {
        return "org/netbeans/modules/collab/core/resources/collab_png.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        CollabExplorerPanel panel = CollabExplorerPanel.getInstance();

        // Show the appropriate panel.  We do this here, because the circum-
        // stances under which open() will be called on the panel are not
        // always consistent.
        if ((CollabManager.getDefault() != null) && (CollabManager.getDefault().getSessions().length > 0)) {
            panel.showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
        } else {
            panel.showComponent(CollabExplorerPanel.COMPONENT_LOGIN);
        }

        panel.open(null);
        panel.requestActive();
    }
}
