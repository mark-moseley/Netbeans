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
package org.openide.explorer.view;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.*;
import org.openide.util.Mutex;
import java.awt.Image;
import java.beans.BeanInfo;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.LogRecord;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.tree.TreeNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;


/** Visual representation of one node. Holds necessary information about nodes
* like icon, name, description and also list of its children.
* <P>
* There is at most one VisualizerNode for one node. All of them are held in a cache.
* <P>
* VisualizerNode provides a thread-safe layer between Nodes, which may fire
* property changes on any thread, and the AWT dispatch thread
* thread.
*
* @author Jaroslav Tulach
*/
final class VisualizerNode extends EventListenerList implements NodeListener, TreeNode, Runnable {
    /** one template to use for searching for visualizers */
    private static final VisualizerNode TEMPLATE = new VisualizerNode(0);
    /** a shared logger for the visualizer functionality */
    static final Logger LOG = Logger.getLogger(VisualizerNode.class.getName());

    /** constant holding empty reference to children */
    private static final Reference<VisualizerChildren> NO_REF = new WeakReference<VisualizerChildren>(null);

    /** cache of visualizers */
    private static WeakHashMap<VisualizerNode, Reference<VisualizerNode>> cache = 
            new WeakHashMap<VisualizerNode, Reference<VisualizerNode>>();

    /** empty visualizer */
    public static final VisualizerNode EMPTY = getVisualizer(null, Node.EMPTY);

    /** queue processor to transfer requests to event queue */
    private static final QP QUEUE = new QP();
    private static final String UNKNOWN = new String();
    static final long serialVersionUID = 3726728244698316872L;
    private static final String NO_HTML_DISPLAYNAME = "noHtmlDisplayName"; //NOI18N

    /** loaded default icon */
    private static Icon defaultIcon;

    /** default icon to use when none is present */
    private static final String DEFAULT_ICON = "org/openide/nodes/defaultNode.png"; // NOI18N
    
    /** node prefetch count before initialization of VisualizerChildren - to avoid*/
    private static final int prefetchCount = Math.max(Integer.getInteger("org.openide.explorer.VisualizerNode.prefetchCount", 50), 0);  // NOI18N    

    /** Cached icon - pre-html, there was a separate cache in NodeRenderer, but
     * if we're keeping a weak cache of VisualizerNodes, there's no reason not
     * to keep it here */
    private Icon icon = null;

    /** node. Do not modify!!! */
    Node node;

    /** system hashcode of the node */
    private int hashCode;

    /** visualizer children attached thru weak references Reference (VisualizerChildren) */
    private Reference<VisualizerChildren> children = NO_REF;

    /** the VisualizerChildren that contains this VisualizerNode or null */
    VisualizerChildren parent;

    /** index in parent */
    int indexOf = -1;
    
    /** cached name */
    private String name;

    /** cached display name */
    private String displayName;

    /** cached short description */
    private String shortDescription;
    private String htmlDisplayName = null;
    private int cachedIconType = -1;

    /** Constructor that creates template for the node.
    */
    private VisualizerNode(int hashCode) {
        this.hashCode = hashCode;
        this.node = null;
    }

    /** Creates new VisualizerNode
    * @param n node to refer to
    */
    private VisualizerNode(Node n) {
        node = n;
        hashCode = System.identityHashCode(node);

        // attach as a listener
        node.addNodeListener(NodeOp.weakNodeListener(this, node));

        // uiListener = WeakListener.propertyChange (this, null);
        // UIManager.addPropertyChangeListener (uiListener);
        name = UNKNOWN;
        displayName = UNKNOWN;
        shortDescription = UNKNOWN;
    }

    // bugfix #29435, getVisualizer is synchronized in place of be called only from EventQueue

    /** Finds VisualizerNode for given node.
    * @param ch the children this visualizer should belong to
    * @param n the node
    * @return the visualizer
    */
    public static VisualizerNode getVisualizer(VisualizerChildren ch, Node n) {
        return getVisualizer(ch, n, true);
    }

