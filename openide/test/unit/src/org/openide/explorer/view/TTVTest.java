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

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.loaders.DataFilter;
import org.openide.loaders.RepositoryNodeFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.windows.TopComponent;


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
                            if (checkWeakRefs(weakNodes)) {
                                repaintTimer.stop();
                                result = repaintCount;
                                // wake up testNodeReleasing method, so that it can finish properly
                                synchronized (TTVTest.this) {
                                    TTVTest.this.notifyAll();
                                }
                            } else {
                                System.out.println("Refs still alive after GC #" + repaintCount);
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
    
    private boolean checkWeakRefs (WeakReference[] weakNodes) {
        for (int j = 0; j < weakNodes.length; j++) {
            if (weakNodes[j].get() != null) {
                return false;
            }
        }
        return true;
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
            Node newNode = new TestNode();
            newNode.setDisplayName("node #" + i);
            createdData.childrenNodes[i] = newNode;
        }
        rootChildren.add(createdData.childrenNodes);
        return createdData;
    }
    
    private static final class TestNode extends AbstractNode {
        
        TestNode () {
            super(Children.LEAF);
        }
        
        public Sheet createSheet () {
            Sheet s = Sheet.createDefault ();
            Sheet.Set ss = s.get (Sheet.PROPERTIES);
            NodeHolderProperty[] props = createProperties();
            ss.put(props);
            wireNode(this, props);
            return s;
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
        children.add(new Node[] { new TestNode() });
        
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
