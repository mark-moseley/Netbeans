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
package org.netbeans.modules.xml.tools.actions;

import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.loaders.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.*;
import org.netbeans.modules.xml.core.actions.*;

import org.netbeans.api.xml.cookies.*;
import org.openide.util.RequestProcessor;


/**
 * checks DTD file sending results to output window.
 *
 * @author  Petr Kuzel
 * @version 1.0
 * @deprecated To be eliminated once a API CheckXMLAction will be introduced
 */
public class CheckDTDAction extends CookieAction implements CollectDTDAction.DTDAction {

    /** serialVersionUID */
    private static final long serialVersionUID = -8772119268950444992L;

    /** Be hooked on XMLDataObjectLook narking XML nodes. */
    protected Class[] cookieClasses () {
        return new Class[] { CheckXMLCookie.class };
    }

    /** All selected nodes must be XML one to allow this action */
    protected int mode () {
        return MODE_ALL;
    }

    /** Check all selected nodes. */
    protected void performAction (Node[] nodes) {

        if (nodes == null) return;
        
        RequestProcessor.postRequest(
               new CheckDTDAction.RunAction (nodes));

    }
    
    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name. */
    public String getName() {
        return Util.THIS.getString("NAME_Validate_DTD");
    }

    protected String iconResource () {
        return "org/netbeans/modules/xml/tools/resources/checkDTDAction.gif";   // NOI18N
    }

    /** Provide accurate help. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CheckDTDAction.class);
    }

    private class RunAction implements Runnable{
        private Node[] nodes;

        RunAction (Node[] nodes){
            this.nodes = nodes;
        }

        public void run() {
            InputOutputReporter console = new InputOutputReporter();
            
            console.message(Util.THIS.getString("MSG_DTD_valid_start"));
            console.moveToFront();
            
            for (int i = 0; i<nodes.length; i++) {
                Node node = nodes[i];
                CheckXMLCookie cake = (CheckXMLCookie) node.getCookie(CheckXMLCookie.class);
                if (cake == null) continue;
                console.setNode(node); //??? how can console determine which editor to highlight
                cake.checkXML(console);
            }

            console.message(Util.THIS.getString("MSG_DTD_valid_end"));
            console.moveToFront();
       }
    }
}
