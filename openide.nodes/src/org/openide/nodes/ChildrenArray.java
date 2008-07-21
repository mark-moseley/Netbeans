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

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.EntrySupport.Default.Info;


/** Holder of nodes for a children object. Communicates
* with children to notify when created/finalized.
*
* @author Jaroslav Tulach
*/
final class ChildrenArray extends NodeAdapter {
    /** children's EntrySupport */
    public  EntrySupport.Default entrySupport;

    /** nodes associated */
    private Node[] nodes;

    private Map<Info, Collection<Node>> map;

    /** the reference that points to us */
    private Reference<ChildrenArray> ref;

    private static final Logger LOG_NODES_FOR = Logger.getLogger(
            "org.openide.nodes.ChildrenArray.nodesFor"); // NOI18N

    /** Creates new ChildrenArray */
    public ChildrenArray() {
    }

    public Children getChildren() {
        return entrySupport == null ? null : entrySupport.children;
    }
    
    /** Now points to me */
    final void pointedBy(Reference<ChildrenArray> ref) {
        this.ref = ref;
    }

    /** Getter method to receive a set of computed nodes.
    */
    public Node[] nodes() {
        if (entrySupport == null) {
            // not fully initialize
            return null;
        }

        if (nodes == null) {
            nodes = entrySupport.justComputeNodes();

            for (int i = 0; i < nodes.length; i++) {
                // keeps a hard reference from the children node to this
                // so we can be GCed only when child nodes are gone
                nodes[i].reassignTo(entrySupport.children, this);
            }

            // if at least one node => be weak
            entrySupport.registerChildrenArray(this, nodes.length > 0);
        }
        return nodes;
    }

    /** Clears the array of nodes. */
    public void clear() {
        if (nodes != null) {
            nodes = null;

            // register in the childrens to be hold by hard reference
            // because we keep no reference to nodes, we can be
            // hard holded by children
            entrySupport.registerChildrenArray(this, false);
        }
    }

    /** Finalizes nodes by calling get on weak hash map,
     * all references stored in the map, that are finalized
     * will be cleared.
     */
    public void finalizeNodes() {
        Map m = map;
        if (m != null) {
            // processes the queue of garbage
            // collected keys
            m.remove(null);
        }
    }

    /** Initilized if has some nodes. */
    public boolean isInitialized() {
        return nodes != null;
    }

    private String logInfo(Info info) {
        return info.toString() + '[' + Integer.toHexString(System.identityHashCode(info)) + ']';
    }

    /** Gets the nodes for given info.
    * @param info the info
    * @return the nodes
    */
    public synchronized Collection<Node> nodesFor(Info info) {
        final boolean IS_LOG = LOG_NODES_FOR.isLoggable(Level.FINE);
        if (IS_LOG) {
            LOG_NODES_FOR.fine("nodesFor(" +logInfo(info) + ") on " + Thread.currentThread()); // NOI18N
        }
        if (map == null) {
            map = new WeakHashMap<Info, Collection<Node>>(7);
        }
        Collection<Node> nodes = map.get(info);

        if (IS_LOG) {
            LOG_NODES_FOR.fine("  map size=" + map.size() + ", nodes=" + nodes); // NOI18N
        }

        if (nodes == null) {
            nodes = info.entry.nodes();
            info.length = nodes.size();
            map.put(info, nodes);
            if (IS_LOG) {
                LOG_NODES_FOR.fine("  created nodes=" + nodes); // NOI18N
            }
        }

        if (IS_LOG) {
            LOG_NODES_FOR.fine("  leaving nodesFor(" +logInfo(info) + ") on " + Thread.currentThread()); // NOI18N
        }
        return nodes;
    }

    /** Refreshes the nodes for given info.
    * @param info the info
    * @return the nodes
    */
    public synchronized void useNodes(Info info, Collection<Node> list) {
        if (map == null) {
            map = new WeakHashMap<Info, Collection<Node>>(7);
        }
        info.length = list.size();
        map.put(info, list);
    }

    @Override
    public String toString() {
        return super.toString() + "  " + getChildren(); //NOI18N
    }
}
