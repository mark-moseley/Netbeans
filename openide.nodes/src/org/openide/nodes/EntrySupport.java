/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Children.Entry;
import org.openide.util.Utilities;

/**
 *
 * @author t_h
 */
abstract class EntrySupport {
    private static final Reference<ChildrenArray> EMPTY = new WeakReference<ChildrenArray>(null);

    /** children we are attached to */
    public Children children;

    /** array of children Reference (ChildrenArray) */
    Reference<ChildrenArray> array = EMPTY;

    /** collection of all entries */
    protected List<Entry> entries = Collections.emptyList();

    /** Creates a new instance of EntrySupport */
    protected EntrySupport(Children children) {
        this.children = children;
    }

    //
    // API methods to be called from Children
    //
    public abstract int getNodesCount(boolean optimalResult);

    public abstract Node[] getNodes(boolean optimalResult);

    /** Getter for a node at given position. If node with such index
     * does not exists it should return null.*/
    public abstract Node getNodeAt(int index);

    public abstract Node[] testNodes();

    public abstract boolean isInitialized();

    abstract void notifySetEntries();

    abstract void setEntries(Collection<? extends Entry> entries);

    /** Access to copy of current entries.
     * @return copy of entries in the objects
     */
    protected final List<Entry> getEntries() {
        return new ArrayList<Entry>(this.entries);
    }

    /** Abililty to create a snaphshot
     * @return immutable and unmodifiable list of Nodes that represent the children at current moment
     */
    abstract List<Node> createSnapshot();

    /** Refreshes content of one entry. Updates the state of children appropriately. */
    abstract void refreshEntry(Entry entry);


    /** Default support that just fires changes directly to children and is suitable
     * for simple mappings.
     */
    static class Default extends EntrySupport {

        /** mapping from entries to info about them */
        private Map<Entry, Info> map;
        private static final Object LOCK = new Object();
        private static final Logger LOG_GET_ARRAY = Logger.getLogger("org.openide.nodes.Children.getArray"); // NOI18N
        private Thread initThread;
        private boolean inited = false;


        public Default(Children ch) {
            super(ch);
        }

        public boolean isInitialized() {
            return inited;
            /*ChildrenArray arr = array.get();
            return (arr != null) && arr.isInitialized();*/
        }

        protected List<Node> createSnapshot() {
            return new DefaultSnapshot(getNodes(), array.get());
        }

        public final Node[] getNodes() {
            //Thread.dumpStack();
            //System.err.println(off + "getNodes: " + getNode ());
            boolean[] results = new boolean[2];

            for (;;) {
                results[1] = isInitialized();

                // initializes the ChildrenArray possibly calls
                // addNotify if this is for the first time
                ChildrenArray tmpArray = getArray(results); // fils results[0]

                Node[] nodes;

                try {
                    Children.PR.enterReadAccess();
                    nodes = tmpArray.nodes();
                } finally {
                    Children.PR.exitReadAccess();
                }

                final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("  length     : " + (nodes == null ? "nodes is null" : nodes.length)); // NOI18N
                    LOG_GET_ARRAY.fine("  entries    : " + entries); // NOI18N
                    LOG_GET_ARRAY.fine("  init now   : " + isInitialized()); // NOI18N

                }
                // if not initialized that means that after
                // we computed the nodes, somebody changed them (as a
                // result of addNotify) => we have to compute them
                // again
                if (results[1]) {
                    // otherwise it is ok.
                    return nodes;
                }

                if (results[0]) {
                    // looks like the result cannot be computed, just give empty one
                    notifySetEntries();
                    return (nodes == null) ? new Node[0] : nodes;
                }
            }
        }

