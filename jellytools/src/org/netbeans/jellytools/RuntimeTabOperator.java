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

package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Runtime TopComponent.<p>
 * This operator is child of TopComponentOperator and thus it can be located
 * independently on Explorer (it can by found undocked or docked in other
 * NbFrame).<p>
 * Functionality related to Runtime tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getRootNode()).<p>
 * Example:<p>
 * <pre>
 *  RuntimeTabOperator tab = ExplorerOperator.invoke().runtimeTab();
 *  // or when Explorer is already invoked
 *  RuntimeTabOperator tab = new ExplorerOperator().runtimeTab();
 *  // or when tab is already (un)docked sowhere else
 *  RuntimeTabOperator tab = RuntimeTabOperator();
 *
 *  JTreeOperator tree = tab.tree();
 *  System.out.println(tab.getRootNode().getText());
 * </pre> */
public class RuntimeTabOperator extends TopComponentOperator {

    static final String RUNTIME_CAPTION = Bundle.getString("org.netbeans.core.Bundle", "UI/Runtime");
    
    private JTreeOperator _tree;
    
    /** Search for Runtime TopComponent through all IDE. */    
    public RuntimeTabOperator() {
        //find everywhere
        super(RUNTIME_CAPTION);
    }
    
    /** Search for RuntimeTopComponent inside given Container
     * @param contOper parent Container */    
    public RuntimeTabOperator(ContainerOperator contOper) {
        super(contOper, RUNTIME_CAPTION);
    }
    
    /** getter for Runtime JTreeOperator
     * @return JTreeOperator of Runtime tree */    
    public JTreeOperator tree() {
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** getter for RuntimeRootNode
     * @return RuntimeRootNode */    
    public Node getRootNode() {
        return new Node(tree(), "");
    }
}
