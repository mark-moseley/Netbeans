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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.openide.util.Enumerations;
import org.openide.util.Mutex;

/** 
* Factory for the child Nodes of a Node.  Every Node has a Children object.
* Children are initially un-initialized, and child Nodes are created on
* demand when, for example, the Node is expanded in an Explorer view.
* If you know your Node has no child nodes, pass <code>Children.LEAF</code>.
* Typically a Children object will create a Collection of objects from
* some data model, and create one or more Nodes for each object on demand.
* 
* If initializing the list of children of a Node is time-consuming (i.e. it
* does I/O, parses a file or some other expensive operation), implement
* ChildFactory and pass it to Children.create (theFactory, true) to have
* the child nodes be computed asynchronously on a background thread.
*
* <p>In almost all cases you want to subclass ChildFactory and pass it to
* Children.create(), or subclass {@link Children.Keys}.
* Subclassing <code>Children</code> directly is not recommended.
* 
* @author Jaroslav Tulach
*/
public abstract class Children extends Object {
    /** A package internal accessor object to provide priviledged
     * access to children.
     */
    static final Mutex.Privileged PR = new Mutex.Privileged();

    /** Lock for access to hierarchy of all node lists.
    * Anyone who needs to ensure that there will not
    * be shared accesses to hierarchy nodes can use this
    * mutex.
    * <P>
    * All operations on the hierarchy of nodes (add, remove, etc.) are
    * done in the {@link Mutex#writeAccess} method of this lock, so if someone
    * needs for a certain amount of time to forbid modification,
    * he can execute his code in {@link Mutex#readAccess}.
    */
    public static final Mutex MUTEX = new Mutex(PR);

    /** The object representing an empty set of children. Should
    * be used to represent the children of leaf nodes. The same
    * object may be used by all such nodes.
    */
    public static final Children LEAF = new Empty();
    
    /** access to entries/nodes */
    private EntrySupport entrySupport;

    /** parent node for all nodes in this list (can be null) */
    Node parent;


    /*
      private StringBuffer debug = new StringBuffer ();

      private void printStackTrace() {
        Exception e = new Exception ();
        java.io.StringWriter w1 = new java.io.StringWriter ();
        java.io.PrintWriter w = new java.io.PrintWriter (w1);
        e.printStackTrace(w);
        w.close ();
        debug.append (w1.toString ());
        debug.append ('\n');
      }
    */

    /** Constructor.
    */
    public Children() {
        this(false);
    }

    Children(boolean lazy) {
        lazySupport = lazy;
    }

    /**
     * Initializes entry support.
     */
    final EntrySupport entrySupport() {
        synchronized (Children.class) {
            if (entrySupport == null) {
                entrySupport = createEntrySource();
                postInitializeEntrySupport();
            }
            return entrySupport;
        }
    }
    
    final boolean lazySupport;
    /**
     * Creates appropriate entry support for this children.
     * Overriden in Children.Keys to sometimes make lazy support.
     */
    EntrySupport createEntrySource() {
        return lazySupport ? new EntrySupport.Lazy(this) : new EntrySupport.Default(this);
        //return new EntrySupport.Lazy(this);
    }

    /**
     * Does further initialization of entry support. Is called just once, under internal
     * lock so subclasses should behave sane. Can be overriden just in this 
     * package, until found that this is needed somewhere else. 
     */
    void postInitializeEntrySupport() {
    }
    
