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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import org.openide.nodes.Node;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;

import org.netbeans.modules.form.*;


/** Action that starts in-place editing of selected component in FormDesigner.
 */
public class InPlaceEditAction extends NodeAction {

    private static String name;

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
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            if (metacomp != null) {
                FormDesigner designer = FormEditor.getFormDesigner(metacomp.getFormModel());
                if (designer != null)
                    designer.startInPlaceEditing(metacomp);
            }
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
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
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            if (metacomp != null) {
                FormDesigner designer = FormEditor.getFormDesigner(metacomp.getFormModel());
                if (designer != null)
                    return designer.isEditableInPlace(metacomp);
            }
        }
        return false;
    }

    /**
     * human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(InPlaceEditAction.class)
                     .getString("ACT_InPlaceEdit"); // NOI18N
        return name;
    }

    /**
     * Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.quickref"); // NOI18N
    }
}
