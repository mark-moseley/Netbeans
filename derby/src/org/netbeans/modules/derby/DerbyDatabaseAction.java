/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * A dummy action serving as the Derby Database menu item. Allows 
 * showing and hiding the menu item programmatically.
 *
 * @author Andrei Badea
 */
public class DerbyDatabaseAction extends AbstractAction implements Presenter.Menu {
    
    private JMenuItem menuPresenter = null;
    
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (DerbyOptions.PROP_DERBY_LOCATION.equals(evt.getPropertyName())) {
                if (menuPresenter != null) {
                    setMenuItemVisible(menuPresenter);
                }
            }
        }
    };
    
    public DerbyDatabaseAction() {
        super(NbBundle.getMessage(DerbyDatabaseAction.class, "LBL_DerbyDatabase"));
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
    }

    public javax.swing.JMenuItem getMenuPresenter() {
        if (menuPresenter == null) {
            DerbyOptions.getDefault().addPropertyChangeListener(pcl);
            menuPresenter = new MyMenu();
            setMenuItemVisible(menuPresenter);
        }
        return menuPresenter;
    }
    
    private static void setMenuItemVisible(JMenuItem menuItem) {
        menuItem.setVisible(!DerbyOptions.getDefault().isLocationNull());        
    }
    
    private final class MyMenu extends JMenu {
        
        public MyMenu() {
            super((String)getValue(Action.NAME));
        }
        
        public JPopupMenu getPopupMenu() {
            removeAll();
            JPopupMenu menu = Utilities.actionsToPopup(new Action[] {
                SystemAction.get(StartAction.class),
                SystemAction.get(StopAction.class),
                SystemAction.get(CreateDatabaseAction.class),
            }, Utilities.actionsGlobalContext());
            while (menu.getComponentCount() > 0) {
                Component c = menu.getComponent(0);
                menu.remove(0);
                add(c);
            }
            return super.getPopupMenu();
        }
    }
}
