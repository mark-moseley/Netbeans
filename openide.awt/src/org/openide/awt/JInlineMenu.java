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
package org.openide.awt;

import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;


/**
 * Menu element that can contain other menu items. These items are then
 * displayed "inline". The JInlineMenu can be used to compose more menu items
 * into one that can be added/removed at once.
 *
 * @deprecated since org.openide.awt 6.5 JInlineMenu is a simple implementation of {@link DynamicMenuContent}, it
 * doesn't update when visible and doesn't handle the separators itself anymore.
 *
 * @author Jan Jancura
 */
public class JInlineMenu extends JMenuItem implements DynamicMenuContent {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2310488127953523571L;
    private static final Icon BLANK_ICON = new ImageIcon(
            Utilities.loadImage("org/openide/resources/actions/empty.gif")
        ); // NOI18N            

//    /** north separator */
//    private JSeparator north = new JSeparator();
//
//    /** south separator */
//    private JSeparator south = new JSeparator();

    /** Stores inner MenuItems added to outer menu. */
    private JComponent[] items = new JComponent[0];

    /** true iff items of this menu are up to date */
    boolean upToDate;

    /** private List of the items previously added to the parent menu */
    private List addedItems;

    /**
    * Creates new JInlineMenu.
    */
    public JInlineMenu() {
        setEnabled(false);
        setVisible(false);
        upToDate = true;
    }

    /** Overriden to eliminate big gap at top of JInline popup painting.
     * @return cleared instets (0, 0, 0, 0) */
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * Setter for array of items to display. Can be called only from event queue
     * thread.
     *
     * @param newItems array of menu items to display
     */
    public void setMenuItems(final JMenuItem[] newItems) {
        //        if(!SwingUtilities.isEventDispatchThread()) {
        //System.err.println("JInlineMenu.setMenuItems called outside of event queue !!!");
        //Thread.dumpStack();
        //        }
        // make a tuned private copy
        JComponent[] local = new JComponent[newItems.length];

        for (int i = 0; i < newItems.length; i++) {
            local[i] = (newItems[i] != null) ? (JComponent) newItems[i] : new JSeparator();
        }

        items = local;
        upToDate = false;

        alignItems();

    }


    /** Overriden to return first non null icon of current items or null if
     * all items has null icons.
     */
    private void alignItems() {
        // hack - we use also getIcon() result of JInlineMenu as indicator if we
        // should try to align items using empty icon or not 
        boolean shouldAlign = getIcon() != null;

        if (!shouldAlign) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof JMenuItem) {
                    if (((JMenuItem) items[i]).getIcon() != null) {
                        shouldAlign = true;

                        break;
                    }
                }
            }
        }

        if (!shouldAlign) {
            return;
        }

        // align items using empty icon
        JMenuItem curItem = null;

        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof JMenuItem) {
                curItem = (JMenuItem) items[i];

                if (curItem.getIcon() == null) {
                    curItem.setIcon(BLANK_ICON);
                }
            }
        }
    }



    /** Finds the index of a component in array of components.
     * @return index or -1
     */
    private static int findIndex(Object of, Object[] arr) {
        int menuLength = arr.length;

        for (int i = 0; i < menuLength; i++) {
            if (of == arr[i]) {
                return i;
            }
        }

        return -1;
    }


    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return this.items;
    }

    public JComponent[] getMenuPresenters() {
        return items;
    }

}
