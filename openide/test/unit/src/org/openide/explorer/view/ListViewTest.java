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

/*
 * 
 */

package org.openide.explorer.view;

import java.lang.ref.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.JList;

import org.openide.*;
import org.openide.explorer.*;
import org.openide.nodes.*;
import org.openide.util.*;
import junit.framework.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;

/**
 * Tests for class ListView
 */
public class ListViewTest extends NbTestCase {
    
    private static final int NO_OF_NODES = 3;

    
    public ListViewTest(String name) {
        super(name);
    }
   
    public static void main(String args[]) {
         TestRunner.run(new NbTestSuite(ListViewTest.class));
    }
    
    /**
     * 1. selects a node in a ListView
     * 2. removes the node
     * 3. Shift-Click another node by java.awt.Robot
     */
    public void testNodeSelectionByRobot() throws Exception {
//        TopManager.getDefault();
        final Children c = new Children.Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        for (int i = NO_OF_NODES-1; i >= 0; i--) {
            //Thread.sleep(500);
            
            // Waiting for until the view is updated.
            // This should not be necessary [HREBEJK]
            javax.swing.SwingUtilities.invokeAndWait( new EmptyRunnable() );
       
            p.getExplorerManager().setSelectedNodes(new Node[] {children[i]} );
            //Thread.sleep(500);
            c.remove(new Node[] { children[i] });
            Thread.sleep(500);
            if (lv.isShowing()) {
                Robot r = new Robot();
                r.keyPress(KeyEvent.VK_SHIFT);
                r.mouseMove(lv.getLocationOnScreen().x + 10,lv.getLocationOnScreen().y + 10);
                r.mousePress(InputEvent.BUTTON1_MASK);
                r.keyRelease(KeyEvent.VK_SHIFT);
                r.mouseRelease(InputEvent.BUTTON1_MASK);
            } else {
                assert(false);
            }
        }
    }
    
    /**
     * Removes selected node by calling destroy
     */
    public void testDestroySelectedNodes() throws Exception {
//        TopManager.getDefault();
        final Children c = new Children.Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        for (int i = NO_OF_NODES-1; i >= 0; i--) {     
            //Thread.sleep(500);
            p.getExplorerManager().setSelectedNodes(new Node[] {children[i]} );
            //Thread.sleep(500);
            children[i].destroy();
        }
    }
    
    /**
     * Removes selected node by calling Children.Array.remove
     */
    public void testRemoveAndAddNodes() throws Exception {
//        TopManager.getDefault();
        final Children c = new Children.Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();
        Node[] children = new Node[NO_OF_NODES];

        for (int i = 0; i < NO_OF_NODES; i++) {
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
        }
        //Thread.sleep(2000);
        
        p.getExplorerManager().setSelectedNodes(new Node[] {children[0]} );
        
        for (int i = 0; i < NO_OF_NODES; i++) {
            c.remove(new Node [] { children[i] } );
            children[i] = new AbstractNode(Children.LEAF);
            children[i].setDisplayName(Integer.toString(i));
            children[i].setName(Integer.toString(i));
            c.add(new Node[] { children[i] } );
            //Thread.sleep(350);
        }
        assert(c.getNodesCount() == NO_OF_NODES);
    }
    
    /**
     * Creates two nodes. Selects one and tries to remove it
     * and replace with the other one (several times).
     */
    public void testNodeAddingAndRemoving() throws Exception {
//        TopManager.getDefault();
        final Children c = new Children.Array();
        Node n = new AbstractNode (c);
        final PListView lv = new PListView();
        final ExplorerPanel p = new ExplorerPanel();
        p.add(lv, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(n);
        p.open();

        final Node c1 = new AbstractNode(Children.LEAF);
        c1.setDisplayName("First");
        c1.setName("First");
        c.add(new Node[] { c1 });
        Node c2 = new AbstractNode(Children.LEAF);
        c2.setDisplayName("Second");
        c2.setName("Second");
        //Thread.sleep(500);

        for (int i = 0; i < 5; i++) {
            c.remove(new Node[] { c1 });
            c.add(new Node[] { c2 });
            
            //Thread.sleep(350);
	    //javax.swing.SwingUtilities.invokeAndWait( new EmptyRunnable() );
            
            p.getExplorerManager().setSelectedNodes(new Node[] { c2 } );
            
            c.remove(new Node[] { c2 });
            c.add(new Node[] { c1 });
            
            //Thread.sleep(350);
        }
    }
    
    private static class PListView extends ListView {
        JList getJList() {
            return list;
        }
    }

    
    private class EmptyRunnable extends Object implements Runnable {

	public void run() {
	}

    }
    

}
