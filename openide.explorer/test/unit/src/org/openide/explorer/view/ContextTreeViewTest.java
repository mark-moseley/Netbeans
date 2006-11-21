/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for class ContextTreeViewTest
 */
public class ContextTreeViewTest extends NbTestCase {

    private static final int NO_OF_NODES = 3;
    
    
    public ContextTreeViewTest(String name) {
        super(name);
    }
    
    public void testLeafNodeReallyNotDisplayed() throws Throwable {
        final AbstractNode root = new AbstractNode(new Children.Array());
        root.setName("test root");
        
        
        
        root.getChildren().add(new Node[] {
            createLeaf("kuk"),
            createLeaf("huk"),
        });
        
        Panel p = new Panel();
        p.getExplorerManager().setRootContext(root);
        
        ContextTreeView ctv = new ContextTreeView();
        p.add(BorderLayout.CENTER, ctv);
        
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
        f.getContentPane().add(BorderLayout.CENTER, p);
        f.setVisible(true);
        
        final JTree tree = ctv.tree;
        
        class AWTTst implements Runnable {
            public void run() {
                // wait a while till the frame is realized and ctv.addNotify called
                Object r = tree.getModel().getRoot();
                assertEquals("There is root", Visualizer.findVisualizer(root), r);
                
                int cnt = tree.getModel().getChildCount(r);
                if (cnt != 0) {
                    fail("Should be zero " + cnt + " but there was:  " +
                            tree.getModel().getChild(r, 0) + " and " +
                            tree.getModel().getChild(r, 1)
                            );
                }
                assertEquals("No children as they are leaves", 0, cnt);
            }
        }
        AWTTst awt = new AWTTst();
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    private static Node createLeaf(String name) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }
    
    private static class Panel extends JPanel
            implements ExplorerManager.Provider {
        private ExplorerManager em = new ExplorerManager();
        
        public ExplorerManager getExplorerManager() {
            return em;
        }
    }
}
