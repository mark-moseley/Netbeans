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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.form.actions;

import java.util.ArrayList;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.undo.UndoableEdit;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.util.actions.NodeAction;

public class EncloseAction extends NodeAction {

    public String getName() {
        return org.openide.util.NbBundle.getBundle(EncloseAction.class)
                     .getString("ACT_EncloseInContainer"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Node[] nodes) {
        List comps = getComponents(nodes);
        return ((comps != null) && getContainer(comps) != null);
    }

    protected void performAction(Node[] nodes) {
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new ContainersMenu(getName(), getComponents(getActivatedNodes()));
        menu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(menu, EncloseAction.class.getName());
        return menu;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    // -------

    private static List<RADComponent> getComponents(Node[] nodes) {
        return FormUtils.getSelectedLayoutComponents(nodes);
    }

    private static RADVisualContainer getContainer(List components) {
        RADVisualContainer commonParent = null;
        for (Object comp : components) {
            if (comp instanceof RADVisualComponent) {
                RADVisualContainer parent = ((RADVisualComponent)comp).getParentContainer();
                if (parent == null || (commonParent != null && parent != commonParent)) {
                    return null;
                }
                if (commonParent == null) {
                    commonParent = parent;
                }
            } else {
                return null;
            }
        }
        return commonParent;
    }

    private static PaletteItem[] getAllContainers() {
        List<PaletteItem> list = new ArrayList<PaletteItem>();
        for (PaletteItem item : PaletteUtils.getAllItems()) {
            if (PaletteItem.TYPE_CHOOSE_BEAN.equals(item.getExplicitComponentType())) {
                continue;
            }
            Class cls = item.getComponentClass();
            if (cls != null
                  && JComponent.class.isAssignableFrom(cls)
                  && !MenuElement.class.isAssignableFrom(cls)
                  && FormUtils.isContainer(cls)) {
                list.add(item);
            }
        }
        return list.toArray(new PaletteItem[list.size()]);
    }

    private static class ContainersMenu extends JMenu {
        private boolean initialized = false;
        private List<RADComponent> components;

        private ContainersMenu(String name, List<RADComponent> components) {
            super(name);
            this.components = components;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            if (!initialized) {
                popup.removeAll();
                for (PaletteItem item : getAllContainers()) {
                    JMenuItem mi = new JMenuItem(item.getNode().getDisplayName());
                    HelpCtx.setHelpIDString(mi, EncloseAction.class.getName());                    
                    addSortedMenuItem(popup, mi);
                    mi.addActionListener(new EncloseActionListener(item));
                }
                initialized = true;
            }
            return popup;
        }
        
        private static void addSortedMenuItem(JPopupMenu menu, JMenuItem menuItem) {
            String text = menuItem.getText();
            for (int i = 0; i < menu.getComponentCount(); i++) {
                if(menu.getComponent(i) instanceof JMenuItem){
                    String tx = ((JMenuItem)menu.getComponent(i)).getText();
                    if (text.compareTo(tx) < 0) {
                        menu.add(menuItem, i);
                        return;
                    }
                }
            }
            menu.add(menuItem);
        }

        private class EncloseActionListener implements ActionListener {
            private PaletteItem paletteItem;

            EncloseActionListener(PaletteItem paletteItem) {
                this.paletteItem = paletteItem;
            }

            public void actionPerformed(ActionEvent evt) {
                RADVisualContainer metacont = getContainer(components);
                if (metacont != null) {
                    FormModel formModel = metacont.getFormModel();
                    MetaComponentCreator creator = formModel.getComponentCreator();
                    if (metacont.getLayoutSupport() == null) { // free design
                        LayoutModel layoutModel = formModel.getLayoutModel();
                        Object layoutUndoMark = null;
                        UndoableEdit layoutEdit = null;
                        boolean autoUndo = true; // in case of unexpected error, for robustness
                        try {
                            // create and add the new container
                            RADVisualContainer newCont = (RADVisualContainer)
                                    creator.createComponent(paletteItem.getComponentClassSource(), metacont, null);
                            // This also added the container's layout component to
                            // layout model (and registered undo edit). But we want
                            // the layout component to be added by LayoutDesigner as
                            // part of the enclosing operation, so we remove it here.
                            // From now we also need to care about layout undo.
                            layoutUndoMark = layoutModel.getChangeMark();
                            layoutEdit = layoutModel.getUndoableEdit();
                            layoutModel.removeComponent(newCont.getId(), false); // to be added by LayoutDesigner
                            String[] compIds = new String[components.size()];
                            int i = 0;
                            for (RADComponent metacomp : components) {
                                creator.moveComponent(metacomp, newCont); // this does not affect layout model
                                compIds[i++] = metacomp.getId();
                            }
                            FormEditor.getFormDesigner(formModel).getLayoutDesigner()
                                    .encloseInContainer(compIds, newCont.getId());
                            autoUndo = false;
                        } finally {
                            if (layoutUndoMark != null && !layoutUndoMark.equals(layoutModel.getChangeMark())) {
                                formModel.addUndoableEdit(layoutEdit);
                            }
                            if (autoUndo) {
                                formModel.forceUndoOfCompoundEdit();
                            }
                        }
                    } else { // old layout support
                        // [TBD]
                    }
                }
            }
        }
    }

}
