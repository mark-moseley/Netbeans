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

package org.openide.nodes;

import java.beans.PropertyChangeEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

public class ChildrenKeysTest extends NbTestCase {
    private Logger LOG;
    
    public ChildrenKeysTest(java.lang.String testName) {
        super(testName);
    }

    protected Node createNode (Children ch) {
        return new AbstractNode (ch);
    }

    protected boolean lazy() {
        return false;
    }
    
//    public static ChildrenKeysTest suite() {
//        return new ChildrenKeysTest("testRefreshKey");
//    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }

    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
    }

    public void testGetNodesFromTwoThreads57769() throws Exception {
        final Ticker tick1 = new Ticker();
        final List who = new java.util.Vector();
        
        final int[] count = new int[1];
        Children children= new Children.Keys(lazy()) {
            protected Node[] createNodes(Object key) {
                StringWriter msg = new StringWriter();
                msg.write("Creating: " + count[0] + " for key: " + key + "\n");
                new Exception().printStackTrace(new PrintWriter(msg));
                LOG.log(Level.INFO, msg.toString());
                count[0]++;
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                try {Thread.sleep(2000);}catch(InterruptedException e) {}
                return n == null ? new Node[]{} : new Node[]{n};
            }

            @Override
            protected void addNotify() {
                setKeys(Arrays.asList(new Object[] {"1", "2"}));
            }            
        };
        final Node node = new AbstractNode(children);
        
        // Get optimal nodes from other thread
        Thread t = new Thread("THREAD") {
            Node[] keep;
            @Override
            public void run() {
                tick1.tick();
                keep = node.getChildren().getNodes(true);
            }
        };
        t.start();
        tick1.waitOn();
        
        // and also from main thread
        Node[] remember = node.getChildren().getNodes();
        
        // wait for other thread
        t.join();
        
        if (2 != count[0]) {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            w.write("Just two nodes created: " + count[0] + " stacks:\n");
            Iterator it = who.iterator();
            while (it.hasNext()) {
                Exception e = (Exception)it.next();
                e.printStackTrace(pw);
            }
            pw.close();
            
            fail(w.toString());
        }//fail("Ok");
    }

    /**
     * See #78519
     * T1 has write access and gets preempted just before call to
     * getNodes() by another thread callig getNodes.
     * Other thread
     */
    public void testGetNodesFromWriteAccess() throws Exception {
        final String[] keys = { "Nenik", "Tulach" };
        Keys o = new Keys (lazy(), keys);
        Node orig = new AbstractNode(o);
        Node filter = new FilterNode(orig);
        final Children k = filter.getChildren();
        
        final Ticker tick1 = new Ticker();
        final Ticker tick2 = new Ticker();
        final boolean[] done = new boolean[2];
        
        // Try to get nodes from writeAccess
        Thread t = new Thread("preempted") {
            @Override
            public void run() {
                Children.PR.enterWriteAccess();
                try {
                    tick1.tick(); // I do have the write access ...
                    tick2.waitOn(); // ... so wait till I'm preempted                    
                    k.getNodes();
                } finally {
                    Children.PR.exitWriteAccess();
                }
                done[0] = true;
            }
        };
        t.start();
        tick1.waitOn();
        
        // and also from another thread
        Thread t2 = new Thread("other") {
            @Override
            public void run() {
                k.getNodes(); // will block in getNodes
                done[1] = true;
            }
        };
        t2.start();
        
        Thread.sleep(2000); // give T2 some time ...
        tick2.tick(); // and unfuse T
        
        // wait for other thread
        t.join(2000);
        t2.join(2000);

        t.stop();
        t2.stop();

        assertTrue ("Preempted thread finished correctly", done[0]);
        assertTrue ("Other thread finished correctly", done[1]);
    }

    /**
     * See issue #76614
     */
    public void testNodesCreatedJustOnce() throws Exception {
        Counter children = new Counter(1);
        Node node = new AbstractNode(children);
        children.keys(Arrays.asList(new Object[] {"Add Children"}));
        Node[] nodes = node.getChildren().getNodes(true);
        
        assertEquals("One node returned", 1, nodes.length);
        assertEquals("One node created", 1, children.count);
    }

    public void testRefreshOnFavorites() throws Exception {
        Keys k = new Keys(lazy());
        k.keys("1", "2", "3");
        Node n = createNode(k);

        FilterChildrenEventsTest.Chldrn filterCh = new FilterChildrenEventsTest.Chldrn(n);
        FilterNode fn = new FilterNode(n, filterCh);

        Node[] now = fn.getChildren().getNodes();
        assertEquals("Three", 3, now.length);

        Listener ml = new Listener();
        fn.addNodeListener( ml );

        filterCh.makeInvisible(now[1].getName());
        
        NodeMemberEvent ev = ml.assertRemoveEvent("one remove", 1);
        assertEquals("The removed node is delivered", now[1], ev.getDelta()[0]);

        Node[] after = fn.getChildren().getNodes();
        assertEquals("Just two", 2, after.length);

        assertSame("First node the same", now[0].getName(), after[0].getName());
        assertSame("Last node the same", now[2].getName(), after[1].getName());
    }

    public void testRefreshOnFavoritesAdding() throws Exception {
        Keys k = new Keys(lazy());
        k.keys("1", "2", "3");
        Node n = createNode(k);

        FilterChildrenEventsTest.Chldrn filterCh = new FilterChildrenEventsTest.Chldrn(n);
        filterCh.makeInvisible("2");

        FilterNode fn = new FilterNode(n, filterCh);

        Node[] now = fn.getChildren().getNodes();
        assertEquals("Just two", 2, now.length);

        Listener ml = new Listener();
        fn.addNodeListener( ml );

        filterCh.makeVisible("2");


        Node[] after = fn.getChildren().getNodes();
        assertEquals("Three:\n" + Arrays.asList(after), 3, after.length);

        ml.assertAddEvent("one add", 1);
        assertSame("First node the same", now[0].getName(), after[0].getName());
        assertSame("Last node the same", now[1].getName(), after[2].getName());
    }


    public void testSimulateCreationOfAFormInAFolder() throws Exception {
        class K extends Keys {
            public K(boolean lazy) {
                super(lazy);
            }

            @Override
            protected Node[] createNodes(Object key) {
                int value = Integer.parseInt(key.toString());
                if (value % 2 == 0) {
                    return null;
                } else {
                    return super.createNodes(key);
                }
            }
        }

        K k = new K(lazy());
        Node root = new AbstractNode(k);
        Listener l = new Listener();
        l.disableConsistencyCheck = true;
        root.addNodeListener(l);
        assertEquals("Empty", 0, k.getNodesCount(true));

        k.keys (new String[] { "1", "2", "33", "43", "53" });

        if (lazy()) {
            l.assertAddEvent("Children added", 5);
        } else {
            l.assertAddEvent("Children added", 4);
        }

        Node[] arr = k.getNodes ();
        if (lazy()) {
            l.assertRemoveEvent("Now we found one is not needed", 1);
        }
        assertEquals ("index 2 is not visible", 4, k.getNodesCount(true));


        k.keys (new String[] { "1", "2", "33", "3", "4", "43", "53" });
        if (lazy()) {
            l.assertAddEvent("2 Children added", 2);
        } else {
            l.assertAddEvent("Just 1 child added", 1);
        }

        Node[] newArr = k.getNodes ();
        if (lazy()) {
            l.assertRemoveEvent("Now we found it was a fake, one is gone", 1);
        }
        assertEquals ("index 2 and 4 is not visible", 5, k.getNodesCount(true));

    }

    
    public void testDestroyIsCalledWhenANodeIsRemovedOrig () throws Exception {
        class K extends Keys {
            public K(boolean lazy) {
                super(lazy);
            }
            public Node[] arr;
            public int cnt;

            @Override
            protected Node[] createNodes(Object key) {
                Node[] tmpArray = super.createNodes(key);
                cnt += tmpArray.length;
                return tmpArray;
            }

            @Override
            protected void destroyNodes (Node[] arr) {
                super.destroyNodes (arr);
                assertNull ("No destroy before", this.arr);
                this.arr = arr;
            }
        }

        K k = new K(lazy());
        k.keys (new String[] { "A", "B", "C" });
        Node root = createNode(k);
        holder = root;

        Listener l = new Listener();
        root.addNodeListener(l);

        Node[] arr = root.getChildren().getNodes ();
        assertEquals ("3", 3, arr.length);
        assertNull ("Still no destroy", k.arr);
        assertEquals("3nodes created", 3, k.cnt);
        l.assertNoEvents("No change");

        Listener l0 = new Listener();
        Listener l1 = new Listener();
        Listener l2 = new Listener();
        arr[0].addNodeListener(l0);
        arr[1].addNodeListener(l1);
        arr[2].addNodeListener(l2);

        k.keys (new String[] { "A" });
        l.assertRemoveEvent("one removal of two nodes", new int[] { 1, 2 });
        assertNotNull ("Some destroyed", k.arr);
        assertEquals ("2 destroyed", 2, k.arr.length);
        k.arr = null;
        arr = root.getChildren().getNodes ();
        assertEquals ("! left", 1, arr.length);
        assertEquals("Still just 3 nodes created", 3, k.cnt);

        
        l.assertNoEvents("No next events");

        l0.assertExpectedProperties("No properties delivered");
        l1.assertExpectedProperties("Destroy", "parentNode");
        l2.assertExpectedProperties("Destroy", "parentNode");
        l1.assertDestroyEvent("B destroyed");
        l2.assertDestroyEvent("C destroyed");
        
        WeakReference ref = new WeakReference (arr[0]);
        arr = null;
        assertGC ("Node can be gced", ref);

        assertNull ("Garbage collected nodes are not notified", k.arr);

        assertEquals("Again only 3 nodes created", 3, k.cnt);
        l.assertNoEvents("No next events");
    }

    public void testSlowRemoveNotify () throws Throwable {
        class K extends Keys {
            int addNotify;
            int removeNotify;

            CountDownLatch slowRemoveNotify = new CountDownLatch(1);
            private Throwable ex;

            public K(boolean lazy) {
                super(lazy);
            }
            public Node[] arr;

            @Override
            protected void addNotify() {
                try {
                    assertFalse("We do not have write access", MUTEX.isWriteAccess());
                } catch (Throwable catched) {
                    this.ex = catched;
                }
                addNotify++;
                keys("A");
            }

            @Override
            protected void removeNotify() {
                removeNotify++;
                try {
                    slowRemoveNotify.await(1000, TimeUnit.MILLISECONDS);
                    assertTrue("We have write access", MUTEX.isWriteAccess());
                } catch (Throwable catched) {
                    this.ex = catched;
                }

                keys();
            }
        }
        
        K k = new K(lazy());
        Node root = createNode(k);
        
        Node[] n = root.getChildren().getNodes ();
        
        n = k.getNodes ();
        assertEquals ("1 left", 1, n.length);
        assertEquals("Once add notify", 1, k.addNotify);
        
        WeakReference ref = new WeakReference (n[0]);
        n = null;
        assertGC ("Node can be gced", ref);

        for (int i = 0; i < 5; i++) {
            if (k.removeNotify == 1) {
                break;
            }
            Thread.sleep(100);
        }

        assertEquals("Remove notify is being called", 1, k.removeNotify);

        n = root.getChildren().getNodes();
        assertEquals("Still remains one", 1, n.length);
        assertEquals("Name A", "A", n[0].getName());

        k.slowRemoveNotify.countDown();
        waitActiveReferenceQueue();

        for (int i = 0; i < 5; i++) {
            n = root.getChildren().getNodes();
            assertEquals("Still one node", 1, n.length);
            assertEquals("Still named right", "A", n[0].getName());
            Thread.sleep(100);
        }

        assertEquals("At the end there needs to be more addNotify than removeNotify", 2, k.addNotify);
        
        if (k.ex != null) {
            throw  k.ex;
        }
    }

    public void testGarbageCollectProblemsWithFilterNodes () throws Throwable {
        class K extends Keys {
            int addNotify;
            int removeNotify;
            Set<Reference<Node>> created = new HashSet<Reference<Node>>();

            private Throwable ex;

            public K(boolean lazy) {
                super(lazy);
            }

            @Override
            protected void addNotify() {
                try {
                    assertFalse("We do not have write access", MUTEX.isWriteAccess());
                } catch (Throwable catched) {
                    this.ex = catched;
                }
                addNotify++;
                keys("A");
            }

            @Override
            protected void removeNotify() {
                removeNotify++;
                try {
                    assertTrue("We have write access", MUTEX.isWriteAccess());
                } catch (Throwable catched) {
                    this.ex = catched;
                }

                keys();
            }

            @Override
            protected Node[] createNodes(Object key) {
                Node n = super.createNodes(key)[0].cloneNode();
                created.add(new WeakReference<Node>(n));
                return new Node[] { n };
            }
        }

        K k = new K(lazy());
        Node root = createNode(k);
        holder = root;

        Node[] arr = root.getChildren().getNodes ();
        assertEquals ("1 left", 1, arr.length);
        assertEquals("Once add notify", 1, k.addNotify);

        for (Reference<Node> ref : k.created) {
            try {
                assertGC ("Node can be gced", ref);
            } catch (AssertionFailedError ex) {
                // OK
                continue;
            }
            fail("Should not be GCed: " + k.created);
        }

        for (int i = 0; i < 5; i++) {
            if (k.removeNotify != 0) {
                break;
            }
            Thread.sleep(100);
        }

        assertEquals("Remove notify not called", 0, k.removeNotify);

        arr = root.getChildren().getNodes();
        assertEquals("Still remains one", 1, arr.length);
        assertEquals("Name A", "A", arr[0].getName());

        waitActiveReferenceQueue();

        for (int i = 0; i < 5; i++) {
            arr = k.getNodes();
            assertEquals("Still one node", 1, arr.length);
            assertEquals("Still named right", "A", arr[0].getName());
            Thread.sleep(100);
        }

        assertEquals("At the end there needs to be more addNotify than removeNotify", 1, k.addNotify);
        
        if (k.ex != null) {
            throw  k.ex;
        }
        
        arr = null;

        for (Reference<Node> ref : k.created) {
            assertGC ("Now the original nodes can disappear", ref);
        }
    }

    public void testDestroyIsCalledWhenANodeIsRemoved () throws Exception {
        class K extends Keys {
            public K(boolean lazy) { super(lazy); }
            public Node[] arr;
            
            @Override
            protected void destroyNodes (Node[] arr) {
                super.destroyNodes (arr);
                assertNull ("No destroy before", this.arr);
                this.arr = arr;
            }
        }
        
        K k = new K (lazy());
        k.keys (new String[] { "A", "B", "C" });
        Node node = createNode (k);
        Listener l = new Listener();
        node.addNodeListener(l);
        
        Node[] n = node.getChildren ().getNodes ();
        assertEquals ("3", 3, n.length);
        assertNull ("Still no destroy", k.arr);
        l.assertNoEvents("No events yet");
        
        k.keys (new String[] { "A" });
        assertNotNull ("Some destroyed", k.arr);
        assertEquals ("2 destroyed", 2, k.arr.length);
        NodeMemberEvent ev = l.assertRemoveEvent("Two nodes removed", 2);
        assertEquals("First one is B:\n" + ev, n[1], ev.getDelta()[0]);
        assertEquals("Snd one is C", n[2], ev.getDelta()[1]);
        ev = null;
        k.arr = null;
        n = node.getChildren ().getNodes ();
        assertEquals ("1 left", 1, n.length);
        l.assertNoEvents("No new events");
        
        WeakReference ref = new WeakReference (n[0]);
        n = null;
        assertGC ("Node can be gced", ref);

        waitActiveReferenceQueue();
        
        assertNull ("Garbage collected nodes are not notified", k.arr);
        l.assertNoEvents("GC does not generate events");

        assertEquals("Count remains one", 1, node.getChildren().getNodesCount());
        // emptied
        k.keys();
        l.assertRemoveEvent("Removing last child", 1);

        k.keys("X");
        l.assertAddEvent("Adding X", 1);

        waitActiveReferenceQueue();

        Node[] arr = node.getChildren().getNodes();
        assertEquals("One node is there", 1, arr.length);
        assertEquals("named as x", "X", arr[0].getName());
    }

    public void testDestroyIsCalledWhenEntryIsRefreshed () throws Exception {
        class K extends Keys {
            public Node[] arr;
            public Node[] toReturn;
            
            public K(boolean lazy) { super(lazy); }
            @Override
            protected void destroyNodes (Node[] arr) {
                super.destroyNodes (arr);
                assertNull ("No destroy before", this.arr);
                this.arr = arr;
            }
            
            @Override
            protected Node[] createNodes (Object key) {
                if (toReturn != null) {
                    return toReturn;
                } else {
                    return super.createNodes (key);
                }
            }
        }
        
        K k = new K (lazy());
        k.keys (new String[] { "A", "B", "C" });
        Node node = createNode (k);
        
        Node[] n = node.getChildren ().getNodes ();
        assertEquals ("3", 3, n.length);
        assertNull ("Still no destroy", k.arr);
        
        Listener l = new Listener();
        n[0].addNodeListener(l);

        k.toReturn = new Node[0];
        k.refreshKey ("A");
        
        assertEquals("One property change expected", 1, l.props.size());
        final PropertyChangeEvent ev = l.props.get(0);
        assertEquals("PROP_PARENT_NODE expected", Node.PROP_PARENT_NODE, ev.getPropertyName());
        assertNotNull("Old parent value should be nonnull", ev.getOldValue());
        assertNull("New parent value should be null", ev.getNewValue());
        assertNull("Parent should be null", n[0].getParentNode());
        
        assertNotNull ("Some destroyed", k.arr);
        assertEquals ("1 destroyed", 1, k.arr.length);
        k.arr = null;
        n = node.getChildren ().getNodes ();
        assertEquals ("B C", 2, n.length);
        
        WeakReference ref = new WeakReference (n[0]);
        n = null;
        assertGC ("Node can be gced", ref);

        waitActiveReferenceQueue();
        
        assertNull ("Garbage collected nodes are not notified", k.arr);
    }
    
    public void testRefreshKeyCanBeCalledFromReadAccess () throws Exception {
        final String[] keys = { "Hrebejk", "Tulach" };
        final Keys k = new Keys(lazy(), keys);

        CharSequence err = Log.enable("org.openide.util.Mutex", Level.WARNING);

        Keys.MUTEX.readAccess (new Runnable () {
            public void run () {
                k.refreshKey ("Hrebejk");
            }
        });

        if (err.toString ().indexOf ("readAccess") >= 0) {
            fail ("Should not contain messages about going from read to write access: " + err);
        }
    }

    public void testProblemsWithRefereshAfterInitializationInReadAccessIssue140313() throws Exception {
        class K extends Keys implements Runnable {
            Node root = createNode(this);
            Node[] inRead;

            public K() {
                super(lazy());
            }

            @Override
            protected void addNotify() {
                keys("A", "B", "C");
            }

            public void run() {
                inRead = root.getChildren().getNodes();
            }
        }

        K keys = new K();
        Listener l = new Listener();
        keys.root.addNodeListener(l);
        K.MUTEX.readAccess(keys);

        assertEquals("Empty in ReadAccess", 0, keys.inRead.length);

        Node[] now = keys.root.getChildren().getNodes();
        assertEquals("Three nodes", 3, now.length);

        l.assertAddEvent("Addition notified", 3);
        l.assertNoEvents("That is all");
    }

    private static Object holder;
    public void testGCKeys () throws Exception {
        class K extends Children.Keys {
            int counterAdd = 0;
            int counterRem = 0;
            int counterDestroy = 0;
            Object key;
            
            Reference createdNode;
            
            K(boolean lazy, Object keyObject) {
                super(lazy);
                key = keyObject;
            }
            
            @Override
            protected void addNotify() {
                counterAdd++;
                setKeys(Collections.singleton(key));
            }

            @Override
            protected void removeNotify() {
                counterRem++;
                setKeys(Collections.EMPTY_LIST);
                key = null;
            }

            @Override
            protected void destroyNodes(Node[] arr) {
                counterDestroy++;
                super.destroyNodes(arr);
            }
            
            protected Node[] createNodes(Object k) {
                Node n = Node.EMPTY.cloneNode();
                assertNull ("Just one created node", createdNode);
                createdNode = new WeakReference (n);
                return new Node[] { n };
            }
        }
        
        Object myKey = new Object();
        K temp = new K(lazy(), myKey);
        holder = temp;

        Node node = createNode (temp);
        
        assertEquals("not touched", 0, temp.counterAdd);
        assertEquals("not touched", 0, temp.counterRem);
        assertEquals("not touched", 0, temp.counterDestroy);
        
        Node[] arr = node.getChildren ().getNodes();
        
        assertEquals("initialized", 1, temp.counterAdd);
        assertEquals("not touched", 0, temp.counterRem);
        assertEquals("one item", 1, arr.length);
        assertEquals("not touched", 0, temp.counterDestroy);

        WeakReference ref = new WeakReference(arr[0]);
        arr = null;
        assertGC("node freed", ref);
        assertGC("and this one as well", temp.createdNode);
        
        waitActiveReferenceQueue();

        assertEquals("initialized", 1, temp.counterAdd);
        assertEquals("removed", 1, temp.counterRem);
        assertEquals("no destroy on GC nodes", 0, temp.counterDestroy);

        ref = new WeakReference(myKey);
        myKey = null;
        assertGC("key freed", ref);

        temp.key = new Object();
        temp.createdNode = null;
        arr = node.getChildren ().getNodes();

        assertEquals("initialized 2nd time", 2, temp.counterAdd);
        assertEquals("not touched 2nd time", 1, temp.counterRem);
        assertEquals("one item", 1, arr.length);
        assertEquals("still no destroy", 0, temp.counterDestroy);
    }

    static void waitActiveReferenceQueue() throws InterruptedException {
        class W extends WeakReference<Object> implements Runnable {
            boolean cleaned;
            boolean finalized;

            public W(Object obj) {
                super(obj, Utilities.activeReferenceQueue());
            }
            public synchronized void run() {
                cleaned = true;
                notifyAll();
            }

            public synchronized void notifyFinalize() {
                finalized = true;
                notifyAll();
            }
            {
                new Object() {
                    @Override
                    public void finalize() {
                        notifyFinalize();
                    }
                };
            }

            public void await() throws InterruptedException {
                for (;;) {
                    synchronized (this) {
                        if (cleaned && finalized) {
                            return;
                        }
                        wait(100);
                    }
                    System.gc();
                    System.runFinalization();
                }
            }
        }
        Object obj = new Object();
        W waitRef = new W(obj);
        obj = null;
        waitRef.await();
    }    
    public void testIndexesAreCorrectWhenInsertingAnObject () {
        doIndexesAreCorrectWhenInsertingAnObject ("B", 3);
    }
    
    public void testIndexesAreCorrectWhenInsertingAnObjectNext () {
        doIndexesAreCorrectWhenInsertingAnObject ("1", 4);
    }
    
    private void doIndexesAreCorrectWhenInsertingAnObject (String add, int index) {
        Keys k = new Keys(lazy());
        Node n = createNode (k);
        
        assertEquals ("Empty", 0, n.getChildren ().getNodesCount ());
        Node[] arr = n.getChildren ().getNodes ();
        
        Listener l = new Listener ();
        n.addNodeListener(l);
        
        
        ArrayList list = new ArrayList ();
        list.add ("A");
        list.add ("B");
        list.add ("1");
        list.add ("2");
        list.add ("3");
        k.setKeys(list);
        
        l.assertAddEvent ("Added 5", new int[] { 0, 1, 2, 3, 4 });
        Node[] newArr = n.getChildren ().getNodes ();
        
        list.add (2, "0");
        list.add (3, add);
        k.setKeys (list);
        
        l.assertAddEvent ("Added 2", new int[] { 2, index });
        l.assertNoEvents("And that is all");
    }
    
    public void testAddingALotOfItems () {
        Keys k = new Keys (lazy());
        Node n = createNode (k);
        
        assertEquals ("Empty", 0, n.getChildren ().getNodesCount ());
        Node[] arr = n.getChildren ().getNodes ();
        
        Listener l = new Listener ();
        n.addNodeListener(l);
        
        
        ArrayList list = new ArrayList ();
        list.add ("A");
        list.add ("B");
        list.add ("1");
        list.add ("2");
        list.add ("3");
        k.setKeys(list);
        
        l.assertAddEvent ("Added 5", new int[] { 0, 1, 2, 3, 4 });
        Node[] newArr = n.getChildren ().getNodes ();
        
        list.add (2, "0.6");
        list.add (2, "0.5");
        list.add (2, "0.4");
        list.add (2, "0.3");
        list.add (2, "0.2");
        list.add (2, "0.1");
        k.setKeys (list);
        
        l.assertAddEvent ("Added 6", new int[] { 2, 3, 4, 5, 6, 7 });
        l.assertNoEvents("And that is all");
    }
    
    /** Check whether a nodes appears when keys are set.
    */
    public void testGetNodes () throws Exception {
        String[] arr = { "1", "2", "3", "4" };
        Children ch = new Keys(lazy(), arr);
        checkNames (createNode (ch), arr);
    }
    
    public void testGetNodesOptimalOn0to1 () throws Exception {
        class K extends Keys {
            private boolean acceptOposite;
            
            public K () {
                super (lazy());
            }
            
            @Override
            protected Node[] createNodes (Object k) {
                boolean a = k instanceof String;
                if (acceptOposite) {
                    a = !a;
                }
                if (a) {
                    return super.createNodes (k);
                } else {
                    return null;
                }
            }
        }
        
        
        K k = new K ();
        Object[] keys = { "Ahoj", new Integer (3), "Kuk", new Integer (2) };
        k.setKeys (keys);
        Node[] arr = k.getNodes (true);
        assertEquals ("Just two", 2, arr.length);
        assertEquals ("Ahoj", arr[0].getName ());
        assertEquals ("Kuk", arr[1].getName ());
        
        try {
            Children.PR.enterWriteAccess ();
            k.acceptOposite = true;
            k.setKeys (new Object[0]);
            k.setKeys (keys);
        } finally {
            Children.PR.exitWriteAccess ();
        }
        
        arr = k.getNodes (true);
        assertEquals ("Just two: " + Arrays.asList(arr), 2, arr.length);
        assertEquals ("3", arr[0].getName ());
        assertEquals ("2", arr[1].getName ());
    }
    
    public void testNoReorderEventJustAdd () {
        Keys k = new Keys (lazy(), new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        Node[] toPreventGC = n.getChildren ().getNodes ();
        assertEquals (10, toPreventGC.length);
        
        k.keys (new String[] { "31", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
        
        l.assertAddEvent ("Adding one index", 1);
        l.assertNoEvents ("And that is all");
    }
    
    public void testComplexReorderAndAddAndRemoveEvent () {
        Keys k = new Keys (lazy(), new String[] { "remove", "1", "0" });
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        Node[] toPreventGC = n.getChildren ().getNodes ();
        assertEquals (3, toPreventGC.length);
        
        k.keys (new String[] { "0", "1", "add" });

        l.assertRemoveEvent ("Removed index 0", 1);
        l.assertReorderEvent ("0->1 and 1->0", new int[] { 1, 0 });
        l.assertAddEvent ("Adding at index 2", 1);
        l.assertNoEvents ("And that is all");

        Node[] arr = n.getChildren ().getNodes ();
        assertEquals (3, arr.length);
        assertEquals ("0", arr[0].getName ());
        assertEquals ("1", arr[1].getName ());
        assertEquals ("add", arr[2].getName ());
    }
    
    /** Check refresh of nodes.
     */
    public void testResetOfNodes () throws Exception {
        String[] arr;
        Keys ch = new Keys(lazy());
        arr = new String[] { "1", "2" };
        Node node = createNode (ch);
        
        ch.keys (arr);
        checkNames (node, arr);
        
        arr = new String[] { "X", "Y", "Z" };
        ch.keys (arr);
        checkNames (node, arr);
        
        Collections.reverse (Arrays.asList (arr));
        ch.keys (arr);
        checkNames (node, arr);
    }
    
    /** Tests whether nodes in children have the correct names on correct places
     * @param ch children
     * @param arr names
     */
    private void checkNames (Node ch, String[] arr) {
        Node[] nodes = ch.getChildren ().getNodes ();
        
        if (nodes.length != arr.length) {
            fail ("Keys: " + arr.length + " Nodes: " + nodes.length);
        }
        
        for (int i = 0; i < arr.length; i++) {
            if (!nodes[i].getName ().equals (arr[i])) {
                fail (i + "th: name: " + nodes[i].getName () + " key: " + arr[i]);
            }
        }
    }
    
    public void testGetAndRemove () {
        Keys k = new Keys(lazy(), new String[] { "1", "2" });
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        
        Node d1 = n.getChildren ().getNodeAt (0);
        assertEquals ("Name is 1", "1", d1.getName ());
        
        k.keys (new String[] { "2" });
        
        l.assertRemoveEvent ("One node removed", 1);
        assertNull (d1.getParentNode ());
    }

    @SuppressWarnings("deprecated")
    public void testSetBefore () {
        Keys k = new Keys (lazy(), new String[] { "Ahoj" });
        Node[] arr = new Node[] { Node.EMPTY.cloneNode () };
        boolean res = k.add (arr);
        if (lazy()) {
            assertFalse("Not supported in lazy mode", res);
            assertFalse("Removal is also unsupported", k.remove(arr));
            return;
        }
        
        Node node = createNode (k);
        
        arr = node.getChildren ().getNodes ();
        assertEquals (2, arr.length);
        assertEquals ("First is ahoj", "Ahoj", arr[0].getName ());
        assertEquals ("2nd equlas to EMPTY", Node.EMPTY, arr[1]);
        
        k.setBefore (true);
        
        arr = node.getChildren ().getNodes ();
        assertEquals (2, arr.length);
        assertEquals ("2nd is ahoj", "Ahoj", arr[1].getName ());
        assertEquals ("First equals to EMPTY", Node.EMPTY, arr[0]);
        
        k.setBefore (false);
        
        arr = node.getChildren ().getNodes ();
        assertEquals (2, arr.length);
        assertEquals ("First is ahoj", "Ahoj", arr[0].getName ());
        assertEquals ("2nd equlas to EMPTY", Node.EMPTY, arr[1]);
        
    }
    
    public void testOperationsOnEqualNumberOfMinAndMax () throws Exception {
        Keys k = new Keys (lazy());
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        
        assertEquals ("No nodes", 0, n.getChildren ().getNodesCount ());
        k.keys (new String[] { "Ahoj", "Kuk" });
        
        NodeMemberEvent mem = l.assertEvents (1);

        assertEquals ("Two nodes", 2, n.getChildren ().getNodesCount ());
    }

    public void testChildrenFireCorrectEvents () throws Exception {
        ChildrenKeysTest.Keys k = new ChildrenKeysTest.Keys(lazy(), new String[] { "1", "2", "3" });
        Node fn = createNode (k);
        ChildrenKeysTest.Listener l = new ChildrenKeysTest.Listener ();
        fn.addNodeListener (l);
        
        assertEquals ("Three", 3, fn.getChildren ().getNodesCount ());
        
        Node n1, n2;
        n1 = fn.getChildren ().getNodeAt (0);
        n2 = fn.getChildren ().getNodeAt (2);
        assertEquals ("Name is 1", "1", n1.getName ());
        assertEquals ("Name is 3", "3", n2.getName ());
        
        k.keys (new String[] { "1", "3"});
        
        NodeMemberEvent ev = l.assertEvents (1);
        assertEquals ("Removal event type", NodeMemberEvent.class, ev.getClass ());
        int[] removed = ev.getDeltaIndices ();
        assertEquals ("One node gone", 1, removed.length);
        assertEquals ("Middle one", 1, removed[0]);
    }

    public void testRemovedNodesWillHaveParentRemoved () throws Exception {
        Keys k = new Keys (lazy(), new String[] { "Ahoj", "Kuk" });
        Node n = createNode (k);
        
        Node[] arr = n.getChildren ().getNodes ();
        assertEquals ("Two", 2, arr.length);
        assertEquals ("Parent1", n, arr[0].getParentNode ());
        assertEquals ("Parent2", n, arr[1].getParentNode ());
        
        k.keys (new String[0]);
        
        Node[] newArr = n.getChildren ().getNodes();
        assertEquals ("Zero is current number of children", 0, newArr.length);
        assertNull ("Old node parent zeroed1", arr[0].getParentNode ());
        assertNull ("Old node parent zeroed2", arr[1].getParentNode ());
    }
    
    
    public void testRefreshClearsSizeWithoutLimits () throws Exception {
        doRefreshClearsSize (0, 0);
    }
    
    public void testRefreshClearsSizeOto1 () throws Exception {
        doRefreshClearsSize (0, 1);
    }
    
    private void doRefreshClearsSize (int min, int max) throws Exception {
        final String[] NULL = { "Null" };
        
        Keys k = new Keys (lazy()) {
            @Override
            protected Node[] createNodes (Object o) {
                if (o == NULL) {
                    if (NULL[0] == null) {
                        return null;
                    }
                    o = NULL[0];
                }
                return super.createNodes (o);
            }
        };
        Node n = createNode (k);
        Listener l = new Listener ();
        n.addNodeListener (l);
        
        assertEquals ("No nodes", 0, n.getChildren ().getNodesCount ());
        k.setKeys (new Object[] { "Ahoj", NULL });
        l.assertAddEvent("Two nodes added", 2);
        l.assertNoEvents("No more events after add");
        assertEquals ("Two nodes", 2, n.getChildren ().getNodesCount ());
        NULL[0] = null;
        k.refreshKey (NULL);
        l.assertRemoveEvent("One node removed", 1);
        assertEquals ("Just one node", 1, n.getChildren ().getNodesCount ());
        l.assertNoEvents("This is all that has been delivered");
    }

    public void testAddingJavaAndFormAtTheEndOfExistingFolder() throws Exception {
        class K extends Keys {
            int cnt;

            K() {
                super(lazy());
            }

            @Override
            protected void addNotify() {
                keys("a", "b", "c");
            }

            @Override
            protected Node[] createNodes(Object key) {
                if (key.toString().startsWith("-")) {
                    return null;
                }
                cnt++;
                return super.createNodes(key);
            }

            public void assertLazyCount(String msg, int exp) {
                if (lazy()) {
                    assertEquals(msg, exp, cnt);
                }
            }
        }

        K lch = new K();
        Node ta = createNode(lch);

        Listener l = new Listener();
        l.disableConsistencyCheck = true;
        ta.addNodeListener(l);

        assertEquals("Child check", "c", ta.getChildren().getNodeAt(2).getName());
        lch.assertLazyCount("Counter shouldbe 1", 1);

        assertEquals("Child check", "b", ta.getChildren().getNodeAt(1).getName());
        lch.assertLazyCount("Counter shouldbe 2", 2);

        assertEquals("Child check", "a", ta.getChildren().getNodeAt(0).getName());
        lch.assertLazyCount("Counter shouldbe all", 3);

        lch.keys("a", "b", "c", "x", "-x");
        
        lch.assertLazyCount("Counter shouldstill be 3", 3);
        if (lazy()) {
            l.assertAddEvent("Two children added", new int[] { 3, 4 });
            assertEquals("Size is 5", 5, ta.getChildren().getNodesCount());
        } else {
            l.assertAddEvent("Only one child added", new int[] { 3 });
            assertEquals("Size is 4", 4, ta.getChildren().getNodesCount());
        }
        
        lch.keys("a", "b", "c", "x", "-x");

        l.assertNoEvents("No changes now");
        
        lch.assertLazyCount("Counter shouldstill be 3", 3);
        if (lazy()) {
            assertEquals("Size is 5", 5, ta.getChildren().getNodesCount());
            assertTrue("Child is empty", ta.getChildren().getNodeAt(4) instanceof EntrySupport.Lazy.DummyNode);
            assertEquals("We have just four children", 4, ta.getChildren().getNodesCount());
            assertEquals("Three nodes created, still", 3, lch.cnt);
            l.assertRemoveEvent("Removal of 4th node", new int[] { 4 });
        } else {
            assertEquals("Size is 4", 4, ta.getChildren().getNodesCount());
        }

        
        assertEquals("x Child check", "x", ta.getChildren().getNodeAt(3).getName());
        l.assertNoEvents("All events processed");
        lch.keys("a", "b", "c", "x", "-x", "-y", "y");

        if (lazy()) {
            assertEquals("We have two more children", 6, ta.getChildren().getNodesCount());
            l.assertAddEvent("Added nodes", new int[] { 4, 5 });
            assertEquals("Child is y", "y", ta.getChildren().getNodeAt(4).getName());
            l.assertRemoveEvent("One remove event", new int[] { 4 });
        } else {
            assertEquals("We have one more children", 5, ta.getChildren().getNodesCount());
            l.assertAddEvent("Added nodes", new int[] { 4 });
            assertEquals("Child is y", "y", ta.getChildren().getNodeAt(4).getName());
        }

        l.assertNoEvents("All events delivered");
    }
    
    public void testChildrensAreNotCreatedOnNodeRemoval() {
        final Integer cnt = new Integer(0);
        class K extends Keys {

            K() {
                super(lazy());
            }

            @Override
            protected void addNotify() {
                fail("Nothing should be created on node removal.");
            }
        }
        
        class PK extends Keys {

            PK() {
                super(lazy());
            }

            @Override
            protected void addNotify() {
                keys("A", "B", "C");
            }

            @Override
            protected Node[] createNodes(Object key) {
                AbstractNode an = new AbstractNode(new K());
                an.setName(key.toString());
                return new Node[]{an};
            }
        }

        Listener l = new Listener();
        PK lch = new PK();
        Node ta = createNode(lch);
        Node[] nodes = lch.getNodes();
        nodes[2].addNodeListener(l);
        lch.keys("A", "B");
    }
    
    
    public void testRefreshKey1() {
        doTestRefreshKey(true);
    }
    
    public void testRefreshKey2() {
        doTestRefreshKey(false);
    }

    public void doTestRefreshKey(boolean initViaGetNodes) {
        class MyNode extends AbstractNode {
            MyNode() {
                super(Children.LEAF);
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof MyNode ? getName().equals(((MyNode) obj).getName()) : false;
            }

            @Override
            public int hashCode() {
                return getName().hashCode();
            }
            
        }
        class K extends Keys {
            boolean canReturnNull = true;
            String append = "";

            K() {
                super(lazy());
            }

            @Override
            protected void addNotify() {
                keys("a", "-b", "c");
            }

            @Override
            protected Node[] createNodes(Object key) {
                if (canReturnNull && key.toString().startsWith("-")) {
                    return null;
                }
                MyNode n = new MyNode();
                n.setName(key.toString() + append);
                return new Node[]{n};
            }
        }

        K ch = new K();
        Node root = createNode(ch);

        Listener l = new Listener();
        l.disableConsistencyCheck = true;

        root.addNodeListener(l);
        if (initViaGetNodes) {
            Node[] nodes = root.getChildren().getNodes();
            assertEquals("Two nodes created", 2, nodes.length);
            assertEquals("1st is a", "a", nodes[0].getName());
            assertEquals("2nd is c", "c", nodes[1].getName());
        } else {
            root.getChildren().getNodesCount();
            Node node = ch.getNodeAt(1);
            assertEquals("1st is a", "a", ch.getNodeAt(0).getName());
            assertEquals("2nd is c", "c", ch.getNodeAt(1).getName());
        }

        if (lazy()) {
            l.assertRemoveEvent("Empty should be removed", new int[] {1});
        } else {
            l.assertNoEvents("No events so far");
        }

        ch.refreshKey("-b");
        l.assertNoEvents("No event should be fired");
        
        ch.canReturnNull = false;
        ch.refreshKey("-b");
        NodeMemberEvent ev = l.assertAddEvent("One add event", new int[] {1});
        assertEquals("-b", ev.getDelta()[0].getName());
        assertEquals("3 nodes expected", 3, ch.getNodesCount());
        assertEquals("1st is a", "a", ch.getNodeAt(0).getName());
        assertEquals("2nd is -b", "-b", ch.getNodeAt(1).getName());
        assertEquals("3rd is c", "c", ch.getNodeAt(2).getName());
        
        ch.refreshKey("-b");
        l.assertNoEvents("No event should be fired");

        ch.append = "_new";
        ch.refreshKey("-b");
        ev = l.assertRemoveEvent("One add event", new int[] {1});
        assertEquals("-b", ev.getDelta()[0].getName());
        ev = l.assertAddEvent("One add event", new int[] {1});
        assertEquals("-b_new", ev.getDelta()[0].getName());
        assertEquals("3 nodes expected", 3, ch.getNodesCount());
        ch.append = "";
        
        ch.canReturnNull = true;
        ch.refreshKey("-b");
        ev = l.assertRemoveEvent("One add event", new int[] {1});
        assertEquals("-b_new", ev.getDelta()[0].getName());
        assertEquals("2 nodes expected", 2, ch.getNodesCount());
    }

    public void testForIssue143777() {

        class K extends Keys {
            boolean canReturnNull = false;

            K() {
                super(lazy());
            }

            @Override
            protected void addNotify() {
                keys("a");
            }

            @Override
            protected Node[] createNodes(Object key) {
                if (canReturnNull && key.toString().startsWith("-")) {
                    return null;
                }
                return super.createNodes(key);
            }
        }

        K ch = new K();
        Node root = createNode(ch);
        Node[] nodes = root.getChildren().getNodes();
        
        ch.canReturnNull = true;
        ch.keys("-b", "a");
        nodes = root.getChildren().getNodes();
        ch.canReturnNull = false;
        ch.keys("a");
        ch.keys("a", "b");
        nodes = root.getChildren().getNodes();
        assertEquals("1st is a", "a", nodes[0].getName());
        assertEquals("2nd is b", "b", nodes[1].getName());
    }    
    
    public void testGetNodesFromTwoThreads57769WhenBlockingAtRightPlaces() throws Exception {
        final Ticker tick = new Ticker();
        final List who = new java.util.Vector();
        
        final int[] count = new int[1];
        class ChildrenKeys extends Children.Keys {
            protected Node[] createNodes(Object key) {
                who.add(new Exception("Creating: " + count[0] + " for key: " + key));
                count[0]++;
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                try {Thread.sleep(2000);}catch(InterruptedException e) {}
                return n == null ? new Node[]{} : new Node[]{n};
            }

            @Override
            protected void addNotify() {
                setKeys(Arrays.asList(new Object[] {"1", "2"}));
            }            
        }
        
        // Get optimal nodes from other thread
        
        final String TOKEN = new String("TOKEN to wait on");
        final String THREAD_NAME = Thread.currentThread().getName();
        
        class Th extends Thread {
            public Th() {
                super("THREAD");
            }
            Node node;
            Node[] keep;
            Error err;
            
            @Override
            public void run() {
                synchronized (TOKEN) {
                }
     
                try {
                    keep = node.getChildren().getNodes(true);
                } catch (Error e) {
                    this.err = e;
                }
            }
        }
        
        Th t = new Th();
        ChildrenKeys children = new ChildrenKeys();
        t.node = new AbstractNode(children);

        Node[] remember;
        synchronized (TOKEN) {
            t.start();

            // this call will result in "point X".notifyAll() when the 
            // main thread reaches "setEntries"
            remember = t.node.getChildren().getNodes();
            
            TOKEN.notifyAll();
        }
        
        // wait for other thread
        t.join();
        
        if (2 != count[0]) {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            w.write("Just two nodes created: " + count[0] + " stacks:\n");
            Iterator it = who.iterator();
            while (it.hasNext()) {
                Exception e = (Exception)it.next();
                e.printStackTrace(pw);
            }
            pw.close();
            
            fail(w.toString());
        }
        
        if (t.err != null) {
            throw t.err;
        }
        
        //fail("Ok");
    }

    
    /** Sample keys.
    */
    public static class Keys extends Children.Keys {
        /** Constructor.
         */
        public Keys (boolean lazy, String... args) {
            super(lazy);
            if (args != null && args.length > 0) {
                setKeys (args);
            }
        }
        
        /** Changes the keys.
         */
        public void keys (String... args) {
            super.setKeys (args);
        }

        /** Changes the keys.
         */
        public void keys (Collection args) {
            super.setKeys (args);
        }
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no 
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode (Children.LEAF);
            an.setName (key.toString ());

            return new Node[] { an };
        }

    }
    
    public static class Counter extends Children.Keys {
        int limit;
        int count = 0;
        
        public Counter (int limit) {
            this.limit = limit;
        }
        
        
        /** Changes the keys.
         */
        public void keys (String[] args) {
            super.setKeys (args);
        }

        /** Changes the keys.
         */
        public void keys (Collection args) {
            super.setKeys (args);
        }
        
        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no 
         *   nodes for this key
         */
        protected Node[] createNodes(Object key) {
            synchronized(this) {
                count++;
                assertTrue("# of created nodes", count <= limit);
            }
            AbstractNode an = new AbstractNode (Children.LEAF);
            an.setName (key.toString ());

            return new Node[] { an };
        }

    }


    static class Listener extends NodeAdapter {
        private LinkedList events = new LinkedList ();
        LinkedList<PropertyChangeEvent> props = new LinkedList<PropertyChangeEvent>();
        LinkedList<NodeEvent> destroyed = new LinkedList<NodeEvent>();
        boolean disableConsistencyCheck;
        private Exception when;
        

        @Override
        public void nodeDestroyed(NodeEvent ev) {
            destroyed.add(ev);
        }
        
        @Override
        public void childrenRemoved (NodeMemberEvent ev) {
            if (!disableConsistencyCheck) {
                ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            }
            events.add (ev);
            when = new Exception("childrenRemoved");
        }

        @Override
        public void childrenAdded (NodeMemberEvent ev) {
            if (!disableConsistencyCheck) {
                ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            }
            events.add (ev);
            when = new Exception("childrenAdded");
        }

        @Override
        public void childrenReordered (NodeReorderEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            events.add (ev);
            when = new Exception("childrenReordered");
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            props.add(ev);
        }
        
        public NodeMemberEvent assertEvents (int number) {
            if (events.size () != number) {
                fail ("There should be " + number + " event(s) but was :" + events.size () + ":\n" + events);
            }
            return (NodeMemberEvent)events.removeFirst ();
        }

        public NodeEvent assertDestroyEvent(String msg) {
            if (destroyed.isEmpty()) {
                fail(msg);
            }
            return destroyed.poll();
        }
        
        public NodeMemberEvent assertAddEvent (String msg, int cnt) {
            return checkOneEvent (msg, cnt, null, true);
        }
        public NodeMemberEvent assertRemoveEvent (String msg, int cnt) {
            return checkOneEvent (msg, cnt, null, false);
        }
        public NodeMemberEvent assertAddEvent (String msg, int[] indexes) {
            return checkOneEvent (msg, indexes.length, indexes, true);
        }
        public NodeMemberEvent assertRemoveEvent (String msg, int[] indexes) {
            return checkOneEvent (msg, indexes.length, indexes, false);
        }

        public void assertExpectedProperties(String msg, String ... expectedProps) {
            for (int i = 0; i < expectedProps.length; i++) {
                if (props.get(i).getPropertyName().equals(expectedProps[i])) {
                    continue;
                }
                List<String> names = new ArrayList<String>();
                for (PropertyChangeEvent ev : props) {
                    names.add(ev.getPropertyName());
                }

                fail("\nExpecting properties: " + Arrays.asList(expectedProps) +
                     "\nBut getting         : " + names
                );
            }
            props.clear();
        }
        
        public void assertReorderEvent (String msg, int[] perm) {
            assertFalse (msg + " Cannot be empty", events.isEmpty ());
            Object o = events.removeFirst ();
            assertEquals (msg + " Reoder event", NodeReorderEvent.class, o.getClass ());
            NodeReorderEvent m = (NodeReorderEvent)o;
            
            int[] arr = m.getPermutation ();
            FAIL: if (arr.length == perm.length) {
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] != perm[i]) break FAIL;
                }
                return;
            }
            
            StringBuffer sb = new StringBuffer ();
            
            for (int i = 0; i < arr.length; i++) {
                sb.append ("at [" + i + "]: ");
                if (arr.length > i) {
                    sb.append (arr[i]);
                } else {
                    sb.append ("none");
                }
                sb.append (" = ");
                if (perm.length > i) {
                    sb.append (perm[i]);
                } else {
                    sb.append ("none");
                }
            }
            fail (sb.toString ());
        }
        
        private NodeMemberEvent checkOneEvent (String msg, int cnt, int[] indexes, boolean add) {
            assertFalse (msg + " Cannot be empty", events.isEmpty ());
            Object o = events.removeFirst ();
            assertEquals (msg + " Remove event", NodeMemberEvent.class, o.getClass ());
            NodeMemberEvent m = (NodeMemberEvent)o;
            if (add) {
                assertTrue (msg + " is add ", m.isAddEvent ());
            } else {
                assertFalse (msg + " Is remove", m.isAddEvent ());
            }
            if (!disableConsistencyCheck) {
                assertEquals (msg + " Right count of removed nodes", cnt, m.getDelta ().length);
            }
            assertEquals (msg + " Right count of removed indicies", cnt, m.getDeltaIndices ().length);
            
            if (indexes != null) {
                StringBuffer f = new StringBuffer ();
                boolean ok = true;
                for (int i = 0; i < cnt; i++) {
                    f.append ("[" + i + "]: " + indexes[i] + " = " + m.getDeltaIndices ()[i] + "\n");
                    ok = ok && indexes[i] == m.getDeltaIndices ()[i];
                }
                if (!ok) {
                    fail ("Indicies are not correct:\n" + f);
                }
            }
            return m;
        }
        
        public void assertNoEvents (String msg) {
            if (events.size() > 0) {
                AssertionFailedError err = new AssertionFailedError(msg + ":\n" + events);
                err.initCause(when);
                err.setStackTrace(when.getStackTrace());
                throw err;
            }
        }
        
    } // end of Listener
    Ticker t1 = new Ticker();
        
    private static class Ticker {
        boolean state;
        
        public void waitOn() {
            synchronized(this) {
                while (!state) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new InternalError();
                    }
                }
                state = false; // reusable
            }
        }
        
        public void tick() {
            synchronized(this) {
                state = true;
                notifyAll();
            }
        }
    }

    
}