    /** Finds VisualizerNode for given node.
    * @param ch the children this visualizer should belong to
    * @param n the node
    * @return the visualizer or null
    */
    public static synchronized VisualizerNode getVisualizer(VisualizerChildren ch, Node n, boolean create) {
        TEMPLATE.hashCode = System.identityHashCode(n);
        TEMPLATE.node = n;

        Reference<VisualizerNode> r = cache.get(TEMPLATE);

        TEMPLATE.hashCode = 0;
        TEMPLATE.node = null;

        VisualizerNode v = (r == null) ? null : r.get();

        if (v == null) {
            if (!create) {
                return null;
            }

            v = new VisualizerNode(n);
            cache.put(v, new WeakReference<VisualizerNode>(v));
        }

        if (ch != null) {
            v.parent = ch;
        }

        return v;
    }

    /** Returns cached short description.
     * @return short description of represented node
     */
    public String getShortDescription() {
        String desc = shortDescription;

        if (desc == UNKNOWN) {
            shortDescription = desc = node.getShortDescription();
        }
        if (icon instanceof Image) {
            String toolTip = ImageUtilities.getImageToolTip((Image) icon);
            if (toolTip.length() > 0) {
                StringBuilder str = new StringBuilder(128);
                str.append("<html>").append(desc).append("<br>").append(toolTip).append("</html>");
                desc = str.toString();
            }
        }
        return desc;
    }

    /** Returns cached display name.
     * @return display name of represented node
     */
    public String getDisplayName() {
        if (displayName == UNKNOWN) {
            displayName = (node == null) ? null : node.getDisplayName();
        }

        return displayName;
    }

    /** Returns cached name.
     * @return name of represented node
     */
    public String getName() {
        if (name == UNKNOWN) {
            name = (node == null) ? null : node.getName();
        }

        return name;
    }

    /** Getter for list of children of this visualizer.
    * @return list of VisualizerNode objects
    */
    public VisualizerChildren getChildren() {
        return getChildren(true);
    }

    final VisualizerChildren getChildren(boolean create) {
        VisualizerChildren ch = children.get();

        if (create && (ch == null) && !node.isLeaf()) {
            // initialize the nodes children before we enter the readAccess section 
            // (otherwise we could receive invalid node count (under lock))
            Children nch = node.getChildren();
            final int count = nch.getNodesCount();
            Node[] nodes = null;
            if (prefetchCount > 0) {
                if (count <= prefetchCount) {
                    // fire empty entries in single event if possible
                    nodes = nch.getNodes();
                } else {
                    nodes = new Node[Math.min(prefetchCount, count)];
                    for (int i = 0; i < nodes.length; i++) {
                        nodes[i] = nch.getNodeAt(i);
                    }
                }
            }
            // go into lock to ensure that no childrenAdded, childrenRemoved,
            // childrenReordered notifications occures and that is why we do
            // not loose any changes
            ch = Children.MUTEX.readAccess(
                    new Mutex.Action<VisualizerChildren>() {
                        public VisualizerChildren run() {
                            List<Node> snapshot = node.getChildren().snapshot();
                            VisualizerChildren vc = new VisualizerChildren(VisualizerNode.this, snapshot);
                            notifyVisualizerChildrenChange(true, vc);
                            return vc;
                        }
                    }
                );
        }
        return ch == null ? VisualizerChildren.EMPTY : ch;
    }

    //
    // TreeNode interface (callable only from AWT-Event-Queue)
    //
    public int getIndex(final javax.swing.tree.TreeNode p1) {
        return getChildren().getIndex(p1);
    }

    public boolean getAllowsChildren() {
        return !isLeaf();
    }

    private LogRecord assertAccess(int index) {
        if (Children.MUTEX.isReadAccess()) {
            return null;
        }
        if (Children.MUTEX.isWriteAccess()) {
            return null;
        }
        if (!LOG.isLoggable(Level.FINE)) {
            return null;
        }
        Level level = LOG.isLoggable(Level.FINEST) ? Level.FINEST : Level.FINE;
        LogRecord rec = new LogRecord(level, "LOG_NO_READ_ACCESS"); // NOI18N
        rec.setResourceBundle(NbBundle.getBundle(VisualizerNode.class));
        rec.setParameters(new Object[] { this, index });
        rec.setLoggerName(LOG.getName());
        if (level == Level.FINEST) {
            rec.setThrown(new AssertionError(rec.getMessage()));
        }
        return rec;
    }

