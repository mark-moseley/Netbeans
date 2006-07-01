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

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.io.SafeException;

import java.awt.Component;

import java.beans.*;

import java.io.*;

import java.util.*;


/**
 * Manages a selection and root context for a (set of) Explorer view(s).  The
 * views should register their {@link java.beans.VetoableChangeListener}s and
 * {@link java.beans.PropertyChangeListener}s at the
 * <code>ExplorerManager</code> of the Explorer they belong to
 * (usually found in AWT hierarchy using {@link ExplorerManager#find}. 
 * The manager is then the mediator that keeps the shared state, 
 * notifies {@link PropertyChangeListener}s and {@link VetoableChangeListener}s
 * about changes and allows views to call its setter methods to incluence
 * the root of the visible hierarchy using {@link #setRootContext}, the 
 * set of selected nodes using {@link #setSelectedNodes} and also the 
 * explored context (useful for {@link org.openide.explorer.view.ListView} for 
 * example) using {@link #setExploredContext}.
 * <p>
 * This class interacts with Swing components in the
 * <code>org.openide.explorer.view</code> package and as such it shall be
 * used according to Swing threading model.
 * <p>
 * To provide an {@link ExplorerManager} from your component just let your
 * component implement {@link Provider} as described at {@link ExplorerUtils}.
 *
 * <P>Deserialization may throw {@link SafeException} if the contexts cannot be
 * restored correctly, but the stream is uncorrupted.
 *
 *
 * @author Ian Formanek, Petr Hamernik, Jaroslav Tulach, Jan Jancura,
 *         Jesse Glick
 * @see ExplorerUtils
 * @see org.openide.explorer.view.TreeView
 * @see org.openide.explorer.view.ListView
 */
