/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.schema.actions;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import org.netbeans.modules.xml.core.actions.*;

import org.netbeans.api.xml.cookies.*;

/**
 * Validates XML Schema file sending results to output window.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ValidateSchemaAction extends CookieAction implements CollectXMLAction.XMLAction {

    /** Serial Version UID */
    private static final long serialVersionUID = -8772119268950444993L;

    
    /** Be hooked on XMLDataObjectLook narking XML nodes. */
    protected Class[] cookieClasses () {
        return new Class[] { ValidateXMLCookie.class };
    }

    /** All selected nodes must be XML one to allow this action */
    protected int mode () {
        return MODE_ALL;
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {

        if (nodes == null) return;

        InputOutputReporter console = new InputOutputReporter();
        boolean allValid = true;
        
        for (int i = 0; i<nodes.length; i++) {
            Node node = nodes[i];
            ValidateXMLCookie cake = (ValidateXMLCookie) node.getCookie(ValidateXMLCookie.class);
            if (cake == null) continue;
            console.setNode(node); //??? how can console determine which editor to highlight
            allValid &= cake.validateXML(console);
        }
        
        if (allValid) {
            console.message(Util.THIS.getString("MSG_Schema_valid_end"));
        } else {
            console.message(Util.THIS.getString("MSG_Schema_invalid_end"));
        }
        console.moveToFront(true);        
    }

    /** Human presentable name. */
    public String getName() {
        return Util.THIS.getString("NAME_Validate_Schema");
    }

    /** Do not slow by any icon. */
    protected String iconResource () {
        return null;
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (ValidateSchemaAction.class);
    }

}