    public javax.swing.tree.TreeNode getChildAt(int p1) {
        return getChildren().getChildAt(p1);
    }

    public int getChildCount() {
        return getChildren().getChildCount();
    }

    public java.util.Enumeration<VisualizerNode> children() {
        return getChildren().children(true);
    }

    public boolean isLeaf() {
        return node.isLeaf();
    }

    public javax.swing.tree.TreeNode getParent() {
        Node parent = node.getParentNode();

        return (parent == null) ? null : getVisualizer(null, parent);
    }

    // **********************************************
    // Can be called under Children.MUTEX.writeAccess
    // **********************************************

    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public void childrenAdded(NodeMemberEvent ev) {
        VisualizerChildren ch = children.get();

        LOG.log(Level.FINER, "childrenAdded {0}", ev); // NOI18N
        if (ch == null) {
            LOG.log(Level.FINER, "childrenAdded - exit"); // NOI18N
            return;
        }

        QUEUE.runSafe(new VisualizerEvent.Added(ch, ev.getDeltaIndices(), ev));
        LOG.log(Level.FINER, "childrenAdded - end"); // NOI18N
    }

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public void childrenRemoved(NodeMemberEvent ev) {
        VisualizerChildren ch = children.get();

        LOG.log(Level.FINER, "childrenRemoved {0}", ev); // NOI18N
        if (ch == null) {
            LOG.log(Level.FINER, "childrenRemoved - exit"); // NOI18N
            return;
        }

        QUEUE.runSafe(new VisualizerEvent.Removed(ch, ev.getDeltaIndices(), ev));
        LOG.log(Level.FINER, "childrenRemoved - end"); // NOI18N
    }

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev) {
        doChildrenReordered(ev);
    }

    // helper method (called from TreeTableView.sort)
    void doChildrenReordered(NodeReorderEvent ev) {
        VisualizerChildren ch = children.get();

        int[] perm = ev.getPermutation();
        LOG.log(Level.FINER, "childrenReordered {0}", perm); // NOI18N
        if (ch == null) {
            LOG.log(Level.FINER, "childrenReordered - exit"); // NOI18N
            return;
        }

        QUEUE.runSafe(new VisualizerEvent.Reordered(ch, perm, ev));
        LOG.log(Level.FINER, "childrenReordered - end"); // NOI18N
    }

    void reorderChildren(Comparator<VisualizerNode> c) {
        assert SwingUtilities.isEventDispatchThread();

        VisualizerChildren ch = children.get();

        if (ch == null) {
            return;
        }

        new VisualizerEvent.Reordered(ch, c, null).run();
    }

    void naturalOrder() {
        //force new creation of the children list in the natural order
        children.clear();
        getChildren();

        //sort the children list with a dummy comparator to throw events needed
        reorderChildren(
            new Comparator<VisualizerNode>() {
                public int compare(VisualizerNode o1, VisualizerNode o2) {
                    return 0;
                }
            }
        );
    }

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public void nodeDestroyed(NodeEvent ev) {
        // ignore for now
    }

    /** Change in the node properties (icon, etc.)
    */
    public void propertyChange(final java.beans.PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        boolean isIconChange = Node.PROP_ICON.equals(name) || Node.PROP_OPENED_ICON.equals(name);

        if (Node.PROP_NAME.equals(name) || Node.PROP_DISPLAY_NAME.equals(name) || isIconChange) {
            if (isIconChange) {
                //Ditch the cached icon type so the next call to getIcon() will
                //recreate the Icon
                cachedIconType = -1;
            }

            if (Node.PROP_DISPLAY_NAME.equals(name)) {
                htmlDisplayName = null;
            }
            
            QUEUE.runSafe(this);
            return;
        }

        // bugfix #37748, VisualizerNode ignores change of short desc if it is not read yet (set to UNKNOWN)
        if (Node.PROP_SHORT_DESCRIPTION.equals(name) && (shortDescription != UNKNOWN)) {
            QUEUE.runSafe(this);
            return;
        }

        if (Node.PROP_LEAF.equals(name)) {
            QUEUE.runSafe(new PropLeafChange());
            return;
        }
    }

    /** Update the state of this class by retrieving new name, etc.
    * And fire change to all listeners. Only by AWT-Event-Queue
    */
    public void run() {
        if (!Children.MUTEX.isReadAccess()) {
            Children.MUTEX.readAccess(this);
            return;
        }

        name = node.getName();
        displayName = node.getDisplayName();
        shortDescription = UNKNOWN;

        //
        // notify models
        //
        VisualizerNode parent = this;

        while (parent != null) {
            Object[] listeners = parent.getListenerList();

            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).update(this);
            }

            parent = (VisualizerNode) parent.getParent();
        }
    }

    //
    // Access to VisualizerChildren
    //

    /** Notifies that change could be needed in the way the children are held
    * (weak or hard reference). Called from VisualizerChildren
    * @param strongly if the children should be held via StrongReference
    * @param ch the children
    */
    void notifyVisualizerChildrenChange(boolean strongly, VisualizerChildren ch) {
        if (strongly) {
            // hold the children hard
            if (children.getClass() != StrongReference.class || children.get() != ch) {
                children = new StrongReference<VisualizerChildren>(ch);
            }
        } else {
            if (children.getClass() != WeakReference.class || children.get() != ch) {
                children = new WeakReference<VisualizerChildren>(ch);
            }
        }
    }

    // ********************************
    // This can be called from anywhere
    // ********************************

    /** Adds visualizer listener.
    */
    public synchronized void addNodeModel(NodeModel l) {
        add(NodeModel.class, l);
    }

    /** Removes visualizer listener.
    */
    public synchronized void removeNodeModel(NodeModel l) {
        remove(NodeModel.class, l);
    }

    /** Hash code
    */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /** Equals two objects are equal if they have the same hash code
    */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VisualizerNode)) {
            return false;
        }

        VisualizerNode v = (VisualizerNode) o;

        return v.node == node;
    }

    /** String name is taken from the node.
    */
    @Override
    public String toString() {
        return getDisplayName();
    }

    public String toId() {
        return "'" + getDisplayName() + "'@" +
            Integer.toHexString(System.identityHashCode(this)) +
            " parent: " + parent + " indexOf: " + indexOf;
    }

    public String getHtmlDisplayName() {
        if (htmlDisplayName == null) {
            htmlDisplayName = node.getHtmlDisplayName();

            if (htmlDisplayName == null) {
                htmlDisplayName = NO_HTML_DISPLAYNAME;
            }
        }

        return (htmlDisplayName == NO_HTML_DISPLAYNAME) ? null : htmlDisplayName;
    }

    Icon getIcon(boolean opened, boolean large) {
        int newCacheType = getCacheType(opened, large);

        if (cachedIconType != newCacheType) {
            int iconType = large ? BeanInfo.ICON_COLOR_32x32 : BeanInfo.ICON_COLOR_16x16;

            Image image;
            try {
                image = opened ? node.getOpenedIcon(iconType) : node.getIcon(iconType);

                // bugfix #28515, check if getIcon contract isn't broken
                if (image == null) {
                    String method = opened ? "getOpenedIcon" : "getIcon"; // NOI18N
                    LOG.warning(
                        "Node \"" + node.getName() + "\" [" + node.getClass().getName() + "] cannot return null from " +
                        method + "(). See Node." + method + " contract."
                        ); // NOI18N
                }
            } catch (RuntimeException x) {
                LOG.log(Level.INFO, null, x);
                image = null;
            }

            if (image == null) {
                icon = getDefaultIcon();
            } else {
                icon = ImageUtilities.image2Icon(image);
            }
        }

        cachedIconType = newCacheType;

        return icon;
    }

    /** Some simple bitmasking to determine the type of the cached icon.
     * Generally, it's worth caching one, but not a bunch - generally one will
     * be used repeatedly. */
    private static final int getCacheType(boolean opened, boolean large) {
        return (opened ? 2 : 0) | (large ? 1 : 0);
    }

    /** Loads default icon if not loaded. */
    private static Icon getDefaultIcon() {
        if (defaultIcon == null) {
            defaultIcon = ImageUtilities.loadImageIcon(DEFAULT_ICON, false);
        }
        return defaultIcon;
    }

    static void runSafe(Runnable run) {
        QUEUE.runSafe(run);
    }

    /** Strong reference.
    */
    private static final class StrongReference<T> extends WeakReference<T> {
        private T o;

        public StrongReference(T o) {
            super(null);
            this.o = o;
        }

        @Override
        public T get() {
            return o;
        }
    }

    /** Class that processes runnables in event queue. It guarantees that
    * the order of processed objects will be exactly the same as they
    * arrived.
    */
    private static final class QP implements Runnable {
        /** queue of all requests (Runnable) that should be processed
         * AWT-Event queue.
         */
        private LinkedList<Runnable> queue = new LinkedList<Runnable>();

        QP() {
        }
        
        boolean shouldBeInvokedLater(Runnable run) {
            return run instanceof VisualizerEvent.Removed && 
                    ((VisualizerEvent) run).getSnapshot().getClass().getName().contains("DelayedLazySnapshot");
        }
        
        /** Runs the runnable in event thread.
         * @param run what should run
         */
        public void runSafe(final Runnable run) {
            boolean wasEmpty = false;

            synchronized (this) {
                if (SwingUtilities.isEventDispatchThread() && shouldBeInvokedLater(run)) {
                    if (!queue.isEmpty()) {
                        // insert marker for interruption
                        queue.addLast(null);
                    }
                    queue.addLast(run);
                    SwingUtilities.invokeLater(this);
                    return;
                }
                
                wasEmpty = queue.isEmpty();
                queue.addLast(run);
            }

            if (wasEmpty) {
                // either starts the processing of the queue immediatelly
                // (if we are in AWT-Event thread) or uses 
                // SwingUtilities.invokeLater to do so
                Mutex.EVENT.writeAccess(this);
            }
        }

        public void run() {

            boolean isEmpty = false;
            while (!isEmpty) {
                Runnable r;
                synchronized (this) {
                    r = queue.poll();
                    if (r == null) {
                        LOG.log(Level.FINER, "Marker found, interrupting queue"); // NOI18N
                        return;
                    }
                    isEmpty = queue.isEmpty();
                }
                LOG.log(Level.FINER, "Running from queue {0}", r); // NOI18N
                Children.MUTEX.readAccess(r); // run the update under Children.MUTEX
                LOG.log(Level.FINER, "Finished {0}", r); // NOI18N
            }
            LOG.log(Level.FINER, "Queue processing over"); // NOI18N
        }
    }


    private class PropLeafChange implements Runnable {

        public PropLeafChange() {
        }

        public void run() {
            if (!Children.MUTEX.isReadAccess()) {
                Children.MUTEX.readAccess(this);
                return;
            }

            children = NO_REF;

            // notify models
            VisualizerNode parent = VisualizerNode.this;

            while (parent != null) {
                Object[] listeners = parent.getListenerList();

                for (int i = listeners.length - 1; i >= 0; i -= 2) {
                    ((NodeModel) listeners[i]).structuralChange(VisualizerNode.this);
                }

                parent = (VisualizerNode) parent.getParent();
            }
        }
    }

    /**
     * Builds the parents of vis. node up to and including the root node
     * from VisualizerNode hierarchy
    */
    VisualizerNode[] getPathToRoot() {
        return getPathToRoot(0);
    }

    VisualizerNode[] getPathToRoot(int depth) {
        depth++;
        VisualizerNode[] retNodes;
        if (parent == null || parent.parent == null) {
            retNodes = new VisualizerNode[depth];
        }
        else {
            retNodes = parent.parent.getPathToRoot(depth);
        }
        retNodes[retNodes.length - depth] = this;
        return retNodes;
    }
}
