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

import java.beans.PropertyVetoException;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.*;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Children.Array;

/*
 * RootContextTest.java tests a change the root context and set selected node
 * in each root context.
 *
 * @author  Jiri Rechtacek
 */
public class RootContextTest extends NbTestCase {
    
    public RootContextTest (java.lang.String testName) {
        super (testName);
    }
    
    public static void main (java.lang.String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
    
    public static Test suite () {
        TestSuite suite = new NbTestSuite (RootContextTest.class);
        return suite;
    }
    
    // helper variables
    Node[] arr1, arr2;
    Node root1, root2;
    boolean initialized = false;

    
    // initialize the test variables
    public void initText () {
        
        // do init only once
        if (initialized) {
            return ;
        } else {
            initialized = true;
        }

        arr1 = new Node [3];
        arr1[0] = new AbstractNode (Children.LEAF);
        arr1[0].setName ("One");
        
        arr1[1] = new AbstractNode (Children.LEAF);
        arr1[1].setName ("Two");
        
        arr1[2] = new AbstractNode (Children.LEAF);
        arr1[2].setName ("Three");
        
        Array ch1 = new Array ();
        ch1.add (arr1);
        
        arr2 = new Node [3];
        arr2[0] = new AbstractNode (Children.LEAF);
        arr2[0].setName ("Aaa");
        
        arr2[1] = new AbstractNode (Children.LEAF);
        arr2[1].setName ("Bbb");
        
        arr2[2] = new AbstractNode (Children.LEAF);
        arr2[2].setName ("Ccc");

        Array ch2 = new Array ();
        ch2.add (arr2);
        
        root1 = new AbstractNode (ch1);
        root1.setName ("Root1");
        root2 = new AbstractNode (ch2);
        root2.setName ("Root2");
        
    }
    
    
    // asure the node selections with given manager
    public void doViewTest (final ExplorerManager mgr) throws Exception {
        
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
        
                mgr.setRootContext (root1);
                try {
                    mgr.setSelectedNodes (new Node[] {arr1[0], arr1[2]});
                } catch (PropertyVetoException pve) {
                    fail (pve.getMessage ());
                }

                Node[] selNodes = mgr.getSelectedNodes ();
                assertEquals ("Root context is ", "Root1", mgr.getRootContext ().getName ());
                assertEquals ("Count of the selected node is ", 2, selNodes.length);
                // pending: an order migth be different
                //Arrays.sort (selNodes);
                assertEquals ("Selected node is ", "One", selNodes[0].getName ());
                assertEquals ("Selected node is ", "Three", selNodes[1].getName ());

                mgr.setRootContext (root2);
                try {
                    mgr.setSelectedNodes (new Node[] { arr2[1] });
                } catch (PropertyVetoException pve) {
                    fail (pve.getMessage ());
                }

                selNodes = mgr.getSelectedNodes ();
                assertEquals ("Root context is ", "Root2", mgr.getRootContext ().getName ());
                assertEquals ("Count of the selected node is ", 1, selNodes.length);
                assertEquals ("Selected node is ", "Bbb", selNodes[0].getName ());
        
            }
        });
    }
    
    // test for each type of view
    
    public void testBeanTreeView () {
        
        initText ();
        
        TreeView view = new BeanTreeView ();
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        try {
            doViewTest (mgr);
        } catch (Exception e) {
            fail (e.toString ());
        }
        
    }
    
    public void testContextTreeView () {
        
        initText ();
        
        TreeView view = new ContextTreeView ();
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        try {
            doViewTest (mgr);
        } catch (Exception e) {
            fail (e.toString ());
        }
        
    }
    
    public void testTreeTableView () {
        
        initText ();
        
        TreeTableView view = new TreeTableView ();
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        try {
            doViewTest (mgr);
        } catch (Exception e) {
            fail (e.toString ());
        }
        
    }
    
    public void testListView () {
        
        initText ();
        
        ListView view = new ListView (); 
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        try {
            doViewTest (mgr);
        } catch (Exception e) {
            fail (e.toString ());
        }
        
    }
    
    public void testListTableView () {
        
        initText ();
        
        ListTableView view = new ListTableView ();
        
        ExplorerPanel panel = new ExplorerPanel ();
        ExplorerManager mgr = panel.getExplorerManager ();
        
        panel.add (view);
        panel.open ();
        
        try {
            doViewTest (mgr);
        } catch (Exception e) {
            fail (e.toString ());
        }
        
    }
    
}
