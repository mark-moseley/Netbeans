/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for class BeanTreeViewTest
 */
public class BeanTreeViewTest extends NbTestCase {
    
    private static final int NO_OF_NODES = 3;
    
    
    public BeanTreeViewTest(String name) {
        super(name);
    }
    
    public void testFirstChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("foo");
    }
    public void testSecondChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("bar");
    }
    public void testThirdChildRemovalCausesSelectionOfSibling() throws Throwable {
        doChildRemovalTest("bla");
    }

    private static Object holder;
    
    private void doChildRemovalTest(final String name) throws Throwable {

        class AWTTst implements Runnable {

            AbstractNode root = new AbstractNode(new Children.Array());
            Node[] children = {
                createLeaf("foo"),
                createLeaf("bar"),
                createLeaf("bla")
            };
            Panel p = new Panel();
            BeanTreeView btv = new BeanTreeView();
            JFrame f = new JFrame();
            JTree tree = btv.tree;
            Node operateOn;

            {
                root.setName("test root");
                root.getChildren().add(children);
                p.getExplorerManager().setRootContext(root);
                p.add(BorderLayout.CENTER, btv);
                f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
                f.getContentPane().add(BorderLayout.CENTER, p);
                f.setVisible(true);
            }

            public void run() {

                for (int i = 0;; i++) {
                    if (name.equals(children[i].getName())) {
                        // this should select a sibling of the removed node
                        operateOn = children[i];
                        break;
                    }
                }

                try {
                    p.getExplorerManager().setSelectedNodes(new Node[]{operateOn});
                } catch (PropertyVetoException e) {
                    fail("Unexpected PropertyVetoException from ExplorerManager.setSelectedNodes()");
                }

                TreePath[] paths = tree.getSelectionPaths();
                assertNotNull("Before removal: one node should be selected, but there are none.", paths);
                assertEquals("Before removal: one node should be selected, but there are none.", 1, paths.length);
                assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(paths[0].getLastPathComponent()));
                assertEquals("Before removal: one node should be selected, but there are none.", operateOn, Visualizer.findNode(tree.getAnchorSelectionPath().getLastPathComponent()));

                // this should select a sibling of the removed node
                root.getChildren().remove(new Node[]{operateOn});
                assertNotNull("After removal: one node should be selected, but there are none.", tree.getSelectionPath());
                children = null;
            }

            public void tryGc() {
                WeakReference<Node> wref = new WeakReference<Node>(operateOn);
                operateOn = null;
                assertGC("Node should be released.", wref);    
            }
        }
        AWTTst awt = new AWTTst();
        holder = awt;
        try {
            SwingUtilities.invokeAndWait(awt);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        awt.tryGc();
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