        public Node[] getNodes(boolean optimalResult) {
            ChildrenArray hold;
            Node find;
            if (optimalResult) {
                final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("computing optimal result");// NOI18N

                }
                hold = getArray(null);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("optimal result is here: " + hold);// NOI18N

                }
                find = children.findChild(null);
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("Find child got: " + find); // NOI18N

                }
            }

            return getNodes();
        }

        public final int getNodesCount(boolean optimalResult) {
            return getNodes(optimalResult).length;
        }

        @Override
        public Node getNodeAt(int index) {
            Node[] nodes = getNodes();
            return index < nodes.length ? nodes[index] : null;
        }


        /** Computes the nodes now.
         */
        final Node[] justComputeNodes() {
            if (map == null) {
                map = Collections.synchronizedMap(new HashMap<Entry, Info>(17));

            //      debug.append ("Map initialized\n"); // NOI18N
            //      printStackTrace();
            }

            List<Node> l = new LinkedList<Node>();
            for (Entry entry : entries) {
                Info info = findInfo(entry);

                try {
                    l.addAll(info.nodes());
                } catch (RuntimeException ex) {
                    NodeOp.warning(ex);
                }
            }

            Node[] arr = l.toArray(new Node[l.size()]);

            // initialize parent nodes
            for (int i = 0; i < arr.length; i++) {
                Node n = arr[i];
                n.assignTo(children, i);
                n.fireParentNodeChange(null, children.parent);
            }

            return arr;
        }

        /** Finds info for given entry, or registers
         * it, if not registered yet.
         */
        private Info findInfo(Entry entry) {
            synchronized (map) {
                Info info = map.get(entry);

                if (info == null) {
                    info = new Info(entry);
                    map.put(entry, info);

                //      debug.append ("Put: " + entry + " info: " + info); // NOI18N
                //      debug.append ('\n');
                //      printStackTrace();
                }
                return info;
            }
        }

        //
        // Entries
        //

        private boolean mustNotifySetEnties = false;

        void notifySetEntries() {
            mustNotifySetEnties = true;
        }

        protected void setEntries(Collection<? extends Entry> entries) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
            // current list of nodes
            ChildrenArray holder = array.get();

            if (IS_LOG_GET_ARRAY) {
                LOG_GET_ARRAY.fine("setEntries for " + this + " on " + Thread.currentThread()); // NOI18N

                LOG_GET_ARRAY.fine("       values: " + entries); // NOI18N

                LOG_GET_ARRAY.fine("       holder: " + holder); // NOI18N

            }

            Node[] current = holder == null ? null : holder.nodes();
            if (mustNotifySetEnties) {
                if (holder == null) {
                    holder = getArray(null);
                }
                if (current == null) {
                    holder.entrySupport = this;
                    current = holder.nodes();
                }
            } else if (holder == null || current == null) {
                this.entries = new ArrayList<Entry>(entries);
                if (map != null) {
                    map.keySet().retainAll(new HashSet<Entry>(entries));
                }
                return;
            }

            // if there are old items in the map, remove them to
            // reflect current state
            map.keySet().retainAll(new HashSet<Entry>(this.entries));

            // what should be removed
            Set<Entry> toRemove = new LinkedHashSet<Entry>(this.entries);
            Set<Entry> entriesSet = new HashSet<Entry>(entries);
            toRemove.removeAll(entriesSet);

            if (!toRemove.isEmpty()) {
                // notify removing, the set must be ready for
                // callbacks with questions
                updateRemove(current, toRemove);
                current = holder.nodes();
            }

            // change the order of entries, notifies
            // it and again brings children to up-to-date state
            Collection<Info> toAdd = updateOrder(current, entries);

            if (!toAdd.isEmpty()) {
                // toAdd contains Info objects that should
                // be added
                updateAdd(toAdd, new ArrayList<Entry>(entries));
            }
        }

        private void checkInfo(Info info, Entry entry, Collection<? extends Entry> entries, java.util.Map<Entry, Info> map) {
            if (info == null) {
                throw new IllegalStateException(
                        "Error in " + getClass().getName() + " with entry " + entry + " from among " + entries + " in " + map + // NOI18N
                        " probably caused by faulty key implementation." + // NOI18N
                        " The key hashCode() and equals() methods must behave as for an IMMUTABLE object" + // NOI18N
                        " and the hashCode() must return the same value for equals() keys."); // NOI18N

            }
        }

        /** Removes the objects from the children.
         */
        private void updateRemove(Node[] current, Set<Entry> toRemove) {
            List<Node> nodes = new LinkedList<Node>();

            for (Entry en : toRemove) {
                Info info = map.remove(en);

                //debug.append ("Removed: " + en + " info: " + info); // NOI18N
                //debug.append ('\n');
                //printStackTrace();
                checkInfo(info, en, null, map);

                nodes.addAll(info.nodes());
            }

            // modify the current set of entries and empty the list of nodes
            // so it has to be recreated again
            //debug.append ("Current : " + this.entries + '\n'); // NOI18N
            this.entries.removeAll(toRemove);

            //debug.append ("Removing: " + toRemove + '\n'); // NOI18N
            //debug.append ("New     : " + this.entries + '\n'); // NOI18N
            //printStackTrace();
            clearNodes();
            notifyRemove(nodes, current);
        }

        /** Updates the order of entries.
         * @param current current state of nodes
         * @param entries new set of entries
         * @return list of infos that should be added
         */
        private List<Info> updateOrder(Node[] current, Collection<? extends Entry> newEntries) {
            List<Info> toAdd = new LinkedList<Info>();

            // that assignes entries their begining position in the array
            // of nodes
            java.util.Map<Info, Integer> offsets = new HashMap<Info, Integer>();

            {
                int previousPos = 0;

                for (Entry entry : entries) {
                    Info info = map.get(entry);
                    checkInfo(info, entry, entries, map);

                    offsets.put(info, previousPos);

                    previousPos += info.length();
                }
            }

            // because map can contain some additional items,
            // that has not been garbage collected yet,
            // retain only those that are in current list of
            // entries
            map.keySet().retainAll(new HashSet<Entry>(entries));

            int[] perm = new int[current.length];
            int currentPos = 0;
            int permSize = 0;
            List<Entry> reorderedEntries = null;

            for (Entry entry : newEntries) {
                Info info = map.get(entry);

                if (info == null) {
                    // this info has to be added
                    info = new Info(entry);
                    toAdd.add(info);
                } else {
                    int len = info.length();

                    if (reorderedEntries == null) {
                        reorderedEntries = new LinkedList<Entry>();
                    }

                    reorderedEntries.add(entry);

                    // already there => test if it should not be reordered
                    Integer previousInt = offsets.get(info);

                    /*
                    if (previousInt == null) {
                    System.err.println("Offsets: " + offsets);
                    System.err.println("Info: " + info);
                    System.err.println("Entry: " + info.entry);
                    System.err.println("This entries: " + this.entries);
                    System.err.println("Entries: " + entries);
                    System.err.println("Map: " + map);

                    System.err.println("---------vvvvv");
                    System.err.println(debug);
                    System.err.println("---------^^^^^");

                    }
                     */
                    int previousPos = previousInt;

                    if (currentPos != previousPos) {
                        for (int i = 0; i < len; i++) {
                            perm[previousPos + i] = 1 + currentPos + i;
                        }

                        permSize += len;
                    }
                }

                currentPos += info.length();
            }

            if (permSize > 0) {
                // now the perm array contains numbers 1 to ... and
                // 0 one places where no permutation occures =>
                // decrease numbers, replace zeros
                for (int i = 0; i < perm.length; i++) {
                    if (perm[i] == 0) {
                        // fixed point
                        perm[i] = i;
                    } else {
                        // decrease
                        perm[i]--;
                    }
                }

                // reorderedEntries are not null
                this.entries = reorderedEntries;

                //      debug.append ("Set3: " + this.entries); // NOI18N
                //      printStackTrace();
                // notify the permutation to the parent
                clearNodes();

                //System.err.println("Paremutaiton! " + getNode ());
                Node p = children.parent;

                if (p != null) {
                    p.fireReorderChange(perm);
                }
            }

            return toAdd;
        }

        /** Updates the state of children by adding given Infos.
         * @param infos list of Info objects to add
         * @param entries the final state of entries that should occur
         */
        private void updateAdd(Collection<Info> infos, List<Entry> entries) {
            List<Node> nodes = new LinkedList<Node>();
            for (Info info : infos) {
                nodes.addAll(info.nodes());
                map.put(info.entry, info);

            //      debug.append ("updateadd: " + info.entry + " info: " + info + '\n'); // NOI18N
            //      printStackTrace();
            }

            this.entries = entries;

            //      debug.append ("Set4: " + entries); // NOI18N
            //      printStackTrace();
            clearNodes();
            notifyAdd(nodes);
        }

        /** Refreshes content of one entry. Updates the state of children
         * appropriately.
         */
        final void refreshEntry(Entry entry) {
            // current list of nodes
            ChildrenArray holder = array.get();

            if (holder == null) {
                return;
            }

            Node[] current = holder.nodes();

            if (current == null) {
                // the initialization is not finished yet =>
                return;
            }

            // because map can contain some additional items,
            // that has not been garbage collected yet,
            // retain only those that are in current list of
            // entries
            map.keySet().retainAll(new HashSet<Entry>(this.entries));

            Info info = map.get(entry);

            if (info == null) {
                // refresh of entry that is not present =>
                return;
            }

            Collection<Node> oldNodes = info.nodes();
            Collection<Node> newNodes = info.entry.nodes();

            if (oldNodes.equals(newNodes)) {
                // nodes are the same =>
                return;
            }

            Set<Node> toRemove = new HashSet<Node>(oldNodes);
            toRemove.removeAll(new HashSet<Node>(newNodes));

            if (!toRemove.isEmpty()) {
                // notify removing, the set must be ready for
                // callbacks with questions
                // modifies the list associated with the info
                oldNodes.removeAll(toRemove);
                clearNodes();

                // now everything should be consistent => notify the remove
                notifyRemove(toRemove, current);
                current = holder.nodes();
            }

            List<Node> toAdd = refreshOrder(entry, oldNodes, newNodes);
            info.useNodes(newNodes);

            if (!toAdd.isEmpty()) {
                // modifies the list associated with the info
                clearNodes();
                notifyAdd(toAdd);
            }
        }

        /** Updates the order of nodes after a refresh.
         * @param entry the refreshed entry
         * @param oldNodes nodes that are currently in the list
         * @param newNodes new nodes (defining the order of oldNodes and some more)
         * @return list of infos that should be added
         */
        private List<Node> refreshOrder(Entry entry, Collection<Node> oldNodes, Collection<Node> newNodes) {
            List<Node> toAdd = new LinkedList<Node>();
            Set<Node> oldNodesSet = new HashSet<Node>(oldNodes);
            Set<Node> toProcess = new HashSet<Node>(oldNodesSet);

            Node[] permArray = new Node[oldNodes.size()];
            Iterator<Node> it2 = newNodes.iterator();

            int pos = 0;

            while (it2.hasNext()) {
                Node n = it2.next();

                if (oldNodesSet.remove(n)) {
                    // the node is in the old set => test for permuation
                    permArray[pos++] = n;
                } else {
                    if (!toProcess.contains(n)) {
                        // if the node has not been processed yet
                        toAdd.add(n);
                    } else {
                        it2.remove();
                    }
                }
            }

            // JST: If you get IllegalArgumentException in following code
            // then it can be cause by wrong synchronization between
            // equals and hashCode methods. First of all check them!
            int[] perm = NodeOp.computePermutation(oldNodes.toArray(new Node[oldNodes.size()]), permArray);

            if (perm != null) {
                // apply the permutation
                clearNodes();

                // temporarily change the nodes the entry should use
                findInfo(entry).useNodes(Arrays.asList(permArray));
                Node p = children.parent;
                if (p != null) {
                    p.fireReorderChange(perm);
                }
            }
            return toAdd;
        }

        /** Notifies that a set of nodes has been removed from
         * children. It is necessary that the system is already
         * in consistent state, so any callbacks will return
         * valid values.
         *
         * @param nodes list of removed nodes
         * @param current state of nodes
         * @return array of nodes that were deleted
         */
        Node[] notifyRemove(Collection<Node> nodes, Node[] current) {
            //System.err.println("notifyRemove from: " + getNode ());
            //System.err.println("notifyRemove: " + nodes);
            //System.err.println("Current     : " + Arrays.asList (current));
            //Thread.dumpStack();
            //Keys.last.printStackTrace();
            // [TODO] Children do not have always a parent
            // see Services->FIRST ($SubLevel.class)
            // during a deserialization it may have parent == null
            Node[] arr = nodes.toArray(new Node[nodes.size()]);

            if (children.parent != null) {
                // fire change of nodes
                children.parent.fireSubNodesChange(false, arr, current);

                // fire change of parent
                Iterator it = nodes.iterator();

                while (it.hasNext()) {
                    Node n = (Node) it.next();
                    n.deassignFrom(children);
                    n.fireParentNodeChange(children.parent, null);
                }
            }
            children.destroyNodes(arr);
            return arr;
        }

        /** Notifies that a set of nodes has been add to
         * children. It is necessary that the system is already
         * in consistent state, so any callbacks will return
         * valid values.
         *
         * @param nodes list of removed nodes
         */
        void notifyAdd(Collection<Node> nodes) {
            // notify about parent change
            for (Node n : nodes) {
                n.assignTo(children, -1);
                n.fireParentNodeChange(null, children.parent);
            }

            Node[] arr = nodes.toArray(new Node[nodes.size()]);

            Node n = children.parent;

            if (n != null) {
                n.fireSubNodesChange(true, arr, null);
            }
        }
        //
        // ChildrenArray operations call only under lock
        //
        /** @return either nodes associated with this children or null if
         * they are not created
         */
        public Node[] testNodes() {
            ChildrenArray arr = array.get();
            return (arr == null) ? null : arr.nodes();
        }

        /** Obtains references to array holder. If it does not exist, it is created.
         *
         * @param cannotWorkBetter array of size 1 or null, will contain true, if
         *    the getArray cannot be initialized (we are under read access
         *    and another thread is responsible for initialization, in such case
         *    give up on computation of best result
         */
        private ChildrenArray getArray(boolean[] cannotWorkBetter) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);

            ChildrenArray arr;
            boolean doInitialize = false;
            synchronized (LOCK) {
                arr = array.get();

                if (arr == null) {
                    arr = new ChildrenArray();

                    // register the array with the children
                    registerChildrenArray(arr, true);
                    doInitialize = true;
                    initThread = Thread.currentThread();
                }
            }

            if (doInitialize) {
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("Initialize " + this + " on " + Thread.currentThread()); // NOI18N

                }

                // this call can cause a lot of callbacks => be prepared
                // to handle them as clean as possible
                try {
                    children.callAddNotify();

                    if (IS_LOG_GET_ARRAY) {
                        LOG_GET_ARRAY.fine("addNotify successfully called for " + this + " on " + Thread.currentThread()); // NOI18N

                    }
                } finally {
                    boolean notifyLater;
                    notifyLater = Children.MUTEX.isReadAccess();

                    if (IS_LOG_GET_ARRAY) {
                        LOG_GET_ARRAY.fine(
                                "notifyAll for " + this + " on " + Thread.currentThread() + "  notifyLater: " + notifyLater); // NOI18N

                    }

                    // now attach to entrySupport, so when entrySupport == null => we are
                    // not fully initialized!!!!
                    inited = true;
                    arr.entrySupport = this;
                    class SetAndNotify implements Runnable {

                        public ChildrenArray toSet;
                        public Children whatSet;

                        public void run() {
                            synchronized (LOCK) {
                                initThread = null;
                                LOCK.notifyAll();
                            }
                            if (IS_LOG_GET_ARRAY) {
                                LOG_GET_ARRAY.fine("notifyAll done"); // NOI18N
                            }
                        }
                    }

                    SetAndNotify setAndNotify = new SetAndNotify();
                    setAndNotify.toSet = arr;
                    setAndNotify.whatSet = children;

                    if (notifyLater) {
                        // the notify to the lock has to be done later than
                        // setKeys is executed, otherwise the result of addNotify
                        // might not be visible to other threads
                        // fix for issue 50308
                        Children.MUTEX.postWriteRequest(setAndNotify);
                    } else {
                        setAndNotify.run();
                    }
                }
            } else {
                // otherwise, if not initialize yet (arr.children) wait
                // for the initialization to finish, but only if we can wait
                if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess() || (initThread == Thread.currentThread())) {
                    // fail, we are in read access
                    if (IS_LOG_GET_ARRAY) {
                        LOG_GET_ARRAY.log(Level.FINE,
                                "cannot initialize better " + this + // NOI18N
                                " on " + Thread.currentThread() + // NOI18N
                                " read access: " + Children.MUTEX.isReadAccess() + // NOI18N
                                " initThread: " + initThread, // NOI18N
                                new Exception("StackTrace") // NOI18N
                                );
                    }

                    if (cannotWorkBetter != null) {
                        cannotWorkBetter[0] = true;
                    }

                    return arr;
                }

                // otherwise we can wait
                synchronized (LOCK) {
                    while (initThread != null) {
                        if (IS_LOG_GET_ARRAY) {
                            LOG_GET_ARRAY.fine(
                                    "waiting for children for " + this + // NOI18N
                                    " on " + Thread.currentThread() // NOI18N
                                    );
                        }

                        try {
                            LOCK.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine(
                            " children are here for " + this + // NOI18N
                            " on " + Thread.currentThread() + // NOI18N
                            " children " + children);
                }
            }

            return arr;
        }

        /** Clears the nodes
         */
        private void clearNodes() {
            ChildrenArray arr = array.get();

            //System.err.println(off + "  clearNodes: " + getNode ());
            if (arr != null) {
                // clear the array
                arr.clear();
            }
        }


        /** Registration of ChildrenArray.
         * @param chArr the associated ChildrenArray
         * @param weak use weak or hard reference
         */
        final void registerChildrenArray(final ChildrenArray chArr, boolean weak) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
            if (IS_LOG_GET_ARRAY) {
                LOG_GET_ARRAY.fine("registerChildrenArray: " + chArr + " weak: " + weak); // NOI18N

            }
            synchronized (LOCK) {
                this.array = new ChArrRef(chArr, weak);
            }
            if (IS_LOG_GET_ARRAY) {
                LOG_GET_ARRAY.fine("pointed by: " + chArr + " to: " + this.array); // NOI18N
            }
        }

        /** Finalized.
         */
        final void finalizedChildrenArray(Reference caller) {
            final boolean IS_LOG_GET_ARRAY = LOG_GET_ARRAY.isLoggable(Level.FINE);
            // usually in removeNotify setKeys is called => better require write access
            try {
                Children.PR.enterWriteAccess();

                if (IS_LOG_GET_ARRAY) {
                    LOG_GET_ARRAY.fine("previous array: " + array + " caller: " + caller);
                }
                synchronized (LOCK) {
                    if (array == caller && children.entrySupport == this) {
                        // really finalized and not reconstructed
                        mustNotifySetEnties = false;
                        array = EMPTY;
                        inited = false;
                        children.callRemoveNotify();
                        assert array == EMPTY;
                    }
                }
            } finally {
                Children.PR.exitWriteAccess();
            }
        }
        /** Forces finalization of nodes for given info.
         * Called from finalizer of Info.
         */
        final void finalizeNodes() {
            ChildrenArray arr = array.get();

            if (arr != null) {
                arr.finalizeNodes();
            }
        }

        /** Information about an entry. Contains number of nodes,
         * position in the array of nodes, etc.
         */
        final class Info extends Object {

            int length;
            final Entry entry;

            public Info(Entry entry) {
                this.entry = entry;
            }

            /** Finalizes the content of ChildrenArray.
             */
            @Override
            protected void finalize() {
                finalizeNodes();
            }

            public Collection<Node> nodes() {
                // forces creation of the array
                ChildrenArray arr = getArray(null);
                return arr.nodesFor(this);
            }

            public void useNodes(Collection<Node> nodes) {
                // forces creation of the array
                ChildrenArray arr = getArray(null);
                arr.useNodes(this, nodes);

                // assign all there nodes the new children
                for (Node n : nodes) {
                    n.assignTo(EntrySupport.Default.this.children, -1);
                    n.fireParentNodeChange(null, children.parent);
                }
            }

            public int length() {
                return length;
            }

            @Override
            public String toString() {
                return "Children.Info[" + entry + ",length=" + length + "]"; // NOI18N
            }
        }
        static class DefaultSnapshot extends  AbstractList<Node> {
            private Node[] nodes;
            Object holder;
            public DefaultSnapshot(Node[] nodes, ChildrenArray cha) {
                this.nodes = nodes;
                this.holder = cha;
            }

            public Node get(int index) {
                return nodes != null && index < nodes.length ? nodes[index] : null;
            }

            public int size() {
                return nodes != null ? nodes.length : 0;
            }
        }

        private class ChArrRef extends WeakReference<ChildrenArray>
        implements Runnable {
            private final ChildrenArray chArr;

            public ChArrRef(ChildrenArray referent, boolean lazy) {
                super(referent, Utilities.activeReferenceQueue());
                this.chArr = lazy ? null : referent;
                referent.pointedBy(this);
            }

            @Override
            public ChildrenArray get() {
                return chArr != null ? chArr : super.get();
            }

            public void run() {
                finalizedChildrenArray(this);
            }
        }
    }

    static class Lazy extends EntrySupport {
        private Map<Entry, EntryInfo> entryToInfo = new HashMap<Entry, EntryInfo>();

        /** entries with node*/
        private List<Entry> visibleEntries = Collections.emptyList();

        private static final Logger LAZY_LOG = Logger.getLogger("org.openide.nodes.Children.getArray"); // NOI18N

        private static final int prefetchCount = Math.max(Integer.getInteger("org.openide.explorer.VisualizerNode.prefetchCount", 50), 0);  // NOI18N

        public Lazy(Children ch) {
            super(ch);
        }

        private final Object LOCK = new Object();
        private boolean initInProgress = false;
        private boolean inited = false;
        private Thread initThread;
        public boolean checkInit() {
            if (inited) {
                return true;
            }
            boolean doInit = false;
            synchronized (LOCK) {
                if (!initInProgress) {
                    doInit = true;
                    initInProgress = true;
                    initThread = Thread.currentThread();
                }
            }

            if (doInit) {

                try {
                    children.callAddNotify();
                } finally {
                    class Notify implements Runnable {
                        public void run() {
                            synchronized (LOCK) {
                                initThread = null;
                                LOCK.notifyAll();
                            }
                        }
                    }
                    Notify notify = new Notify();
                    inited = true;
                    if (Children.MUTEX.isReadAccess()) {
                        Children.MUTEX.postWriteRequest(notify);
                    } else {
                        notify.run();
                    }
                }
            } else {
                if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess() || (initThread == Thread.currentThread())) {
                    // we cannot wait
                    notifySetEntries();
                    return false;
                }

                // otherwise we can wait
                synchronized (LOCK) {
                    while (initThread != null) {
                        try {
                            LOCK.wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
            return true;
        }

        final void registerNode(int delta, EntryInfo who) {
            if (delta == -1) {
                try {
                    Children.PR.enterWriteAccess();
                    boolean zero = false;
                    LAZY_LOG.fine("register node"); // NOI18N
                    synchronized (Lazy.this.LOCK) {
                        int cnt = 0;
                        boolean found = false;
                        cnt += snapshotCount;
                        if (cnt == 0) {
                            for (Entry entry : visibleEntries) {
                                EntryInfo info = entryToInfo.get(entry);
                                if (info.currentNode() != null) {
                                    cnt++;
                                    break;
                                }
                                if (info == who) {
                                    found = true;
                                }
                            }
                        }
                        zero = cnt == 0 && (found || who == null);

                        if (zero) {
                            inited = false;
                            initThread = null;
                            initInProgress = false;
                            if (children != null && children.entrySupport == this) {
                                children.callRemoveNotify();
                            }
                        }
                    }
                } finally {
                    Children.PR.exitWriteAccess();
                }
            }
        }


        @Override
        public Node getNodeAt(int index) {
            if (!checkInit()) {
                return null;
            }
            Node node = null;
            while (true) {
                try {
                    Children.PR.enterReadAccess();
                    if (index >= visibleEntries.size()) {
                        return node;
                    }
                    Entry entry = visibleEntries.get(index);
                    EntryInfo info = entryToInfo.get(entry);
                    node = info.getNode();
                    if (!isDummyNode(node)) {
                        return node;
                    }
                    hideEmpty(null, entry, null);
                } finally {
                    Children.PR.exitReadAccess();
                }
                if (Children.MUTEX.isReadAccess()) {
                    return node;
                }
            }
        }

        @Override
        public Node[] getNodes(boolean optimalResult) {
            if (!checkInit()) {
                return new Node[0];
            }
            if (optimalResult) {
                children.findChild(null);
            }
            while (true) {
                Set<Entry> invalidEntries = null;
                Node[] tmpNodes = null;
                try {
                    Children.PR.enterReadAccess();

                    List<Node> toReturn = new ArrayList<Node>(visibleEntries.size());
                    for (Entry entry : visibleEntries) {
                        EntryInfo info = entryToInfo.get(entry);
                        assert !info.isHidden();
                        Node node = info.getNode();
                        if (isDummyNode(node)) {
                            if (invalidEntries == null) {
                                invalidEntries = new HashSet<Entry>();
                            }
                            invalidEntries.add(entry);
                        }
                        toReturn.add(node);
                    }
                    tmpNodes = toReturn.toArray(new Node[0]);
                    nodesCreated = true;
                    if (invalidEntries == null) {
                        return tmpNodes;
                    }
                    hideEmpty(invalidEntries, null, null);
                } finally {
                    Children.PR.exitReadAccess();
                }

                if (Children.MUTEX.isReadAccess()) {
                    return tmpNodes;
                }
            }
        }

        boolean nodesCreated = false;
        @Override
        public Node[] testNodes() {
            return nodesCreated ? getNodes(false) : null;
        }

        @Override
        public int getNodesCount(boolean optimalResult) {
            checkInit();
            try {
                Children.PR.enterReadAccess();
                return visibleEntries.size();
            } finally {
                Children.PR.exitReadAccess();
            }
        }

        @Override
        public boolean isInitialized() {
            return inited;
        }

        Entry entryForNode(Node key) {
            for (Map.Entry<Entry, EntryInfo> entry : entryToInfo.entrySet()) {
                if (entry.getValue().currentNode() == key) {
                    return entry.getKey();
                }
            }
            return null;
        }
        
        final boolean isDummyNode(Node node) {
            return node.getClass().getName().endsWith("EntrySupport$Lazy$DummyNode");
        }

        @Override
        void refreshEntry(Entry entry) {
            if (!inited) {
                return;
            }
            EntryInfo info = entryToInfo.get(entry);

            if (info == null) {
                // no such entry
                return;
            }

            Node oldNode = info.currentNode();
            Node newNode = info.getNode(true);

            boolean newIsDummy = isDummyNode(newNode);
            if (newIsDummy && info.isHidden()) {
                // dummy is already hidden
                return;
            }

            if (newNode.equals(oldNode)) {
                // same node =>
                return;
            }

            boolean oldIsDummy = info.isHidden() || (oldNode != null && isDummyNode(oldNode));
            if ((oldNode != null && !oldIsDummy) || newIsDummy) {
                removeEntries(null, entry, oldNode, true, false);
                if (newIsDummy) {
                    return;
                }
            }

            // recompute indexes
            int index = 0;
            info.setIndex(-1);
            List<Entry> arr = new ArrayList<Entry>();
            for (Entry tmpEntry : entries) {
                EntryInfo tmpInfo = entryToInfo.get(tmpEntry);
                if (tmpInfo.isHidden()) {
                    continue;
                }
                tmpInfo.setIndex(index++);
                arr.add(tmpEntry);
            }
            visibleEntries = arr;
            fireSubNodesChangeIdx(true, new int[]{info.getIndex()}, entry, createSnapshot(), null);
        }

        private boolean mustNotifySetEnties = false;

        void notifySetEntries() {
            mustNotifySetEnties = true;
        }

        @Override
        void setEntries(Collection<? extends Entry> newEntries) {
            assert entries.size() == entryToInfo.size() : "Entries: " + entries.size() 
                    + "; vis. entries: " + visibleEntries.size() + "; Infos: " + entryToInfo.size();

            if (!mustNotifySetEnties && !inited) {
                entries = new ArrayList<Entry>(newEntries);
                visibleEntries = new ArrayList<Entry>(newEntries);
                entryToInfo.keySet().retainAll(entries);
                for (int i = 0; i < entries.size(); i++) {
                    Entry entry = entries.get(i);
                    EntryInfo info = entryToInfo.get(entry);
                    if (info == null) {
                        info = new EntryInfo(entry);
                        entryToInfo.put(entry, info);
                    }
                    info.setIndex(i);
                }
                return;
            }

            Set<Entry> entriesToRemove = new HashSet<Entry>(entries);
            entriesToRemove.removeAll(newEntries);
            if (!entriesToRemove.isEmpty()) {
                removeEntries(entriesToRemove, null, null, false, false);
            }

            // change the order of entries, notifies
            // it and again brings children to up-to-date state, recomputes indexes
            Collection<Entry> toAdd = updateOrder(newEntries);
            if (!toAdd.isEmpty()) {
                entries = new ArrayList<Entry>(newEntries);
                int[] idxs = new int[toAdd.size()];
                int addIdx = 0;
                int inx = 0;
                boolean createNodes = toAdd.size() == 2 && prefetchCount > 0;
                visibleEntries = new ArrayList<Entry>();
                for (int i = 0; i < entries.size(); i++) {
                    Entry entry = entries.get(i);
                    EntryInfo info = entryToInfo.get(entry);
                    if (info == null) {
                        info = new EntryInfo(entry);
                        entryToInfo.put(entry, info);
                        if (createNodes) {
                            Node n = info.getNode();
                            if (isDummyNode(n)) {
                                // mark as hidden
                                info.setIndex(-2);
                                continue;
                            }
                        }
                        idxs[addIdx++] = inx;
                    }
                    if (info.isHidden()) {
                        continue;
                    }
                    info.setIndex(inx++);
                    visibleEntries.add(entry);
                }
                if (addIdx == 0) {
                    return;
                }
                if (idxs.length != addIdx) {
                    int[] tmp = new int[addIdx];
                    for (int i = 0; i < tmp.length; i++) {
                        tmp[i] = idxs[i];
                    }
                    idxs = tmp;
                }
                fireSubNodesChangeIdx(true, idxs, null, createSnapshot(), null);
            }
        }

        /** Updates the order of entries.
         * @param current current state of nodes
         * @param entries new set of entries
         * @return list of infos that should be added
         */
        private List<Entry> updateOrder(Collection<? extends Entry> newEntries) {
            List<Entry> toAdd = new LinkedList<Entry>();
            int[] perm = new int[visibleEntries.size()];
            int currentPos = 0;
            int permSize = 0;
            List<Entry> reorderedEntries = null;
            List<Entry> newVisible = null;

            for (Entry entry : newEntries) {
                EntryInfo info = entryToInfo.get(entry);
                if (info == null) {
                    // this entry has to be added
                    toAdd.add(entry);
                } else {
                    if (reorderedEntries == null) {
                        reorderedEntries = new LinkedList<Entry>();
                        newVisible = new ArrayList<Entry>();
                    }
                    reorderedEntries.add(entry);
                    if (info.isHidden()) {
                        continue;
                    }
                    newVisible.add(entry);
                    int oldPos = info.getIndex();
                    // already there => test if it should not be reordered
                    if (currentPos != oldPos) {
                        info.setIndex(currentPos);
                        perm[oldPos] = 1 + currentPos;
                        permSize++;
                    }
                    currentPos++;
                }
            }

            if (permSize > 0) {
                // now the perm array contains numbers 1 to ... and
                // 0 one places where no permutation occures =>
                // decrease numbers, replace zeros
                for (int i = 0; i < perm.length; i++) {
                    if (perm[i] == 0) {
                        // fixed point
                        perm[i] = i;
                    } else {
                        // decrease
                        perm[i]--;
                    }
                }

                // reorderedEntries are not null
                entries = reorderedEntries;
                visibleEntries = newVisible;

                Node p = children.parent;
                if (p != null) {
                    p.fireReorderChange(perm);
                }
            }
            return toAdd;
        }

        Node getNode(Entry entry) {
            checkInit();
            try {
                Children.PR.enterReadAccess();
                EntryInfo info = entryToInfo.get(entry);
                if (info == null) {
                    return null;
                }
                Node node = info.getNode();
                return isDummyNode(node) ? null : node;
            } finally {
                Children.PR.exitReadAccess();
            }
        }

        /** @param added added or removed
         *  @param indices list of integers with indexes that changed
         */
        protected void fireSubNodesChangeIdx(boolean added, int[] idxs, Entry sourceEntry, List<Node> current, List<Node> previous) {
            if (children.parent != null) {
                children.parent.fireSubNodesChangeIdx(added, idxs, sourceEntry, current, previous);
            }
        }
        
        /** holds node for entry; 1:1 mapping */
        final class EntryInfo {
            /** corresponding entry */
            final Entry entry;

            /** cached node for this entry */
            private NodeRef refNode;

            /** my index in list of entries */
            private int index = -1;

            public EntryInfo(Entry entry) {
                this.entry = entry;
            }

            final EntryInfo duplicate(Node node) {
                EntryInfo ei = new EntryInfo(entry);
                ei.index = index;
                ei.refNode = node != null ? new NodeRef(node, ei) : refNode;
                return ei;
            }

            final Lazy lazy() {
                return Lazy.this;
            }

            /** Gets or computes the nodes. It holds them using weak reference
             * so they can get garbage collected.
             */
            public final Node getNode() {
                return getNode(false);
            }
            
            private boolean creatingNode = false;
            public final Node getNode(boolean refresh) {
                while (true) {
                    Node node = null;
                    boolean creating = false;
                    synchronized (LOCK) {
                        if (refresh) {
                            refNode = null;
                        }
                        if (refNode != null) {
                            node = refNode.get();
                            if (node != null) {
                                return node;
                            }
                        }
                        if (creatingNode) {
                            try {
                                LOCK.wait();
                            } catch (InterruptedException ex) {
                            }
                        } else {
                            creatingNode = creating = true;
                        }
                    }
                    Collection<Node> nodes = Collections.emptyList();
                    if (creating) {
                        try {
                            nodes = entry.nodes();
                        } catch (RuntimeException ex) {
                            NodeOp.warning(ex);
                        }
                    }
                    synchronized (LOCK) {
                        if (!creating) {
                            if (refNode != null) {
                                node = refNode.get();
                                if (node != null) {
                                    return node;
                                }
                            }
                            // node created by other thread was GCed meanwhile, try once again
                            continue;
                        }
                        if (nodes.size() == 0) {
                            node = new DummyNode();
                        } else {
                            if (nodes.size() > 1) {
                                LAZY_LOG.fine("Number of nodes for Entry: " + entry + " is " + nodes.size() + " instead of 1");
                            }
                            node = nodes.iterator().next();
                        }
                        refNode = new NodeRef(node, this);
                        if (creating) {
                            creatingNode = false;
                            LOCK.notifyAll();
                        }
                    }
                    // assign node to the new children
                    node.assignTo(children, -1);
                    node.fireParentNodeChange(null, children.parent);
                    return node;
                }
            }
            
            /** extract current node (if was already created) */
            Node currentNode() {
                synchronized (LOCK) {
                    return refNode == null ? null : refNode.get();
                }
            }

            final boolean isHidden() {
                return this.index == -2;
            }

            /** Sets the index of the entry. */
            final void setIndex(int i) {
                this.index = i;
            }

            /** Get index. */
            final int getIndex() {
                assert index >= 0 : "When first asked for it has to be set: " + index; // NOI18N
                return index;
            }

            @Override
            public String toString() {
                return "EntryInfo for entry: " + entry + ", node: " + (refNode == null ? null : refNode.get()); // NOI18N
            }

        }
        private static final class NodeRef extends WeakReference<Node> implements Runnable {
            private final EntryInfo info;
            public NodeRef(Node node, EntryInfo info) {
                super(node, Utilities.activeReferenceQueue());
                info.lazy().registerNode(1, info);
                this.info = info;
            }

            public void run() {
                info.lazy().registerNode(-1, info);
            }
        }
        volatile int snapshotCount;

        /** Dummy node class for entries without any node */
        static class DummyNode extends AbstractNode {

            public DummyNode() {
                super(Children.LEAF);
                //setName("---"); // NOI18N
            }
        }     

        private void hideEmpty(final Set<Entry> entries, final Entry entry, final Node oldNode) {
            Children.MUTEX.postWriteRequest(new Runnable() {

                public void run() {
                    removeEntries(entries, entry, oldNode, true, true);
                }
            });
        }

       private void removeEntries(Set<Entry> entriesToRemove, Entry entryToRemove, Node oldNode, boolean justHide, boolean delayed) {
            int index = 0;
            int removedIdx = 0;
            int removedNodesIdx = 0;
            int expectedSize = entriesToRemove != null ? entriesToRemove.size() : 1;
            int[] idxs = new int[expectedSize];

            List<Entry> previousEntries = visibleEntries;
            Map<Entry, EntryInfo> previousInfos = null;
            List<Entry> newEntries = justHide ? null : new ArrayList<Entry>();
            Node[] removedNodes = null;
            visibleEntries = new ArrayList<Entry>();
            for (Entry entry : entries) {
                EntryInfo info = entryToInfo.get(entry);
                boolean remove;
                if (entriesToRemove != null) {
                    remove = entriesToRemove.remove(entry);
                } else {
                    remove = entryToRemove.equals(entry);
                }
                if (remove) {
                    if (info.isHidden()) {
                        if (!justHide) {
                            entryToInfo.remove(entry);
                        }
                        continue;
                    }
                    idxs[removedIdx++] = info.getIndex();
                    if (previousInfos == null) {
                        previousInfos = new HashMap<Entry, EntryInfo>(entryToInfo);
                    }

                    Node node = oldNode == null ? info.currentNode() : oldNode;
                    if (!info.isHidden() && node != null && !isDummyNode(node)) {
                        if (removedNodes == null) {
                            removedNodes = new Node[expectedSize];
                        }
                        removedNodes[removedNodesIdx++] = node;
                    }

                    if (justHide) {
                        EntryInfo dup = info.duplicate(oldNode);
                        previousInfos.put(info.entry, dup);
                        // mark as hidden
                        info.setIndex(-2);
                    } else {
                        entryToInfo.remove(entry);
                    }
                } else {
                    if (!info.isHidden()) {
                        visibleEntries.add(info.entry);
                        info.setIndex(index++);
                    }
                    if (!justHide) {
                        newEntries.add(info.entry);
                    }
                }
            }
            if (!justHide) {
                entries = newEntries;
            }
            if (removedIdx == 0) {
                return;
            }
            if (removedIdx < idxs.length) {
                idxs = (int[]) resizeArray(idxs, removedIdx);
            }
            List<Node> curSnapshot = createSnapshot(visibleEntries, new HashMap<Entry, EntryInfo>(entryToInfo), delayed);
            List<Node> prevSnapshot = createSnapshot(previousEntries, previousInfos, false);
            fireSubNodesChangeIdx(false, idxs, entryToRemove, curSnapshot, prevSnapshot);

            if (removedNodesIdx > 0) {
                if (removedNodesIdx < removedNodes.length) {
                    removedNodes = (Node[]) resizeArray(removedNodes, removedNodesIdx);
                }
                if (children.parent != null) {
                    for (Node node : removedNodes) {
                        node.deassignFrom(children);
                        node.fireParentNodeChange(children.parent, null);
                    }
                }
                children.destroyNodes(removedNodes);
            }
        }

        private static Object resizeArray(Object oldArray, int newSize) {
            int oldSize = java.lang.reflect.Array.getLength(oldArray);
            Class elementType = oldArray.getClass().getComponentType();
            Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
            int preserveLength = Math.min(oldSize, newSize);
            if (preserveLength > 0) {
                System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
            }
            return newArray;
        }

        @Override
        List<Node> createSnapshot() {
            return createSnapshot(visibleEntries, new HashMap<Entry, EntryInfo>(entryToInfo), false);
        }
        
        protected List<Node> createSnapshot(List<Entry> entries, Map<Entry,EntryInfo> e2i, boolean delayed) {
            return delayed ? new DelayedLazySnapshot(entries, e2i) : new LazySnapshot(entries, e2i);
        }
        

        class LazySnapshot extends AbstractList<Node> {
            private final List<Entry> entries;
            private final Map<Entry, EntryInfo> entryToInfo;
            Object holder;      // little hack for FilterNode (to have possibility to hold original snapshot)
            
            public LazySnapshot(List<Entry> entries, Map<Entry,EntryInfo> e2i) {
                snapshotCount++;
                this.entries = entries;
                this.entryToInfo = e2i != null ? e2i : Collections.<Entry, EntryInfo>emptyMap();
            }

            public Node get(int index) {
                Entry entry = entries.get(index);
                EntryInfo info = entryToInfo.get(entry);
                Node node = info.getNode();
                if (isDummyNode(node)) {
                    // force new snapshot
                    hideEmpty(null, entry, null);
                }
                return node;
            }

            @Override
            public String toString() {
                return entries.toString();
            }

            public int size() {
                return entries.size();
            }

            @Override
            protected void finalize() throws Throwable {
                if (--snapshotCount == 0) {
                    registerNode(-1, null);
                }
            }
            
        }
        final class DelayedLazySnapshot extends LazySnapshot {

            public DelayedLazySnapshot(List<Entry> entries, Map<Entry, EntryInfo> e2i) {
                super(entries, e2i);
            }
        }
    }
}
