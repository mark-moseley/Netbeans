/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xsl.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import org.netbeans.api.xml.cookies.TransformableCookie;
import org.netbeans.modules.xml.core.actions.CollectXMLAction;

import org.netbeans.modules.xsl.XSLDataObject;
import org.netbeans.modules.xsl.transform.TransformPerformer;

/**
 * Perform Transform action on XML document.
 *
 * @author  Libor Kramolis
 */
public class TransformAction extends CookieAction implements CollectXMLAction.XMLAction {
    /** Serial Version UID */
    private static final long serialVersionUID = -640535981015250507L;

    
    /** */
    protected Class[] cookieClasses () {
        return new Class[] { TransformableCookie.class, XSLDataObject.class };
    }

    /** All selected nodes must be XML one to allow this action. */
    protected int mode () {
        return MODE_ALL;
    }


    /** Human presentable name. */
    public String getName() {
        return Util.THIS.getString ("NAME_transform_action");
    }

    /** Do not slow by any icon. */
    protected String iconResource () {
        return "org/netbeans/modules/xsl/resources/transformAction.gif"; // NOI18N
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (TransformAction.class);
    }


    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {
        TransformPerformer performer = new TransformPerformer (nodes);
        performer.perform();
    }

}
