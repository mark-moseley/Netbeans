/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;


/** Tests for TreeTableView.
 *
 * @author  Dafe Simonek
 */
public class TTVTest extends NbTestCase {

    private TreeTableView ttv;
    private ExplorerPanel ep;
    private NodeHolderProperty[] props;
    private NodeStructure nodeStructure;
    private WeakReference[] weakNodes;
    private int result;
    
    public TTVTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        TestRunner.run(new NbTestSuite(TTVTest.class));
        //new TTVTest("bleble").testNodesReleasing();
    }

    public void testNodesReleasing () {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fillAndShowTTV();
                // wait for a while to be sure that TTV was completely painted
                // and references between property panels -> properties -> nodes
                // established
                Timer timer = new Timer(5000, new ActionListener () {
                    public void actionPerformed (ActionEvent evt) {
                        TTVTest.this.cleanAndCheckTTV();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
        // wait for results
        synchronized (this) {
            try {
                wait();
                KeyboardFocusManager.getCurrentKeyboardFocusManager ().clearGlobalFocusOwner ();
                for (int j = 0; j < weakNodes.length; j++) {
                    assertGC ("Check Node #" + j, weakNodes[j]);
                }
            } catch (InterruptedException exc) {
                fail("Test was interrupted somehow.");
            }
        }
        // result needn't be synced, was set before we were notified
        if (result > 0) {
            System.out.println("OK, TreeTableView released nodes after " + result + " GC cycles");
        } else {
            System.out.println("TreeTableView leaks memory! Nodes were not freed even after 10 GC cycles.");
            fail("TreeTableView leaks memory! Nodes were not freed even after 10 GC cycles.");
        }
    }
    
    private void fillAndShowTTV () {
        ttv = createTTV();
        props = createProperties();
        nodeStructure = createNodeStructure(props);
        setupTTV(nodeStructure.rootNode, props);
        showTTV();
        weakNodes = createWeakNodes(nodeStructure.childrenNodes);
    }

    private int repaintCount = 0;
    private Timer repaintTimer;
    
    private void cleanAndCheckTTV () {
        // make nodes and props gc'able
        replaceTTVContent();
        nodeStructure = null;
        props = null;
        // assure that weak hash map cache in TreeViewCell is busy a bit,
        // so that it really releases refs to its values
        repaintTimer = new Timer(1000, new ActionListener () {
            public void actionPerformed (ActionEvent evt) {
                if (repaintCount < 10) {
                    ep.invalidate();
                    ep.validate();
                    ep.repaint();
                    repaintCount++;
                    // test if nodes were released correctly
                    // invokeLater so that it comes really after explorer
                    // panel repaint
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            System.gc();
                            repaintTimer.stop();
                            result = repaintCount;
                            // wake up testNodeReleasing method, so that it can finish properly
                            synchronized (TTVTest.this) {
                                TTVTest.this.notifyAll();
                            }
                        }
                    });
                } else {
                    repaintTimer.stop();
                    result = -1;
                    // wake up testNodeReleasing method, so that it can finish properly
                    synchronized (TTVTest.this) {
                        TTVTest.this.notifyAll();
                    }
                }
            }
        });
        repaintTimer.start();
    }
    
    private TreeTableView createTTV () {
        return new TreeTableView();
    }
    
    private static NodeHolderProperty[] createProperties () {
        return new NodeHolderProperty[] {
            new NodeHolderProperty("boolean_prop", Boolean.TYPE,     // NOI18N
                                   "boolean prop.", "Short desc") {  // NOI18N
                public Object getValue () {
                    return Boolean.TRUE;
                }
            },
            new NodeHolderProperty("string_prop", String.class,          // NOI18N
                                   "string prop", "Test string prop") {  // NOI18N
                public Object getValue () {
                    return "value";  // NOI18N
                }
            }
        };
    }
    
    private static final class NodeStructure {
        public Node[] childrenNodes;
        public Node rootNode;
    }
    
    /** Specialized property that will hold reference to any node method
     * holdNode was called on.
     */
    private static abstract class NodeHolderProperty extends PropertySupport.ReadOnly {
        private Node heldNode;
        
        NodeHolderProperty (String propName, Class propClass, String name, String hint) {
            super(propName, propClass, name, hint);
        }
        
        public void holdNode (Node node) {
            heldNode = node;
        }
        
    }
    
    private NodeStructure createNodeStructure (NodeHolderProperty[] props) {
        NodeStructure createdData = new NodeStructure();
        createdData.childrenNodes = new Node[100];
        Children rootChildren = new Children.Array();
        createdData.rootNode = new AbstractNode(rootChildren);
        createdData.rootNode.setDisplayName("Root test node");
        for (int i = 0; i < 100; i++) {
            Node newNode = new TestNode("node #" + i);
            createdData.childrenNodes[i] = newNode;
        }
        rootChildren.add(createdData.childrenNodes);
        return createdData;
    }
    
    private static final class TestNode extends AbstractNode {
        
        TestNode (String name) {
            super(Children.LEAF);
            setName (name);
        }
        
        public Sheet createSheet () {
            Sheet s = Sheet.createDefault ();
            Sheet.Set ss = s.get (Sheet.PROPERTIES);
            NodeHolderProperty[] props = createProperties();
            ss.put(props);
            wireNode(this, props);
            return s;
        }
        
        @Override
        public String toString () {
            return getName () + ": " + super.toString ();
        }


        
    } // end of TestNode

    private static void wireNode (Node node, NodeHolderProperty[] props) {
        for (int i = 0; i < props.length; i++) {
            props[i].holdNode(node);
        }
    }
    
    private void setupTTV (Node rootNode, Node.Property[] props) {
        ttv.setProperties(props);
        ttv.setRootVisible(false);
        
        ExplorerManager em = new ExplorerManager();
        em.setRootContext(rootNode);
        ep = new ExplorerPanel(em);
        ep.add(ttv, BorderLayout.CENTER);
    }
    
    private void replaceTTVContent () {
        Children children = new Children.Array();
        children.add(new Node[] { new TestNode("Not held node") });
        
        ep.getExplorerManager().setRootContext(new AbstractNode (children));
    }
    
    private void showTTV () {
        ep.open();
    }
    
    private WeakReference[] createWeakNodes (Node[] nodes) {
        WeakReference[] weakNodes = new WeakReference[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            weakNodes[i] = new WeakReference(nodes[i]);
        }
        return weakNodes;
    }
    
    
}