    /** Setter of parent node for this list of children. Each children in the list
    * will have this node set as parent. The parent node will return nodes in
    * this list as its children.
    * <P>
    * This method is called from the Node constructor
    *
    * @param n node to attach to
    * @exception IllegalStateException when this object is already used with
    *    different node
    */
    final void attachTo(final Node n) throws IllegalStateException {
        // special treatment for LEAF object.
        if (this == LEAF) {
            // do not attaches the node because the LEAF cannot have children
            // and that is why it need not set parent node for them
            return;
        }

        synchronized (this) {
            if (parent != null) {
                // already used
                throw new IllegalStateException(
                    "An instance of Children may not be used for more than one parent node."
                ); // NOI18N
            }

            // attach itself as a node list for given node
            parent = n;
        }

        // this is the only place where parent is changed,
        // but only under readAccess => double check if
        // it happened correctly
        try {
            PR.enterReadAccess();

            Node[] nodes = testNodes();

            if (nodes == null) {
                return;
            }

            // fire the change
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                node.assignTo(Children.this, i);
                node.fireParentNodeChange(null, parent);
            }
        } finally {
            PR.exitReadAccess();
        }
    }

    /** Called when the node changes it's children to different nodes.
    *
    * @param n node to detach from
    * @exception IllegalStateException if children were already detached
    */
    final void detachFrom() {
        // special treatment for LEAF object.
        if (this == LEAF) {
            // no need to do anything
            return;
        }

        Node oldParent = null;

        synchronized (this) {
            if (parent == null) {
                // already detached
                throw new IllegalStateException("Trying to detach children which do not have parent"); // NOI18N
            }

            // remember old parent 
            oldParent = parent;

            // attach itself as a node list for given node
            parent = null;
        }

        // this is the only place where parent is changed,
        // but only under readAccess => double check if
        // it happened correctly
        try {
            PR.enterReadAccess();

            Node[] nodes = testNodes();

            if (nodes == null) {
                return;
            }

            // fire the change
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                node.deassignFrom(Children.this);
                node.fireParentNodeChange(oldParent, null);
            }
        } finally {
            PR.exitReadAccess();
        }
    }
    
    /**
     * Create a <code>Children</code> object using the passed <code>ChildFactory</code>
     * object.  The <code>ChildFactory</code> will be asked to create a list
     * of model objects that are the children;  then for each object in the list,
     * {@link ChildFactory#createNodesForKey} will be called to instantiate
     * one or more <code>Node</code>s for that object.
     * @param factory a factory which will provide child objects
     * @param asynchronous If true, the factory will always be called to
     *   create the list of keys on
     *   a background thread, displaying a &quot;Please Wait&quot; child node until
     *   some or all child nodes have been computed. If so,
     *   when it is expanded, the node that owns
     *   the returned <code>Children</code> object will display a &quot;Please Wait&quot;
     *   node while the children are computed in the background.  Pass true
     *   for any case where computing child nodes is expensive and should
     *   not be done in the event thread.
     * @return a children object which
     *   will invoke the factory instance as needed to supply model
     *   objects and child nodes for it
     * @throws IllegalStateException if the passed factory has already
     *   been used in a previous call to this method
     * @since org.openide.nodes 7.1
     */ 
    public static <T> Children create (ChildFactory <T> factory, boolean asynchronous) {
        if (factory == null) throw new NullPointerException ("Null factory");
        return asynchronous ? new AsynchChildren <T> (factory) : 
            new SynchChildren <T> (factory);
    }

    /** Get the parent node of these children.
    * @return the node attached to this children object, or <code>null</code> if there is none yet
    */
    protected final Node getNode() {
        return parent;
    }

    /** Allows access to the clone method for Node.
    * @return cloned hierarchy
    * @exception CloneNotSupportedException if not supported
    */
    final Object cloneHierarchy() throws CloneNotSupportedException {
        return clone();
    }

    /** Handles cloning in the right way, that can be later extended by
    * subclasses. Of course each subclass that wishes to support cloning
    * must implement the <code>Cloneable</code> interface, otherwise this method throws
    * <code>CloneNotSupportedException</code>.
    *
    * @return cloned version of this object, with the same class, uninitialized and without
    *   a parent node
    * *exception CloneNotSupportedException if <code>Cloneable</code> interface is not implemented
    */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Children ch = (Children) super.clone();
        ch.parent = null;
        ch.entrySupport = null;
        return ch;
    }

    /**
     * Add nodes to this container but <strong>do not call this method</strong>.
     * If you think you need to do this probably you really wanted to use
     * {@link Children.Keys#setKeys} instead.
    * The parent node of these nodes
    * is changed to the parent node of this list. Each node can be added
    * only once. If there is some reason a node cannot be added, for example
    * if the node expects only a special type of subnodes, the method should
    * do nothing and return <code>false</code> to signal that the addition has not been successful.
    * <P>
    * This method should be implemented by subclasses to filter some nodes, etc.
    *
    * @param nodes set of nodes to add to the list
    * @return <code>true</code> if successfully added
    */
    public abstract boolean add(final Node[] nodes);

    /** Remove nodes from the list. Only nodes that are present are
    * removed.
    *
    * @param nodes nodes to be removed
    * @return <code>true</code> if the nodes could be removed
    */
    public abstract boolean remove(final Node[] nodes);

    /** Get the nodes as an enumeration.
    * @return enumeration of nodes
    */
    public final Enumeration<Node> nodes() {
        return Enumerations.array(getNodes());
    }

    /** Find a child node by name.
    * This may be overridden in subclasses to provide a more advanced way of finding the
    * child, but the default implementation simply scans through the list of nodes
    * to find the first one with the requested name.
    * <p>Normally the list of nodes should have been computed by the time this returns,
    * but see {@link #getNodes()} for an important caveat as to why this may not
    * be doing what you want and what to do instead.
    * @param name (code) name of child node to find or <code>null</code> if any arbitrary child may
    *    be returned
    * @return the node or <code>null</code> if it could not be found
    */
    public Node findChild(String name) {
        Node[] list = getNodes();

        if (list.length == 0) {
            return null;
        }

        if (name == null) {
            // return any node
            return list[0];
        }

        for (int i = 0; i < list.length; i++) {
            if (name.equals(list[i].getName())) {
                // ok, we have found it
                return list[i];
            }
        }

        return null;
    }

    /** Method that can be used to test whether the children content has
    * ever been used or it is still not initalized.
    * @return true if children has been used before
    * @see #addNotify
    */
    protected final boolean isInitialized() {
        return entrySupport().isInitialized();
    }

    /** Getter for a child at a given position. If child with such index
     * does not exists it returns null.
     *
     * @param index the index of a node we want to know (non negative)
     * @return the node at given index or null
     * @since org.openide.nodes 7.5
     */
    public final Node getNodeAt(int index) {
        return entrySupport().getNodeAt(index);
    }

    /** Get a (sorted) array of nodes in this list.
     * If the children object is not yet initialized,
     * it will be (using {@link #addNotify}) before
     * the nodes are returned.
     * <p><strong>Warning:</strong> not all children
     * implementations do a complete calculation at
     * this point, see {@link #getNodes(boolean)}
     * @return array of nodes
     */

    //  private static String off = ""; // NOI18N
    public final Node[] getNodes() {
        return entrySupport().getNodes(false);
    }

    /** Get a (sorted) array of nodes in this list.
     *
     * This method is usefull if you need a fully initialized array of nodes
     * for things like MenuView, node navigation from scripts/tests and so on.
     * But in general if you are trying to get useful data by calling
     * this method, you are probably doing something wrong.
     * Usually you should be asking some underlying model
     * for information, not the nodes for children. For example,
     * <code>DataFolder.getChildren()</code>
     * is a much more appropriate way to get what you want for the case of folder children.
     *
     * If you're extending children, you should make sure this method
     * will return a complete list of nodes. The default implementation will do
     * this correctly so long as your subclass implement findChild(null)
     * to initialize all subnodes.
     *
     * <p><strong>Note:</strong>You should not call this method from inside
     * <code>{@link org.openide.nodes.Children#MUTEX Children.MUTEX}.readAccess()</code>.
     * If you do so, the <code>Node</code> will be unable to update its state
     * before you leave the <code>readAccess()</code>.
     *
     * @since 2.17
     *
     * @param optimalResult whether to try to get a fully initialized array
     * or to simply delegate to {@link #getNodes()}
     * @return array of nodes
     */
    public Node[] getNodes(boolean optimalResult) {
        return entrySupport().getNodes(optimalResult);
    }

    /** Get the number of nodes in the list.
    * @return the count
    */
    public final int getNodesCount() {
        return entrySupport().getNodesCount(false);
    }

    /** Get the number of nodes in the list
     * @param optimalResult whether to try to perform full initialization
     * or to simply delegate to {@link #getNodesCount()}
     * @return the count
     * @since org.openide.nodes 7.6
     */
    public int getNodesCount(boolean optimalResult) {
        return entrySupport().getNodesCount(optimalResult);
    }

    /** Creates an immutable snapshot representing the current view of the nodes
     * in this children object. This is No attempt is made to extract incorrect or invalid
     * nodes from the list, as a result, the value may not be exactly the same
     * as returned by {@link #getNodes()}.
     * 
     * @return immutable and unmodifiable list of nodes in this children object
     * @since 7.6
     */
    public final List<Node> snapshot() {
        try {
            PR.enterReadAccess();
            return entrySupport().createSnapshot(false);
        } finally {
            PR.exitReadAccess();
        }
    }

    //
    // StateNotifications
    //

    /** Called when children are first asked for nodes.
     * Typical implementations at this time calculate
     * their node list (or keys for {@link Children.Keys} etc.).<BR>
     * Notice: call to getNodes() inside of this method will return
     * an empty array of nodes.
     * @see #isInitialized
    */
    protected void addNotify() {
    }

    /** Called when all the children Nodes are freed from memory.
     * Typical implementations at this time clear all the keys
     * (in case of {@link Children.Keys}) etc.
     *
     * Note that this is usually not the best place for unregistering
     * listeners, etc., as listeners usually keep the child nodes
     * in memory, preventing them from being collected, thus preventing
     * this method to be called in the first place.
     */
    protected void removeNotify() {
    }

    /** Method that can be overriden in subclasses to
    * do additional work and then call addNotify.
    */
    void callAddNotify() {
        //System.err.println("Thread: " + Thread.currentThread().getName() + ", N: " + getNode());
        //System.err.println("Children: " + this);
        addNotify();
        //System.err.println("Finished: " + this);
    }

    final void callRemoveNotify() {
        removeNotify();
    }

    /** Called when the nodes have been removed from the children.
     * This method should allow subclasses to clean the nodes somehow.
     * <p>
     * Current implementation notifies all listeners on the nodes
     * that nodes have been deleted.
     *
     * @param arr array of deleted nodes
     */
    void destroyNodes(Node[] arr) {
    }

    /** @return either nodes associated with this children or null if
     * they are not created
     */
    private Node[] testNodes() {
        return entrySupport == null ? null : entrySupport().testNodes();
    }


    /** Interface that provides a set of nodes.
    */
    static interface Entry {
        /** Set of nodes associated with this entry.
        */
        public Collection<Node> nodes();
    }

    /** Empty list of children. Does not allow anybody to insert a node.
    * Treated especially in the attachTo method.
    */
    private static final class Empty extends Children {
        Empty() {
        }

        /** @return false, does no action */
        public boolean add(Node[] nodes) {
            return false;
        }

        /** @return false, does no action */
        public boolean remove(Node[] nodes) {
            return false;
        }
    }

    /** Implements the storage of node children by an array.
    * Each new child is added at the end of the array. The nodes are
    * returned in the order they were inserted.
    *
    * <p><strong>
    * Directly subclassing this class is discouraged.
    * {@link Children.Keys} is preferable.
     * </strong>
    */
    public static class Array extends Children implements Cloneable {
        /** the entry used for all nodes in the following collection
        * this object is used for synchronization of operations that
        * need to be synchronized on this instance of Children, but
        * we cannot synchronize on this instance because it is public
        * and somebody else could synchronize too.
        */
        private Entry nodesEntry;

        /** vector of added children */
        protected Collection<Node> nodes;

        /** Constructs a new list and allows a subclass to
        * provide its own implementation of <code>Collection</code> to store
        * data in. The collection should be empty and should not
        * be directly accessed in any way after creation.
        *
        * @param c collection to store data in
        */
        protected Array(Collection<Node> c) {
            this();
            nodes = c;
        }

        /** Constructs a new array children without any assigned collection.
        * The collection will be created by a call to method initCollection the
        * first time, children will be used.
        */
        public Array() {
            this(false);
        }

        Array(boolean lazy) {
            super(lazy);
            if (!lazy) {
                nodesEntry = createNodesEntry();
            }
        }
        @Override
        void postInitializeEntrySupport() {
            if (!lazySupport) {
                entrySupport().setEntries(Collections.singleton(getNodesEntry()));
            }
        }

        /** Clones all nodes that are contained in the children list.
        *
        * @return the cloned array for this children
        */
        @Override
        public Object clone() {
            try {
                final Children.Array ar = (Array) super.clone();

                try {
                    PR.enterReadAccess();

                    if (nodes != null) {
                        // nodes already initilized
                        // used to create the right type of collection
                        // clears the content of the collection
                        // JST: hack, but I have no better idea how to write this
                        //     pls. notice that in initCollection you can test
                        //     whether nodes == null => real initialization
                        //             nodes != null => only create new empty collection
                        ar.nodes = ar.initCollection();
                        ar.nodes.clear();

                        // insert copies of the nodes
                        for (Node n : nodes) {
                            ar.nodes.add(n.cloneNode());
                        }
                    }
                } finally {
                    PR.exitReadAccess();
                }

                return ar;
            } catch (CloneNotSupportedException e) {
                // this cannot happen
                throw new InternalError();
            }
        }

        /** Allow subclasses to create a collection, the first time the
        * children are used. It is called only if the collection has not
        * been passed in the constructor.
        * <P>
        * The current implementation returns ArrayList.
        *
        * @return empty or initialized collection to use
        */
        protected Collection<Node> initCollection() {
            return new ArrayList<Node>();
        }

        /** This method can be called by subclasses that
        * directly modify the nodes collection to update the
        * state of the nodes appropriatelly.
        * This method should be called under
        * MUTEX.writeAccess.
        */
        final void refreshImpl() {
            if (isInitialized()) {
                Array.this.entrySupport().refreshEntry(getNodesEntry());
                entrySupport().getNodes(false);
            } else if (nodes != null) {
                for (Node n : nodes) {
                    n.assignTo(this, -1);
                }
            }
        }

        /** This method can be called by subclasses that
        * directly modify the nodes collection to update the
        * state of the nodes appropriatelly.
        */
        protected final void refresh() {
            MUTEX.postWriteRequest(new Runnable() {
                    public void run() {
                        refreshImpl();
                    }
                }
            );
        }

        /** Getter for the entry.
        */
        final Entry getNodesEntry() {
            return nodesEntry;
        }

        /** This method allows subclasses (only in this package) to
        * provide own version of entry. Usefull for SortedArray.
        */
        Entry createNodesEntry() {
            return new AE();
        }

        /** Getter for nodes.
        */
        final Collection<Node> getCollection() {
            synchronized (getNodesEntry()) {
                if (nodes == null) {
                    nodes = initCollection();
                }
            }
            return nodes;
        }

        /*
        * @param arr nodes to add
        * @return true if changed false if not
        */
        public boolean add(final Node[] arr) {
            synchronized (getNodesEntry()) {
                if (!getCollection().addAll(Arrays.asList(arr))) {
                    // no change to the collection
                    return false;
                }
            }
            refresh();
            return true;
        }

        /*
        * @param arr nodes to remove
        * @return true if changed false if not
        */
        public boolean remove(final Node[] arr) {
            synchronized (getNodesEntry()) {
                if (!getCollection().removeAll(Arrays.asList(arr))) {
                    // the collection was not changed
                    return false;
                }
            }

            refresh();

            return true;
        }

        /** One entry that holds all the nodes in the collection
        * member called nodes.
        */
        private final class AE extends Object implements Entry {
            AE() {
            }

            /** List of elements.
            */
            public Collection<Node> nodes() {
                Collection<Node> c = getCollection();

                if (c.isEmpty()) {
                    return Collections.emptyList();
                } else {
                    return new ArrayList<Node>(c);
                }
            }

            @Override
            public String toString() {
                return "Children.Array.AE" + getCollection(); // NOI18N
            }

        }
    }

    /** Implements the storage of node children by a map.
    * This class also permits
    * association of a key with any node and to remove nodes by key.
    * Subclasses should reasonably
    * implement {@link #add} and {@link #remove}.
     * @param T the key type
    */
    public static class Map<T> extends Children {
        /** A map to use to store children in.
        * Keys are <code>Object</code>s, values are {@link Node}s.
        * Do <em>not</em> modify elements in the map! Use it only for read access.
        */
        protected java.util.Map<T,Node> nodes;

        /** Constructs a new list with a supplied map object.
        * Should be used by subclasses desiring an alternate storage method.
        * The map must not be explicitly modified after creation.
        *
        * @param m the map to use for this list
        */
        protected Map(java.util.Map<T,Node> m) {
            nodes = m;
        }

        /** Constructs a new list using {@link HashMap}.
        */
        public Map() {
        }

        /** Getter for the map.
        * Ensures that the map has been initialized.
        */
        final java.util.Map<T,Node> getMap() {
            // package private only to simplify access from inner classes
            if (nodes == null) {
                nodes = initMap();
            }

            return nodes;
        }

        /** Called on first use.
        */
        @Override
        final void callAddNotify() {
            entrySupport().setEntries(createEntries(getMap()));
            super.callAddNotify();
        }

        /** Method that allows subclasses (SortedMap) to redefine
        * order of entries.
        * @param map the map (Object, Node)
        * @return collection of (Entry)
        */
        Collection<? extends Entry> createEntries(java.util.Map<T,Node> map) {
            List<ME> l = new LinkedList<ME>();
            for (java.util.Map.Entry<T,Node> e : map.entrySet()) {
                l.add(new ME(e.getKey(), e.getValue()));
            }
            return l;
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        * This method should be called under
        * MUTEX.writeAccess.
        */
        final void refreshImpl() {
            entrySupport().setEntries(createEntries(getMap()));
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        */
        protected final void refresh() {
            try {
                PR.enterWriteAccess();
                refreshImpl();
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        * This method should be called under
        * MUTEX.writeAccess.
        *
        * @param key the key that should be refreshed
        */
        final void refreshKeyImpl(T key) {
            entrySupport().refreshEntry(new ME(key, null));
        }

        /** Allows subclasses that directly modifies the
        * map with nodes to synchronize the state of the children.
        *
        * @param key the key that should be refreshed
        */
        protected final void refreshKey(final T key) {
            try {
                PR.enterWriteAccess();
                refreshKeyImpl(key);
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Add a collection of new key/value pairs into the map.
        * The supplied map may contain any keys, but the values must be {@link Node}s.
        *
        * @param map the map with pairs to add
        */
        protected final void putAll(final java.util.Map<? extends T,? extends Node> map) {
            try {
                PR.enterWriteAccess();
                nodes.putAll(map);
                refreshImpl();

                // PENDING sometime we should also call refreshKey...
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Add one key and one node to the list.
        * @param key the key
        * @param node the node
        */
        protected final void put(final T key, final Node node) {
            try {
                PR.enterWriteAccess();

                if (nodes.put(key, node) != null) {
                    refreshKeyImpl(key);
                } else {
                    refreshImpl();
                }
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Remove some children from the list by key.
        * @param keys collection of keys to remove
        */
        protected final void removeAll(final Collection<? extends T> keys) {
            try {
                PR.enterWriteAccess();
                nodes.keySet().removeAll(keys);
                refreshImpl();
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Remove a given child node from the list by its key.
        * @param key key to remove
        */
        protected void remove(final T key) {
            try {
                PR.enterWriteAccess();

                if (nodes.remove(key) != null) {
                    refreshImpl();
                }
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Initialize some nodes. Allows a subclass to
        * provide a default map to initialize the map with.
        * Called only if the map has not been provided in the constructor.
        *
        * <P>
        * The default implementation returns <code>new HashMap (7)</code>.
        *
        * @return a map from keys to nodes
        */
        protected java.util.Map<T,Node> initMap() {
            return new HashMap<T,Node>(7);
        }

        /** Does nothing. Should be reimplemented in a subclass wishing
        * to support external addition of nodes.
        *
        * @param arr nodes to add
        * @return <code>false</code> in the default implementation
        */
        public boolean add(Node[] arr) {
            return false;
        }

        /** Does nothing. Should be reimplemented in a subclass wishing
        * to support external removal of nodes.
        * @param arr nodes to remove
        * @return <code>false</code> in the default implementation
        */
        public boolean remove(Node[] arr) {
            return false;
        }

        /** Entry mapping one key to the node.
        */
        final static class ME extends Object implements Entry {
            /** key */
            public Object key;

            /** node set */
            public Node node;

            /** Constructor.
            */
            public ME(Object key, Node node) {
                this.key = key;
                this.node = node;
            }

            /** Nodes */
            public Collection<Node> nodes() {
                return Collections.singleton(node);
            }

            /** Hash code.
            */
            @Override
            public int hashCode() {
                return key.hashCode();
            }

            /** Equals.
            */
            @Override
            public boolean equals(Object o) {
                if (o instanceof ME) {
                    ME me = (ME) o;

                    return key.equals(me.key);
                }

                return false;
            }

            @Override
            public String toString() {
                return "Key (" + key + ")"; // NOI18N
            }
        }
    }

    /** Maintains a list of children sorted by the provided comparator in an array.
    * The comparator can change during the lifetime of the children, in which case
    * the children are resorted.
    */
    public static class SortedArray extends Children.Array {
        /** comparator to use */
        private Comparator<? super Node> comp;

        /** Create an empty list of children. */
        public SortedArray() {
        }

        /** Create an empty list with a specified storage method.
        *
        * @param c collection to store data in
        * @see Children.Array#Children.Array(Collection)
        */
        protected SortedArray(Collection<Node> c) {
            super(c);
        }

        /** Set the comparator. The children will be resorted.
        * The comparator is used to compare Nodes, if no
        * comparator is used then nodes will be compared by
        * the use of natural ordering.
        *
        * @param c the new comparator
        */
        public void setComparator(final Comparator<? super Node> c) {
            try {
                PR.enterWriteAccess();
                comp = c;
                refresh();
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Get the current comparator.
        * @return the comparator
        */
        public Comparator<? super Node> getComparator() {
            return comp;
        }

        /** This method allows subclasses (only in this package) to
        * provide own version of entry. Useful for SortedArray.
        */
        @Override
        Entry createNodesEntry() {
            return new SAE();
        }

        /** One entry that holds all the nodes in the collection
        * member called nodes.
        */
        private final class SAE extends Object implements Entry {
            /** Constructor that provides the original comparator.
            */
            public SAE() {
            }

            /** List of elements.
            */
            public Collection<Node> nodes() {
                List<Node> al = new ArrayList<Node>(getCollection());
                Collections.sort(al, comp);

                return al;
            }
        }
    }
     // end of SortedArray

    /** Maintains a list of children sorted by the provided comparator in a map.
    * Similar to {@link Children.SortedArray}.
    */
    public static class SortedMap<T> extends Children.Map<T> {
        /** comparator to use */
        private Comparator<? super Node> comp;

        /** Create an empty list. */
        public SortedMap() {
        }

        /** Create an empty list with a specific storage method.
        *
        * @param map the map to use with this object
        * @see Children.Map#Children.Map(java.util.Map)
        */
        protected SortedMap(java.util.Map<T,Node> map) {
            super(map);
        }

        /** Set the comparator. The children will be resorted.
        * The comparator is used to compare Nodes, if no
        * comparator is used then values will be compared by
        * the use of natural ordering.
        *
        * @param c the new comparator that should compare nodes
        */
        public void setComparator(final Comparator<? super Node> c) {
            try {
                PR.enterWriteAccess();
                comp = c;
                refresh();
            } finally {
                PR.exitWriteAccess();
            }
        }

        /** Get the current comparator.
        * @return the comparator
        */
        public Comparator<? super Node> getComparator() {
            return comp;
        }

        /** Method that allows subclasses (SortedMap) to redefine
        * order of entries.
        * @param map the map (Object, Node)
        * @return collection of (Entry)
        */
        @Override
        Collection<? extends Entry> createEntries(java.util.Map<T,Node> map) {
            // SME objects use natural ordering
            Set<ME> l = new TreeSet<ME>(new SMComparator());

            for (java.util.Map.Entry<T,Node> e : map.entrySet()) {
                l.add(new ME(e.getKey(), e.getValue()));
            }

            return l;
        }

        /** Sorted map entry can be used for comparing.
        */
        final class SMComparator implements Comparator<ME> {
            public int compare(ME me1, ME me2) {
                Comparator<? super Node> c = comp;

                if (c == null) {
                    // compare keys
                    @SuppressWarnings("unchecked") // we just assume that it is comparable, not statically checked
                    int r = ((Comparable) me1.key).compareTo(me2.key);
                    return r;
                } else {
                    return c.compare(me1.node, me2.node);
                }
            }
        }
    }
     // end of SortedMap

    /** Implements an array of child nodes associated nonuniquely with keys and sorted by these keys.
    * There is a {@link #createNodes(Object) method} that should for each
    * key create an array of nodes that represents the key.
    *
     * <p>This class is preferable to {@link Children.Array} because
     * <ol>
     * <li>It more clearly separates model from view and encourages use of a discrete model.
     * <li>It correctly handles adding, removing, and reordering children while preserving
     *     existing node selections in a tree (or other) view where possible.
     * </ol>
     *
    * <p>Typical usage:
    * <ol>
    * <li>Subclass.
    * <li>Decide what type your key should be.
    * <li>Implement {@link #createNodes} to create some nodes
    * (usually exactly one) per key.
    * <li>Override {@link Children#addNotify} to compute a set of keys
    * and set it using {@link #setKeys(Collection)}.
    * The collection may be ordered.
    * <li>Override {@link Children#removeNotify} to just call
    * <code>setKeys</code> on {@link Collections#EMPTY_SET}.
    * <li>When your model changes, call <code>setKeys</code>
    * with the new set of keys. <code>Children.Keys</code> will
    * be smart and calculate exactly what it needs to do effficiently.
    * <li><i>(Optional)</i> if your notion of what the node for a
    * given key changes (but the key stays the same), you can
    * call {@link #refreshKey}. Usually this is not necessary.
    * </ol>
    * Note that for simple cases, it may be preferable to subclass
    * <a href="ChildFactory.html">ChildFactory</a> and pass the result to
    * <a href="Children.html#create(org.openide.nodes.ChildFactory, boolean)">
    * create()</a>; doing so makes it easy to switch to using child
    * nodes computed on a background thread if necessary for performance
    * reasons.
     * @param T the type of the key
    */
    public static abstract class Keys<T> extends Children.Array {
        /** the last runnable (created in method setKeys) for each children object.
         */
        private static java.util.Map<Keys<?>,Runnable> lastRuns = new HashMap<Keys<?>,Runnable>(11);

        /** add array children before or after keys ones */
        boolean before;
        
        public Keys() {
            this(false);
        }
        
        /** Constructor for optional "lazy behavoir" (tries to avoid 
         * computation of nodes if possible).
         * <p> There are certain requirements for usage of lazy mode:
         * It is forbidden to create more than 1 node in {@link #createNodes}
         * for key. In optimal case there should be 1:1 pairing between key and Node,
         * but it is also possible to have 1:0 pairing - create no node (return null). 
         * In such case after detection that there is no Node for key, 
         * the key is automatically removed and change (removal of 
         * "dummy" Node) is fired.
         * @param lazy whether to introduce lazy behavior
         * @since org.openide.nodes 7.6
         */
        protected Keys(boolean lazy) {
            super(lazy);
        }
        
        /** Special handling for clonning.
        */
        @Override
        public Object clone() {
            Keys<?> k = (Keys<?>) super.clone();

            return k;
        }

        /**
         * @deprecated Do not use! Just call {@link #setKeys(Collection)} with a larger set.
         */
        @Deprecated
        @Override
        public boolean add(Node[] arr) {
            if (lazySupport) {
                return false;
            }
            return super.add(arr);
        }

        /**
         * @deprecated Do not use! Just call {@link #setKeys(Collection)} with a smaller set.
         */
        @Deprecated
        @Override
        public boolean remove(final Node[] arr) {
            if (lazySupport) {
                return false;
            }
            try {
                PR.enterWriteAccess();

                if (nodes != null) {
                    // removing from array, just if the array nodes are 
                    // really created
                    // expecting arr.length == 1, which is the usual case
                    for (int i = 0; i < arr.length; i++) {
                        if (!nodes.contains(arr[i])) {
                            arr[i] = null;
                        }
                    }

                    super.remove(arr);
                }
            } finally {
                PR.exitWriteAccess();
            }

            return true;
        }

        /** Refresh the child nodes for a given key.
        *
        * @param key the key to refresh
        */
        protected final void refreshKey(final T key) {
            MUTEX.postWriteRequest(
                new Runnable() {
                    public void run() {
                        Keys.this.entrySupport().refreshEntry(createEntryForKey(key));
                    }
                }
            );
        }

        /** To be overriden in FilterNode.Children */
        Entry createEntryForKey(T key) {
            return new KE(key);
        }

        /** Set new keys for this children object. Setting of keys
        * does not necessarily lead to the creation of nodes. It happens only
        * when the list has already been initialized.
        *
        * @param keysSet the keys for the nodes (collection of any objects)
        */
        protected final void setKeys(Collection<? extends T> keysSet) {
            boolean asserts = false;
            assert asserts = true;
            int sz = keysSet.size();
            if (asserts && sz < 10) {
                List<? extends T> keys = new ArrayList<T>(keysSet);
                for (int i = 0; i < sz - 1; i++) {
                    T a = keys.get(i);
                    for (int j = i + 1; j < sz; j++) {
                        T b = keys.get(j);
                        assert !(a.equals(b) && a.hashCode() != b.hashCode()) : "bad equals/hashCode in " + a + " vs. " + b;
                    }
                }
            }

            final List<Entry> l = new ArrayList<Entry>(keysSet.size() + 1);
            KE updator = new KE();

            if (lazySupport) {
                updator.updateList(keysSet, l);
            } else {
                if (before) {
                    l.add(getNodesEntry());
                }
                updator.updateList(keysSet, l);
                if (!before) {
                    l.add(getNodesEntry());
                }
            }

            applyKeys(l);
        }

        /** Set keys for this list.
        *
        * @param keys the keys for the nodes
        * @see #setKeys(Collection)
        */
        protected final void setKeys(final T[] keys) {
            boolean asserts = false;
            assert asserts = true;
            int sz = keys.length;
            if (asserts && sz < 10) {
                for (int i = 0; i < sz - 1; i++) {
                    T a = keys[i];
                    for (int j = i + 1; j < sz; j++) {
                        T b = keys[j];
                        assert !(a.equals(b) && a.hashCode() != b.hashCode()) : "bad equals/hashCode in " + a + " vs. " + b;
                    }
                }
            }

            final List<Entry> l = new ArrayList<Entry>(keys.length + 1);
            KE updator = new KE();

            if (lazySupport) {
                updator.updateList(keys, l);
            } else {
                if (before) {
                    l.add(getNodesEntry());
                }
                updator.updateList(keys, l);
                if (!before) {
                    l.add(getNodesEntry());
                }
            }

            applyKeys(l);
        }

        /** Applies the keys.
         */
        private void applyKeys(final List<? extends Entry> l) {
            Runnable invoke = new Runnable() {
                    public void run() {
                        if (keysCheck(Keys.this, this)) {
                            // no next request after me
                            entrySupport().setEntries(l);

                            // clear this runnable
                            keysExit(Keys.this, this);
                        }
                    }
                };

            keysEnter(this, invoke);
            MUTEX.postWriteRequest(invoke);
        }

        /** Set whether new nodes should be added to the beginning or end of sublists for a given key.
        * Generally should not be used.
        * @param b <code>true</code> if the children should be added before
        */
        protected final void setBefore(final boolean b) {
            try {
                PR.enterWriteAccess();

                if (before != b && !lazySupport) {
                    List<Entry> l = entrySupport().getEntries();
                    l.remove(getNodesEntry());
                    before = b;

                    if (b) {
                        l.add(0, getNodesEntry());
                    } else {
                        l.add(getNodesEntry());
                    }

                    entrySupport().setEntries(l);
                }
            } finally {
                PR.exitWriteAccess();
            }
        }
        
        /** Create nodes for a given key.
        * @param key the key
        * @return child nodes for this key or null if there should be no
        *    nodes for this key
        */
        protected abstract Node[] createNodes(T key);

        /** Called when the nodes have been removed from the children.
        * This method should allow subclasses to clean the nodes somehow.
        * <p>
        * Current implementation notifies all listeners on the nodes
        * that nodes have been deleted.
        *
        * @param arr array of deleted nodes
        */
        @Override
        protected void destroyNodes(Node[] arr) {
            for (int i = 0; i < arr.length; i++) {
                arr[i].fireNodeDestroyed();
            }
        }

        /** Enter of setKeys.
         * @param ch the children
         * @param run runnable
         */
        private static synchronized void keysEnter(Keys<?> ch, Runnable run) {
            lastRuns.put(ch, run);
        }

        /** Clears the entry for the children
         * @param ch children
         */
        private static synchronized void keysExit(Keys ch, Runnable r) {
            Runnable reg = lastRuns.remove(ch);

            if ((reg != null) && !reg.equals(r)) {
                lastRuns.put(ch, reg);
            }
        }

        /** Check whether the runnable is "the current" for given children.
         * @param ch children
         * @param run runnable
         * @return true if the runnable shoul run
         */
        private static synchronized boolean keysCheck(Keys ch, Runnable run) {
            return run == lastRuns.get(ch);
        }

        /** Entry for a key
        */
        class KE extends Dupl<T> {
            /** Has default constructor that allows to create a factory
            * of KE objects for use with updateList
            */
            public KE() {
            }

            /** Creates directly an instance of the KE object.
            */
            public KE(T key) {
                this.key = key;
            }

            /** Nodes are taken from the create nodes.
            */
            public Collection<Node> nodes() {
                Node[] arr = createNodes(getKey());

                if (arr == null) {
                    return Collections.emptyList();
                } else {
                    return new LinkedList<Node>(Arrays.asList(arr));
                }
            }

            @Override
            public String toString() {
                String s = getKey().toString();
                if (s.length() > 80) {
                    s = s.substring(s.length() - 80);
                }
                return "Children.Keys.KE[" + s + "," + getCnt() + "]"; // NOI18N
            }
        }
    }
     // end of Keys

    /** Supporting class that provides support for duplicated
    * objects that still should not be equal.
    * <P>
    * It counts the number of times an object has been
    * added to the collection and if the same object is added
    * more than once it is indexed by a number.
    */
    private static abstract class Dupl<T> implements Cloneable, Entry {
        /** the key either real value or Dupl (Dupl (Dupl (... value ...)))*/
        protected Object key;

        Dupl() {
        }

        /** Updates the second collection with values from the first one.
        * If there is a multiple occurrence of an object in the first collection
        * a Dupl for the object is created to encapsulate it.
        */
        public final void updateList(Collection<? extends T> src, Collection<? super Dupl<T>> target) {
            java.util.Map<T,Object> map = new HashMap<T,Object>(src.size() * 2);
            for (T o : src) {
                updateListAndMap(o, target, map);
            }
        }

        /** Updates the second collection with values from the first array.
        * If there is a multiple occurrence of an object in the first array
        * a Dupl for the object is created to encapsulate it.
        */
        public final void updateList(T[] arr, Collection<? super Dupl<T>> target) {
            java.util.Map<T,Object> map = new HashMap<T,Object>(arr.length * 2);
            for (T o : arr) {
                updateListAndMap(o, target, map);
            }
        }

        /** Updates the linked list and the map with right
        * values. The map is used to count the number of times
        * a element occurs in the list.
        *
        * @param obj object to add
        * @param list the list to add obj to
        * @param map to track number of occurrences in the array
        */
        public final void updateListAndMap(T obj, Collection<? super Dupl<T>> list, java.util.Map<T,Object> map) {
            // optimized for first occurrence
            // of each object because often occurrences should be rare
            Object prev = map.put(obj, this);

            if (prev == null) {
                // first occurrence of object obj
                list.add(createInstance(obj, 0));

                return;
            } else {
                if (prev == this) {
                    // second occurrence of the object
                    map.put(obj, 1);
                    list.add(createInstance(obj, 1));

                    return;
                } else {
                    int cnt = ((Integer) prev) + 1;
                    map.put(obj, cnt);
                    list.add(createInstance(obj, cnt));

                    return;
                }
            }
        }

        /** Gets the key represented by this object.
        * @return the key
        */
        @SuppressWarnings("unchecked") // data structure really weird
        public T getKey() {
            if (key instanceof Dupl) {
                return (T) ((Dupl) key).getKey();
            } else {
                return (T) key;
            }
        }

        /** Counts the index of this key.
        * @return integer
        */
        public int getCnt() {
            int cnt = 0;
            Dupl d = this;

            while (d.key instanceof Dupl) {
                d = (Dupl) d.key;
                cnt++;
            }

            return cnt;
        }

        /** Create instance of Dupl (uses cloning of the class)
        */
        @SuppressWarnings("unchecked") // data structure really weird
        private final Dupl<T> createInstance(Object obj, int cnt) {
            try {
                // creates the chain of Dupl (Dupl (Dupl (obj))) where
                // for cnt = 0 the it would look like Dupl (obj)
                // for cnt = 1 like Dupl (Dupl (obj))
                Dupl d = (Dupl) this.clone();
                Dupl first = d;

                while (cnt-- > 0) {
                    Dupl n = (Dupl) this.clone();
                    d.key = n;
                    d = n;
                }

                d.key = obj;

                return first;
            } catch (CloneNotSupportedException ex) {
                throw new InternalError();
            }
        }

        @Override
        public int hashCode() {
            return getKey().hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Dupl) {
                Dupl d = (Dupl) o;

                return getKey().equals(d.getKey()) && (getCnt() == d.getCnt());
            }

            return false;
        }
    }

    /*
      static void printNodes (Node[] n) {
        for (int i = 0; i < n.length; i++) {
          System.out.println ("  " + i + ". " + n[i].getName () + " number: " + System.identityHashCode (n[i]));
        }
        }
        */
    /* JST: Useful test routine ;-) *
    static {
      final TopComponent.Registry r = TopComponent.getRegistry ();
      r.addPropertyChangeListener (new PropertyChangeListener () {
        Node last = new AbstractNode (LEAF);

        public void propertyChange (PropertyChangeEvent ev) {
          Node[] arr = r.getCurrentNodes ();
          if (arr != null && arr.length == 1) {
            last = arr[0];
          }
          System.out.println (
            "Activated node: " + last + " \nparent: " + last.getParentNode ()
          );
        }
      });
    }
    */
}
