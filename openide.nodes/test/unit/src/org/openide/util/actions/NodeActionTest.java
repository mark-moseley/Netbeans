/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Test that node actions are enabled on the right nodes and track selection changes.
 * @author Jesse Glick
 */
public class NodeActionTest extends NbTestCase {
    
    public NodeActionTest(String name) {
        super(name);
    }
    
    private Node n1, n2, n3;
    
    
    /**
     * in order to run in awt event queue
     * fix for #39789
     */
    protected boolean runInEQ() {
        return true;
    }
    
    protected void setUp() throws Exception {
        n1 = new AbstractNode(Children.LEAF);
        n1.setName("n1");
        n1.setDisplayName("text");
        n2 = new AbstractNode(Children.LEAF);
        n2.setName("n2");
        n2.setDisplayName("text");
        n3 = new AbstractNode(Children.LEAF);
        n3.setName("n3");
        n3.setDisplayName("somethingelse");
    }
    
    public void testBasicUsage() throws Exception {
        SimpleNodeAction a1 = (SimpleNodeAction)SystemAction.get(SimpleNodeAction.class);
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL(NodeAction.PROP_ENABLED);
        try {
            // Check enablement logic.
            a1.addPropertyChangeListener(l);
            assertFalse(a1.isEnabled());
            // Note that changes to enabled are made asynch, so it is necessary to listen
            // for that (will not generally take effect immediately).
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n1, n2});
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n2});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n3});
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            // Check that the action is performed correctly.
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            a1.actionPerformed(null);
            a1.actionPerformed(new ActionEvent(a1, ActionEvent.ACTION_PERFORMED, "runit"));
            assertEquals(Arrays.asList(new List[] {
                Collections.singletonList(n1),
                Collections.singletonList(n1),
            }), a1.runOn);
            // Also that idempotent node list changes do not harm anything, at least.
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            // It need not fire a change event; if not, just wait a moment for it to recalc.
            if (!l.changed()) {
                //System.err.println("waiting a moment...");
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n3});
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n3});
            if (!l.changed()) {
                //System.err.println("waiting a moment...");
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(a1.isEnabled());
        } finally {
            a1.removePropertyChangeListener(l);
            ActionsInfraHid.setCurrentNodes(new Node[0]);
            ActionsInfraHid.setCurrentNodes(null);
            a1.runOn.clear();
        }
    }
    
    public void testPerformActionWithArgs() throws Exception {
        SimpleNodeAction a1 = (SimpleNodeAction)SystemAction.get(SimpleNodeAction.class);
        try {
            assertFalse(a1.isEnabled());
            assertEquals(Collections.EMPTY_LIST, a1.runOn);
            a1.actionPerformed(new ActionEvent(n1, ActionEvent.ACTION_PERFORMED, "exec"));
            a1.actionPerformed(new ActionEvent(new Node[] {n1}, ActionEvent.ACTION_PERFORMED, "exec"));
            assertEquals(Arrays.asList(new List[] {
                Collections.singletonList(n1),
                Collections.singletonList(n1),
            }), a1.runOn);
            // XXX probably NodeAction.actionPerformed with Node or Node[] should
            // first check that the action is in fact enabled on those nodes, else
            // throw an IllegalArgumentException; in which case add a test to that effect here
        } finally {
            ActionsInfraHid.setCurrentNodes(new Node[0]);
            ActionsInfraHid.setCurrentNodes(null);
            a1.runOn.clear();
        }
    }
    
    /** Test that surviveFocusChange really controls whether node actions are enabled or not.
     */
    public void testFocusChange() throws Exception {
        helpTestFocusChange();
        // XXX does not work: refuses to collect the node actions!
        // Yet similar code works in CallbackSystemActionTest.
        // Profiler shows that the references are held only from WeakReference's,
        // one of which is in the finalizer queue. ???
        /*
        ActionsInfraHid.doGC();
        assertEquals("Garbage collection removed all SimpleNodeAction's", 0, SimpleNodeAction.INSTANCES);
        helpTestFocusChange();
         */
    }
    private void helpTestFocusChange() throws Exception {
        SimpleNodeAction a1 = (SimpleNodeAction)SystemAction.get(SimpleNodeAction.class);
        DoesNotSurviveFocusChgAction a2 = (DoesNotSurviveFocusChgAction)SystemAction.get(DoesNotSurviveFocusChgAction.class);
        ActionsInfraHid.WaitPCL l1 = new ActionsInfraHid.WaitPCL(NodeAction.PROP_ENABLED);
        ActionsInfraHid.WaitPCL l2 = new ActionsInfraHid.WaitPCL(NodeAction.PROP_ENABLED);
        try {
            /*
            assertEquals(null, ActionsInfraHid.getCurrentNodes());
            assertEquals(Collections.EMPTY_LIST, Arrays.asList(ActionsInfraHid.getActivatedNodes()));
             */
            a1.addPropertyChangeListener(l1);
            a2.addPropertyChangeListener(l2);
            assertFalse(a1.isEnabled());
            assertFalse(a2.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l1.changed());
            l1.gotit = 0;
            assertTrue(a1.isEnabled());
            assertTrue(l2.changed());
            l2.gotit = 0;
            assertTrue(a2.isEnabled());
            ActionsInfraHid.setCurrentNodes(null);
            assertTrue(l2.changed());
            l2.gotit = 0;
            assertFalse(a2.isEnabled());
            if (!l1.changed()) {
                Thread.sleep(1000);
            }
            l1.gotit = 0;
            assertTrue(a1.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n2});
            assertTrue(l2.changed());
            l2.gotit = 0;
            assertTrue(a2.isEnabled());
            if (!l1.changed()) {
                Thread.sleep(1000);
            }
            l1.gotit = 0;
            assertTrue(a1.isEnabled());
            
            // another trick, sets n1 to enable everything and then
            // switches to Node[0] to disable everything
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l1.changed());
            l1.gotit = 0;
            assertTrue(a1.isEnabled());
            assertTrue(l2.changed());
            l2.gotit = 0;
            assertTrue(a2.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[0]);
            assertTrue(l2.changed());
            l2.gotit = 0;
            assertFalse(a2.isEnabled());
            l1.gotit = 0;
            assertFalse(a1.isEnabled());
        } finally {
            a1.removePropertyChangeListener(l1);
            a2.removePropertyChangeListener(l2);
            ActionsInfraHid.setCurrentNodes(new Node[0]);
            ActionsInfraHid.setCurrentNodes(null);
        }
        a1 = null;
        a2 = null;
    }
    
    /** Make sure NodeAction itself does not do anything dumb by requiring enablement
     * checks too often.
     * The important fix is that even when it has listeners, after firing PROP_ENABLED
     * in response to a selection change, it should not actually compute the enablement
     * status again until someone asks isEnabled(). Otherwise it will perpetually be
     * firing changes, when in fact no one cares (the action is not even visible).
     * @see "#13505"
     */
    public void testNoRedundantEnablementChecks() throws Exception {
        LazyNodeAction a = (LazyNodeAction)SystemAction.get(LazyNodeAction.class);
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL(NodeAction.PROP_ENABLED);
        try {
            assertEquals(0, a.count);
            assertFalse(a.listeners);
            assertFalse(a.isEnabled());
            a.addPropertyChangeListener(l);
            assertTrue(a.listeners);
            assertFalse(a.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a.isEnabled());
            // Now make sure calls to isEnabled() do not do anything while the selection has not changed.
            a.count = 0;
            assertTrue(a.isEnabled());
            assertEquals("Adjacent calls to isEnabled() do not recheck the same node selection", 0, a.count);
            /* This is pretty irrelevant, it probably never happens anyway:
            // Make sure equivalent node arrays are not considered significant.
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(a.isEnabled());
            assertEquals("Adjacent calls to isEnabled() do not recheck equivalent node selections", 0, a.count);
             */
            // But a real change is significant and enable(Node[]) is checked.
            ActionsInfraHid.setCurrentNodes(new Node[] {n2});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(a.isEnabled());
            assertEquals("A real change to selection calls enable(Node[]) again", 1, a.count);
            // No checks made just because there was a selection change, but no request.
            a.count = 0;
            ActionsInfraHid.setCurrentNodes(new Node[] {n1, n3});
            assertTrue(l.changed());
            l.gotit = 0;
            assertEquals("Do not make extra checks until someone asks", 0, a.count);
            ActionsInfraHid.setCurrentNodes(new Node[] {n2, n3});
            assertTrue("Do not keep firing changes when nobody is paying attention", !l.changed());
            // After detaching all listeners, selection changes are not tracked more than once.
            a.removePropertyChangeListener(l);
            assertFalse(a.listeners);
            ActionsInfraHid.setCurrentNodes(new Node[] {});
            Thread.sleep(1000);
            assertFalse(a.isEnabled());
            a.count = 0;
            assertFalse(a.isEnabled());
            assertEquals("Even with no listeners, adjacent isEnabled()s are clean", 0, a.count);
            ActionsInfraHid.setCurrentNodes(new Node[] {n3});
            Thread.sleep(1000);
            assertEquals("With no listeners, node selection changes are ignored", 0, a.count);
            assertTrue(a.isEnabled());
            assertEquals("With no listeners, isEnabled() works on demand", 1, a.count);
        } finally {
            a.removePropertyChangeListener(l);
            ActionsInfraHid.setCurrentNodes(new Node[0]);
            ActionsInfraHid.setCurrentNodes(null);
            a.count = 0;
        }
    }
    
    /** Due to lack of a coherent API in NodeAction for telling it that any previous
     * results with a given node selection are now void, some subclasses such as
     * CookieAction and Move{Up,Down}Action actually call setEnabled directly, when
     * some aspect of the selected nodes changes without the selection itself changing.
     * Make sure that such changes are respected - they had best not call enable() as
     * typically the subclass itself does the new check anyway - but a subsequent selection
     * must call enable() again.
     */
    public void testCallSetEnabledDirectly() throws Exception {
        SimpleNodeAction a1 = (SimpleNodeAction)SystemAction.get(SimpleNodeAction.class);
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL(NodeAction.PROP_ENABLED);
        try {
            assertFalse(a1.isEnabled());
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(a1.isEnabled());
            n1.setDisplayName("foo");
            a1.setEnabled(false);
            assertFalse(a1.isEnabled());
            n1.setDisplayName("text");
            ActionsInfraHid.setCurrentNodes(new Node[] {n2});
            assertTrue(a1.isEnabled());
            // Now try it with listeners.
            a1.addPropertyChangeListener(l);
            assertTrue(a1.isEnabled());
            n2.setDisplayName("foo");
            a1.setEnabled(false);
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            n2.setDisplayName("text");
            ActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            n1.setDisplayName("foo");
            a1.setEnabled(false);
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            n1.setDisplayName("text");
            ActionsInfraHid.setCurrentNodes(new Node[] {n2});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
        } finally {
            a1.removePropertyChangeListener(l);
            n1.setDisplayName("text");
            ActionsInfraHid.setCurrentNodes(new Node[0]);
            ActionsInfraHid.setCurrentNodes(null);
        }
    }
    
    //
    // cloneAction support
    //
    
    public void testNodeActionIsCorrectlyClonned() throws Exception {
        class MN extends AbstractNode {
            public MN(String displayName) {
                super(Children.LEAF);
                setDisplayName(displayName);
            }
        }
        
        class Counter implements PropertyChangeListener {
            int cnt;
            
            public void propertyChange(PropertyChangeEvent ev) {
                cnt++;
            }
            
            public void assertCnt(String txt, int cnt) {
                assertEquals(txt, cnt, this.cnt);
                this.cnt = 0;
            }
        }
        
        
        SimpleNodeAction s = (SimpleNodeAction)SimpleNodeAction.get(SimpleNodeAction.class);
        Counter counter = new Counter();
        
        InstanceContent ic = new InstanceContent();
        AbstractLookup lookup = new AbstractLookup(ic);
        
        Action clone = s.createContextAwareInstance(lookup);
        clone.addPropertyChangeListener(counter);
        
        assertTrue("Not enabled", !clone.isEnabled());
        
        MN mn1 = new MN("text");
        ic.add(mn1);
        
        assertTrue("Enabled", clone.isEnabled());
        counter.assertCnt("Once change in enabled state", 1);
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        
        assertEquals("Has been executed just once: ", 1, SimpleNodeAction.runOn.size());
        Collection c = (Collection)SimpleNodeAction.runOn.iterator().next();
        SimpleNodeAction.runOn.clear();
        assertTrue("Has been executed on mn1", c.contains(mn1));
        
        MN mn2 = new MN("x");
        ic.add(mn2);
        
        assertTrue("Not enabled, because there are two items", !clone.isEnabled());
        counter.assertCnt("Another change in the state", 1);
        
        ic.remove(mn1);
        assertTrue("Not enabled, the one item is not named correctly", !clone.isEnabled());
        counter.assertCnt("No change right now, the action remains disabled", 0);
        
    }
    
    
    public static class SimpleNodeAction extends NodeAction {
        protected boolean enable(Node[] activatedNodes) {
            boolean r = activatedNodes.length == 1 &&
                    activatedNodes[0].getDisplayName().equals("text");
            //System.err.println("enable: activatedNodes=" + Arrays.asList(activatedNodes) + " r=" + r);
            return r;
        }
        public static final List runOn = new ArrayList(); // List<List<Node>>
        protected void performAction(Node[] activatedNodes) {
            runOn.add(Arrays.asList(activatedNodes));
        }
        public String getName() {
            return "SimpleNodeAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        public static int INSTANCES = 0;
        public SimpleNodeAction() {
            INSTANCES++;
        }
        protected boolean clearSharedData() {
            INSTANCES--;
            System.err.println("collecting a SimpleNodeAction or subclass");//XXX
            return super.clearSharedData();
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    public static class DoesNotSurviveFocusChgAction extends SimpleNodeAction {
        protected boolean surviveFocusChange() {
            return false;
        }
        public String getName() {
            return "DoesNotSurviveFocusChgAction";
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
    public static class LazyNodeAction extends NodeAction {
        public static int count = 0;
        protected boolean enable(Node[] activatedNodes) {
            count++;
            return activatedNodes.length == 1;
        }
        public static boolean listeners = false;
        protected void addNotify() {
            if (listeners) throw new IllegalStateException();
            super.addNotify();
            listeners = true;
        }
        protected void removeNotify() {
            if (!listeners) throw new IllegalStateException();
            listeners = false;
            super.removeNotify();
        }
        protected void performAction(Node[] activatedNodes) {
            // do nothing
        }
        public String getName() {
            return "LazyNodeAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
    
}
