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

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.netbeans.modules.form.*;

/** CustomizeLayout action - enabled on RADContainerNodes and RADLayoutNodes.
 *
 * @author   Ian Formanek
 */

public class CustomizeLayoutAction extends CookieAction {

    private static String name;

    /** @return the mode of action. Possible values are disjunctions of MODE_XXX
     * constants. */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /** Creates new set of classes that are tested by the cookie.
     *
     * @return list of classes the that the cookie tests
     */
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(CustomizeLayoutAction.class)
                     .getString("ACT_CustomizeLayout"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
        RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualContainer) {
                Node layoutNode = ((RADVisualContainer)metacomp)
                                      .getLayoutNodeReference();
                if (layoutNode != null && layoutNode.hasCustomizer())
                    NodeOperation.getDefault().customize(layoutNode);
            }
        }
    }

    /*
     * In this method the enable / disable action logic can be defined.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if (metacomp instanceof RADVisualContainer) {
                    Node layoutNode = ((RADVisualContainer)metacomp)
                                      .getLayoutNodeReference();
                    return layoutNode != null && layoutNode.hasCustomizer();
                }
            }
        }
        return false;
    }
}
