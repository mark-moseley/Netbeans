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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelui.switcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander Simon, Vladimir Voskresensky
 */
public class CodeAssistanceSubmenuAction extends NodeAction {

    private LazyPopupMenu popupMenu;
    private final Collection<Action> items = new ArrayList(5);
    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return popupMenu;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return popupMenu;
    }

    private void createSubMenu() {
        if (popupMenu == null) {
            popupMenu = new LazyPopupMenu(getName(), items); 
        }
        items.clear();
        boolean enabled = false;
        for(Action action : addActionsFromLayers()) {
            items.add(action);
            enabled = true;
        }
        popupMenu.setEnabled(enabled);
    }

    private List<Action> addActionsFromLayers() {
        String path = "NativeProjects/Actions"; // NOI18N
        List<Action> popup = new ArrayList<Action>();
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                popup.add((Action) next);
            }
        }
        return popup;
    }

    protected void performAction(Node[] activatedNodes) {
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public String getName() {
        return NbBundle.getMessage(CodeAssistanceSubmenuAction.class, "LBL_CodeAssistanceAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private final static class LazyPopupMenu extends JMenu {
        private final Collection<Action> items;
        public LazyPopupMenu(String name, Collection<Action> items) {
            super(name);
            assert items != null : "array must be inited";
            this.items = items;
        }
        
        @Override
        public synchronized JPopupMenu getPopupMenu() {
            super.removeAll();
            for (Action action : items) {
                if (action instanceof Presenter.Popup) {
                    JMenuItem item = ((Presenter.Popup)action).getPopupPresenter();
                    add(item);
                } else if (action instanceof Presenter.Menu) {
                    JMenuItem item = ((Presenter.Menu)action).getMenuPresenter();
                    add(item);
                } else {
                    add(action);
                }
            }
            return super.getPopupMenu();
        }
    }
}
