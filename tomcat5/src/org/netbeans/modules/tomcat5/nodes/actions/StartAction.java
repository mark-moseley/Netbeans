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

package org.netbeans.modules.tomcat5.nodes.actions;

import org.netbeans.modules.tomcat5.nodes.TomcatWebModuleNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  Petr Pisl
 */
public class StartAction extends NodeAction {
    
    /** Creates a new instance of Undeploy */
    public StartAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] nodes) {
        TomcatWebModuleCookie cookie;
        for (int i=0; i<nodes.length; i++) {
            cookie = (TomcatWebModuleCookie)nodes[i].getCookie(TomcatWebModuleCookie.class);            
            if (cookie == null || cookie.isRunning())
                return false;

        }
         
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(StartAction.class, "LBL_StartAction"); //NOI18N
    }
    
    protected void performAction(org.openide.nodes.Node[] nodes) {
        
        for (int i=0; i<nodes.length; i++) {
            TomcatWebModuleCookie cookie = (TomcatWebModuleCookie)nodes[i].getCookie(TomcatWebModuleCookie.class);            
            if (cookie != null)
                cookie.start();

        }
    }
    
    
    protected boolean asynchronous() { return false; }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
