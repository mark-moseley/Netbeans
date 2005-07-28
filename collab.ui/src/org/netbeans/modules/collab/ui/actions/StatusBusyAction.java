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

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.awt.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;


/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class StatusBusyAction extends StatusActionBase {
    /**
     *
     *
     */
    protected String getDisplayName() {
        return NbBundle.getMessage(StatusBusyAction.class, "LBL_ChangeStatusAction_BUSY"); // NOI18N
    }

    /**
     *
     *
     */
    protected int getStatus() {
        return CollabPrincipal.STATUS_BUSY;
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/busy_png.gif"; // NOI18N
    }

    /**
     *
     *
     */
    protected void setSessionStatus(CollabSession session) {
        try {
            session.setVisibleToAll();
            session.publishStatus(
                CollabPrincipal.STATUS_BUSY, NbBundle.getMessage(StatusBusyAction.class, "LBL_ChangeStatusAction_BUSY")
            ); // NOI18N
        } catch (CollabException e) {
            Debug.debugNotify(e);
        }
    }
}
