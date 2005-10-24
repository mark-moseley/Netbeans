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

import org.openide.util.*;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class StatusInvisibleAction extends StatusActionBase {
    /**
     *
     *
     */
    protected String getDisplayName() {
        return NbBundle.getMessage(StatusInvisibleAction.class, "LBL_ChangeStatusAction_INVISIBLE"); // NOI18N
    }

    /**
     *
     *
     */
    protected int getStatus() {
        return CollabPrincipal.STATUS_INVISIBLE;
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/offline_png.gif"; // NOI18N
    }

    /**
     *
     *
     */
    protected void setSessionStatus(CollabSession session) {
        try {
            session.setInvisibleToAll();
            session.getUserPrincipal().setStatus(CollabPrincipal.STATUS_INVISIBLE);
        } catch (CollabException e) {
            Debug.debugNotify(e);
        }
    }
}
