/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.gui.actions;

import org.netbeans.test.editor.app.core.Test;
import org.netbeans.test.editor.app.core.TestNode;
import org.netbeans.test.editor.app.gui.actions.TreeNodeAction;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestDeleteAction extends TreeNodeAction {
    
    /** Creates new TestRenameAction */
    public TestDeleteAction() {
    }
    
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        TestNode n;
        for (int i=0;i < activatedNodes.length;i++) {
            n=(TestNode)(activatedNodes[i].getTestNode());
            if (n instanceof Test) {
                return false;
            }
        }
        return true;
    }
    
    public void performAction(TestNodeDelegate[] activatedNodes) {
        TestNode[] n=new TestNode[activatedNodes.length];
        
        for (int i=0;i < activatedNodes.length;i++) {
            n[i]=(TestNode)(activatedNodes[i].getTestNode());
        }
        n[0].getOwner().removeNodes(n);
    }
    
    public String getHelpCtx() {
        return "Delete selected nodes.";
    }
    
    public String getName() {
        return "Delete";
    }
    
}
