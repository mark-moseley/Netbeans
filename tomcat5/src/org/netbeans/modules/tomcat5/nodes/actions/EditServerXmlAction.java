/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.tomcat5.nodes.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.nodes.TomcatInstanceNode;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.EditorCookie;
import java.io.File;

/**
 * Opens server.xml file in editor.
 *
 * @author  Stepan Herold
 */
public class EditServerXmlAction extends NodeAction {
    
    protected boolean enable(Node[] nodes) {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(SharedContextLogAction.class, "LBL_EditServerXmlAction"); // NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie != null) {
                cookie.editServerXml();
            }
        }
    }
}
