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

import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import org.netbeans.modules.collab.ui.*;

/**
 *
 *
 */
public class AddContactGroupAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(AddContactGroupAction.class, "LBL_AddContactGroupAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     *
     *
     */
    protected Class[] cookieClasses() {
        return new Class[] { CollabSessionCookie.class };
    }

    /**
     *
     *
     */
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[0].getCookie(CollabSessionCookie.class);

        if (sessionCookie != null) {
            AddContactGroupForm form = new AddContactGroupForm(sessionCookie.getCollabSession());
            form.addContactGroup();
        }
    }
}