public final class ExplorerManager extends Object implements Serializable, Cloneable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4330330689803575792L;

    /** Name of property for the root context. */
    public static final String PROP_ROOT_CONTEXT = "rootContext"; // NOI18N

    /** Name of property for the explored context. */
    public static final String PROP_EXPLORED_CONTEXT = "exploredContext"; // NOI18N

    /** Name of property for the node selection. */
    public static final String PROP_SELECTED_NODES = "selectedNodes"; // NOI18N

    /** Name of property for change in a node. */
    public static final String PROP_NODE_CHANGE = "nodeChange"; // NOI18N

    /** Request processor for managing selections.
    */
    static RequestProcessor selectionProcessor;

    /** Delay for coalescing events before removing destroyed nodes from
        the selection.
    */
    private static final int SELECTION_SYNC_DELAY = 200;

    /** defines serialized fields for the manager.
    */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("root", Node.Handle.class), // NOI18N
        new ObjectStreamField("rootName", String.class), // NOI18N
        new ObjectStreamField("explored", String[].class), // NOI18N
        new ObjectStreamField("selected", Object[].class) // NOI18N
    };

    /** The support for VetoableChangeEvent */
    private transient VetoableChangeSupport vetoableSupport;

    /** The support for PropertyChangeEvent */
    private transient PropertyChangeSupport propertySupport;

    /** The current root context */
    private Node rootContext;

    /** The current explored context */
    private Node exploredContext;

    /** The currently selected beans */
    private Node[] selectedNodes;

    /** listener to destroy of root node */
    private transient Listener listener;

    /** weak listener */
    private transient NodeListener weakListener;

    /** Task that removes manages node selection issues.
    */
    private RequestProcessor.Task selectionSyncTask;

    /** Actions factory provided for this explorer manager */
    private ExplorerActionsImpl actions;

    /** Construct a new manager. */
    public ExplorerManager() {
        init();
    }

    /** Initializes the nodes.
    */
    private void init() {
        exploredContext = rootContext = Node.EMPTY;
        selectedNodes = new Node[0];
        listener = new Listener();
        weakListener = NodeOp.weakNodeListener(listener, null);
    }

    /** Clones the manager.
    * @return manager with the same settings like this one
    */
    public Object clone() {
        ExplorerManager em = new ExplorerManager();
        em.rootContext = rootContext;
        em.exploredContext = exploredContext;
        em.selectedNodes = selectedNodes;

        return em;
    }

    /** Get the set of selected nodes.
    * @return the selected nodes; empty (not <code>null</code>) if none are selected
    */
    public Node[] getSelectedNodes() {
        return selectedNodes;
    }

    // compare two arrays of nodes in respect to a path to root
    private boolean equalNodes(Node[] arr1, Node[] arr2) {
        // generic tests
        if (!Arrays.equals(arr1, arr2)) {
            return false;
        }

        if ((arr1 == null) || (arr1.length == 0)) {
            return true;
        }

        // compare paths from each node to root
        int i = 0;

        while ((i < arr1.length) && Arrays.equals(NodeOp.createPath(arr1[i], null), NodeOp.createPath(arr2[i], null))) {
            i++;
        }

        return i == arr1.length;
    }

    /** Set the set of selected nodes.
    * @param value the nodes to select; empty (not <code>null</code>) if none are to be selected
    * @exception PropertyVetoException when the given nodes cannot be selected
    * @throws IllegalArgumentException if <code>null</code> is given, or if any elements
    *                                  of the selection are not within the current root context
    */
    public final void setSelectedNodes(final Node[] value)
    throws PropertyVetoException {
        class AtomicSetSelectedNodes implements Runnable {
            public PropertyVetoException veto;
            private boolean doFire;
            private Node[] oldValue;

            /** @return false if no further processing is needed */
            private boolean checkArgumentIsValid() {
                if (value == null) {
                    throw new IllegalArgumentException(getString("EXC_NodeCannotBeNull"));
                }

                if (equalNodes(value, selectedNodes)) {
                    return false;
                }

                for (int i = 0; i < value.length; i++) {
                    if (value[i] == null) {
                        throw new IllegalArgumentException(getString("EXC_NoElementOfNodeSelectionMayBeNull"));
                    }

                    checkUnderRoot(value[i], "EXC_NodeSelectionCannotContainNodes");
                }

                if ((value.length != 0) && (vetoableSupport != null)) {
                    try {
                        // we send the vetoable change event only for non-empty selections
                        vetoableSupport.fireVetoableChange(PROP_SELECTED_NODES, selectedNodes, value);
                    } catch (PropertyVetoException ex) {
                        veto = ex;

                        return false;
                    }
                }

                return true;
            }

            private void updateSelection() {
                oldValue = selectedNodes;

                Collection currentNodes = Arrays.asList(oldValue);

                // PENDING: filter out duplicities from the selection
                Collection newSelection = Arrays.asList(value);

                Collection nodesToAdd = new LinkedList(newSelection);
                nodesToAdd.removeAll(currentNodes);

                Collection nodesToRemove = new LinkedList(currentNodes);
                nodesToRemove.removeAll(newSelection);

                selectedNodes = value;

                Iterator it;

                // remove listeners from nodes that are being deselected
                for (it = nodesToRemove.iterator(); it.hasNext();) {
                    Node n = (Node) it.next();
                    n.removeNodeListener(weakListener);
                }

                // and add listeners to nodes that become selected
                for (it = nodesToAdd.iterator(); it.hasNext();) {
                    Node n = (Node) it.next();
                    n.removeNodeListener(weakListener);
                    n.addNodeListener(weakListener);
                }

                doFire = true;
            }
            
            public void fire() {
                if (doFire) {
                    fireInAWT(PROP_SELECTED_NODES, oldValue, selectedNodes);
                }
            }

            public void run() {
                if (checkArgumentIsValid()) {
                    updateSelection();
                }
            }
        }

        AtomicSetSelectedNodes setNodes = new AtomicSetSelectedNodes();
        Children.MUTEX.readAccess(setNodes);
        setNodes.fire();
        
        if (setNodes.veto != null) {
            throw setNodes.veto;
        }
    }

    /** Get the explored context.
     * <p>The "explored context" is not as frequently used as the node selection;
     * generally it refers to a parent node which contains all of the things
     * being displayed at this moment. For <code>BeanTreeView</code> this is
     * irrelevant, but <code>ContextTreeView</code> uses it (in lieu of the node
     * selection) and for <code>IconView</code> it is important (the node
     * whose children are visible, i.e. the "background" of the icon view).
     * @return the node being explored, or <code>null</code>
     */
    public final Node getExploredContext() {
        return exploredContext;
    }

    /** Set the explored context.
     * The node selection will be cleared as well.
     * @param value the new node to explore, or <code>null</code> if none should be explored.
     * @throws IllegalArgumentException if the node is not within the current root context in the node hierarchy
     */
    public final void setExploredContext(Node value) {
        setExploredContext(value, new Node[0]);
    }

    /** Set the explored context.
     * The node selection will be changed as well. Note: node selection cannot be
     * vetoed if calling this method. It is generally better to call setExploredContextAndSelection.
     * @param value the new node to explore, or <code>null</code> if none should be explored.
     * @throws IllegalArgumentException if the node is not within the current root context in the node hierarchy
     */
    public final void setExploredContext(final Node value, final Node[] selection) {
        class SetExploredContext implements Runnable {
            boolean doFire;
            Object oldValue;
            
            public void run() {
                // handles nulls correctly:
                if (Utilities.compareObjects(value, exploredContext)) {
                    setSelectedNodes0(selection);

                    return;
                }

                checkUnderRoot(value, "EXC_ContextMustBeWithinRootContext");
                setSelectedNodes0(selection);

                oldValue = exploredContext;
                exploredContext = value;

                doFire = true;
            }
            public void fire() {
                if (doFire) {
                    fireInAWT(PROP_EXPLORED_CONTEXT, oldValue, value);
                }
            }
        }

        SetExploredContext set = new SetExploredContext();
        Children.MUTEX.readAccess(set);
        set.fire();
    }

    /** Set the explored context and selected nodes. If the change in selected nodes is vetoed,
     * PropertyVetoException is rethrown from here.
     * @param value the new node to explore, or <code>null</code> if none should be explored.
     * @param selection the new nodes to be selected
     * @throws IllegalArgumentException if the node is not within the current root context in the node hierarchy
     * @throws PropertyVetoExcepion if listeners attached to this explorer manager do so
     */
    public final void setExploredContextAndSelection(final Node value, final Node[] selection)
    throws PropertyVetoException {
        class SetExploredContextAndSelection implements Runnable {
            public PropertyVetoException veto;
            private boolean doFire;
            private Object oldValue;

            public void run() {
                try {
                    // handles nulls correctly:
                    if (Utilities.compareObjects(value, exploredContext)) {
                        setSelectedNodes(selection);

                        return;
                    }

                    checkUnderRoot(value, "EXC_ContextMustBeWithinRootContext");
                    setSelectedNodes(selection);

                    oldValue = exploredContext;
                    exploredContext = value;

                    doFire = true;
                } catch (PropertyVetoException ex) {
                    veto = ex;
                }
            }
            
            public void fire() {
                if (doFire) {
                    fireInAWT(PROP_EXPLORED_CONTEXT, oldValue, exploredContext);
                }
            }
        }

        SetExploredContextAndSelection set = new SetExploredContextAndSelection();
        Children.MUTEX.readAccess(set);
        set.fire();

        if (set.veto != null) {
            throw set.veto;
        }
    }

    /** Sets selected nodes and handles PropertyVetoException */
    final void setSelectedNodes0(Node[] nodes) {
        try {
            setSelectedNodes(nodes);
        } catch (PropertyVetoException e) {
        }
    }

    /** Get the root context.
    * <p>The "root context" is simply the topmost node that this explorer can
    * display or manipulate. For <code>BeanTreeView</code>, this would mean
    * the root node of the tree. For e.g. <code>IconView</code>, this would
    * mean the uppermost possible node that that icon view could display;
    * while the explored context would change at user prompting via the
    * up button and clicking on subfolders, the root context would be fixed
    * by the code displaying the explorer.
    * @return the root context node
    */
    public final Node getRootContext() {
        return rootContext;
    }

    /** Set the root context.
    * The explored context will be set to the new root context as well.
    * If any of the selected nodes are not inside it, the selection will be cleared.
    * @param value the new node to serve as a root
    * @throws IllegalArgumentException if it is <code>null</code>
    */
    public final void setRootContext(Node value) {
        if (value == null) {
            throw new IllegalArgumentException(getString("EXC_CannotHaveNullRootContext"));
        }

        if (rootContext.equals(value)) {
            return;
        }

        Node oldValue = rootContext;
        rootContext = value;

        oldValue.removeNodeListener(weakListener);
        rootContext.addNodeListener(weakListener);

        fireInAWT(PROP_ROOT_CONTEXT, oldValue, rootContext);

        Node[] newselection = getSelectedNodes();

        if (!areUnderTarget(newselection, rootContext)) {
            newselection = new Node[0];
        }

        setExploredContext(rootContext, newselection);
    }

    /** @return true iff all nodes are under the target node */
    private boolean areUnderTarget(Node[] nodes, Node target) {
bigloop: 
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];

            while (node != null) {
                if (node.equals(target)) {
                    continue bigloop;
                }

                node = node.getParentNode();
            }

            return false;
        }

        return true;
    }

    /** Add a <code>PropertyChangeListener</code> to the listener list.
    * @param l the listener to add
    */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertySupport == null) {
            propertySupport = new PropertyChangeSupport(this);
        }

        propertySupport.addPropertyChangeListener(l);
    }

    /** Remove a <code>PropertyChangeListener</code> from the listener list.
    * @param l the listener to remove
    */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (propertySupport != null) {
            propertySupport.removePropertyChangeListener(l);
        }
    }

    /** Add a <code>VetoableListener</code> to the listener list.
    * @param l the listener to add
    */
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        if (vetoableSupport == null) {
            vetoableSupport = new VetoableChangeSupport(this);
        }

        vetoableSupport.addVetoableChangeListener(l);
    }

    /** Remove a <code>VetoableChangeListener</code> from the listener list.
    * @param l the listener to remove
    */
    public synchronized void removeVetoableChangeListener(VetoableChangeListener l) {
        if (vetoableSupport != null) {
            vetoableSupport.removeVetoableChangeListener(l);
        }
    }

    /** Checks whether given Node is a subnode of rootContext.
    * @return true if specified Node is under current rootContext
    */
    private boolean isUnderRoot(Node node) {
        while (node != null) {
            if (node.equals(rootContext)) {
                return true;
            }

            node = node.getParentNode();
        }

        return false;
    }

    /** Checks whether given Node is a subnode of rootContext.
    * and throws IllegalArgumentException if not.
    */
    private void checkUnderRoot(Node value, String errorKey) {
        if ((value != null) && !isUnderRoot(value)) {
            throw new IllegalArgumentException(
                NbBundle.getMessage(
                    ExplorerManager.class, errorKey, value.getDisplayName(), rootContext.getDisplayName()
                )
            );
        }
    }

    /** Waits till all async processing is finished
     */
    final void waitFinished() {
        if (selectionSyncTask != null) {
            selectionSyncTask.waitFinished();
        }
    }

    /** serializes object
    * @serialData the following objects are written in sequence:
    * <ol>
    * <li> a Node.Handle for the root context; may be null if root context
    *      is not persistable
    * <li> the display name of the root context (to give nicer error messages
    *      later on)
    * <li> the path from root context to explored context; null if no explored
    *      context or no such path
    * <li> for every element of node selection, path from root context to that node;
    *      null if no such path
    * <li> null to terminate
    * </ol>
    * Note that if the root context handle is null, the display name is still written
    * but the paths to explored context and node selection are not written, the stream
    * ends there.
    */
    private void writeObject(ObjectOutputStream os) throws IOException {
        // indication that we gonna use put fields and not the old method.
        os.writeObject(this);

        ObjectOutputStream.PutField fields = os.putFields();

        // [PENDING] is this method (and readObject) always called from within
        // the Nodes mutex? It should be!
        //System.err.println("rootContext: " + rootContext);
        Node.Handle rCH = rootContext.getHandle();
        fields.put("root", rCH); // NOI18N

        //System.err.println("writing: " + rCH);
        fields.put("rootName", rootContext.getDisplayName()); // NOI18N

        if (rCH != null) {
            // Note that explored context may be null (this is valid).
            // Also, it may have happened that the hierarchy changed so that
            // the explored context is *no longer* under the root (though it was at
            // the time these things were set up). In this case, we cannot store the
            // path. Caution: NodeOp.createPath will create a path to a root (parentless)
            // node even if you specify a non-null root, if the first arg is not a child!
            String[] explored;

            if (exploredContext == null) {
                explored = null;
            } else if (isUnderRoot(exploredContext)) {
                explored = NodeOp.createPath(exploredContext, rootContext);
            } else {
                explored = null;
            }

            fields.put("explored", explored); // NOI18N

            LinkedList selected = new LinkedList();

            for (int i = 0; i < selectedNodes.length; i++) {
                if (isUnderRoot(selectedNodes[i])) {
                    selected.add(NodeOp.createPath(selectedNodes[i], rootContext));
                }
            }

            fields.put("selected", selected.toArray()); // NOI18N
        }

        os.writeFields();
    }

    /** Deserializes the view and initializes it
     * @serialData see writeObject
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // perform initialization
        init();

        // read the first object in the stream
        Object firstObject = ois.readObject();

        if (firstObject != this) {
            // use old version of deserialization
            readObjectOld((Node.Handle) firstObject, ois);

            return;
        }

        // work with get fields
        ObjectInputStream.GetField fields = ois.readFields();

        // read root handle
        Node.Handle h = (Node.Handle) fields.get("root", null); // NOI18N

        //System.err.println("reading: " + h);
        final String rootName = (String) fields.get("rootName", null); // NOI18N

        //System.err.println("reading: " + rootName);
        if (h == null) {
            // Cancel deserialization (e.g. of the ExplorerPanel window) in case the
            // root handle was not persistent:
            throw new SafeException(
                new IOException(
                    "Could not restore Explorer window; the root node \"" + rootName +
                    "\" is not persistent; override Node.getHandle to fix"
                )
            ); // NOI18N
        } else {
            String[] exploredCtx = (String[]) fields.get("explored", null); // NOI18N
            Object[] selPaths = (Object[]) fields.get("selected", null); // NOI18N

            try {
                Node root = h.getNode();

                if (root == null) {
                    throw new IOException("Node.Handle.getNode (for " + rootName + ") should not return null"); // NOI18N
                }

                restoreSelection(root, exploredCtx, Arrays.asList(selPaths));
            } catch (IOException ioe) {
                SafeException safe = new SafeException(ioe);

                if (!Utilities.compareObjects(ioe.getMessage(), ioe.getLocalizedMessage())) {
                    Exceptions.attachLocalizedMessage(safe,
                                                      NbBundle.getMessage(ExplorerManager.class,
                                                                          "EXC_handle_failed",
                                                                          rootName));
                }

                throw safe;
            }
        }
    }

    private void readObjectOld(Node.Handle h, ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
        if (h == null) {
            // do nothing => should not occur to often and moreover this is also
            // dead code replaced by new version
            return;
        } else {
            String[] rootCtx = (String[]) ois.readObject();
            String[] exploredCtx = (String[]) ois.readObject();
            LinkedList ll = new LinkedList();

            for (;;) {
                String[] path = (String[]) ois.readObject();

                if (path == null) {
                    break;
                }

                ll.add(path);
            }

            Node root = findPath(h.getNode(), rootCtx);
            restoreSelection(root, exploredCtx, ll);
        }
    }

    private void restoreSelection(
        final Node root, final String[] exploredCtx, final List selectedPaths /* of String[] */
    ) {
        setRootContext(root);

        // XXX(-ttran) findPath() can take a long time and employs DataSystems
        // and others.  We cannot call it synchrorously, in the past deadlocks
        // have happened because of this.  OTOH as we call setSelectedNodes
        // asynchonously someone else can change the root context or the Node
        // hierarchy in between, which causes setSelectedNodes to throw
        // IllegalArgumentException.  There seems to be no simple good
        // solution.  For now we just catch IllegalArgumentException and be
        // decently silent about the fact.
        //RequestProcessor.getDefault().post(new Runnable() {
        // bugfix #38207, select nodes has to be called in AWT queue here
        Mutex.EVENT.readAccess(
            new Runnable() {
                public void run() {
                    // convert paths to Nodes
                    List selNodes = new ArrayList(selectedPaths.size());

                    for (Iterator iter = selectedPaths.iterator(); iter.hasNext();) {
                        String[] path = (String[]) iter.next();
                        selNodes.add(findPath(root, path));
                    }

                    // set the selection
                    Node[] newSelection = (Node[]) selNodes.toArray(new Node[selNodes.size()]);

                    if (exploredCtx != null) {
                        setExploredContext(findPath(root, exploredCtx), newSelection);
                    } else {
                        setSelectedNodes0(newSelection);
                    }
                }
            }
        );
    }

    /**
     * Finds the proper Explorer manager for a given component.  This is done
     * by traversing the component hierarchy and finding the first ancestor
     * that implements {@link Provider}.  <P> This method should be used in
     * {@link Component#addNotify} of each component that works with the
     * Explorer manager, e.g.:
     * <p><pre>
     * private transient ExplorerManager explorer;
     *
     * public void addNotify () {
     *   super.addNotify ();
     *   explorer = ExplorerManager.find (this);
     * }
     * </pre>
     *
     * @param comp component to find the manager for
     * @return the manager, or a new empty manager if no ancestor implements
     * <code>Provider</code>
     *
     * @see Provider
     */
    public static ExplorerManager find(Component comp) {
        // start looking for manager from parent, not the component itself
        for (;;) {
            comp = comp.getParent();

            if (comp == null) {
                // create new explorer because nothing has been found
                return new ExplorerManager();
            }

            if (comp instanceof Provider) {
                // ok, found a provider, return its manager
                return ((Provider) comp).getExplorerManager();
            }
        }
    }

    /** Finds node by given path */
    static Node findPath(Node r, String[] path) {
        try {
            return NodeOp.findPath(r, path);
        } catch (NodeNotFoundException ex) {
            return ex.getClosestNode();
        }
    }

    /** Creates or retrieves RequestProcessor for selection updates. */
    static synchronized RequestProcessor getSelectionProcessor() {
        if (selectionProcessor == null) {
            selectionProcessor = new RequestProcessor("ExplorerManager-selection"); //NOI18N
        }

        return selectionProcessor;
    }

    /** Finds ExplorerActionsImpl for a explorer manager.
     * @param em the manager
     * @return ExplorerActionsImpl
     */
    static synchronized ExplorerActionsImpl findExplorerActionsImpl(ExplorerManager em) {
        if (em.actions == null) {
            em.actions = new ExplorerActionsImpl();
            em.actions.attach(em);
        }

        return em.actions;
    }

    private void fireInAWT(final String propName, final Object oldVal, final Object newVal) {
        if (propertySupport != null) {
            Mutex.EVENT.readAccess(
                new Runnable() {
                    public void run() {
                        propertySupport.firePropertyChange(propName, oldVal, newVal);
                    }
                }
            );
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(ExplorerManager.class, key);
    }

    //
    // inner classes
    //

    /** Interface for components wishing to provide their own <code>ExplorerManager</code>.
    * @see ExplorerManager#find
    * @see ExplorerUtils
    */
    public static interface Provider {
        /** Get the explorer manager.
        * @return the manager
        */
        public ExplorerManager getExplorerManager();
    }

    /** Listener to be notified when root node has been destroyed.
    * Then the root node is changed to Node.EMPTY
    */
    private class Listener extends NodeAdapter implements Runnable {
        Collection removeList = new HashSet();

        Listener() {
        }

        /** Fired when the node is deleted.
         * @param ev event describing the node
         */
        public void nodeDestroyed(NodeEvent ev) {
            if (ev.getNode().equals(getRootContext())) {
                // node has been deleted
                // [PENDING] better to show a node with a label such as "<deleted>"
                // and a tool tip explaining the situation
                setRootContext(Node.EMPTY);
            } else {
                // assume that the node is among currently selected nodes
                scheduleRemove(ev.getNode());
            }
        }

        /* Change in a node.
         * @param ev the event
         */
        public void propertyChange(java.beans.PropertyChangeEvent ev) {
            fireInAWT(PROP_NODE_CHANGE, null, null);
        }

        /** Schedules removal of a node
        */
        private void scheduleRemove(Node n) {
            synchronized (ExplorerManager.this) {
                if (selectionSyncTask == null) {
                    selectionSyncTask = getSelectionProcessor().create(this);
                } else {
                    selectionSyncTask.cancel();
                }
            }

            synchronized (this) {
                removeList.add(n);
            }

            // invariant: selectionSyncTask != null && is not running yet.
            selectionSyncTask.schedule(SELECTION_SYNC_DELAY);
        }

        public void run() {
            if (!Children.MUTEX.isReadAccess()) {
                Children.MUTEX.readAccess(this);

                return;
            }

            Collection remove;

            synchronized (this) {
                // atomically clears the list while keeping a copy.
                // if another node is removed after this point, the selection
                // will be updated later.
                remove = removeList;
                removeList = new HashSet();
            }

            LinkedList newSel = new LinkedList(Arrays.asList(getSelectedNodes()));
            Iterator it = remove.iterator();

            while (it.hasNext()) {
                Node n_remove = (Node) it.next();

                if (newSel.contains(n_remove)) {
                    // compare paths to root
                    Node n_selection = (Node) newSel.get(newSel.indexOf(n_remove));

                    if (!Arrays.equals(NodeOp.createPath(n_remove, null), NodeOp.createPath(n_selection, null))) {
                        it.remove();
                    }
                }
            }

            newSel.removeAll(remove);
            for( Iterator i=newSel.iterator(); i.hasNext(); ) {
                Node n = (Node)i.next();
                if( !isUnderRoot( n ) )
                    i.remove();
            }

            Node[] selNodes = (Node[]) newSel.toArray(new Node[newSel.size()]);
            setSelectedNodes0(selNodes);
        }
    }
}
