/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.Node;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.CPManager;
import org.netbeans.modules.form.*;

/**
 * Action for setting layout on selected container(s). Presented only in
 * contextual menus within the Form Editor.
 */

public class SelectLayoutAction extends CallableSystemAction {

    private static String name;

     /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(SelectLayoutAction.class)
                     .getString("ACT_SelectLayout"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SelectLayoutAction.class);
    }

    public boolean isEnabled() {
        Node[] nodes = getNodes();
        for (int i=0; i < nodes.length; i++) {
            RADVisualContainer container = getContainer(nodes[i]);
            if (container == null || container.getLayoutSupport().isDedicated())
                return false;
        }
        return true;
    }

    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    public JMenuItem getPopupPresenter() {
        JMenu layoutMenu = new LayoutMenu(getName());
        layoutMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(layoutMenu, SelectLayoutAction.class.getName());
        return layoutMenu;
    }

    protected boolean asynchronous() {
        return false;
    }

    public void performAction() {
    }

    // -------

    private static Node[] getNodes() {
        // using NodeAction and global activated nodes is not reliable
        // (activated nodes are set with a delay after selection in
        // ComponentInspector)
        return ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
    }

    private static RADVisualContainer getContainer(Node node) {
        RADComponentCookie radCookie = (RADComponentCookie)
            node.getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualContainer)
                return (RADVisualContainer) metacomp;
        }
        return null;
    }

    private static PaletteItem[] getAllLayouts() {
        PaletteItem[] allItems = CPManager.getDefault().getAllItems();
        ArrayList layoutsList = new ArrayList();
        for (int i = 0; i < allItems.length; i++) {
            if (allItems[i].isLayout()) {
                layoutsList.add(allItems[i]);
            }
        }

        PaletteItem[] layouts = new PaletteItem[layoutsList.size()];
        layoutsList.toArray(layouts);
        return layouts;
    }

    private static class LayoutMenu extends org.openide.awt.JMenuPlus {
        private boolean initialized = false;

        private LayoutMenu(String name) {
            super(name);
        }

        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            Node[] nodes = getNodes();

            if (nodes.length != 0 && !initialized) {
                popup.removeAll();
                PaletteItem[] layouts = getAllLayouts();
                for (int i = 0; i < layouts.length; i++) {
                    JMenuItem mi = new JMenuItem(layouts[i].getNode().getDisplayName());
                    HelpCtx.setHelpIDString(mi, SelectLayoutAction.class.getName());
                    popup.add(mi);
                    mi.addActionListener(new LayoutActionListener(layouts[i]));
                }
                initialized = true;
            }
            return popup;
        }
    }

    private static class LayoutActionListener implements ActionListener {
        private PaletteItem paletteItem;

        LayoutActionListener(PaletteItem paletteItem) {
            this.paletteItem = paletteItem;
        }

        public void actionPerformed(ActionEvent evt) {
            Node[] nodes = getNodes();
            for (int i = 0; i < nodes.length; i++) {
                RADVisualContainer container = getContainer(nodes[i]);
                if (container == null)
                    continue;

                // set the selected layout on the activated container
                container.getFormModel().getComponentCreator().createComponent(
                    paletteItem.getComponentClassSource(), container, null);
            }
        }
    }
}
