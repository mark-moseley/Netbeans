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

package org.netbeans.modules.debugger.delegatingview;

import org.netbeans.modules.debugger.support.DebuggerModule;
import org.netbeans.modules.debugger.support.nodes.ExplorerViewSupport;
import org.netbeans.modules.debugger.support.nodes.DebuggerNode;

import javax.swing.ImageIcon;
import java.awt.Image;

public class VariablesView extends ExplorerViewSupport {

    public VariablesView () {
        super (false);
    }

    public String getRootNode () {
        return DebuggerModule.LOCALS_ROOT_NODE;
    }
    
    public String getName () {
        return DebuggerNode.getLocalizedString ("CTL_Variables_view");
    }

    public Image getIcon () {
        return new ImageIcon (VariablesView.class.getResource (
            "/org/netbeans/modules/debugger/resources/variables.gif" // NOI18N
        )).getImage ();
    }
}
