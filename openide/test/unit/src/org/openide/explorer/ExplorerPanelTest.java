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


package org.openide.explorer;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import javax.swing.Action;
import javax.swing.JMenu;

import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.io.NbMarshalledObject;


/**
 * Tests for <code>ExplorerPanel</code>.
 *
 * @author Peter Zavadsky
 */
public class ExplorerPanelTest extends NbTestCase {
    /** context the action should work in */
    private Lookup context;
    /** explorer manager to work on */
    private ExplorerManager manager;
    
    
    
    /** need it for stopActions here */
    private ExplorerPanel ep;
    
    public ExplorerPanelTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ExplorerPanelTest.class);
        return suite;
    }

    /** Run all tests in AWT thread */
    public final void run(final junit.framework.TestResult result) {
        try {
            // XXX ExplorerManager when updating selected nodes
            // replanes all firing into AWT thread, therefore the test
            // has to run in AWT.
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ExplorerPanelTest.super.run (result);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException ();
        }
    }
    
    /** Setups the tests.
     */
    protected final void setUp () {
        Object[] arr = createManagerAndContext ();
        manager = (ExplorerManager)arr[0];
        context = (Lookup)arr[1];
        
    }
    
    /** Creates a manager to operate on.
     */
    protected Object[] createManagerAndContext () {
        ep = new ExplorerPanel (null, false);
        return new Object[] { ep.getExplorerManager(), ep.getLookup() };
    }
    
    /** Instructs the actions to stop/
     */
    protected void stopActions (ExplorerManager em) {
        ep.componentDeactivated ();
    }
    /** Instructs the actions to start again.
     */
    protected void startActions(ExplorerManager em) {
        ep.componentActivated();
    }
    
    /** Tests whether the cut, copy (callback) actions are enabled/disabled
     * in the right time, see # */
    public void testCutCopyActionsEnabling() throws Exception {
        assertTrue ("Can run only in AWT thread", java.awt.EventQueue.isDispatchThread());

        TestNode enabledNode = new TestNode(true, true, true);
        TestNode disabledNode = new TestNode(false, false, false);

        manager.setRootContext(new TestRoot(
            new Node[] {enabledNode, disabledNode}));

        Action copy = ((ContextAwareAction)SystemAction.get(CopyAction.class)).createContextAwareInstance(context);
        Action cut = ((ContextAwareAction)SystemAction.get(CutAction.class)).createContextAwareInstance(context);

        assertTrue("Copy action has to be disabled", !copy.isEnabled());
        assertTrue("Cut action has to be disabled", !cut.isEnabled());

        manager.setSelectedNodes(new Node[] {enabledNode});

        assertTrue("Copy action has to be enabled", copy.isEnabled());
        assertTrue("Cut action has to be enabled", cut.isEnabled());
        
        copy.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        assertEquals ("clipboardCopy invoked", 1, enabledNode.countCopy);
        
        cut.actionPerformed (new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        assertEquals ("clipboardCut invoked", 1, enabledNode.countCut);
        

        manager.setSelectedNodes(new Node[] {disabledNode});

        assertTrue("Copy action has to be disabled", !copy.isEnabled());
        assertTrue("Cut action has to be disabled", !cut.isEnabled());
    }
    
    public void testDeleteAction () throws Exception {
        TestNode enabledNode = new TestNode(true, true, true);
        TestNode enabledNode2 = new TestNode(true, true, true);
        TestNode disabledNode = new TestNode(false, false, false);

        manager.setRootContext(new TestRoot(
            new Node[] {enabledNode, enabledNode2, disabledNode}));

        Action delete = ((ContextAwareAction)SystemAction.get(org.openide.actions.DeleteAction.class)).createContextAwareInstance(context);
        
        assertTrue ("By default delete is disabled", !delete.isEnabled());
        
        manager.setSelectedNodes(new Node[] { enabledNode });
        assertTrue ("Now it gets enabled", delete.isEnabled ());
        
        manager.setSelectedNodes (new Node[] { disabledNode });
        assertTrue ("Is disabled", !delete.isEnabled ());

        manager.setSelectedNodes(new Node[] { manager.getRootContext() });
        assertTrue ("Delete is enabled on root", delete.isEnabled ());
        
        manager.setSelectedNodes(new Node[] { manager.getRootContext(), enabledNode });
        assertTrue ("But is disabled on now, as one selected node is child of another", !delete.isEnabled ());
        
        manager.setSelectedNodes (new Node[] { enabledNode, enabledNode2 });
        assertTrue ("It gets enabled", delete.isEnabled ());
        
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        
        assertEquals ("Destoy was called", 1, enabledNode.countDelete);
        assertEquals ("Destoy was called", 1, enabledNode2.countDelete);
        
        
    }

    public void testPasteAction () throws Exception {
        TestNode enabledNode = new TestNode(true, true, true);
        TestNode disabledNode = new TestNode(false, false, false);

        manager.setRootContext(new TestRoot(
            new Node[] {enabledNode, disabledNode}));

        Action paste = ((ContextAwareAction)SystemAction.get(org.openide.actions.PasteAction.class)).createContextAwareInstance(context);
        assertTrue ("Disabled by default", !paste.isEnabled ());
        
        class PT extends PasteType {
            public int count;
            
            public java.awt.datatransfer.Transferable paste () {
                count++;
                return null;
            }
        }
        PT[] arr = { new PT () };
        
        enabledNode.types = arr;
        
        manager.setSelectedNodes (new Node[] { enabledNode });
        
        assertTrue ("Paste is enabled", paste.isEnabled ());
        
        paste.actionPerformed(new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        assertEquals ("Paste invoked", 1, arr[0].count);
        
        manager.setSelectedNodes (new Node[] { disabledNode });
        assertTrue ("Disabled paste", !paste.isEnabled ());
        
        arr = new PT[] { new PT(), new PT() };
        enabledNode.types = arr;
        
        manager.setSelectedNodes (new Node[] { enabledNode });
        assertTrue ("Paste enabled again", paste.isEnabled ());
        
        org.openide.util.datatransfer.ExClipboard ec = (org.openide.util.datatransfer.ExClipboard)Lookup.getDefault().lookup (org.openide.util.datatransfer.ExClipboard.class);
        assertNotNull ("Without ExClipboard this will not work much", ec);
        
        java.awt.datatransfer.StringSelection ss = new java.awt.datatransfer.StringSelection ("Hi");
        ec.setContents(ss, ss);
        
        assertTranferables (ss, enabledNode.lastTransferable);
        
        stopActions (manager);

        manager.setSelectedNodes(new Node[] { disabledNode });
        assertTrue("Paste still enabled as we are not listening", paste.isEnabled());
        
        java.awt.datatransfer.StringSelection ns = new java.awt.datatransfer.StringSelection ("New Selection");
        ec.setContents(ns, ns);

        assertTranferables (ss, enabledNode.lastTransferable);
        
        startActions (manager);
        
        assertFalse ("The selected node is the disabled one, so now we are disabled", paste.isEnabled ());
        assertTranferables (ns, disabledNode.lastTransferable);
        
        
        ns = new java.awt.datatransfer.StringSelection("Another Selection");
        ec.setContents(ns, ns);
        assertTranferables (ns, disabledNode.lastTransferable);
    }
    
    public void testSelectedNodesInDeserializedPanel () throws Exception {
        ExplorerPanel panel = new ExplorerPanel ();

        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        DataObject rootFilesystem = DataObject.find (fo);
        Node root = rootFilesystem.getNodeDelegate ();
        panel.getExplorerManager ().setRootContext (root);
        panel.getExplorerManager ().setSelectedNodes (new Node[] {root});
        
        assertNotNull ("Array of selected nodes is not null.", panel.getExplorerManager ().getSelectedNodes ());
        assertFalse ("Array of selected nodes is not empty.",  panel.getExplorerManager ().getSelectedNodes ().length == 0);
        assertEquals ("The selected node is Filesystems root.", panel.getExplorerManager ().getSelectedNodes ()[0], root);
        
        NbMarshalledObject mar = new NbMarshalledObject (panel);
        Object obj = mar.get ();
        ExplorerPanel deserializedPanel = (ExplorerPanel) obj;
        
        assertNotNull ("Deserialized panel is not null.", deserializedPanel);
        
        assertNotNull ("[Deserialized panel] Array of selected nodes is not null.", deserializedPanel.getExplorerManager ().getSelectedNodes ());
        assertFalse ("[Deserialized panel] Array of selected nodes is not empty.",  deserializedPanel.getExplorerManager ().getSelectedNodes ().length == 0);
        assertEquals ("[Deserialized panel] The selected node is Filesystems root.", deserializedPanel.getExplorerManager ().getSelectedNodes ()[0], root);
        
    }
    
    /** Compares whether two transferables are the same.
     */
    private static void assertTranferables (java.awt.datatransfer.Transferable t1, java.awt.datatransfer.Transferable t2) 
    throws Exception {
       // if (t1 == t2) return;
        
        java.awt.datatransfer.DataFlavor[] arr1 = t1.getTransferDataFlavors ();
        java.awt.datatransfer.DataFlavor[] arr2 = t2.getTransferDataFlavors ();
        
        assertEquals ("Flavors are the same", 
            Arrays.asList (arr1),
            Arrays.asList (arr2)
        );
        
        for (int i = 0; i < arr1.length; i++) {
            Object f1 = convert (t1.getTransferData(arr1[i]));
            Object f2 = convert (t2.getTransferData(arr1[i]));
            
            assertEquals (i + " flavor " + arr1[i], f1, f2);
        }
    }
    
    private static Object convert (Object obj) throws Exception {
        if (obj instanceof java.io.StringReader) {
            java.io.StringReader sr = (java.io.StringReader)obj;
            StringBuffer sb = new StringBuffer ();
            for (;;) {
                int ch = sr.read ();
                if (ch == -1) return sb.toString ();
                sb.append ((char)ch);
            }
        }
        
        return obj;
    }
    
    /** Test root node. */
    private static class TestRoot extends AbstractNode {
        public TestRoot(Node[] children) {
            super(new Children.Array());
            getChildren().add(children);
        }
        
        public boolean canDestroy () {
            return true;
        }
    }
    
    /** Node which enables both cut and copy actions. */
    private static class TestNode extends AbstractNode {
        public boolean canCopy;
        public boolean canCut;
        public boolean canDelete;
        public PasteType[] types = new PasteType[0];
        public java.awt.datatransfer.Transferable lastTransferable;
        
        public int countCopy;
        public int countCut;
        public int countDelete;
        
        public TestNode(boolean b, boolean c, boolean d) {
            super(Children.LEAF);
            canCopy = b;
            canCut = c;
            canDelete = d;
        }
        
        public void destroy () {
            countDelete++;
        }
        
        public boolean canDestroy () {
            return canDelete;
        }
        
        public boolean canCopy() {
            return canCopy;
        }
        public boolean canCut() {
            return canCut;
        }

        public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException {
            java.awt.datatransfer.Transferable retValue;
            
            retValue = super.clipboardCopy();
            
            countCopy++;
            
            return retValue;
        }
        
        public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException {
            java.awt.datatransfer.Transferable retValue;
            
            retValue = super.clipboardCut();
            
            countCut++;
            
            return retValue;
        }
        
        protected void createPasteTypes(java.awt.datatransfer.Transferable t, java.util.List s) {
            this.lastTransferable = t;
            s.addAll (Arrays.asList (types));
        }
        
    }
    
}
