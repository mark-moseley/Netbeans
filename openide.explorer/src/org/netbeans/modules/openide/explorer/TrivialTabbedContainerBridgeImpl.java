/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.openide.explorer;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import java.awt.*;

/**
 * Trivial implementation of TabbedContainerBridge for use with unit tests, etc.
 * Does not actually support changing tabs, this is just so things link and do not
 * throw NPEs.
 * <p>
 * Given sufficient interest, a JTabbedPane implementation could be provided,
 * though there are some non-trivial difficulties getting a JTabbedPane to show
 * the same component for all tabs, and the technique that worked on 1.4 does not
 * work on 1.5.
 *
 */
public class TrivialTabbedContainerBridgeImpl extends TabbedContainerBridge {
    public TrivialTabbedContainerBridgeImpl() {

    }

    public JComponent createTabbedContainer() {
        JPanel result = new JPanel();
        result.setLayout (new BorderLayout());
        result.putClientProperty ("titles", new String[0]);
        result.putClientProperty ("items", new Object[0]);
        return result;
    }

    public void setInnerComponent(JComponent container, JComponent inner) {
        if (container.getComponentCount() > 0) {
            container.removeAll();
        }
        container.add (inner, BorderLayout.CENTER);
    }

    public JComponent getInnerComponent(JComponent jc) {
        JComponent result = null;
        if (jc.getComponentCount() > 0 && jc.getComponent(0) instanceof JComponent) {
            result = (JComponent) jc.getComponent(0);
        }
        return result;
    }

    public Object[] getItems(JComponent jc) {
        return new Object[0];
    }

    public void setItems(JComponent jc, Object[] objects, String[] titles) {
        jc.putClientProperty ("items", objects);
        jc.putClientProperty ("titles", titles);
    }

    public void attachSelectionListener(JComponent jc, ChangeListener listener) {
        //do nothing
    }

    public void detachSelectionListener(JComponent jc, ChangeListener listener) {
        //do nothing
    }

    public Object getSelectedItem(JComponent jc) {
        Object[] items = (Object[]) jc.getClientProperty ("items");
        if (items != null && items.length > 0) {
            return items[0];
        }
        return null;
    }

    public void setSelectedItem(JComponent jc, Object selection) {
        //do nothing
    }

    public boolean setSelectionByName(JComponent jc, String tabname) {
        return false;
    }

    public String getCurrentSelectedTabName(JComponent jc) {
        String[] titles = (String[]) jc.getClientProperty("titles");
        if (titles != null && titles.length > 0) {
            return titles[0];
        }
        return ""; //NOI18N
    }
}
