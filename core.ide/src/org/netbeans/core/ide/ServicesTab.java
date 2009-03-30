/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.ide;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Handle;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Services tab which lists nodes found in {@code UI/Runtime}.
 */
public class ServicesTab extends TopComponent implements ExplorerManager.Provider {

    private static final long serialVersionUID = 1L;

    private final ExplorerManager manager;
    private final TreeView view;

    private ServicesTab() {
        manager = new ExplorerManager();
        manager.setRootContext(new ServicesNode());
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        associateLookup(ExplorerUtils.createLookup(manager, map));
        view = new BeanTreeView();
        view.setRootVisible(false);
        setLayout(new BorderLayout());
        add(view);
        setName(preferredID());
        setDisplayName(NbBundle.getMessage(ServicesTab.class, "LBL_Services"));
        setIcon(ImageUtilities.loadImage("org/netbeans/core/ide/resources/services.gif", true));
    }

    private static final String ID = "services"; // NOI18N
    protected @Override String preferredID() {
        return ID;
    }
    private static ServicesTab DEFAULT;
    public static synchronized ServicesTab getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new ServicesTab();
        }
        return DEFAULT;
    }
    static synchronized ServicesTab findDefault() {
        if (DEFAULT == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent(ID);
            if (tc instanceof ServicesTab) {
                DEFAULT = (ServicesTab) tc;
            } else {
                Logger.getLogger(ServicesTab.class.getName()).warning("Cannot find tab by ID");
                DEFAULT = new ServicesTab();
            }
        }
        return DEFAULT;
    }

    public @Override int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    protected @Override void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    protected @Override void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    @SuppressWarnings("deprecation")
    public @Override boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return view.requestFocusInWindow();
    }

    public @Override HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.core.actions.ViewRuntimeTabAction"); // NOI18N
    }

    private static class ServicesNode extends AbstractNode {

        ServicesNode() {
            super(Children.create(new ChildFactory<Node>() {
                protected @Override Node createNodeForKey(Node key) {
                    return key;
                }
                protected boolean createKeys(List<Node> toPopulate) {
                    toPopulate.addAll(Lookups.forPath("UI/Runtime").lookupAll(Node.class)); // NOI18N
                    return true;
                }
            }, true));
        }

        public @Override PasteType getDropType(Transferable t, int action, int index) {
            return null; // #118628
        }

        public @Override Handle getHandle() {
            return new Handle() {
                public Node getNode() throws IOException {
                    return findDefault().manager.getRootContext();
                }
            };
        }

        public @Override Action[] getActions(boolean context) {
            return new Action[0];
        }

    }

}
