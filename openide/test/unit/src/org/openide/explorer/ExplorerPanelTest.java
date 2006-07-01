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


package org.openide.explorer;

import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
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


    protected boolean runInEQ() {
        return true;
    }
    
    /** Setups the tests.
     */
    protected final void setUp () {
        // XXX consider replacing with MockServices
        System.setProperty ("org.openide.util.Lookup", "org.openide.explorer.ExplorerPanelTest$Lkp");
        
        Object[] arr = createManagerAndContext (false);
        manager = (ExplorerManager)arr[0];
        context = (Lookup)arr[1];
        
    }
    
    /** Creates a manager to operate on.
     */
    protected Object[] createManagerAndContext (boolean confirm) {
        ep = new ExplorerPanel (null, confirm);
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

    public void testDeleteConfirmAction () throws Exception {
        TestNode [] nodes = new TestNode [] {
            new TestNode(true, true, true),
            new TestNode(true, true, true, true),
            new TestNode(true, true, true),
            new TestNode(true, true, true, true),
            new TestNode(false, false, false)
        };

        YesDialogDisplayer ydd = (YesDialogDisplayer)Lookup.getDefault().lookup(YesDialogDisplayer.class);
        DialogDisplayer dd = (DialogDisplayer)Lookup.getDefault().lookup(DialogDisplayer.class);
        assertNotNull("Custom DialogDisplayer is not set", ydd);
        int notifyCount = ydd.getNotifyCount();
        assertEquals("YesDialogDisplayer is current DialogDisplayer", ydd, dd);
        
        Object[] arr = createManagerAndContext (true);
        
        ExplorerPanel delep = new ExplorerPanel ((ExplorerManager)arr[0], true);
        ExplorerManager delManager = delep.getExplorerManager();
        delManager.setRootContext(new TestRoot(
            nodes));

        Action delete = ((ContextAwareAction)SystemAction.get(org.openide.actions.DeleteAction.class)).createContextAwareInstance((Lookup)arr[1]);

        // delete should ask for confirmation
        delManager.setSelectedNodes (new Node[] { nodes[0] });
        assertTrue ("It gets enabled", delete.isEnabled ());
        
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        
        assertEquals ("Destoy was called", 1, nodes[0].countDelete);
        
        assertEquals ("Confirm delete was called ", notifyCount+1, ydd.getNotifyCount());
        
        // but delete should not ask for confirmation if the node wants to perform handle delete 
        delManager.setSelectedNodes (new Node[] { nodes[1] });
        assertTrue ("It gets enabled", delete.isEnabled ());
        
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        
        assertEquals ("Destoy was called", 1, nodes[1].countDelete);
        
        assertEquals ("Confirm delete was called ", notifyCount+1, ydd.getNotifyCount()); // no next dialog
        
        // anyway ask for confirmation if at least one node has default behaviour
        delManager.setSelectedNodes (new Node[] { nodes[2], nodes[3] });
        assertTrue ("It gets enabled", delete.isEnabled ());
        
        delete.actionPerformed(new java.awt.event.ActionEvent (this, 0, "waitFinished"));
        
        assertEquals ("Destoy was called", 1, nodes[2].countDelete);
        assertEquals ("Destoy was called", 1, nodes[3].countDelete);
        
        assertEquals ("Confirm delete was called ", notifyCount+2, ydd.getNotifyCount()); // no next dialog
        
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
    
    public void skipForNowtestSelectedNodesInDeserializedPanel () throws Exception {
        ExplorerPanel panel = new ExplorerPanel ();

        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        
        Node root = new SerializableNode ();
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
        private boolean customDelete;
        public PasteType[] types = new PasteType[0];
        public java.awt.datatransfer.Transferable lastTransferable;
        
        public int countCopy;
        public int countCut;
        public int countDelete;
        
        public TestNode(boolean canCopy, boolean canCut, boolean canDelete, boolean customDelete) {
            this (canCopy, canCut, canDelete);
            this.customDelete = customDelete;
        }
        
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

        public Object getValue(String attributeName) {
            if (customDelete && "customDelete".equals(attributeName)) {
                return Boolean.TRUE;
            }
            return super.getValue(attributeName);
        }
        
    }

    public static final class SerializableNode extends AbstractNode 
    implements Node.Handle, java.io.Externalizable {
        
        static final long serialVersionUID = 439503248509342L;
        
        public SerializableNode () {
            super (Children.LEAF);
        }
        
        public Handle getHandle () {
            return this;
        }
        
        public Node getNode () {
            return this;
        }
        
        public void writeExternal (java.io.ObjectOutput oo) {
        }
        public void readExternal (java.io.ObjectInput oi) {
        }
        
        //
        // All instances of SerializableNode are equal
        //

        public int hashCode() {
            return getClass ().hashCode ();
        }

        public boolean equals(java.lang.Object obj) {
            return obj != null && getClass ().equals (obj.getClass ());
        }
    } // end SerializableNode
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new Clb ("Testing clipboard"));
            ic.add (new YesDialogDisplayer());
        }
    }
    
    private static final class Clb extends org.openide.util.datatransfer.ExClipboard {
        public Clb (String s) {
            super (s);
        }

        protected org.openide.util.datatransfer.ExClipboard.Convertor[] getConvertors() {
            return new org.openide.util.datatransfer.ExClipboard.Convertor[0];
        }
        
        public void setContents (Transferable t, ClipboardOwner o) {
            super.setContents (t, o);
            fireClipboardChange ();
        }
    }
    
    private static final class YesDialogDisplayer extends DialogDisplayer {
        private int counter = 0;
        
        public YesDialogDisplayer() {
            super();
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            counter++;
            return NotifyDescriptor.YES_OPTION;
        }

        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            return null;
        }
        
        public int getNotifyCount() {
            return counter;
        }
    }
}
