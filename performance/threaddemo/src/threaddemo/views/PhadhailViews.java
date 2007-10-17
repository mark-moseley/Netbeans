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

package threaddemo.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyVetoException;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreeModel;
import threaddemo.views.looktree.LookTreeView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import threaddemo.model.Phadhail;
import org.netbeans.api.nodes2looks.Nodes;
import org.netbeans.spi.looks.Selectors;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import threaddemo.locking.LockAction;
import threaddemo.locking.Locks;

/**
 * Factory for views over Phadhail.
 * All views are automatically scrollable; you do not need to wrap them in JScrollPane.
 * @author Jesse Glick
 */
public class PhadhailViews {
    
    private PhadhailViews() {}
    
    private static Component nodeBasedView(Node root) {
        Node root2;
        if (Children.MUTEX == Mutex.EVENT) {
            // #35833 branch.
            root2 = root;
        } else {
            root2 = new EQReplannedNode(root);
        }
        ExpPanel p = new ExpPanel();
        p.setLayout(new BorderLayout());
        JComponent tree = new BeanTreeView();
        p.add(tree, BorderLayout.CENTER);
        p.getExplorerManager().setRootContext(root2);
        try {
            p.getExplorerManager().setSelectedNodes(new Node[] {root2});
        } catch (PropertyVetoException pve) {
            pve.printStackTrace();
        }
        Object key = "org.openide.actions.PopupAction";
        KeyStroke ks = KeyStroke.getKeyStroke("shift F10");
        tree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, key);
        return p;
    }
    
    /** use Nodes API with an Explorer view */
    public static Component nodeView(Phadhail root) {
        return nodeBasedView(new PhadhailNode(root));
    }
    
    /** use Looks and Nodes API with an Explorer view */
    public static Component lookNodeView(Phadhail root) {
        return nodeBasedView(Nodes.node(root, null, Selectors.selector( new PhadhailLookProvider())));
    }
    
    /** use raw Looks API with a JTree */
    public static Component lookView(Phadhail root) {
        // XXX pending a stable API...
        return new JScrollPane(new LookTreeView(root, Selectors.selector( new PhadhailLookProvider() )));
    }
    
    /** use Phadhail directly in a JTree */
    public static Component rawView(Phadhail root) {
        TreeModel model = new PhadhailTreeModel(root);
        JTree tree = new JTree(model) {
            // Could also use a custom TreeCellRenderer, but this is a bit simpler for now.
            public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Phadhail ph = (Phadhail)value;
                return ph.getPath();
            }
        };
        tree.setLargeModel(true);
        return new JScrollPane(tree);
    }
    
    /**
     * Workaround for the fact that Node/Look currently do not run only in AWT.
     */
    private static final class EQReplannedNode extends FilterNode {
        public EQReplannedNode(Node n) {
            super(n, n.isLeaf() ? Children.LEAF : new EQReplannedChildren(n));
        }
        public String getName() {
            return Locks.event().read(new LockAction<String>() {
                public String run() {
                    return EQReplannedNode.super.getName();
                }
            });
        }
        public String getDisplayName() {
            return Locks.event().read(new LockAction<String>() {
                public String run() {
                    return EQReplannedNode.super.getDisplayName();
                }
            });
        }
        // XXX any other methods could also be replanned as needed
    }
    private static final class EQReplannedChildren extends FilterNode.Children {
        public EQReplannedChildren(Node n) {
            super(n);
        }
        protected Node copyNode(Node n) {
            return new EQReplannedNode(n);
        }
        public Node findChild(final String name) {
            return Locks.event().read(new LockAction<Node>() {
                public Node run() {
                    return EQReplannedChildren.super.findChild(name);
                }
            });
        }
        public Node[] getNodes(final boolean optimalResult) {
            return Locks.event().read(new LockAction<Node[]>() {
                public Node[] run() {
                    return EQReplannedChildren.super.getNodes(optimalResult);
                }
            });
        }
        protected void filterChildrenAdded(final NodeMemberEvent ev) {
            Locks.event().readLater(new Runnable() {
                public void run() {
                    EQReplannedChildren.super.filterChildrenAdded(ev);
                }
            });
        }
        protected void filterChildrenRemoved(final NodeMemberEvent ev) {
            Locks.event().readLater(new Runnable() {
                public void run() {
                    EQReplannedChildren.super.filterChildrenRemoved(ev);
                }
            });
        }
        protected void filterChildrenReordered(final NodeReorderEvent ev) {
            Locks.event().readLater(new Runnable() {
                public void run() {
                    EQReplannedChildren.super.filterChildrenReordered(ev);
                }
            });
        }
    }
    
    /**
     * Replacement for ExplorerPanel, which is deprecated (and uses Filesystems!).
     * @see "#36315"
     */
    private static final class ExpPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
        
        private final ExplorerManager manager;
        private final Lookup lookup;
        
        public ExpPanel() {
            manager = new ExplorerManager();
            ActionMap map = getActionMap();
            map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
            map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
            map.put("delete", ExplorerUtils.actionDelete(manager, true));
            lookup = ExplorerUtils.createLookup(manager, map);
            InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            keys.put(KeyStroke.getKeyStroke("control c"), DefaultEditorKit.copyAction);
            keys.put(KeyStroke.getKeyStroke("control x"), DefaultEditorKit.cutAction);
            keys.put(KeyStroke.getKeyStroke("control v"), DefaultEditorKit.pasteAction);
            keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");
        }
        
        public void addNotify() {
            super.addNotify();
            ExplorerUtils.activateActions(manager, true);
        }
        
        public void removeNotify() {
            ExplorerUtils.activateActions(manager, false);
            super.removeNotify();
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        
    }
    
}
