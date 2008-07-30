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

import org.openide.nodes.*;
import java.util.*;


/** List of Visualizers. This is holded by parent visualizer by a
* weak reference,
*
* @author Jaroslav Tulach
*/
final class VisualizerChildren extends Object {
    /** empty visualizer children for any leaf */
    public static final VisualizerChildren EMPTY = new VisualizerChildren();   
    
    /** parent visualizer */
    public final VisualizerNode parent;

    /** visualizer nodes (children) */
    private final List<VisualizerNode> visNodes;
    
    List<Node> snapshot;
    
    private static final int prefetchCount = Math.max(Integer.getInteger("org.openide.explorer.VisualizerChildren.prefetchCount", 50), 0);  // NOI18N

    /** Empty VisualizerChildren. */
    private VisualizerChildren () {
        visNodes = Collections.EMPTY_LIST;
        parent = null;
    }    
    
    /** Creates new VisualizerChildren.
     * Can be called only from EventQueue.
     */
    public VisualizerChildren(VisualizerNode parent, int size, List<Node> snapshot) {
        this.parent = parent;
        visNodes = new ArrayList<VisualizerNode>(size);
        int prefetched = Math.min(prefetchCount, size);
        for (int i = 0; i < prefetched; i++) {
            VisualizerNode vn = VisualizerNode.getVisualizer(this, snapshot.get(i));
            visNodes.add(vn);
        }
        for (int i = prefetched; i < size; i++) {
            visNodes.add(null);
        }
        this.snapshot = snapshot;
    }

    /** recomputes indexes for all nodes.
     * @param tn tree node that we are looking for
     * @return true if there is non-null object inside
     */
    private final boolean recomputeIndexes(VisualizerNode tn) {
        assert tn == null || this.parent == tn.getParent() : "tn must be our child!"; // NOI18N

        boolean isNonNull = false;
        for (int i = 0; i < visNodes.size(); i++) {
            VisualizerNode node = (VisualizerNode) visNodes.get(i);
            if (node != null) {
                node.indexOf = i;
                isNonNull = true;
            }
        }

        if (tn != null && tn.indexOf == -1) {
            // not computed => force computation
            for (int i = 0; i < visNodes.size(); i++) {
                VisualizerNode visNode = (VisualizerNode) getChildAt(i);
                visNode.indexOf = i;
                if (visNode == tn) {
                    return isNonNull;
                }
            }
        }
        return isNonNull;
    }  
    
    public javax.swing.tree.TreeNode getChildAt(int pos) {
        if (pos >= visNodes.size()) {
            return VisualizerNode.EMPTY;
        }
        VisualizerNode visNode = visNodes.get(pos);
        if (visNode == null) {
            Node node = snapshot.get(pos);
            visNode = VisualizerNode.getVisualizer(this, node);
            visNode.indexOf = pos;
            visNodes.set(pos, visNode);
        }
        return visNode;
    }
    
    public int getChildCount() {
        return visNodes.size();
    }

    public java.util.Enumeration children() {
        return new java.util.Enumeration() {

            private int index;

            public boolean hasMoreElements() {
                return index < visNodes.size();
            }

            public Object nextElement() {
                return getChildAt(index++);
            }
        };
    }

    /** Delegated to us from VisualizerNode
     * 
     */
    public int getIndex(final javax.swing.tree.TreeNode p1) {
        VisualizerNode visNode = (VisualizerNode) p1;
        if (visNode.getParent() != this.parent) {
            return -1;
        }

        if (visNode.indexOf == -1) {
            recomputeIndexes(visNode);
        }
        assert visNode.indexOf != -1 : dumpIndexes(visNode); // NOI18N
        return visNode.indexOf;
    }

