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

import org.netbeans.test.editor.app.core.*;
import org.netbeans.test.editor.app.gui.tree.TestNodeDelegate;

/**
 *
 * @author  ehucka
 * @version
 */
public class TestDownAction extends TreeNodeAction {
    
    /** Creates new TestDownAction */
    public TestDownAction() {
    }
    
    /** Test whether the action should be enabled based
     * on the currently activated nodes.
     *
     * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
     * @return <code>true</code> to be enabled, <code>false</code> to be disabled
     */
    public boolean enable(TestNodeDelegate[] activatedNodes) {
        if (activatedNodes.length == 0) return false;
        if (activatedNodes.length > 1) return false;
        TestNodeDelegate n = activatedNodes[0];
        if (n.getTestNode() instanceof Test) return false;        
        //is it group??
        TestGroup g;
        g=n.getTestNode().owner;
        if (g != null) {
            if (g.get(g.getChildCount()-1) == n.getTestNode())
                return false;
        }
        return true;
    }
    
    /** Perform the action based on the currently activated nodes.
     * Note that if the source of the event triggering this action was itself
     * a node, that node will be the sole argument to this method, rather
     * than the activated nodes.
     *
     * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
     */
    public void performAction(TestNodeDelegate[] activatedNodes) {
        TestNodeDelegate n = (TestNodeDelegate)(activatedNodes[0]);
        if (n == null) return;
        TestNodeDelegate own = (TestNodeDelegate)(n.getParent());
        ((TestGroup)(own.getTestNode())).downNode(n.getTestNode());
    }
    
    /** Get a help context for the action.
     * @return the help context for this action
     */
    public String getHelpCtx() {
        return "Move selected node down";
    }
    
    /** Get a human presentable name of the action.
     * This may be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return "Down shift";
    }
    
}
