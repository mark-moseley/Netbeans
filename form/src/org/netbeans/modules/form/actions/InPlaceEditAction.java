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

package org.netbeans.modules.form.actions;

import org.openide.nodes.Node;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;


/** Action that starts in-place editing of selected component in FormDesigner.
 */
public class InPlaceEditAction extends NodeAction {

    /**
    * Perform the action based on the currently activated nodes.
    * Note that if the source of the event triggering this action was itself
    * a node, that node will be the sole argument to this method, rather
    * than the activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    */
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = (RADComponentCookie)activatedNodes[0]
                                            .getCookie(RADComponentCookie.class);
            RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            if (metacomp != null)
                metacomp.getFormModel().getFormDesigner().startInPlaceEditing(metacomp);
        }
    }

    /**
    * Test whether the action should be enabled based
    * on the currently activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    * @return <code>true</code> to be enabled, <code>false</code> to be disabled
    */
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = (RADComponentCookie)activatedNodes[0]
                                            .getCookie(RADComponentCookie.class);
            RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            if (metacomp != null)
                return metacomp.getFormModel().getFormDesigner().isEditableInPlace(metacomp);
        }
        return false;
    }

    /**
     * human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getBundle(InPlaceEditAction.class).getString("ACT_InPlaceEdit");
    }

    /**
     * Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(InPlaceEditAction.class);
    }
}