    final String dumpIndexes(VisualizerNode visNode) {
        StringBuilder sb = new StringBuilder();
        sb.append("EMPTY: " + (visNode == VisualizerNode.EMPTY) + ", Lazy: " + snapshot.getClass().getName().endsWith("LazySnapshot"));
        sb.append("\nSeeking for: ").append(visNode.toId());
        sb.append("\nwith parent: ").append(((VisualizerNode)visNode.getParent()) != null
                ? ((VisualizerNode)visNode.getParent()).toId() : "null");
        sb.append("\nSeeking in : ").append(parent != null ? parent.toId() : "null").append("\n");
        for (int i = 0; i < visNodes.size(); i++) {
            VisualizerNode node = (VisualizerNode) visNodes.get(i);
            sb.append("  ").append(i);
            if (node != null) {
                sb.append(" = ").append(node.toId());
            } else {
                sb.append(" = null");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    /** Notification of children addded event. Modifies the list of nodes
     * and fires info to all listeners.
     */
    public void added(VisualizerEvent.Added ev) {
        if (this != parent.getChildren()) {
            // children were replaced (e.g. VisualizerNode.naturalOrder()), quit processing event
            return;
        }        
        snapshot = ev.originalEvent.getSnapshot();
        ListIterator<VisualizerNode> it = visNodes.listIterator();
        boolean empty = !it.hasNext();

        int[] indxs = ev.getArray();

        int current = 0;
        int inIndxs = 0;

        while (inIndxs < indxs.length) {
            while (current++ < indxs[inIndxs]) {
                it.next();
            }
            it.add(null);
            inIndxs++;
        }

        boolean isNonNull = recomputeIndexes(null);

        VisualizerNode parent = this.parent;
        while (parent != null) {
            Object[] listeners = parent.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).added(ev);
            }
            parent = (VisualizerNode) parent.getParent();
        }
        if (empty) {
            // change of state
            this.parent.notifyVisualizerChildrenChange(isNonNull, this);
        }
    }

    /** Notification that children has been removed. Modifies the list of nodes
     * and fires info to all listeners.
     */
   public void removed(VisualizerEvent.Removed ev) {
        if (this != parent.getChildren()) {
            // children were replaced (e.g. VisualizerNode.naturalOrder()), quit processing event
            return;
        }
        snapshot = ev.originalEvent.getSnapshot();
        int[] idxs = ev.getArray();
        if (idxs.length == 0) {
            return;
        }

        //NodeMemberEvent origEvent = (NodeMemberEvent) ev.originalEvent;
        for (int i = idxs.length - 1; i >= 0; i--) {
            VisualizerNode visNode = visNodes.remove(idxs[i]);
            /*if (visNode == null) {
                Node node = origEvent.getDelta()[i];
                visNode = VisualizerNode.getVisualizer(this, node);
            }
            ev.removed.add(visNode);*/
            ev.removed.add(visNode != null ? visNode : VisualizerNode.EMPTY);
        }

        // notify event about changed indexes
        recomputeIndexes(null);

        VisualizerNode parent = this.parent;
        while (parent != null) {
            Object[] listeners = parent.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).removed(ev);
            }
            parent = (VisualizerNode) parent.getParent();
        }

        if (visNodes.isEmpty()) {
            // now is empty
            this.parent.notifyVisualizerChildrenChange(true, this);
        }
    }

    /**
     * Issue 37802, sort the actual list of children with the comparator,
     * rather than expecting it to match the current children of the node,
     * which may be in an inconsistent state.
     */
    private int[] reorderByComparator(Comparator<VisualizerNode> c) {
        VisualizerNode[] old = visNodes.toArray(new VisualizerNode[visNodes.size()]);
        Arrays.sort(old, c);

        int[] idxs = new int[old.length];
        for (int i = 0; i < idxs.length; i++) {
            idxs[i] = visNodes.indexOf(old[i]);
        }

        visNodes.clear();
        visNodes.addAll(Arrays.asList(old));
        return idxs;
    }

    /** Notification that children has been reordered. Modifies the list of nodes
     * and fires info to all listeners.
     */
    public void reordered(VisualizerEvent.Reordered ev) {
        if (ev.originalEvent != null) {
            snapshot = ev.originalEvent.getSnapshot();
        }
        
        if (ev.getComparator() != null) {
            //#37802
            ev.array = reorderByComparator(ev.getComparator());
        } else {
            int[] indxs = ev.getArray();
            VisualizerNode[] old = visNodes.toArray(new VisualizerNode[visNodes.size()]);
            VisualizerNode[] arr = new VisualizerNode[old.length];
            int s = indxs.length;
            try {
                for (int i = 0; i < s; i++) {
                    // arr[indxs[i]] = old[i];
                    VisualizerNode old_i = old[i];
                    int indxs_i = indxs[i];

                    if (arr[indxs_i] != null) {
                        // this is bad <-- we are rewriting some old value --> there will remain some null somewhere
                        System.err.println("Writing to this index for the second time: " + indxs_i); // NOI18N
                        System.err.println("Length of indxs array: " + indxs.length); // NOI18N
                        System.err.println("Length of actual array: " + old.length); // NOI18N
                        System.err.println("Indices of reorder event:"); // NOI18N

                        for (int j = 0; i < indxs.length; j++)
                            System.err.println("\t" + indxs[j]); // NOI18N

                        Thread.dumpStack();

                        return;
                    }

                    arr[indxs_i] = old_i;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                System.err.println("Length of actual array: " + old.length); // NOI18N
                System.err.println("Indices of reorder event:"); // NOI18N

                for (int i = 0; i < indxs.length; i++)
                    System.err.println("\t" + indxs[i]); // NOI18N

                return;
            }

            /*assert !Arrays.asList(arr).contains(null) : "Null element in reorderer list " + Arrays.asList(arr) +
            "; list=" + visNodes + " indxs=" + Arrays.asList(org.openide.util.Utilities.toObjectArray(indxs));*/
            visNodes.clear();
            visNodes.addAll(Arrays.asList(arr));
            //assert !visNodes.contains(null);
        }
        recomputeIndexes(null);

        VisualizerNode parent = this.parent;

        while (parent != null) {
            Object[] listeners = parent.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).reordered(ev);
            }
            parent = (VisualizerNode) parent.getParent();
        }
    }
}
