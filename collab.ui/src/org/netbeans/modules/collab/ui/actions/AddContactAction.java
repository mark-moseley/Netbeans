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

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ContactGroupCookie;


/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class AddContactAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(AddContactAction.class, "LBL_AddContactAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/group_png.gif"; // NOI18N
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
            ContactGroupCookie groupCookie = (ContactGroupCookie) nodes[0].getCookie(ContactGroupCookie.class);

            if ((groupCookie == null) && (nodes[0].getParentNode() != null)) {
                groupCookie = (ContactGroupCookie) nodes[0].getParentNode().getCookie(ContactGroupCookie.class);
            }

            AddContactForm form = null;

            if (groupCookie != null) {
                form = new AddContactForm(sessionCookie.getCollabSession(), groupCookie.getContactGroup());
            } else {
                form = new AddContactForm(sessionCookie.getCollabSession());
            }

            form.addContacts();
        }
    }
}
