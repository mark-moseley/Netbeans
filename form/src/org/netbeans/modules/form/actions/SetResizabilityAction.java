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

package org.netbeans.modules.form.actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action class providing popup menu presenter for setresizability submenu.
 *
 * @author Martin Grebac
 */

public class SetResizabilityAction extends NodeAction {

    private JCheckBoxMenuItem[] items;
    
    protected boolean enable(Node[] nodes) {
        List comps = FormUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && (comps.size() > 0));
    }
    
    public String getName() {
        return NbBundle.getMessage(SetResizabilityAction.class, "ACT_SetResizability"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] activatedNodes) { }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    /**
     * Returns a JMenuItem that presents this action in a Popup Menu.
     * @return the JMenuItem representation for the action
     */
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenu(
            NbBundle.getMessage(SetResizabilityAction.class, "ACT_SetResizability")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, SetResizabilityAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createResizabilitySubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createResizabilitySubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        List components = FormUtils.getSelectedLayoutComponents(nodes);
        if ((components == null) || (components.size() < 1)) {
            return;
        }
        if (!(menu.getMenuComponentCount() > 0)) {
            ResourceBundle bundle = NbBundle.getBundle(SetResizabilityAction.class);

            JCheckBoxMenuItem hItem = new ResizabilityMenuItem(
                    bundle.getString("CTL_ResizabilityH"), // NOI18N
                    components,
                    0);
            JCheckBoxMenuItem vItem = new ResizabilityMenuItem(
                    bundle.getString("CTL_ResizabilityV"), // NOI18N
                    components,
                    1);
            items = new JCheckBoxMenuItem[] {hItem, vItem};
            
            for (int i=0; i<2; i++) {
                items[i].addActionListener(getMenuItemListener());
                HelpCtx.setHelpIDString(items[i], SetResizabilityAction.class.getName());
                menu.add(items[i]);
            }
        }
        updateState(components);
    }

    private void updateState(List components) {
        if ((components == null) || (components.size()<1)) {
            return;
        }
        RADComponent rc = (RADComponent)components.get(0);
        FormDesigner formDesigner = FormEditor.getFormDesigner(rc.getFormModel());
        formDesigner.updateResizabilityActions();
        for (int i=0; i<2; i++) {
            items[i].setEnabled(formDesigner.getResizabilityButtons()[i].isEnabled());
            items[i].setSelected(formDesigner.getResizabilityButtons()[i].isSelected());
        }
    }
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new ResizabilityMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class ResizabilityMenuItem extends JCheckBoxMenuItem {
        private int direction;
        private List components;

        ResizabilityMenuItem(String text, List components, int direction) {
            super(text);
            this.components = components;
            this.direction = direction;
        }
        
        int getDirection() {
            return direction;
        }

        List getRADComponents() {
            return components;
        }
    }

    private static class ResizabilityMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof ResizabilityMenuItem)) {
                return;
            }
            ResizabilityMenuItem mi = (ResizabilityMenuItem) source;
            if (!mi.isEnabled()) {
                return;
            }
            int index = mi.getDirection();
            RADComponent radC = (RADComponent)mi.getRADComponents().get(0);
            FormModel fm = radC.getFormModel();
            FormDesigner fd = FormEditor.getFormDesigner(fm);
            fd.getResizabilityButtons()[index].setSelected(!fd.getResizabilityButtons()[index].isSelected());
            ((Action)fd.getResizabilityActions().toArray()[index]).actionPerformed(evt);            
        }
    }
        
    private ActionListener menuItemListener;
}
